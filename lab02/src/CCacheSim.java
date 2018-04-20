import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;

import java.io.FileNotFoundException;
import javax.swing.JRadioButton;
import javax.swing.JOptionPane;

import java.util.*;


public class CCacheSim extends JFrame implements ActionListener{

	private JPanel panelTop, panelLeft, panelRight, panelBottom;
	private JButton execStepBtn, execAllBtn, fileBotton;
	private JComboBox csBox, bsBox, wayBox, replaceBox, prefetchBox, writeBox, allocBox;
	private JFileChooser fileChoose = new JFileChooser("./");
	
	private JLabel labelTop,labelLeft,rightLabel,bottomLabel,fileLabel,fileAddrBtn,
			csLabel, bsLabel, wayLabel, replaceLabel, prefetchLabel, writeLabel, allocLabel;
	private JLabel results[], stepLabel[];
	//private JLabel singleCache, multiCache;
	private JRadioButton singleCache, multiCache;
	private JLabel icLabel, dcLabel;
	private JComboBox icBox, dcBox;
	private boolean singleChosen = true, multiChosen = false;
	private JButton resetBtn;


    //参数定义
	private String cachesize[] = { "2KB", "8KB", "32KB", "128KB", "512KB", "2MB" };
	private String blocksize[] = { "16B", "32B", "64B", "128B", "256B" };
	private String way[] = { "直接映象", "2路", "4路", "8路", "16路", "32路" };
	private String replace[] = { "LRU", "FIFO", "RAND" };
	private String pref[] = { "不预取", "不命中预取" };
	private String write[] = { "写回法", "写直达法" };
	private String alloc[] = { "按写分配", "不按写分配" };
	private String typename[] = { "读数据", "写数据", "读指令" };
	private String hitname[] = {"不命中", "命中" };
	
	//右侧结果显示
	private String resultText[]={" ", "访问次数", "不命中次数", "不命中率", "总次数：", "0", "0", "0","读指令：", "0", "0", "0", "读数据：", "0", "0", "0","写数据：", "0", "0", "0"};
	private String stepText[] ={" ", " ", " ", " ", "访问类型", "", "地址", "", "块号", "", "块内地址", "", "索引", "", "命中情况", ""};
	
	//打开文件
	private File file;
	
	//分别表示左侧几个下拉框所选择的第几项，索引从 0 开始
	private int csIndex, bsIndex, wayIndex, replaceIndex, prefetchIndex, writeIndex, allocIndex;
	
	//其它变量定义
	//...
	Scanner fscan;
	private int[] csSizeList = {2, 8, 32, 128, 512, 2048};
	private int[] bsSizeList = {16, 32, 64, 128, 256};
	private int[] wayValueList = {0, 2, 4, 8, 16, 32};
	private int icIndex, dcIndex;		//独立Cache的大小下拉框选项
	private int csSize, icSize, dcSize;		//统一与独立Cache大小数值
	private int cBlockNum, icBlockNum, dcBlockNum; 		//统一与独立Cache数组大小
	private int bsSize, wayValue;		//块大小及路数
	private int[][] cacheBlock, iCacheBlock, dCacheBlock;		//统一及独立Cache数组
	private int type, address;		//指令类型及地址
	private int blockOffset, blockNum, index, tag, hit;		//块内偏移，对应Cache内块号，索引，标志，命中情况
	private int accessNum, missNum, readInstrNum, readInstrMissNum;		//访问总次数，缺失总次数，读指令次数，读指令缺失次数
	private int readDataNum, readDataMissNum, writeDataNum, writeDataMissNum;	//读数据次数，读数据缺失次数，写数据次数，写数据缺失次数
	private double missRate, readInstrMissRate, readDataMissRate, writeDataMissRate;	//各种缺失率
	private Queue<Integer>[] cFIFO, icFIFO, dcFIFO;		//统一及独立的Cache FIFO队列
	private int[] cLRU, icLRU, dcLRU;		//统一及独立的Cache LRU表
	private boolean prefetchTag;
	private int nline;
	
