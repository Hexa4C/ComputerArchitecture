#include "cuda_runtime.h"
#include "device_launch_parameters.h"
#include <Windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>

#define BLOCKSIZE 8
#define RLEN 160
#define CLEN 160

void MatGenerator(double *M, int N1, int N2) {
	int i;
	for (i = 0; i < N1 * N2; i++) {
		M[i] = (double)(rand() % 100);
	}
}

__global__ static void CUDAMatMul1(const double *A, const double *B, double *C){
	int row = blockDim.y * blockIdx.y + threadIdx.y;
	int col = blockDim.x * blockIdx.x + threadIdx.x;
	int i;
	int N = RLEN * BLOCKSIZE;
	int M = CLEN * BLOCKSIZE;
	double sum = 0;
	for (i = 0; i < N; i++) {
		sum += A[row * N + i] * B[i * M + col];
	}
	C[row * M + col] = sum;
}

__global__ static void CUDAMatMul2(const double *A, const double *B, double *C) {
	int BlockRow = blockIdx.y;
	int BlockCol = blockIdx.x;
	int row = threadIdx.y;
	int col = threadIdx.x;
	int N = RLEN * BLOCKSIZE;
	int M = CLEN * BLOCKSIZE;
	int i, j;
	double sum = 0;
	for (i = 0; i < (N / BLOCKSIZE); i++) {
		__shared__ double Asub[BLOCKSIZE][BLOCKSIZE];
		__shared__ double Bsub[BLOCKSIZE][BLOCKSIZE];
		Asub[row][col] = A[(blockDim.y * blockIdx.y + threadIdx.y) * N + (i * BLOCKSIZE + threadIdx.x)];
		Bsub[row][col] = B[(i * BLOCKSIZE + threadIdx.y) * M + (blockDim.x * blockIdx.x + threadIdx.x)];
		__syncthreads();
		for (j = 0; j < BLOCKSIZE; j++) {
			sum += Asub[row][j] * Bsub[j][col];
		}
		__syncthreads();
	}
	C[(blockDim.y * blockIdx.y + threadIdx.y) * M + (blockDim.x * blockIdx.x + threadIdx.x)] = sum;
}

double execute1(double *A, double *B, double *D) {
	int i, j, k;
	int N = RLEN * BLOCKSIZE;
	int M = CLEN * BLOCKSIZE;
	LARGE_INTEGER nFreq = { 0 };
	LARGE_INTEGER nBeginTime = { 0 };
	LARGE_INTEGER nEndTime = { 0 };
	double totaltime;
	double sum;
	SetThreadAffinityMask(GetCurrentThread(), 1);
	QueryPerformanceFrequency(&nFreq);
	QueryPerformanceCounter(&nBeginTime);
	for (i = 0; i < N; i++) {
		for (j = 0; j < M; j++) {
			sum = 0;
			for (k = 0; k < N; k++) {
				sum += A[i * N + k] * B[k * M + j];
			}
			D[i * M + j] = sum;
		}
	}
	QueryPerformanceCounter(&nEndTime);
	totaltime = (double)(nEndTime.QuadPart - nBeginTime.QuadPart) / ((double)nFreq.QuadPart);
	return totaltime;
}

