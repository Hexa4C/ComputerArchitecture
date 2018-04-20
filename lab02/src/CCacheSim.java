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


    //��������
	private String cachesize[] = { "2KB", "8KB", "32KB", "128KB", "512KB", "2MB" };
	private String blocksize[] = { "16B", "32B", "64B", "128B", "256B" };
	private String way[] = { "ֱ��ӳ��", "2·", "4·", "8·", "16·", "32·" };
	private String replace[] = { "LRU", "FIFO", "RAND" };
	private String pref[] = { "��Ԥȡ", "������Ԥȡ" };
	private String write[] = { "д�ط�", "дֱ�﷨" };
	private String alloc[] = { "��д����", "����д����" };
	private String typename[] = { "������", "д����", "��ָ��" };
	private String hitname[] = {"������", "����" };
	
	//�Ҳ�����ʾ
	private String resultText[]={" ", "���ʴ���", "�����д���", "��������", "�ܴ�����", "0", "0", "0","��ָ�", "0", "0", "0", "�����ݣ�", "0", "0", "0","д���ݣ�", "0", "0", "0"};
	private String stepText[] ={" ", " ", " ", " ", "��������", "", "��ַ", "", "���", "", "���ڵ�ַ", "", "����", "", "�������", ""};
	
	//���ļ�
	private File file;
	
	//�ֱ��ʾ��༸����������ѡ��ĵڼ�������� 0 ��ʼ
	private int csIndex, bsIndex, wayIndex, replaceIndex, prefetchIndex, writeIndex, allocIndex;
	
	//������������
	//...
	Scanner fscan;
	private int[] csSizeList = {2, 8, 32, 128, 512, 2048};
	private int[] bsSizeList = {16, 32, 64, 128, 256};
	private int[] wayValueList = {0, 2, 4, 8, 16, 32};
	private int icIndex, dcIndex;		//����Cache�Ĵ�С������ѡ��
	private int csSize, icSize, dcSize;		//ͳһ�����Cache��С��ֵ
	private int cBlockNum, icBlockNum, dcBlockNum; 		//ͳһ�����Cache�����С
	private int bsSize, wayValue;		//���С��·��
	private int[][] cacheBlock, iCacheBlock, dCacheBlock;		//ͳһ������Cache����
	private int type, address;		//ָ�����ͼ���ַ
	private int blockOffset, blockNum, index, tag, hit;		//����ƫ�ƣ���ӦCache�ڿ�ţ���������־���������
	private int accessNum, missNum, readInstrNum, readInstrMissNum;		//�����ܴ�����ȱʧ�ܴ�������ָ���������ָ��ȱʧ����
	private int readDataNum, readDataMissNum, writeDataNum, writeDataMissNum;	//�����ݴ�����������ȱʧ������д���ݴ�����д����ȱʧ����
	private double missRate, readInstrMissRate, readDataMissRate, writeDataMissRate;	//����ȱʧ��
	private Queue<Integer>[] cFIFO, icFIFO, dcFIFO;		//ͳһ��������Cache FIFO����
	private int[] cLRU, icLRU, dcLRU;		//ͳһ��������Cache LRU��
	private boolean prefetchTag;
	private int nline;
	
	/*
	 * ���캯��������ģ�������
	 */
	public CCacheSim(){
		super("Cache Simulator");
		draw();
	}
	
	
	//��Ӧ�¼������������¼���
	//   1. ִ�е����¼�
	//   2. ����ִ���¼�
	//   3. �ļ�ѡ���¼�
	public void actionPerformed(ActionEvent e){
				
		if (e.getSource() == execAllBtn) {
			if (fscan == null) {
				JOptionPane.showMessageDialog(null, "����Ҫ��ѡ���ļ���", "����", JOptionPane.ERROR_MESSAGE);
			}
			else {
				simExecAll();
				OutputResult();
			}
		}
		if (e.getSource() == execStepBtn) {
			if (fscan == null) {
				JOptionPane.showMessageDialog(null, "����Ҫ��ѡ���ļ���", "����", JOptionPane.ERROR_MESSAGE);
			}
			else {
				simExecStep();
				OutputResult();
			}
		}
		if (e.getSource() == resetBtn) {//��λ������
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
	 * ��ʼ�� Cache ģ����
	 */
	public void initCache() {
		prefetchTag = false;
		bsSize = bsSizeList[bsIndex];
		wayValue = wayValueList[wayIndex];
		if (singleChosen) {//ͳһCache
			csSize = csSizeList[csIndex];
			cBlockNum = csSize * 1024 / bsSize;
			cacheBlock = new int[cBlockNum][2];	//��ʼ��cache����
			if (wayValue != 0) {//ֱ��ӳ���滻����û��
				if (replaceIndex == 0) {
					cLRU = new int[cBlockNum];	//��ʼ��LRU��
				}//if
				else if (replaceIndex == 1) {
					cFIFO = new LinkedList[cBlockNum / wayValue];	//��ʼ��FIFO����
					for (int i = 0; i < cFIFO.length; i ++) {
						cFIFO[i] = new LinkedList<Integer>();
					}//for
				}//else if
			}//if
		}//if
		else {//����Cache
			icSize = csSizeList[icIndex];	//����ʼ������cache����
			icBlockNum = icSize * 1024 / bsSize;
			iCacheBlock = new int[icBlockNum][2];
			dcSize = csSizeList[dcIndex];
			dcBlockNum = dcSize * 1024 / bsSize;
			dCacheBlock = new int[dcBlockNum][2];
			if (wayValue != 0) {
				if (replaceIndex == 0) {	//��ʼ��LRU��
					icLRU = new int[icBlockNum];
					dcLRU = new int[dcBlockNum];
				}//if
				else if (replaceIndex == 1) {	//��ʼ��FIFO����
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
		accessNum = 0;//��ʼ��������ֵ
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
	 * ��ָ������������ļ��ж���
	 */
	public void readFile() {
		try {
			fscan = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * ��ʼ�����
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
	 * ģ�ⵥ��ִ��
	 */
	public void simExecStep() {
		if (fscan.hasNext() || prefetchTag) {
			if (singleChosen && wayValue > cBlockNum) {
				JOptionPane.showMessageDialog(null, "������·������", "����", JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if (multiChosen && (wayValue > dcBlockNum || wayValue > icBlockNum)) {
				JOptionPane.showMessageDialog(null, "������·������", "����", JOptionPane.ERROR_MESSAGE);
				return;
			}
			simExecBody();
		}
	}
	
	/*
	 * ģ��ִ�е���
	 */
	public void simExecAll() {
		while (fscan.hasNext() || prefetchTag) {
			if (singleChosen && wayValue > cBlockNum) {
				JOptionPane.showMessageDialog(null, "������·������", "����", JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if (multiChosen && (wayValue > dcBlockNum || wayValue > icBlockNum)) {
				JOptionPane.showMessageDialog(null, "������·������", "����", JOptionPane.ERROR_MESSAGE);
				return;
			}
			simExecBody();
		}
	}
	
	/*
	 * ִ������
	 */
	public void simExecBody() {
		if (prefetchTag) {//��ԤȡtagΪ�棬��Ԥȡ��һ��
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
			prefetchTag = true;	//���������ϸ�Ԥȡ���ϴη�Ԥȡ��Ԥȡtag��Ϊtrue
		}
		else if(prefetchTag) {
			prefetchTag = false;//���ϴ���Ԥȡ����Ԥȡtag��Ϊfalse
		}
	}
	
	/*
	 * ͳһCacheִ������
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
		if (wayValue == 0) {	//ֱ��ӳ��
			index = blockNum % cBlockNum;
			tag = blockNum / cBlockNum;
			if (cacheBlock[index][0] == tag) {	//����
				hit = 1;
			}
			else {//δ����
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
		else {//��·������
			index = blockNum % (cBlockNum / wayValue);
			tag = blockNum / (cBlockNum / wayValue);
			for (i = 0; i < wayValue; i ++) {//Cache��Ѱ����Ӧ��
				if (cacheBlock[index * wayValue + i][0] == tag) {
					break;
				}
			}
			if (i < wayValue) {//����
				hit = 1;
				if (replaceIndex == 0) {
					hitModifyLRU(index, wayValue, i, cLRU);
				}
				else if (replaceIndex == 1) {
					/* cFIFO[index].remove(i);
					cFIFO[index].offer(i); */
				}
			}
			else {//δ����
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
	 * ����ִ������
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
		if (type != 2) {//���ݲ���
			if (wayValue == 0) {	//ֱ��ӳ��
				index = blockNum % dcBlockNum;
				tag = blockNum / dcBlockNum;
				if (dCacheBlock[index][0] == tag) {	//����
					hit = 1;
				}
				else {//δ����
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
			else {//��·������
				index = blockNum % (dcBlockNum / wayValue);
				tag = blockNum / (dcBlockNum / wayValue);
				for (i = 0; i < wayValue; i ++) {
					if (dCacheBlock[index * wayValue + i][0] == tag) {
						break;
					}
				}
				if (i < wayValue) {//����
					hit = 1;
					if (replaceIndex == 0) {
						hitModifyLRU(index, wayValue, i, dcLRU);
					}
				}
				else {//δ����
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
		else {//ָ�����
			if (wayValue == 0) {	//ֱ��ӳ��
				index = blockNum % icBlockNum;
				tag = blockNum / icBlockNum;
				if (iCacheBlock[index][0] == tag) {	//����
					hit = 1;
				}
				else {//δ����
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
				if (i < wayValue) {//����
					hit = 1;
					if (replaceIndex == 0) {
						hitModifyLRU(index, wayValue, i, icLRU);
					}
				}
				else {//δ����
					hit = 0;
					missNum ++;
					readInstrMissNum ++;
					replaceIn();
				}
			}
		}
	}
	
	/*
	 * ����֮���LRU����޸�
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
	 *�����滻���Ի���
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
			if (singleChosen) {//ͳһCache
				if (!(type == 1 && allocIndex == 1)) {
					if (replaceIndex == 0) {//LRU
						int toBeReplacedIndex = index * wayValue + 0;
						int maxLRU = cLRU[toBeReplacedIndex];
						int tmpIndex = toBeReplacedIndex;
						while(tmpIndex < (index + 1) * wayValue) {//Ѱ��δռ�ÿ�
							if (cacheBlock[tmpIndex][1] == 0) {
								toBeReplacedIndex = tmpIndex;
								break;
							}
							tmpIndex ++;
						}
						if (tmpIndex == (index + 1) * wayValue) {
							tmpIndex = index * wayValue;
							while(tmpIndex < (index + 1) * wayValue) {//��û��δռ�ÿ飬��Ѱ��LRUֵ����
								if (cLRU[tmpIndex] > maxLRU) {
									maxLRU = cLRU[tmpIndex];
									toBeReplacedIndex = tmpIndex;
								}
								tmpIndex ++;
							}//while
						}
						for (int in = index * wayValue; in < index *wayValue + wayValue; in ++) {//��������LRUֵ����
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
						if (cFIFO[index].size() < wayValue) {//Ѱ��δռ�ÿ�
							while (toBeReplacedIndex < (index + 1) * wayValue) {
								if(cacheBlock[toBeReplacedIndex][1] == 0) {
									break;
								}
								toBeReplacedIndex ++;
							}
						}
						if (cFIFO[index].size() >= wayValue || toBeReplacedIndex >= (index + 1) * wayValue ) {//Ѱ�Ҵ�������
							toBeReplacedIndex = cFIFO[index].poll() + index * wayValue;
						}
						cacheBlock[toBeReplacedIndex][0] = tag;
						cacheBlock[toBeReplacedIndex][1] = 1;
						cFIFO[index].offer(toBeReplacedIndex - index * wayValue);
					}
					else {//RAND
						int toBeReplacedIndex = index * wayValue;
						while (toBeReplacedIndex < (index + 1) * wayValue) {//Ѱ��δռ�ÿ�
							if(cacheBlock[toBeReplacedIndex][1] == 0) {
								break;
							}
							toBeReplacedIndex ++;
						}
						if (toBeReplacedIndex == (index + 1) * wayValue) {//Ѱ�Ҵ�������
							toBeReplacedIndex = index * wayValue + new Random().nextInt(wayValue);
						}
						cacheBlock[toBeReplacedIndex][0] = tag;
						cacheBlock[toBeReplacedIndex][1] = 1;
					}
				}//if
			}//if
			else {//����Cache
				if (type == 0 || (type == 1 && allocIndex == 0)) {//���ݲ���
					if (replaceIndex == 0) {//LRU
						int toBeReplacedIndex = index * wayValue + 0;
						int maxLRU = dcLRU[toBeReplacedIndex];
						int tmpIndex = toBeReplacedIndex;
						while(tmpIndex < (index + 1) * wayValue) {//Ѱ��δռ�ÿ�
							if (dCacheBlock[tmpIndex][1] == 0) {
								toBeReplacedIndex = tmpIndex;
								break;
							}
							tmpIndex ++;
						}
						if (tmpIndex == (index + 1) * wayValue) {
							tmpIndex = index * wayValue;
							while(tmpIndex < (index + 1) * wayValue) {//Ѱ�Ҵ�������
								if (dcLRU[tmpIndex] > maxLRU) {
									maxLRU = dcLRU[tmpIndex];
									toBeReplacedIndex = tmpIndex;
								}
								tmpIndex ++;
							}//while
						}
						for (int in = index * wayValue; in < index *wayValue + wayValue; in ++) {//��������LRUֵ����
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
							while (toBeReplacedIndex < (index + 1) * wayValue) {//Ѱ��δռ�ÿ�
								if(dCacheBlock[toBeReplacedIndex][1] == 0) {
									break;
								}
								toBeReplacedIndex ++;
							}
						}
						if (dcFIFO[index].size() >= wayValue || toBeReplacedIndex >= (index + 1) * wayValue ) {//Ѱ�Ҵ�������
							toBeReplacedIndex = dcFIFO[index].poll() + index * wayValue;
						}
						dCacheBlock[toBeReplacedIndex][0] = tag;
						dCacheBlock[toBeReplacedIndex][1] = 1;
						dcFIFO[index].offer(toBeReplacedIndex - index * wayValue);
					}
					else {//RAND
						int toBeReplacedIndex = index * wayValue;
						while (toBeReplacedIndex < (index + 1) * wayValue) {//Ѱ��δռ�ÿ�
							if(dCacheBlock[toBeReplacedIndex][1] == 0) {
								break;
							}
							toBeReplacedIndex ++;
						}
						if (toBeReplacedIndex == (index + 1) * wayValue) {//Ѱ�Ҵ�������
							toBeReplacedIndex = index * wayValue + new Random().nextInt(wayValue);
						}
						dCacheBlock[toBeReplacedIndex][0] = tag;
						dCacheBlock[toBeReplacedIndex][1] = 1;
					}
				}
				else if(type == 2){//ָ�����
					if (replaceIndex == 0) {//LRU
						int toBeReplacedIndex = index * wayValue + 0;
						int maxLRU = icLRU[toBeReplacedIndex];
						int tmpIndex = toBeReplacedIndex;
						while(tmpIndex < (index + 1) * wayValue) {//Ѱ��δռ�ÿ�
							if (iCacheBlock[tmpIndex][1] == 0) {
								toBeReplacedIndex = tmpIndex;
								break;
							}
							tmpIndex ++;
						}
						if (tmpIndex == (index + 1) * wayValue) {
							tmpIndex = index * wayValue;
							while(tmpIndex < (index + 1) * wayValue) {//Ѱ�Ҵ�������
								if (icLRU[tmpIndex] > maxLRU) {
									maxLRU = icLRU[tmpIndex];
									toBeReplacedIndex = tmpIndex;
								}
								tmpIndex ++;
							}//while
						}
						for (int in = index * wayValue; in < index *wayValue + wayValue; in ++) {//��������LRUֵ����
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
							while (toBeReplacedIndex < (index + 1) * wayValue) {//Ѱ��δռ�ÿ�
								if(iCacheBlock[toBeReplacedIndex][1] == 0) {
									break;
								}
								toBeReplacedIndex ++;
							}
						}
						if (icFIFO[index].size() >= wayValue || toBeReplacedIndex >= (index + 1) * wayValue ) {//Ѱ�Ҵ�������
							toBeReplacedIndex = icFIFO[index].poll() + index * wayValue;
						}
						iCacheBlock[toBeReplacedIndex][0] = tag;
						iCacheBlock[toBeReplacedIndex][1] = 1;
						icFIFO[index].offer(toBeReplacedIndex - index * wayValue);
					}
					else {//RAND
						int toBeReplacedIndex = index * wayValue;
						while (toBeReplacedIndex < (index + 1) * wayValue) {//Ѱ��δռ�ÿ�
							if(iCacheBlock[toBeReplacedIndex][1] == 0) {
								break;
							}
							toBeReplacedIndex ++;
						}
						if (toBeReplacedIndex == (index + 1) * wayValue) {//Ѱ�Ҵ�������
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
	 * ������
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
	 * ���� Cache ģ����ͼ�λ�����
	 * �������޸�
	 */
	public void draw() {
		//ģ�����������
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

		//*****************************����������*****************************************//
		labelTop = new JLabel("Cache Simulator");
		labelTop.setAlignmentX(CENTER_ALIGNMENT);
		panelTop.add(labelTop);

		
		//*****************************���������*****************************************//
		labelLeft = new JLabel("Cache ��������");
		labelLeft.setPreferredSize(new Dimension(300, 40));
		
		//cache ��С����
		//ͳһCache��С
		//csLabel = new JLabel("�ܴ�С");
		//csLabel.setPreferredSize(new Dimension(120, 30));
		singleCache = new JRadioButton("ͳһCache��С");
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
		//����ָ��������Cache
		multiCache = new JRadioButton("����Cache:");
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
		icLabel = new JLabel("ָ��Cache��С");
		icLabel.setPreferredSize(new Dimension(100, 30));
		icBox = new JComboBox(Arrays.copyOfRange(cachesize, 0, cachesize.length - 1));
		icBox.setPreferredSize(new Dimension(160, 30));
		icBox.addItemListener(e -> {
			icIndex = icBox.getSelectedIndex();
		});
		dcLabel = new JLabel("����Cache��С");
		dcLabel.setPreferredSize(new Dimension(100, 30));
		dcBox = new JComboBox(Arrays.copyOfRange(cachesize, 0, cachesize.length - 1));
		dcBox.setPreferredSize(new Dimension(160, 30));
		dcBox.addItemListener(e -> {
			dcIndex = dcBox.getSelectedIndex();
		});
		
		//cache ���С����
		bsLabel = new JLabel("���С");
		bsLabel.setPreferredSize(new Dimension(120, 30));
		bsBox = new JComboBox(blocksize);
		bsBox.setPreferredSize(new Dimension(160, 30));
		bsBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				bsIndex = bsBox.getSelectedIndex();
			}
		});
		
		//����������
		wayLabel = new JLabel("������");
		wayLabel.setPreferredSize(new Dimension(120, 30));
		wayBox = new JComboBox(way);
		wayBox.setPreferredSize(new Dimension(160, 30));
		wayBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				wayIndex = wayBox.getSelectedIndex();
			}
		});
		
		//�滻��������
		replaceLabel = new JLabel("�滻����");
		replaceLabel.setPreferredSize(new Dimension(120, 30));
		replaceBox = new JComboBox(replace);
		replaceBox.setPreferredSize(new Dimension(160, 30));
		replaceBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				replaceIndex = replaceBox.getSelectedIndex();
			}
		});
		
		//��ȡ��������
		prefetchLabel = new JLabel("Ԥȡ����");
		prefetchLabel.setPreferredSize(new Dimension(120, 30));
		prefetchBox = new JComboBox(pref);
		prefetchBox.setPreferredSize(new Dimension(160, 30));
		prefetchBox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				prefetchIndex = prefetchBox.getSelectedIndex();
			}
		});
		
		//д��������
		writeLabel = new JLabel("д����");
		writeLabel.setPreferredSize(new Dimension(120, 30));
		writeBox = new JComboBox(write);
		writeBox.setPreferredSize(new Dimension(160, 30));
		writeBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				writeIndex = writeBox.getSelectedIndex();
			}
		});
		
		//�������
		allocLabel = new JLabel("д�����е������");
		allocLabel.setPreferredSize(new Dimension(120, 30));
		allocBox = new JComboBox(alloc);
		allocBox.setPreferredSize(new Dimension(160, 30));
		allocBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				allocIndex = allocBox.getSelectedIndex();
			}
		});
		
		//ѡ��ָ�����ļ�
		fileLabel = new JLabel("ѡ��ָ�����ļ�");
		fileLabel.setPreferredSize(new Dimension(120, 30));
		fileAddrBtn = new JLabel();
		fileAddrBtn.setPreferredSize(new Dimension(210,30));
		fileAddrBtn.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		fileBotton = new JButton("���");
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
		//���ó�ʼ����
		singleCache.setSelected(true);
		csBox.setEnabled(true);
		multiCache.setSelected(false);
		icBox.setEnabled(false);
		dcBox.setEnabled(false);
		
		//*****************************�Ҳ�������*****************************************//
		//ģ����չʾ����
		rightLabel = new JLabel("ģ����");
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


		//*****************************�ײ�������*****************************************//
		
		bottomLabel = new JLabel("ִ�п���");
		bottomLabel.setPreferredSize(new Dimension(800, 30));
		execStepBtn = new JButton("����");
		execStepBtn.setLocation(100, 30);
		execStepBtn.addActionListener(this);
		execAllBtn = new JButton("ִ�е���");
		execAllBtn.setLocation(300, 30);
		execAllBtn.addActionListener(this);
		resetBtn = new JButton("��λ");
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