	/*
	 * 构造函数，绘制模拟器面板
	 */
	public CCacheSim(){
		super("Cache Simulator");
		draw();
	}
	
	
	//响应事件，共有三种事件：
	//   1. 执行到底事件
	//   2. 单步执行事件
	//   3. 文件选择事件
	public void actionPerformed(ActionEvent e){
				
		if (e.getSource() == execAllBtn) {
			if (fscan == null) {
				JOptionPane.showMessageDialog(null, "你需要先选择文件！", "错误！", JOptionPane.ERROR_MESSAGE);
			}
			else {
				simExecAll();
				OutputResult();
			}
		}
		if (e.getSource() == execStepBtn) {
			if (fscan == null) {
				JOptionPane.showMessageDialog(null, "你需要先选择文件！", "错误！", JOptionPane.ERROR_MESSAGE);
			}
			else {
				simExecStep();
				OutputResult();
			}
		}
		if (e.getSource() == resetBtn) {//复位键操作
			if (fscan != null ) {
				fscan.close();
				nline = 0;
				readFile();
				initCache();
				initOuput();
			}
		}
		if (e.getSource() == fileBotton){
			int fileOver = fileChoose.showOpenDialog(null);
			if (fileOver == 0) {
				String path = fileChoose.getSelectedFile().getAbsolutePath();
				fileAddrBtn.setText(path);
				file = new File(path);
				nline = 0;
				readFile();
				initCache();
			}
		}
	}
	
	/*
	 * 初始化 Cache 模拟器
	 */
	public void initCache() {
		prefetchTag = false;
		bsSize = bsSizeList[bsIndex];
		wayValue = wayValueList[wayIndex];
		if (singleChosen) {//统一Cache
			csSize = csSizeList[csIndex];
			cBlockNum = csSize * 1024 / bsSize;
			cacheBlock = new int[cBlockNum][2];	//初始化cache数组
			if (wayValue != 0) {//直接映射替换策略没用
				if (replaceIndex == 0) {
					cLRU = new int[cBlockNum];	//初始化LRU表
				}//if
				else if (replaceIndex == 1) {
					cFIFO = new LinkedList[cBlockNum / wayValue];	//初始化FIFO队列
					for (int i = 0; i < cFIFO.length; i ++) {
						cFIFO[i] = new LinkedList<Integer>();
					}//for
				}//else if
			}//if
		}//if
		else {//独立Cache
			icSize = csSizeList[icIndex];	//↓初始化独立cache数组
			icBlockNum = icSize * 1024 / bsSize;
			iCacheBlock = new int[icBlockNum][2];
			dcSize = csSizeList[dcIndex];
			dcBlockNum = dcSize * 1024 / bsSize;
			dCacheBlock = new int[dcBlockNum][2];
			if (wayValue != 0) {
				if (replaceIndex == 0) {	//初始化LRU表
					icLRU = new int[icBlockNum];
					dcLRU = new int[dcBlockNum];
				}//if
				else if (replaceIndex == 1) {	//初始化FIFO队列
					icFIFO = new LinkedList[icBlockNum / wayValue];
					for (int i = 0; i < icFIFO.length; i ++) {
						icFIFO[i] = new LinkedList<Integer>();
					}//for
					dcFIFO = new LinkedList[dcBlockNum / wayValue];
					for (int i = 0; i < dcFIFO.length; i ++) {
						dcFIFO[i] = new LinkedList<Integer>();
					}//for
				}//else if
			}//if
		}//else
		accessNum = 0;//初始化各个数值
		missNum = 0;
		readInstrNum = 0;
		readInstrMissNum = 0;
		readDataNum = 0;
		readDataMissNum = 0;
		writeDataNum = 0;
		writeDataMissNum = 0;
		missRate = 0;
		readInstrMissRate = 0;
		readDataMissRate = 0;
		writeDataMissRate = 0;
	}
	