double execute2(double *A, double *B, double *C) {
	int N = RLEN * BLOCKSIZE;
	int M = CLEN * BLOCKSIZE;
	double *cudaA, *cudaB, *cudaC;
	LARGE_INTEGER nFreq;
	LARGE_INTEGER nBeginTime;
	LARGE_INTEGER nEndTime;
	double totaltime;
	QueryPerformanceFrequency(&nFreq);
	//分配显卡内存
	cudaMalloc((void **)&cudaA, sizeof(double) * N * N);
	cudaMalloc((void **)&cudaB, sizeof(double) * N * M);
	cudaMalloc((void **)&cudaC, sizeof(double) * N * M);
	//将生成的矩阵复制到显卡内存中
	QueryPerformanceCounter(&nBeginTime);
	cudaMemcpy(cudaA, A, sizeof(double) * N * N, cudaMemcpyHostToDevice);
	cudaMemcpy(cudaB, B, sizeof(double) * N * M, cudaMemcpyHostToDevice);
	dim3 block(BLOCKSIZE, BLOCKSIZE);
	dim3 grid(CLEN, RLEN);
	CUDAMatMul1 << < grid, block >> >(cudaA, cudaB, cudaC);
	cudaMemcpy(C, cudaC, sizeof(double) * N * M, cudaMemcpyDeviceToHost);
	QueryPerformanceCounter(&nEndTime);
	totaltime = (double)(nEndTime.QuadPart - nBeginTime.QuadPart) / ((double)nFreq.QuadPart);
	cudaFree(cudaA);
	cudaFree(cudaB);
	cudaFree(cudaC);
	return totaltime;
}

double execute3(double *A, double *B, double *C) {
	int N = RLEN * BLOCKSIZE;
	int M = CLEN * BLOCKSIZE;
	double *cudaA, *cudaB, *cudaC;
	LARGE_INTEGER nFreq;
	LARGE_INTEGER nBeginTime;
	LARGE_INTEGER nEndTime;
	double totaltime;
	QueryPerformanceFrequency(&nFreq);
	//分配显卡内存
	cudaMalloc((void **)&cudaA, sizeof(double) * N * N);
	cudaMalloc((void **)&cudaB, sizeof(double) * N * M);
	cudaMalloc((void **)&cudaC, sizeof(double) * N * M);
	//将生成的矩阵复制到显卡内存中
	QueryPerformanceCounter(&nBeginTime);
	cudaMemcpy(cudaA, A, sizeof(double) * N * N, cudaMemcpyHostToDevice);
	cudaMemcpy(cudaB, B, sizeof(double) * N * M, cudaMemcpyHostToDevice);
	dim3 block(BLOCKSIZE, BLOCKSIZE);
	dim3 grid(CLEN, RLEN);
	CUDAMatMul2 << < grid, block >> >(cudaA, cudaB, cudaC);
	cudaMemcpy(C, cudaC, sizeof(double) * N * M, cudaMemcpyDeviceToHost);
	QueryPerformanceCounter(&nEndTime);
	totaltime = (double)(nEndTime.QuadPart - nBeginTime.QuadPart) / (double)nFreq.QuadPart;
	cudaFree(cudaA);
	cudaFree(cudaB);
	cudaFree(cudaC);
	return totaltime;
}

int main(int argc, char* argv[]){
	double *A, *B, *C1, *C2, *D;
	int i, j, k, M, N;
	int tag1 = 1, tag2 = 1;
	double time1, time2, time3;
	double max_err = 0, ave_err = 0;
	char result[2][10] = { "false!\0", "true!\0" };
	N = RLEN * BLOCKSIZE;
	M = CLEN * BLOCKSIZE;

	//分配内存空间
	A = (double *)malloc(N * N * sizeof(double));
	B = (double *)malloc(N * M * sizeof(double));
	C1 = (double *)malloc(N * M * sizeof(double));
	C2 = (double *)malloc(N * M * sizeof(double));
	D = (double *)malloc(N * M * sizeof(double));
	//初始化矩阵A和矩阵B
	srand(0);
	MatGenerator(A, N, N);
	MatGenerator(B, N, M);

	time1 = execute1(A, B, D);
	time2 = execute2(A, B, C1);
	time3 = execute3(A, B, C2);
	printf("Serial Method:\n");
	printf("time cost is %lfs\n", time1);
	printf("CUDA method 1: %s\n", result[tag1]);
	printf("time cost is %lfs.\n", time2);
	printf("CUDA method 2: %s\n", result[tag2]);
	printf("time cost is %lfs.\n", time3);
	system("pause");
	return 0;
}