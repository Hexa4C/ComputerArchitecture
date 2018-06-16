import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class directory {	
	/*****����panel2~panel5******/
	static Mypanel panel2 =new Mypanel();
	static Mypanel panel3 =new Mypanel();
	static Mypanel panel4 =new Mypanel();
	static Mypanel panel5 =new Mypanel();
	static int seq = 0;
	
	static JComboBox<String> Mylistmodel1_1 = new JComboBox<>(new Mylistmodel());
	static class Mylistmodel extends AbstractListModel<String> implements ComboBoxModel<String>{		
		private static final long serialVersionUID = 1L;
		String selecteditem=null;
		private String[] test={"ֱ��ӳ��","��·������","��·������"};
		public String getElementAt(int index){
			return test[index];
		}
		public int getSize(){
			return test.length;
		}
		public void setSelectedItem(Object item){
			selecteditem=(String)item;
		}
		public Object getSelectedItem( ){
			return selecteditem;
		}
		public int getIndex() {
			for (int i = 0; i < test.length; i++) {
				if (test[i].equals(getSelectedItem()))
					return i;
			}
			return 0;
		}
		
	}
	static class Mylistmodel2 extends AbstractListModel<String> implements ComboBoxModel<String>{		
		private static final long serialVersionUID = 1L;
		String selecteditem=null;
		private String[] test={"��","д"};
		public String getElementAt(int index){
			return test[index];
		}
		public int getSize(){
			return test.length;
		}
		public void setSelectedItem(Object item){
			selecteditem=(String)item;
		}
		public Object getSelectedItem( ){
			return selecteditem;
		}
		public int getIndex() {
			for (int i = 0; i < test.length; i++) {
				if (test[i].equals(getSelectedItem()))
					return i;
			}
			return 0;
		}
		
	}
	
	static class Mypanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1L;
		JLabel label = new JLabel("���ʵ�ַ");
		JLabel label_2 = new JLabel("Process1");

		JTextField jtext = new JTextField("");
		JButton button = new JButton("ִ��");
		JComboBox<String> Mylistmodel = new JComboBox<>(new Mylistmodel2());
		JTextArea infoText = new JTextArea();

		/*********cache�еı���*********/
		String[] Cache_ca = { "Cache", "��/д", "Ŀ���ַ", "״̬"};
		/*********cache�е�����*********/
		String[][] Cache_Content = { { "0", "", "", ""}, { "1", "", "", ""}, { "2", "", "", ""}, { "3", "", "", ""} };
		/*********memory�ı���*********/
		String[] Mem_ca = { "Memory", "state", "P1", "P2", "P3", "P4"};
		private String[] stateStr = { "��Ч", "��ռ", "����"};
		private int invalid = 0, modified = 1, shared = 2;
		private String[] reqStr = { "��ȡȱʧ", "ʧЧ", "дȱʧ" };
		private int[] memAddr = new int[4];
		private int[] state = new int[4];
		private int[] memState = new int[10];
		private int[][] remote = new int[10][4];
		int procId;

		/*********memory�е�����*********/
		String[][] Mem_Content = {
			{ "0", "", "", "", "", ""}, { "1", "", "" , "", "", ""}, { "2", "", "", "", "", "" },
			{ "3", "", "", "", "", "" }, { "4", "", "" , "", "", ""}, { "5", "", "" , "", "", ""},
			{ "6", "", "" , "", "", ""}, { "7", "", "", "", "", "" }, { "8", "", "", "", "", "" }, { "9", "", "", "", "", "" }
		};
		/************cache�Ĺ���ģ��***********/
		JTable table_1 = new JTable(Cache_Content, Cache_ca);
		JScrollPane scrollPane = new JScrollPane(table_1);
		/************memory�Ĺ���ģ��***********/
		JTable table_2 = new JTable(Mem_Content, Mem_ca);
		JScrollPane scrollPane2 = new JScrollPane(table_2);

		public Mypanel() {
			super();
			setSize(350, 600);
			setLayout(null);

			/*****���ԭ��********/
			add(jtext);
			add(label);
			add(label_2);
			add(button);
			add(Mylistmodel);
			add(scrollPane);
			add(scrollPane2);
			add(infoText);

			/****����ԭ����С������********/
			label_2.setFont(new Font("", 1, 16));
			label_2.setBounds(10, 10, 100, 30);

			label.setFont(new Font("", 1, 16));
			label.setBounds(10, 50, 100, 30);

			jtext.setFont(new Font("", 1, 15));
			jtext.setBounds(100, 50, 50, 30);

			Mylistmodel.setFont(new Font("", 1, 15));
			Mylistmodel.setBounds(160, 50, 50, 30);

			scrollPane.setFont(new Font("", 1, 15));
			scrollPane.setBounds(10, 90, 310, 90);

			scrollPane2.setFont(new Font("", 1, 15));
			scrollPane2.setBounds(10, 190, 310, 180);

			button.setFont(new Font("", 1, 15));
			button.setBounds(220, 50, 100, 35);

			infoText.setFont(new Font("", 1, 15));
			infoText.setBounds(10, 390, 310, 190);

			/******��Ӱ�ť�¼�********/
			button.addActionListener(this);
		}

		public void init() {
			/******Mypanel�ĳ�ʼ��******/
			jtext.setText("");
			Mylistmodel.setSelectedItem(null);
            Mylistmodel.setSelectedIndex(0);
			for (int i = 0; i <= 3; i++)
				for (int j = 1; j <= 3; j++)
					Cache_Content[i][j] = " ";
			for (int i = 0; i <= 9; i++)
				for (int j = 1; j <= 5; j++)
					Mem_Content[i][j] = " ";
			for (int i = 0; i < 4; i ++) {
				state[i] = 0;
				memAddr[i] = -1;
			}
			for (int i = 0; i < 10; i ++) {
				memState[i] = 0;
				for (int j = 0; j < 4; j++) {
					remote[i][j] = -1;
				}
			}
			setVisible(false);
			setVisible(true);

		}

		public void showInfo(String str) {
			JOptionPane.showConfirmDialog(null, str, "��ʾ", JOptionPane.DEFAULT_OPTION);
		}

		public void setProcInfo(String str, int mode) {
			seq ++;
			if (mode == 0) {
				infoText.setText("("+ String.valueOf(seq) + ")" + str);
			} else {
				String origText = infoText.getText();
				if (!origText.equals("")){
					infoText.setText(origText + ",\n" + "("+ String.valueOf(seq) + ")" + str);
				}
				else {
					infoText.setText("("+ String.valueOf(seq) + ")" + str);
				}
			}
		}
		
		public int inCache(int address) {
			for (int i = 0; i < 4; i++) {
				if (memAddr[i] == address) {
					return i;
				}
			}
			return -1;
		}

		public String getRemoteProc(int addr) {
			String procs = "";
			for (int i = 0; i < 4; i ++) {
				if(remote[addr][i] == -1) {
					continue;
				}
				if (!procs.equals("")) {
					procs += "," + String.valueOf(i + 1);
				}
				else {
					procs += String.valueOf(i + 1);
				}
			}
			return "{" + procs + "}";
		}

		public int addrSwitch(int address) {
			int cacheAddr = 0;
			int wayNum = 0, waySize = 1;
			if (Mylistmodel1_1.getSelectedItem().equals("ֱ��ӳ��")) {
				cacheAddr = address % 4;
				return cacheAddr;
			} else if (Mylistmodel1_1.getSelectedItem().equals("��·������")) {
				waySize = 2;
				cacheAddr = address % 2;
				int i = 0;
				for (i = 0; i < waySize; i++) {
					if (memAddr[cacheAddr * 2 + i] == -1) {
						break;
					}
				}
				if (i == waySize) {
					i = 0;
				}
				wayNum = i;
			} else if (Mylistmodel1_1.getSelectedItem().equals("��·������")) {
				waySize = 4;
				cacheAddr = 0;
				int i = 0;
				for (i = 0; i < waySize; i++) {
					if (memAddr[cacheAddr * 2 + i] == -1) {
						break;
					}
				}
				if (i == waySize) {
					i = 0;
				}
				wayNum = i;
			}
			return cacheAddr * waySize + wayNum;
		}

		public void updateDirectory(int srcProc, int address, int readOrWrite, boolean hit) {
			int host = address / 10;
			if (readOrWrite == 0) {//��
				if (hit) {//����
					//�޲���
				}
				else {//������
					int cAddr = addrSwitch(address);
					int formerAddr = memAddr[cAddr];
					if (formerAddr > 0) {
						swapOut(srcProc, formerAddr);
					}
					if (state[cAddr] == invalid || state[cAddr] == shared) {//��Ч����
						//TODO
					}
					else if (state[cAddr] == modified) {//��ռ
						setProcInfo("д������", 1);
					}
					setProcInfo("֪ͨ����address" + String.valueOf(address) + "��ȱʧ", 1);
					setProcInfo("������ֵ���͵����أ�" + stateStr[state[cAddr]] + "->����", 1);
					updateHost(host, address, 0);
					Cache_Content[cAddr][1] = "��";
					memAddr[cAddr] = address;
					Cache_Content[cAddr][2] = String.valueOf(address);
					state[cAddr] = shared;
					Cache_Content[cAddr][3] = stateStr[shared];
				}
			}
			else {//д
				if (hit) {//����
					int cAddr = inCache(address);
					if (state[cAddr] == shared) {//����
						//TODO
						setProcInfo("֪ͨ����address" + String.valueOf(address) + "д��" + stateStr[state[cAddr]] + "->��ռ", 1);
						updateHost(host, address, 1);
						Cache_Content[cAddr][1] = "д";
						memAddr[cAddr] = address;
						Cache_Content[cAddr][2] = String.valueOf(address);
						state[cAddr] = modified;
						Cache_Content[cAddr][3] = stateStr[modified];
					}
					else if (state[cAddr] == modified) {//��ռ
						//TODO
					}
				}
				else {//������
					int cAddr = addrSwitch(address);
					int formerAddr = memAddr[cAddr];
					if (formerAddr > 0) {
						swapOut(srcProc, formerAddr);
					}
					if (state[cAddr] == invalid) {//��Ч
						//TODO
					}
					else if (state[cAddr] == shared) {//����
						//TODO
					}
					else if (state[cAddr] == modified) {//��ռ
						setProcInfo("д������", 1);
					}
					setProcInfo("֪ͨ����address" + String.valueOf(address) + "д��:" + stateStr[state[cAddr]] + "->��ռ", 1);
					updateHost(host, address, 2);
					Cache_Content[cAddr][1] = "д";
					memAddr[cAddr] = address;
					Cache_Content[cAddr][2] = String.valueOf(address);
					state[cAddr] = modified;
					Cache_Content[cAddr][3] = stateStr[modified];
				}
			}
		}

		public void swapOut(int srcProc, int address) {
			Mypanel[] procs = { panel2, panel3, panel4, panel5 };
			Mypanel proc = procs[address / 10];
			int procAddr = address % 10;
			String change = proc.getRemoteProc(procAddr);
			proc.remote[procAddr][srcProc - 1] = -1;
			proc.Mem_Content[procAddr][srcProc + 1] = "";
			change += "->" + proc.getRemoteProc(procAddr);
			proc.setProcInfo("�޸Ĺ���" + change, 1);
			if (proc.getRemoteProc(procAddr).equals("{}")) {
				proc.memState[procAddr] = 0;
				proc.Mem_Content[procAddr][1] = "";
			}
		}

		public void updateHost(int hostProc, int address, int req) {
			Mypanel[] procs = { panel2, panel3, panel4, panel5 };
			Mypanel host = procs[hostProc];
			int hostAddr = address % 10;
			if (req == 0) {//��ȡȱʧ
				if (host.memState[hostAddr] == shared || host.memState[hostAddr] == invalid) {//����
					host.setProcInfo("����address" + String.valueOf(address) + ":" + stateStr[host.memState[hostAddr]] + "->" + stateStr[shared], 1);
					host.memState[hostAddr] = shared;
					host.Mem_Content[hostAddr][1] = stateStr[shared];
					String change = host.getRemoteProc(hostAddr);
					host.remote[hostAddr][procId - 1] = procId;
					host.Mem_Content[hostAddr][procId + 1] = String.valueOf(procId);
					host.Mem_Content[hostAddr][1] = stateStr[shared];
					change += "->" + host.getRemoteProc(hostAddr);
					host.setProcInfo(change, 1);
				}
				else if(host.memState[hostAddr] == modified) {
					host.setProcInfo("����address" + String.valueOf(address) + ":" + stateStr[modified] + "->" + stateStr[shared], 1);
					host.memState[hostAddr] = shared;
					host.Mem_Content[hostAddr][1] = stateStr[shared];
					String change = host.getRemoteProc(hostAddr);
					host.remote[hostAddr][procId - 1] = procId;
					host.Mem_Content[hostAddr][procId + 1] = String.valueOf(procId);
					host.Mem_Content[hostAddr][1] = stateStr[shared];
					change += "->" + host.getRemoteProc(hostAddr);
					host.setProcInfo(change, 1);
				}
			}
			else if (req == 1) {//ʧЧ
				String change = host.getRemoteProc(hostAddr);
				for (int i = 0; i < 4; i ++) {
					if (i == procId - 1) {
						continue;
					}
					for (int j = 0; j < 4; j ++) {
						if (procs[i].memAddr[j] == address) {
							procs[i].setProcInfo("��������ʧЧ�����أ�"  + String.valueOf(address) + ":" + stateStr[state[j]] + "->" + stateStr[invalid], 1);
							procs[i].memAddr[j] = -1;
							procs[i].state[j] = 0;
							procs[i].Cache_Content[j][1] = "";
							procs[i].Cache_Content[j][2] = "";
							procs[i].Cache_Content[j][3] = "";
							host.remote[hostAddr][i] = -1;
							host.Mem_Content[hostAddr][i + 2] = "";
						}
					}
				}
				change += "->" + host.getRemoteProc(hostAddr);
				host.setProcInfo("����address" + String.valueOf(address) + ":" + stateStr[host.memState[hostAddr]] + "->" + stateStr[modified], 1);
				host.setProcInfo(change, 1);
				host.memState[hostAddr] = modified;
				host.Mem_Content[hostAddr][1] = stateStr[modified];
			}
			else {//дȱʧ
				String change = host.getRemoteProc(hostAddr);
				for (int i = 0; i < 4; i ++) {
					if (i == procId - 1) {
						continue;
					}
					for (int j = 0; j < 4; j ++) {
						if (procs[i].memAddr[j] == address) {
							procs[i].setProcInfo("��������ʧЧ�����أ�"  + String.valueOf(address) + ":" + stateStr[state[j]] + "->" + stateStr[invalid], 1);
							procs[i].memAddr[j] = -1;
							procs[i].state[j] = 0;
							procs[i].Cache_Content[j][1] = "";
							procs[i].Cache_Content[j][2] = "";
							procs[i].Cache_Content[j][3] = "";
							host.remote[hostAddr][i] = -1;
							host.Mem_Content[hostAddr][i + 2] = "";
						}
					}
				}
				host.remote[hostAddr][procId - 1] = procId;
				host.Mem_Content[hostAddr][procId + 1] = String.valueOf(procId);
				change += "->" + host.getRemoteProc(hostAddr);
				host.setProcInfo("����address" + String.valueOf(address) + ":" + stateStr[host.memState[hostAddr]] + "->" + stateStr[modified], 1);
				host.setProcInfo(change, 1);
				host.memState[hostAddr] = modified;
				host.Mem_Content[hostAddr][1] = stateStr[modified];
			}
		} 

		public void clearAllArea() {
			Mypanel[] procs = { panel2, panel3, panel4, panel5 };
			for (int i = 0; i < 4; i ++) {
				procs[i].infoText.setText("");
			}
		}

		public void actionPerformed(ActionEvent e) {
			/******��д�Լ��Ĵ�����*******/
			if (e.getSource() == button) {
				seq=0;
				clearAllArea();
				if (jtext.getText().equals("") || Mylistmodel.getSelectedItem() == null) {
					showInfo("������/ѡ�����ݺ�ѡ�");
					return;
				}
				int address = Integer.parseInt(jtext.getText());
				if (address < 0 || address > 39) {
					showInfo("���ݳ���Memory��ַ�ռ�");
					return;
				}
				int readOrWrite = Mylistmodel.getSelectedIndex();
				if (readOrWrite == 0) {//��
					boolean hit = false;
					int idx = inCache(address);
					if (idx >= 0) {//����
						hit = true;
						setProcInfo("��ȡ����", 0);
						updateDirectory(procId, address, readOrWrite, hit);
					}
					else {//ȱʧ
						hit = false;
						setProcInfo("��ȡȱʧ", 0);
						updateDirectory(procId, address, readOrWrite, hit);
					}
				}
				else {
					boolean hit = false;
					int idx = inCache(address);
					if (idx >= 0) {//����
						hit = true;
						setProcInfo("д������", 0);
						updateDirectory(procId, address, readOrWrite, hit);
					}
					else {//ȱʧ
						hit = false;
						setProcInfo("д��ȱʧ", 0);
						updateDirectory(procId, address, readOrWrite, hit);
					}
				}
			}
			/**********��ʾˢ�º������********/
			panel2.setVisible(false);
			panel2.setVisible(true);
			panel3.setVisible(false);
			panel3.setVisible(true);
			panel4.setVisible(false);
			panel4.setVisible(true);
			panel5.setVisible(false);
			panel5.setVisible(true);
		}
	}
	
	public directory() {
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame myjf = new JFrame("��cacheһ����ģ��֮Ŀ¼��");
		myjf.setSize(1500, 800);
		myjf.setLayout(null);
		myjf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Container C1 = myjf.getContentPane();
		
		/*****�½�panel1*****/
		JPanel panel1 = new JPanel();

		C1.add(panel2);
		C1.add(panel3);
		C1.add(panel4);
		C1.add(panel5);
		panel2.setBounds(10, 100, 350, 600);
		panel3.setBounds(360, 100, 350, 600);
		panel4.setBounds(720, 100, 350, 600);
		panel5.setBounds(1080, 100, 350, 600);
		
		
		/********����ÿ��Mypanel�Ĳ�ͬ�Ĳ���************/
		panel2.label_2.setText("Process1");
		panel3.label_2.setText("Process2");
		panel4.label_2.setText("Process3");
		panel5.label_2.setText("Process4");
		panel2.table_1.getColumnModel().getColumn(0).setHeaderValue("cache1");
		panel2.Cache_ca[0]="Cache1";
		panel3.table_1.getColumnModel().getColumn(0).setHeaderValue("cache2");
		panel3.Cache_ca[0]="Cache2";
		panel4.table_1.getColumnModel().getColumn(0).setHeaderValue("cache3");
		panel4.Cache_ca[0]="Cache3";
		panel5.table_1.getColumnModel().getColumn(0).setHeaderValue("cache4");
		panel5.Cache_ca[0]="Cache4";
		panel2.init();
		panel3.init();
		panel4.init();
		panel5.init();
		panel2.procId = 1;
		panel3.procId = 2;
		panel4.procId = 3;
		panel5.procId = 4;
		
		
		panel2.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory1");
		panel3.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory2");
		panel4.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory3");
		panel5.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory4");
		
		for(int i=0;i<10;i++){
			panel3.Mem_Content[i][0]=String.valueOf((Integer.parseInt(panel2.Mem_Content[i][0])+10));
			panel4.Mem_Content[i][0]=String.valueOf((Integer.parseInt(panel2.Mem_Content[i][0])+20));
			panel5.Mem_Content[i][0]=String.valueOf((Integer.parseInt(panel2.Mem_Content[i][0])+30));
		}
		/********����ͷ��panel*****/
		panel1.setBounds(10, 10, 1500, 100);
		panel1.setLayout(null);
		
		JLabel label1_1=new JLabel("ִ�з�ʽ:����ִ��");
		label1_1.setFont(new Font("",1,20));
		label1_1.setBounds(15, 15, 200, 40);
		panel1.add(label1_1);
		
		//JComboBox<String> Mylistmodel1_1 = new JComboBox<>(new Mylistmodel());
		Mylistmodel1_1.setBounds(220, 15, 150, 40);
		Mylistmodel1_1.setFont(new Font("",1,20));
		panel1.add(Mylistmodel1_1);
		Mylistmodel1_1.setSelectedIndex(0);
		
		JButton button1_1=new JButton("��λ");
		button1_1.setBounds(400, 15, 70, 40);
		
		/**********��λ��ť�¼�����ʼ����***********/
		button1_1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				panel2.init();
				panel3.init();
				panel4.init();
				panel5.init();
				panel2.procId = 1;
				panel3.procId = 2;
				panel4.procId = 3;
				panel5.procId = 4;
				Mypanel[] procs = { panel2, panel3, panel4, panel5 };
				for (int i = 0; i < 4; i ++) {
					procs[i].infoText.setText("");
				}
				Mylistmodel1_1.setSelectedIndex(0);
				
			}
		});
		
		/*panel2.Mem_Content[1][1]="11";*/
		panel1.add(button1_1);
		C1.add(panel1);
		myjf.setVisible(true);
		

		
	}

	
}