	/*
	 * 将指令和数据流从文件中读入
	 */
	public void readFile() {
		try {
			fscan = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 初始化输出
	 */
	public void initOuput() {
		for (int i = 0; i < results.length; i ++) {
			results[i].setText(resultText[i]);
		}
		for (int i= 0; i < stepLabel.length; i ++) {
			stepLabel[i].setText("");
		}
	}

	/*
	 * 模拟单步执行
	 */
	public void simExecStep() {
		if (fscan.hasNext() || prefetchTag) {
			if (singleChosen && wayValue > cBlockNum) {
				JOptionPane.showMessageDialog(null, "组相联路数过大！", "错误！", JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if (multiChosen && (wayValue > dcBlockNum || wayValue > icBlockNum)) {
				JOptionPane.showMessageDialog(null, "组相联路数过大！", "错误！", JOptionPane.ERROR_MESSAGE);
				return;
			}
			simExecBody();
		}
	}
	
	/*
	 * 模拟执行到底
	 */
	public void simExecAll() {
		while (fscan.hasNext() || prefetchTag) {
			if (singleChosen && wayValue > cBlockNum) {
				JOptionPane.showMessageDialog(null, "组相联路数过大！", "错误！", JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if (multiChosen && (wayValue > dcBlockNum || wayValue > icBlockNum)) {
				JOptionPane.showMessageDialog(null, "组相联路数过大！", "错误！", JOptionPane.ERROR_MESSAGE);
				return;
			}
			simExecBody();
		}
	}
	
	/*
	 * 执行主体
	 */
	public void simExecBody() {
		if (prefetchTag) {//若预取tag为真，则预取下一块
			address = address + bsSize;
		}
		else {
			String aline = fscan.nextLine();
			nline ++;
			String[] nums = aline.split(" |\t");
			type = Integer.parseInt(nums[0]);
			address = Integer.parseInt(nums[1], 16);
		}
		
		if (singleChosen) {
			simExecStepSCache();
		}
		else {
			simExecStepMCache();
		}
		if (prefetchIndex == 1 && type != 1 && hit == 0 && prefetchTag == false) {
			prefetchTag = true;	//若条件符合该预取且上次非预取则将预取tag置为true
		}
		else if(prefetchTag) {
			prefetchTag = false;//若上次是预取的则将预取tag置为false
		}
	}
	
	/*
	 * 统一Cache执行主体
	 */
	public void simExecStepSCache() {
		int i;
		blockOffset = address % bsSize;
		blockNum = address / bsSize;
		accessNum ++;
		if (type == 0) {
			readDataNum ++;
		}
		else if (type == 1) {
			writeDataNum ++;
		}
		else 
			readInstrNum ++;
		if (wayValue == 0) {	//直接映射
			index = blockNum % cBlockNum;
			tag = blockNum / cBlockNum;
			if (cacheBlock[index][0] == tag) {	//命中
				hit = 1;
			}
			else {//未命中
				hit = 0;
				missNum ++;
				if (type == 0) {
					readDataMissNum ++;
				}
				else if(type == 1) {
					writeDataMissNum ++;
				}
				else {
					readInstrMissNum ++;
				}
				replaceIn();
			}
		}
		else {//多路组相联
			index = blockNum % (cBlockNum / wayValue);
			tag = blockNum / (cBlockNum / wayValue);
			for (i = 0; i < wayValue; i ++) {//Cache中寻找相应块
				if (cacheBlock[index * wayValue + i][0] == tag) {
					break;
				}
			}
			if (i < wayValue) {//命中
				hit = 1;
				if (replaceIndex == 0) {
					hitModifyLRU(index, wayValue, i, cLRU);
				}
				else if (replaceIndex == 1) {
					/* cFIFO[index].remove(i);
					cFIFO[index].offer(i); */
				}
			}
			else {//未命中
				hit = 0;
				missNum ++;
				if (type == 0) {
					readDataMissNum ++;
				}
				else if(type == 1) {
					writeDataMissNum ++;
				}
				else {
					readInstrMissNum ++;
				}
				replaceIn();
			}
		}
	}
	
	/*
	 * 独立执行主体
	 */
	public void simExecStepMCache() {
		int i;
		blockOffset = address % bsSize;
		blockNum = address / bsSize;
		accessNum ++;
		if (type == 0) {
			readDataNum ++;
		}
		else if (type == 1) {
			writeDataNum ++;
		}
		else {
			readInstrNum ++;
		}
		if (type != 2) {//数据操作
			if (wayValue == 0) {	//直接映射
				index = blockNum % dcBlockNum;
				tag = blockNum / dcBlockNum;
				if (dCacheBlock[index][0] == tag) {	//命中
					hit = 1;
				}
				else {//未命中
					hit = 0;
					missNum ++;
					if (type == 0) {
						readDataMissNum ++;
					}
					else if(type == 1) {
						writeDataMissNum ++;
					}
					replaceIn();
				}
			}
			else {//多路组相联
				index = blockNum % (dcBlockNum / wayValue);
				tag = blockNum / (dcBlockNum / wayValue);
				for (i = 0; i < wayValue; i ++) {
					if (dCacheBlock[index * wayValue + i][0] == tag) {
						break;
					}
				}
				if (i < wayValue) {//命中
					hit = 1;
					if (replaceIndex == 0) {
						hitModifyLRU(index, wayValue, i, dcLRU);
					}
				}
				else {//未命中
					hit = 0;
					missNum ++;
					if (type == 0) {
						readDataMissNum ++;
					}
					else if(type == 1) {
						writeDataMissNum ++;
					}
					replaceIn();
				}
			}
		}
		else {//指令操作
			if (wayValue == 0) {	//直接映射
				index = blockNum % icBlockNum;
				tag = blockNum / icBlockNum;
				if (iCacheBlock[index][0] == tag) {	//命中
					hit = 1;
				}
				else {//未命中
					hit = 0;
					missNum ++;
					readInstrMissNum ++;
					replaceIn();
				}
			}
			else {
				index = blockNum % (icBlockNum / wayValue);
				tag = blockNum / (icBlockNum / wayValue);
				for (i = 0; i < wayValue; i ++) {
					if (iCacheBlock[index * wayValue + i][0] == tag) {
						break;
					}
				}
				if (i < wayValue) {//命中
					hit = 1;
					if (replaceIndex == 0) {
						hitModifyLRU(index, wayValue, i, icLRU);
					}
				}
				else {//未命中
					hit = 0;
					missNum ++;
					readInstrMissNum ++;
					replaceIn();
				}
			}
		}
	}
	
	/*
	 * 命中之后对LRU表的修改
	 */
	public void hitModifyLRU(int ind, int value, int offset, int[] listLRU) {
		for (int i = ind * value; i < ind *value + value; i ++) {
			if (listLRU[i] != 0){
				listLRU[i] ++;
			}
		}
		listLRU[ind * value + offset] = 1;
	}

	/*
	 *根据替换策略换入
	 */
	public void replaceIn() {
		if (wayValue == 0) {
			if (singleChosen) {
				if (type != 1) {
					cacheBlock[index][0] = tag;
				}//if
				else {
					if (allocIndex == 0) {
						cacheBlock[index][0] = tag;
					}//if 
				}//else
			}//if
			else {
				if(type == 0) {
					dCacheBlock[index][0] = tag;
				}//if
				else if(type == 1) {
					if (allocIndex == 0) {
						dCacheBlock[index][0] = tag;
					}//if
				}//else if
				else {
					iCacheBlock[index][0] = tag;
				}//else
			}//else
		}//if
		else {
			if (singleChosen) {//统一Cache
				if (!(type == 1 && allocIndex == 1)) {
					if (replaceIndex == 0) {//LRU
						int toBeReplacedIndex = index * wayValue + 0;
						int maxLRU = cLRU[toBeReplacedIndex];
						int tmpIndex = toBeReplacedIndex;
						while(tmpIndex < (index + 1) * wayValue) {//寻找未占用块
							if (cacheBlock[tmpIndex][1] == 0) {
								toBeReplacedIndex = tmpIndex;
								break;
							}
							tmpIndex ++;
						}
						if (tmpIndex == (index + 1) * wayValue) {
							tmpIndex = index * wayValue;
							while(tmpIndex < (index + 1) * wayValue) {//若没有未占用块，则寻找LRU值最大的
								if (cLRU[tmpIndex] > maxLRU) {
									maxLRU = cLRU[tmpIndex];
									toBeReplacedIndex = tmpIndex;
								}
								tmpIndex ++;
							}//while
						}
						for (int in = index * wayValue; in < index *wayValue + wayValue; in ++) {//将其他的LRU值增加
							if (cLRU[in] != 0){
								cLRU[in] ++;
							}
						}
						cacheBlock[toBeReplacedIndex][0] = tag;
						cacheBlock[toBeReplacedIndex][1] = 1;
						cLRU[toBeReplacedIndex] = 1;
					}//if
					else if (replaceIndex == 1) {//FIFO
						int toBeReplacedIndex = index * wayValue;
						if (cFIFO[index].size() < wayValue) {//寻找未占用块
							while (toBeReplacedIndex < (index + 1) * wayValue) {
								if(cacheBlock[toBeReplacedIndex][1] == 0) {
									break;
								}
								toBeReplacedIndex ++;
							}
						}
						if (cFIFO[index].size() >= wayValue || toBeReplacedIndex >= (index + 1) * wayValue ) {//寻找待换出块
							toBeReplacedIndex = cFIFO[index].poll() + index * wayValue;
						}
						cacheBlock[toBeReplacedIndex][0] = tag;
						cacheBlock[toBeReplacedIndex][1] = 1;
						cFIFO[index].offer(toBeReplacedIndex - index * wayValue);
					}
					else {//RAND
						int toBeReplacedIndex = index * wayValue;
						while (toBeReplacedIndex < (index + 1) * wayValue) {//寻找未占用块
							if(cacheBlock[toBeReplacedIndex][1] == 0) {
								break;
							}
							toBeReplacedIndex ++;
						}
						if (toBeReplacedIndex == (index + 1) * wayValue) {//寻找待换出块
							toBeReplacedIndex = index * wayValue + new Random().nextInt(wayValue);
						}
						cacheBlock[toBeReplacedIndex][0] = tag;
						cacheBlock[toBeReplacedIndex][1] = 1;
					}
				}//if
			}//if
			else {//独立Cache
				if (type == 0 || (type == 1 && allocIndex == 0)) {//数据操作
					if (replaceIndex == 0) {//LRU
						int toBeReplacedIndex = index * wayValue + 0;
						int maxLRU = dcLRU[toBeReplacedIndex];
						int tmpIndex = toBeReplacedIndex;
						while(tmpIndex < (index + 1) * wayValue) {//寻找未占用块
							if (dCacheBlock[tmpIndex][1] == 0) {
								toBeReplacedIndex = tmpIndex;
								break;
							}
							tmpIndex ++;
						}
						if (tmpIndex == (index + 1) * wayValue) {
							tmpIndex = index * wayValue;
							while(tmpIndex < (index + 1) * wayValue) {//寻找待换出块
								if (dcLRU[tmpIndex] > maxLRU) {
									maxLRU = dcLRU[tmpIndex];
									toBeReplacedIndex = tmpIndex;
								}
								tmpIndex ++;
							}//while
						}
						for (int in = index * wayValue; in < index *wayValue + wayValue; in ++) {//将其他的LRU值增加
							if (dcLRU[in] != 0){
								dcLRU[in] ++;
							}
						}
						dCacheBlock[toBeReplacedIndex][0] = tag;
						dCacheBlock[toBeReplacedIndex][1] = 1;
						dcLRU[toBeReplacedIndex] = 1;
					}//if
					else if (replaceIndex == 1) {//FIFO
						int toBeReplacedIndex = index * wayValue;
						if (dcFIFO[index].size() < wayValue) {
							while (toBeReplacedIndex < (index + 1) * wayValue) {//寻找未占用块
								if(dCacheBlock[toBeReplacedIndex][1] == 0) {
									break;
								}
								toBeReplacedIndex ++;
							}
						}
						if (dcFIFO[index].size() >= wayValue || toBeReplacedIndex >= (index + 1) * wayValue ) {//寻找待换出块
							toBeReplacedIndex = dcFIFO[index].poll() + index * wayValue;
						}
						dCacheBlock[toBeReplacedIndex][0] = tag;
						dCacheBlock[toBeReplacedIndex][1] = 1;
						dcFIFO[index].offer(toBeReplacedIndex - index * wayValue);
					}
					else {//RAND
						int toBeReplacedIndex = index * wayValue;
						while (toBeReplacedIndex < (index + 1) * wayValue) {//寻找未占用块
							if(dCacheBlock[toBeReplacedIndex][1] == 0) {
								break;
							}
							toBeReplacedIndex ++;
						}
						if (toBeReplacedIndex == (index + 1) * wayValue) {//寻找待换出块
							toBeReplacedIndex = index * wayValue + new Random().nextInt(wayValue);
						}
						dCacheBlock[toBeReplacedIndex][0] = tag;
						dCacheBlock[toBeReplacedIndex][1] = 1;
					}
				}
				else if(type == 2){//指令操作
					if (replaceIndex == 0) {//LRU
						int toBeReplacedIndex = index * wayValue + 0;
						int maxLRU = icLRU[toBeReplacedIndex];
						int tmpIndex = toBeReplacedIndex;
						while(tmpIndex < (index + 1) * wayValue) {//寻找未占用块
							if (iCacheBlock[tmpIndex][1] == 0) {
								toBeReplacedIndex = tmpIndex;
								break;
							}
							tmpIndex ++;
						}
						if (tmpIndex == (index + 1) * wayValue) {
							tmpIndex = index * wayValue;
							while(tmpIndex < (index + 1) * wayValue) {//寻找待换出块
								if (icLRU[tmpIndex] > maxLRU) {
									maxLRU = icLRU[tmpIndex];
									toBeReplacedIndex = tmpIndex;
								}
								tmpIndex ++;
							}//while
						}
						for (int in = index * wayValue; in < index *wayValue + wayValue; in ++) {//将其他的LRU值增加
							if (icLRU[in] != 0){
								icLRU[in] ++;
							}
						}
						iCacheBlock[toBeReplacedIndex][0] = tag;
						iCacheBlock[toBeReplacedIndex][1] = 1;
						icLRU[toBeReplacedIndex] = 1;
					}//if
					else if (replaceIndex == 1) {//FIFO
						int toBeReplacedIndex = index * wayValue;
						if (icFIFO[index].size() < wayValue) {
							while (toBeReplacedIndex < (index + 1) * wayValue) {//寻找未占用块
								if(iCacheBlock[toBeReplacedIndex][1] == 0) {
									break;
								}
								toBeReplacedIndex ++;
							}
						}
						if (icFIFO[index].size() >= wayValue || toBeReplacedIndex >= (index + 1) * wayValue ) {//寻找待换出块
							toBeReplacedIndex = icFIFO[index].poll() + index * wayValue;
						}
						iCacheBlock[toBeReplacedIndex][0] = tag;
						iCacheBlock[toBeReplacedIndex][1] = 1;
						icFIFO[index].offer(toBeReplacedIndex - index * wayValue);
					}
					else {//RAND
						int toBeReplacedIndex = index * wayValue;
						while (toBeReplacedIndex < (index + 1) * wayValue) {//寻找未占用块
							if(iCacheBlock[toBeReplacedIndex][1] == 0) {
								break;
							}
							toBeReplacedIndex ++;
						}
						if (toBeReplacedIndex == (index + 1) * wayValue) {//寻找待换出块
							toBeReplacedIndex = index * wayValue + new Random().nextInt(wayValue);
						}
						iCacheBlock[toBeReplacedIndex][0] = tag;
						iCacheBlock[toBeReplacedIndex][1] = 1;
					}
				}
			}
		}//else
	}

	/*
	 * 输出结果
	 */
	public void OutputResult() {
		if (missNum == 0){
			missRate = 0;
		}
		else{
			missRate = (double) missNum / (double) accessNum * 100;
		}
		if (readInstrMissNum == 0) {
			readInstrMissRate = 0;
		}
		else {
			readInstrMissRate = (double) readInstrMissNum / (double) readInstrNum * 100;
		}
		if (readDataMissNum == 0) {
			readDataMissRate = 0;
		}
		else {
			readDataMissRate = (double) readDataMissNum / (double) readDataNum * 100;
		}
		if (writeDataMissNum == 0) {
			writeDataMissRate = 0;
		}
		else {
			writeDataMissRate = (double) writeDataMissNum / (double) writeDataNum * 100;
		}
		results[5].setText(String.valueOf(accessNum));
		results[6].setText(String.valueOf(missNum));
		results[7].setText(String.format("%.4f", missRate) + "%");
		
		results[9].setText(String.valueOf(readInstrNum));
		results[10].setText(String.valueOf(readInstrMissNum));
		results[11].setText(String.format("%.4f", readInstrMissRate) + "%");
		
		results[13].setText(String.valueOf(readDataNum));
		results[14].setText(String.valueOf(readDataMissNum));
		results[15].setText(String.format("%.4f", readDataMissRate) + "%");
		
		results[17].setText(String.valueOf(writeDataNum));
		results[18].setText(String.valueOf(writeDataMissNum));
		results[19].setText(String.format("%.4f", writeDataMissRate) + "%");
		
		for (int i = 0; i < stepLabel.length; i ++) {
			stepLabel[i].setText(stepText[i]);
		}
		stepLabel[5].setText(typename[type]);
		stepLabel[7].setText(String.valueOf(address));
		stepLabel[9].setText(String.valueOf(blockNum));
		stepLabel[11].setText(String.valueOf(blockOffset));
		stepLabel[13].setText(String.valueOf(index));
		stepLabel[15].setText(hitname[hit]);
	}


	public static void main(String[] args) {
		new CCacheSim();
	}
	
	/**
	 * 绘制 Cache 模拟器图形化界面
	 * 无需做修改
	 */
	public void draw() {
		//模拟器绘制面板
		setLayout(new BorderLayout(5,5));
		panelTop = new JPanel();
		panelLeft = new JPanel();
		panelRight = new JPanel();
		panelBottom = new JPanel();
		panelTop.setPreferredSize(new Dimension(800, 50));
		panelLeft.setPreferredSize(new Dimension(300, 600));
		panelRight.setPreferredSize(new Dimension(500, 600));
		panelBottom.setPreferredSize(new Dimension(800, 100));
		panelTop.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		panelLeft.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		panelRight.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		panelBottom.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//*****************************顶部面板绘制*****************************************//
		labelTop = new JLabel("Cache Simulator");
		labelTop.setAlignmentX(CENTER_ALIGNMENT);
		panelTop.add(labelTop);

		
		//*****************************左侧面板绘制*****************************************//
		labelLeft = new JLabel("Cache 参数设置");
		labelLeft.setPreferredSize(new Dimension(300, 40));
		
		//cache 大小设置
		//统一Cache大小
		//csLabel = new JLabel("总大小");
		//csLabel.setPreferredSize(new Dimension(120, 30));
		singleCache = new JRadioButton("统一Cache大小");
		singleCache.setPreferredSize(new Dimension(120, 30));
		singleCache.addActionListener(e -> {
			singleChosen = true;
			multiChosen = false;
			singleCache.setSelected(true);
			multiCache.setSelected(false);
			csBox.setEnabled(true);
			icBox.setEnabled(false);
			dcBox.setEnabled(false);
		});
		csBox = new JComboBox(cachesize);
		csBox.setPreferredSize(new Dimension(160, 30));
		csBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				csIndex = csBox.getSelectedIndex();
			}
		});
		//分离指令与数据Cache
		multiCache = new JRadioButton("独立Cache:");
		multiCache.setPreferredSize(new Dimension(280, 30));
		multiCache.addActionListener(e -> {
			singleChosen = false;
			multiChosen = true;
			singleCache.setSelected(false);
			multiCache.setSelected(true);
			csBox.setEnabled(false);
			icBox.setEnabled(true);
			dcBox.setEnabled(true);
		});
		icLabel = new JLabel("指令Cache大小");
		icLabel.setPreferredSize(new Dimension(100, 30));
		icBox = new JComboBox(Arrays.copyOfRange(cachesize, 0, cachesize.length - 1));
		icBox.setPreferredSize(new Dimension(160, 30));
		icBox.addItemListener(e -> {
			icIndex = icBox.getSelectedIndex();
		});
		dcLabel = new JLabel("数据Cache大小");
		dcLabel.setPreferredSize(new Dimension(100, 30));
		dcBox = new JComboBox(Arrays.copyOfRange(cachesize, 0, cachesize.length - 1));
		dcBox.setPreferredSize(new Dimension(160, 30));
		dcBox.addItemListener(e -> {
			dcIndex = dcBox.getSelectedIndex();
		});
		
		//cache 块大小设置
		bsLabel = new JLabel("块大小");
		bsLabel.setPreferredSize(new Dimension(120, 30));
		bsBox = new JComboBox(blocksize);
		bsBox.setPreferredSize(new Dimension(160, 30));
		bsBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				bsIndex = bsBox.getSelectedIndex();
			}
		});
		
		//相连度设置
		wayLabel = new JLabel("相联度");
		wayLabel.setPreferredSize(new Dimension(120, 30));
		wayBox = new JComboBox(way);
		wayBox.setPreferredSize(new Dimension(160, 30));
		wayBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				wayIndex = wayBox.getSelectedIndex();
			}
		});
		
		//替换策略设置
		replaceLabel = new JLabel("替换策略");
		replaceLabel.setPreferredSize(new Dimension(120, 30));
		replaceBox = new JComboBox(replace);
		replaceBox.setPreferredSize(new Dimension(160, 30));
		replaceBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				replaceIndex = replaceBox.getSelectedIndex();
			}
		});
		
		//欲取策略设置
		prefetchLabel = new JLabel("预取策略");
		prefetchLabel.setPreferredSize(new Dimension(120, 30));
		prefetchBox = new JComboBox(pref);
		prefetchBox.setPreferredSize(new Dimension(160, 30));
		prefetchBox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				prefetchIndex = prefetchBox.getSelectedIndex();
			}
		});
		
		//写策略设置
		writeLabel = new JLabel("写策略");
		writeLabel.setPreferredSize(new Dimension(120, 30));
		writeBox = new JComboBox(write);
		writeBox.setPreferredSize(new Dimension(160, 30));
		writeBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				writeIndex = writeBox.getSelectedIndex();
			}
		});
		
		//调块策略
		allocLabel = new JLabel("写不命中调块策略");
		allocLabel.setPreferredSize(new Dimension(120, 30));
		allocBox = new JComboBox(alloc);
		allocBox.setPreferredSize(new Dimension(160, 30));
		allocBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				allocIndex = allocBox.getSelectedIndex();
			}
		});
		
		//选择指令流文件
		fileLabel = new JLabel("选择指令流文件");
		fileLabel.setPreferredSize(new Dimension(120, 30));
		fileAddrBtn = new JLabel();
		fileAddrBtn.setPreferredSize(new Dimension(210,30));
		fileAddrBtn.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		fileBotton = new JButton("浏览");
		fileBotton.setPreferredSize(new Dimension(70,30));
		fileBotton.addActionListener(this);
		
		panelLeft.add(labelLeft);
		//panelLeft.add(csLabel);
		panelLeft.add(singleCache);
		panelLeft.add(csBox);
		panelLeft.add(multiCache);
		panelLeft.add(icLabel);
		panelLeft.add(icBox);
		panelLeft.add(dcLabel);
		panelLeft.add(dcBox);
		//====added over======
		panelLeft.add(bsLabel);
		panelLeft.add(bsBox);
		panelLeft.add(wayLabel);
		panelLeft.add(wayBox);
		panelLeft.add(replaceLabel);
		panelLeft.add(replaceBox);
		panelLeft.add(prefetchLabel);
		panelLeft.add(prefetchBox);
		panelLeft.add(writeLabel);
		panelLeft.add(writeBox);
		panelLeft.add(allocLabel);
		panelLeft.add(allocBox);
		panelLeft.add(fileLabel);
		panelLeft.add(fileAddrBtn);
		panelLeft.add(fileBotton);
		//设置初始属性
		singleCache.setSelected(true);
		csBox.setEnabled(true);
		multiCache.setSelected(false);
		icBox.setEnabled(false);
		dcBox.setEnabled(false);
		
		//*****************************右侧面板绘制*****************************************//
		//模拟结果展示区域
		rightLabel = new JLabel("模拟结果");
		rightLabel.setPreferredSize(new Dimension(500, 40));
		results = new JLabel[20];
		for (int i = 0; i < results.length; i ++) {
			results[i] = new JLabel("");
			results[i].setPreferredSize(new Dimension(100, 40));
			results[i].setText(resultText[i]);
		}
		stepLabel = new JLabel[16];
		for (int i= 0; i < stepLabel.length; i ++) {
			stepLabel[i] = new JLabel("");
			stepLabel[i].setPreferredSize(new Dimension(100, 40));
		}
		
		panelRight.add(rightLabel);
		for (int i=0; i<results.length; i++) {
			panelRight.add(results[i]);
		}
		for (int i= 0; i < stepLabel.length; i ++) {
			panelRight.add(stepLabel[i]);
		}
		/* panelRight.add(stepLabel1);
		panelRight.add(stepLabel2); */


		//*****************************底部面板绘制*****************************************//
		
		bottomLabel = new JLabel("执行控制");
		bottomLabel.setPreferredSize(new Dimension(800, 30));
		execStepBtn = new JButton("步进");
		execStepBtn.setLocation(100, 30);
		execStepBtn.addActionListener(this);
		execAllBtn = new JButton("执行到底");
		execAllBtn.setLocation(300, 30);
		execAllBtn.addActionListener(this);
		resetBtn = new JButton("复位");
		resetBtn.setLocation(500, 30);
		resetBtn.addActionListener(this);
		
		panelBottom.add(bottomLabel);
		panelBottom.add(execStepBtn);
		panelBottom.add(execAllBtn);
		panelBottom.add(resetBtn);

		add("North", panelTop);
		add("West", panelLeft);
		add("Center", panelRight);
		add("South", panelBottom);
		setSize(820, 770);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
