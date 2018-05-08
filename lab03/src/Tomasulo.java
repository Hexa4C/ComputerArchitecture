import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

/**
 * @author yanqing.qyq 2012-2015@USTC
 * 模板说明：该模板主要提供依赖Swing组件提供的JPanle，JFrame，JButton等提供的GUI。使用“监听器”模式监听各个Button的事件，从而根据具体事件执行不同方法。
 * Tomasulo算法核心需同学们自行完成，见说明（4）
 * 对于界面必须修改部分，见说明(1),(2),(3)
 *
 *  (1)说明：根据你的设计完善指令设置中的下拉框内容
 *	(2)说明：请根据你的设计指定各个面板（指令状态，保留站，Load部件，寄存器部件）的大小
 *	(3)说明：设置界面默认指令
 *	(4)说明： Tomasulo算法实现
 */

public class Tomasulo extends JFrame implements ActionListener{
	/*
	 * 界面上有六个面板：
	 * ins_set_panel : 指令设置
	 * EX_time_set_panel : 执行时间设置
	 * ins_state_panel : 指令状态
	 * RS_panel : 保留站状态
	 * Load_panel : Load部件
	 * Registers_state_panel : 寄存器状态
	 */
	private JPanel ins_set_panel,EX_time_set_panel,ins_state_panel,RS_panel,Load_panel,Registers_state_panel;

	/*
	 * 四个操作按钮：步进，进5步，重置，执行
	 */
	private JButton stepbut,step5but,resetbut,startbut;

	/*
	 * 指令选择框
	 */
	private JComboBox inst_typebox[]=new JComboBox[24];

	/*
	 * 每个面板的名称
	 */
	private JLabel inst_typel, timel, tl1,tl2,tl3,tl4,resl,regl,ldl,insl,stepsl;
	private int time[]=new int[4];	//各个浮点运算及载入执行时间

	/*
	 * 部件执行时间的输入框
	 */
	private JTextField tt1,tt2,tt3,tt4;

	private int intv[][]=new int[6][4],		//输入的指令的类型及操作数等的序号
				cnow,						//当前周期数
				inst_typenow=0;				//当前指令类型？
	private int cal[][]={{-1,0,0},{-1,0,0},{-1,0,0},{-1,0,0},{-1,0,0}};	//浮点运算部件的剩余时间，
	private int ld[][]={{0,0},{0,0},{0,0}};								//Load的地址，值
	private int ff[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};				//浮点寄存器的状态序列号

	private int istime[] = new int[6];
	private int exetime[][] = new int[6][2];
	private int wbtime[] = new int[6];
	//private int loadhead = -1;		//当前存储序列的头的序号
	private int inst_pos = 0;		//当前执行的指令序号
	private int calresttime[] = {-1, -1, -1, -1, -1};	//浮点运算部件的剩余时间
	private int ldrestime[] = {-1, -1, -1};				//Load部件剩余时间
	private int inst_rs[] = new int[6];		//指令与其对应的保留站
	private String regsrc[] = new String[16];	//寄存器结果来源（其实本来直接用寄存器表就行了，但是为了和示例保持一致只能这样）
	private int memcnt;

	/*
	 * (1)说明：根据你的设计完善指令设置中的下拉框内容
	 * inst_type： 指令下拉框内容:"NOP","L.D","ADD.D","SUB.D","MULT.D","DIV.D"…………
	 * regist_table：       目的寄存器下拉框内容:"F0","F2","F4","F6","F8" …………
	 * rx：       源操作数寄存器内容:"R0","R1","R2","R3","R4","R5","R6","R7","R8","R9" …………
	 * ix：       立即数下拉框内容:"0","1","2","3","4","5","6","7","8","9" …………
	 */
	private String  inst_type[]={"NOP","L.D","ADD.D","SUB.D","MULT.D","DIV.D"},
					regist_table[]={"F0","F2","F4","F6","F8","F10","F12","F14","F16"
							,"F18","F20","F22","F24","F26","F28","F30","F32"},
					rx[]={"R0","R1","R2","R3","R4","R5","R6"},
					ix[]={"0","1","2","3","4","5","6","7", "8", "9", "10","11","12","13","14","15","16","17", "18", "19", "20","21","22","23","24","25","26","27", "28", "29", "30", "31"};
	
	/*
	 * (2)说明：请根据你的设计指定各个面板（指令状态，保留站，Load部件，寄存器部件）的大小
	 * 		指令状态 面板
	 * 		保留站 面板
	 * 		Load部件 面板
	 * 		寄存器 面板
	 * 					的大小
	 */
	private	String  my_inst_type[][]=new String[7][4],		//显示在指令状态面板的内容
					my_rs[][]=new String[6][8],				//显示在保留站面板中内容
					my_load[][]=new String[4][4],			//显示在Load部件面板中的内容
					my_regsters[][]=new String[3][17];		//显示在寄存器面板中的内容
	private	JLabel  inst_typejl[][]=new JLabel[7][4], resjl[][]=new JLabel[6][8],
					ldjl[][]=new JLabel[4][4], regjl[][]=new JLabel[3][17];

//构造方法
	public Tomasulo(){
		super("Tomasulo Simulator");

		//设置布局
		Container cp=getContentPane();
		//cp.setPreferredSize(new Dimension(800, 1000));
		FlowLayout layout=new FlowLayout();
		cp.setLayout(layout);
		//cp.setSize(new Dimension(800, 1000));
		//cp.setVisible(true);

		//指令设置。GridLayout(int 指令条数, int 操作码+操作数, int hgap, int vgap)
		inst_typel = new JLabel("指令设置");
		ins_set_panel = new JPanel(new GridLayout(6,4,0,0));
		ins_set_panel.setPreferredSize(new Dimension(350, 150));
		ins_set_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//操作按钮:执行，重设，步进，步进5步
		timel = new JLabel("执行时间设置");
		EX_time_set_panel = new JPanel(new GridLayout(2,4,0,0));
		EX_time_set_panel.setPreferredSize(new Dimension(280, 80));
		EX_time_set_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		//指令状态
		insl = new JLabel("指令状态");
		ins_state_panel = new JPanel(new GridLayout(7,4,0,0));
		ins_state_panel.setPreferredSize(new Dimension(420, 175));
		ins_state_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));


		//寄存器状态
		regl = new JLabel("寄存器");
		Registers_state_panel = new JPanel(new GridLayout(3,17,0,0));
		Registers_state_panel.setPreferredSize(new Dimension(740, 75));
		Registers_state_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		//保留站
		resl = new JLabel("保留站");
		RS_panel = new JPanel(new GridLayout(6,7,0,0));
		RS_panel.setPreferredSize(new Dimension(420, 150));
		RS_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		//Load部件
		ldl = new JLabel("Load部件");
		Load_panel = new JPanel(new GridLayout(4,4,0,0));
		Load_panel.setPreferredSize(new Dimension(450, 100));
		Load_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

		tl1 = new JLabel("Load");
		tl2 = new JLabel("加/减");
		tl3 = new JLabel("乘法");
		tl4 = new JLabel("除法");

//操作按钮:执行，重设，步进，步进5步
		stepsl = new JLabel();
		stepsl.setPreferredSize(new Dimension(200, 30));
		stepsl.setHorizontalAlignment(SwingConstants.CENTER);
		stepsl.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		stepbut = new JButton("步进");
		stepbut.addActionListener(this);
		step5but = new JButton("步进5步");
		step5but.addActionListener(this);
		startbut = new JButton("执行");
		startbut.addActionListener(this);
		resetbut= new JButton("重设");
		resetbut.addActionListener(this);
		tt1 = new JTextField("2");
		tt2 = new JTextField("2");
		tt3 = new JTextField("10");
		tt4 = new JTextField("40");

//指令设置
		/*
		 * 设置指令选择框（操作码，操作数，立即数等）的default选择
		 */
		for (int i=0;i<2;i++)
			for (int j=0;j<4;j++){
				if (j==0){
					inst_typebox[i*4+j]=new JComboBox(inst_type);
				}
				else if (j==1){
					inst_typebox[i*4+j]=new JComboBox(regist_table);
				}
				else if (j==2){
					inst_typebox[i*4+j]=new JComboBox(ix);
				}
				else {
					inst_typebox[i*4+j]=new JComboBox(rx);
				}
				inst_typebox[i*4+j].addActionListener(this);
				ins_set_panel.add(inst_typebox[i*4+j]);
			}
		for (int i=2;i<6;i++)
			for (int j=0;j<4;j++){
				if (j==0){
					inst_typebox[i*4+j]=new JComboBox(inst_type);
				}
				else {
					inst_typebox[i*4+j]=new JComboBox(regist_table);
				}
				inst_typebox[i*4+j].addActionListener(this);
				ins_set_panel.add(inst_typebox[i*4+j]);
			}
		/*
		 * (3)说明：设置界面默认指令，根据你设计的指令，操作数等的选择范围进行设置。
		 * 默认6条指令。待修改
		 */
		inst_typebox[0].setSelectedIndex(1);
		inst_typebox[1].setSelectedIndex(3);
		inst_typebox[2].setSelectedIndex(21);
		inst_typebox[3].setSelectedIndex(2);

		inst_typebox[4].setSelectedIndex(1);
		inst_typebox[5].setSelectedIndex(1);
		inst_typebox[6].setSelectedIndex(20);
		inst_typebox[7].setSelectedIndex(3);

		inst_typebox[8].setSelectedIndex(4);
		inst_typebox[9].setSelectedIndex(0);
		inst_typebox[10].setSelectedIndex(1);
		inst_typebox[11].setSelectedIndex(2);

		inst_typebox[12].setSelectedIndex(3);
		inst_typebox[13].setSelectedIndex(4);
		inst_typebox[14].setSelectedIndex(3);
		inst_typebox[15].setSelectedIndex(1);

		inst_typebox[16].setSelectedIndex(5);
		inst_typebox[17].setSelectedIndex(5);
		inst_typebox[18].setSelectedIndex(0);
		inst_typebox[19].setSelectedIndex(3);

		inst_typebox[20].setSelectedIndex(2);
		inst_typebox[21].setSelectedIndex(3);
		inst_typebox[22].setSelectedIndex(4);
		inst_typebox[23].setSelectedIndex(1);

//执行时间设置
		EX_time_set_panel.add(tl1);
		EX_time_set_panel.add(tt1);
		EX_time_set_panel.add(tl2);
		EX_time_set_panel.add(tt2);
		EX_time_set_panel.add(tl3);
		EX_time_set_panel.add(tt3);
		EX_time_set_panel.add(tl4);
		EX_time_set_panel.add(tt4);

//指令状态设置
		for (int i=0;i<7;i++)
		{
			for (int j=0;j<4;j++){
				inst_typejl[i][j]=new JLabel(my_inst_type[i][j]);
				inst_typejl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				ins_state_panel.add(inst_typejl[i][j]);
			}
		}
//保留站设置
		for (int i=0;i<6;i++)
		{
			for (int j=0;j<8;j++){
				resjl[i][j]=new JLabel(my_rs[i][j]);
				resjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				RS_panel.add(resjl[i][j]);
			}
		}
//Load部件设置
		for (int i=0;i<4;i++)
		{
			for (int j=0;j<4;j++){
				ldjl[i][j]=new JLabel(my_load[i][j]);
				ldjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				Load_panel.add(ldjl[i][j]);
			}
		}
//寄存器设置
		for (int i=0;i<3;i++)
		{
			for (int j=0;j<17;j++){
				regjl[i][j]=new JLabel(my_regsters[i][j]);
				regjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
				Registers_state_panel.add(regjl[i][j]);
			}
		}

//向容器添加以上部件
		cp.add(inst_typel);
		cp.add(ins_set_panel);
		cp.add(timel);
		cp.add(EX_time_set_panel);

		cp.add(startbut);
		cp.add(resetbut);
		cp.add(stepbut);
		cp.add(step5but);

		cp.add(Load_panel);
		cp.add(ldl);
		cp.add(RS_panel);
		cp.add(resl);
		cp.add(stepsl);
		cp.add(Registers_state_panel);
		cp.add(regl);
		cp.add(ins_state_panel);
		cp.add(insl);

		stepbut.setEnabled(false);
		step5but.setEnabled(false);
		ins_state_panel.setVisible(false);
		insl.setVisible(false);
		RS_panel.setVisible(false);
		ldl.setVisible(false);
		Load_panel.setVisible(false);
		resl.setVisible(false);
		stepsl.setVisible(false);
		Registers_state_panel.setVisible(false);
		regl.setVisible(false);
		setSize(820,820);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

/*
 * 点击”执行“按钮后，根据选择的指令，初始化其他几个面板
 */
	public void init(){
		// get value
		for (int i=0;i<6;i++){
			intv[i][0]=inst_typebox[i*4].getSelectedIndex();
			if (intv[i][0]!=0){
				intv[i][1]=inst_typebox[i*4+1].getSelectedIndex();
				if (intv[i][0]==1){
					intv[i][2]=inst_typebox[i*4+2].getSelectedIndex();
					intv[i][3]=inst_typebox[i*4+3].getSelectedIndex();
				}
				else {
					intv[i][2]=inst_typebox[i*4+2].getSelectedIndex();
					intv[i][3]=inst_typebox[i*4+3].getSelectedIndex();
				}
			}
		}
		time[0]=Integer.parseInt(tt1.getText());
		time[1]=Integer.parseInt(tt2.getText());
		time[2]=Integer.parseInt(tt3.getText());
		time[3]=Integer.parseInt(tt4.getText());
		//System.out.println(time[0]);
		// set 0
		my_inst_type[0][0]="指令";
		my_inst_type[0][1]="流出";
		my_inst_type[0][2]="执行";
		my_inst_type[0][3]="写回";


		my_load[0][0]="名称";
		my_load[0][1]="Busy";
		my_load[0][2]="地址";
		my_load[0][3]="值";
		my_load[1][0]="Load1";
		my_load[2][0]="Load2";
		my_load[3][0]="Load3";
		my_load[1][1]="no";
		my_load[2][1]="no";
		my_load[3][1]="no";

		my_rs[0][0]="Time";
		my_rs[0][1]="名称";
		my_rs[0][2]="Busy";
		my_rs[0][3]="Op";
		my_rs[0][4]="Vj";
		my_rs[0][5]="Vk";
		my_rs[0][6]="Qj";
		my_rs[0][7]="Qk";
		my_rs[1][1]="Add1";
		my_rs[2][1]="Add2";
		my_rs[3][1]="Add3";
		my_rs[4][1]="Mult1";
		my_rs[5][1]="Mult2";
		my_rs[1][2]="no";
		my_rs[2][2]="no";
		my_rs[3][2]="no";
		my_rs[4][2]="no";
		my_rs[5][2]="no";

		my_regsters[0][0]="字段";
		for (int i=1;i<17;i++){
			//System.out.print(i+" "+regist_table[i-1];
			my_regsters[0][i]=regist_table[i-1];

		}
		my_regsters[1][0]="状态";
		my_regsters[2][0]="值";

		for (int i=1;i<7;i++)
		for (int j=0;j<4;j++){
			if (j==0){
				int temp=i-1;
				String disp;
				disp = inst_type[inst_typebox[temp*4].getSelectedIndex()]+" ";
				if (inst_typebox[temp*4].getSelectedIndex()==0) disp=disp;
				else if (inst_typebox[temp*4].getSelectedIndex()==1){
					disp=disp+regist_table[inst_typebox[temp*4+1].getSelectedIndex()]+','+ix[inst_typebox[temp*4+2].getSelectedIndex()]+'('+rx[inst_typebox[temp*4+3].getSelectedIndex()]+')';
				}
				else {
					disp=disp+regist_table[inst_typebox[temp*4+1].getSelectedIndex()]+','+regist_table[inst_typebox[temp*4+2].getSelectedIndex()]+','+regist_table[inst_typebox[temp*4+3].getSelectedIndex()];
				}

				
				my_inst_type[i][j]=disp;
			}
			else my_inst_type[i][j]="";
		}
		for (int i=1;i<6;i++)
		for (int j=0;j<8;j++)if (j!=1&&j!=2){
			my_rs[i][j]="";
		}
		for (int i=1;i<4;i++)
		for (int j=2;j<4;j++){
			my_load[i][j]="";
		}
		for (int i=1;i<3;i++)
		for (int j=1;j<17;j++){
			my_regsters[i][j]="";
		}
		inst_typenow=0;
		for (int i=0;i<5;i++){
			for (int j=1;j<3;j++) cal[i][j]=0;
			cal[i][0]=-1;
		}
		for (int i=0;i<3;i++)
			for (int j=0;j<2;j++) ld[i][j]=0;
		for (int i=0;i<17;i++) ff[i]=0;

		cnow = 0;
		inst_pos = 0;
		initTime();
		memcnt = 0;
	}

/*
 * 点击操作按钮后，用于显示结果
 */
	public void display(){
		for (int i=0;i<7;i++)
			for (int j=0;j<4;j++){
				inst_typejl[i][j].setText(my_inst_type[i][j]);
			}
		for (int i=0;i<6;i++)
			for (int j=0;j<8;j++){
				resjl[i][j].setText(my_rs[i][j]);
			}
		for (int i=0;i<4;i++)
			for (int j=0;j<4;j++){
				ldjl[i][j].setText(my_load[i][j]);
			}
		for (int i=0;i<3;i++)
			for (int j=0;j<17;j++){
				regjl[i][j].setText(my_regsters[i][j]);
			}
		stepsl.setText("当前周期："+String.valueOf(cnow-1));
	}

	public void actionPerformed(ActionEvent e){
//点击“执行”按钮的监听器
		if (e.getSource()==startbut) {
			for (int i=0;i<24;i++) inst_typebox[i].setEnabled(false);
			tt1.setEnabled(false);tt2.setEnabled(false);
			tt3.setEnabled(false);tt4.setEnabled(false);
			stepbut.setEnabled(true);
			step5but.setEnabled(true);
			startbut.setEnabled(false);
			//根据指令设置的指令初始化其他的面板
			init();
			cnow=1;
			//展示其他面板
			display();
			ins_state_panel.setVisible(true);
			RS_panel.setVisible(true);
			Load_panel.setVisible(true);
			Registers_state_panel.setVisible(true);
			insl.setVisible(true);
			ldl.setVisible(true);
			resl.setVisible(true);
			stepsl.setVisible(true);
			regl.setVisible(true);
		}
//点击“重置”按钮的监听器
		if (e.getSource()==resetbut) {
			for (int i=0;i<24;i++) inst_typebox[i].setEnabled(true);
			tt1.setEnabled(true);tt2.setEnabled(true);
			tt3.setEnabled(true);tt4.setEnabled(true);
			stepbut.setEnabled(false);
			step5but.setEnabled(false);
			startbut.setEnabled(true);
			ins_state_panel.setVisible(false);
			insl.setVisible(false);
			RS_panel.setVisible(false);
			ldl.setVisible(false);
			Load_panel.setVisible(false);
			resl.setVisible(false);
			stepsl.setVisible(false);
			Registers_state_panel.setVisible(false);
			regl.setVisible(false);

			inst_pos = 0;
			cnow = 0;
			initTime();
			memcnt = 0;
		}
//点击“步进”按钮的监听器
		if (e.getSource()==stepbut) {
			core();
			cnow++;
			display();
		}
//点击“进5步”按钮的监听器
		if (e.getSource()==step5but) {
			for (int i=0;i<5;i++){
				core();
				cnow++;
			}
			display();
		}

		for (int i=0;i<24;i=i+4)
		{
			if (e.getSource()==inst_typebox[i]) {
				if (inst_typebox[i].getSelectedIndex()==1){
					inst_typebox[i+2].removeAllItems();
					for (int j=0;j<ix.length;j++) inst_typebox[i+2].addItem(ix[j]);
					inst_typebox[i+3].removeAllItems();
					for (int j=0;j<rx.length;j++) inst_typebox[i+3].addItem(rx[j]);
				}
				else {
					inst_typebox[i+2].removeAllItems();
					for (int j=0;j<regist_table.length;j++) inst_typebox[i+2].addItem(regist_table[j]);
					inst_typebox[i+3].removeAllItems();
					for (int j=0;j<regist_table.length;j++) inst_typebox[i+3].addItem(regist_table[j]);
				}
			}
		}
	}
/*
 * (4)说明： Tomasulo算法实现
 */
	public void initTime(){//初始化发射时间，执行时间以及写回时间，以及一些其他的东西吧
		for(int i = 0; i < 6; i ++) {
			istime[i] = -1;
			exetime[i][0] = -1;
			exetime[i][1] = -1;
			wbtime[i] = -1;
		}
		for (int i = 0; i < 16; i ++) {
			regsrc[i] = "";
		}
	}
	
	public void issue(){
		int[] curr_inst = new int[4];
		if (inst_pos > 5) {
			return;
		}
		for(int i = 0; i < 4; i ++) {
			curr_inst[i] = intv[inst_pos][i];
			//System.out.print(curr_inst[i] + " ");
		}
		if (curr_inst[0] == 0) {//NOP
			istime[inst_pos] = cnow;
		}
		else if(curr_inst[0] == 1) {//LD
			int idx = -1;
			/* for (idx = (loadhead + 1) % 3; idx != loadhead; idx = (idx + 1) % 3) {//寻找空闲load部件
				if (my_load[idx + 1][1].equals("no")) {
					break;
				}
			} */
			for (int i = 0; i < 3; i ++) {//寻找空闲load部件
				if (my_load[i + 1][1].equals("no")) {
					idx = i;
					break;
				}
			}
			if (idx >= 0) {//有空闲
				/* if (loadhead == -1) {
					loadhead = 0;
				} */
				my_load[idx + 1][1] = "yes";
				my_load[idx + 1][2] = String.valueOf(curr_inst[2]);
				my_regsters[1][curr_inst[1] + 1] = my_load[idx + 1][0];
				regsrc[curr_inst[1]] = my_load[idx + 1][0];
				istime[inst_pos] = cnow;
				my_inst_type[inst_pos + 1][1] = String.valueOf(istime[inst_pos]);
				inst_rs[inst_pos] = idx + 1;
				inst_pos ++;
			}
		}
		else if(curr_inst[0] < 4) {//ADD or SUB
			int idx = -1;
			for (idx = 0; idx < 3; idx ++) {//寻找空闲Add部件
				if (my_rs[idx + 1][2].equals("no")) {
					break;
				}
			}
			if (idx >= 0) {//有空闲
				idx += 1;
				if (!regsrc[curr_inst[2]].equals("")) {
					my_rs[idx][6] = regsrc[curr_inst[2]];
				}
				else {
					my_rs[idx][4] = "R[" + regist_table[curr_inst[2]] + "]";
					my_rs[idx][6] = "";
				}
				if (!regsrc[curr_inst[3]].equals("")) {
					my_rs[idx][7] = regsrc[curr_inst[3]];
				}
				else {
					my_rs[idx][5] = "R[" + regist_table[curr_inst[3]] + "]";
					my_rs[idx][7] = "";
				}
				my_rs[idx][2] = "yes";
				my_regsters[1][curr_inst[1] + 1] = my_rs[idx][1];
				regsrc[curr_inst[1]] = my_rs[idx][1];
				my_rs[idx][3] = inst_type[curr_inst[0]];
				istime[inst_pos] = cnow;
				my_inst_type[inst_pos + 1][1] = String.valueOf(istime[inst_pos]);
				inst_rs[inst_pos] = idx;
				inst_pos ++;
			}
		}
		else {//MULT or DIV
			int idx = -1;
			for (idx = 0; idx < 2; idx ++) {//寻找空闲Mult部件
				if (my_rs[idx + 4][2].equals("no")) {
					break;
				}
			}
			if (idx >= 0) {//有空闲
				idx += 4;
				if (!regsrc[curr_inst[2]].equals("")) {
					my_rs[idx][6] = regsrc[curr_inst[2]];
				}
				else {
					my_rs[idx][4] = "R[" + regist_table[curr_inst[2]] + "]";
					my_rs[idx][6] = "";
				}
				if (!regsrc[curr_inst[3]].equals("")) {
					my_rs[idx][7] = regsrc[curr_inst[3]];
				}
				else {
					my_rs[idx][5] = "R[" + regist_table[curr_inst[3]] + "]";
					my_rs[idx][7] = "";
				}
				my_rs[idx][2] = "yes";
				my_regsters[1][curr_inst[1] + 1] = my_rs[idx][1];
				regsrc[curr_inst[1]] = my_rs[idx][1];
				my_rs[idx][3] = inst_type[curr_inst[0]];
				istime[inst_pos] = cnow;
				my_inst_type[inst_pos + 1][1] = String.valueOf(istime[inst_pos]);
				inst_rs[inst_pos] = idx;
				inst_pos ++;
			}
		}
	}

	public boolean execute(int idx){
		if (exetime[idx][0] < 0) {//还未执行
			if (intv[idx][0] == 1) {//LD
				int ldidx = inst_rs[idx];
				exetime[idx][0] = cnow;
				my_load[ldidx][2] = "R[" + rx[intv[idx][3]] + "]+" + my_load[ldidx][2];
				my_inst_type[idx + 1][2] = String.valueOf(exetime[idx][0]) + "~";
				ldrestime[ldidx] = time[0] - 1;
			}
			else if (intv[idx][0] > 1 && intv[idx][0] < 6) {//ADD, SUB, MULT, DIV
				int rsidx = inst_rs[idx];
				if (my_rs[rsidx][6].equals("") && my_rs[rsidx][7].equals("")) {
					exetime[idx][0] = cnow;
					my_inst_type[idx + 1][2] = String.valueOf(exetime[idx][0]) + "~";
					if (intv[idx][0] < 4) {
						calresttime[rsidx - 1] = time[1] - 1;
					}
					else {
						calresttime[rsidx - 1] = time[intv[idx][0] - 2] - 1;
					}
					my_rs[rsidx][0] = String.valueOf(calresttime[rsidx - 1]);
				}
			}
		}
		else if (exetime[idx][0] > 0 && exetime[idx][1] < 0) {//正在执行
			if (intv[idx][0] == 1){//LD
				int ldidx = inst_rs[idx];
				ldrestime[ldidx] --;
				if (ldrestime[ldidx] == 0) {
					exetime[idx][1] = cnow;
					my_inst_type[idx + 1][2] = String.valueOf(exetime[idx][0]) + "~" + String.valueOf(exetime[idx][1]);
					memcnt ++;
					my_load[ldidx][3] = "M" + String.valueOf(memcnt) + " = M[" +my_load[ldidx][2] + "]";
				}
			}
			else if (intv[idx][0] > 1 && intv[idx][0] < 6) {//ADD, SUB, MULT, DIV
				int rsidx = inst_rs[idx];
				calresttime[rsidx - 1] --;
				my_rs[rsidx][0] = String.valueOf(calresttime[rsidx - 1]);
				if (calresttime[rsidx - 1] == 0) {
					exetime[idx][1] = cnow;
					my_inst_type[idx + 1][2] = String.valueOf(exetime[idx][0]) + "~" + String.valueOf(exetime[idx][1]);
					my_rs[rsidx][0] = "";
				}
			}
		}
		else if (exetime[idx][0] > 0 && exetime[idx][1] > 0) {//已执行完
			return true;
		}
		return false;
	}

	public void writeResult(int idx){
		if (wbtime[idx] > 0) {
			return;
		}
		else {
			if (intv[idx][0] == 1) {//LD
				int ldidx = inst_rs[idx];
				String fname = my_load[ldidx][0];
				String content = my_load[ldidx][3].split(" ")[0];
				for (int i = 0; i < 16; i ++) {
					if (regsrc[i].equals(fname)) {
						my_regsters[2][i + 1] = content;
						regsrc[i] = "";
					}
				}
				for (int i = 1; i < 6; i ++) {
					if (my_rs[i][6].equals(fname)) {
						my_rs[i][4] = content;
						my_rs[i][6] = "";
					}
					if (my_rs[i][7].equals(fname)) {
						my_rs[i][5] = content;
						my_rs[i][7] = "";
					}
				}
				my_load[ldidx][1] = "no";
				my_load[ldidx][2] = "";
				my_load[ldidx][3] = "";
				wbtime[idx] = cnow;
				my_inst_type[idx + 1][3] = String.valueOf(wbtime[idx]);
			}
			else if (intv[idx][0] >1 && intv[idx][0] < 6) {//FP
				int rsidx = inst_rs[idx];
				String fname = my_rs[rsidx][1];
				memcnt ++;
				String content = "M" + String.valueOf(memcnt);
				for (int i = 0; i < 16; i ++) {
					if (regsrc[i].equals(fname)) {
						my_regsters[2][i + 1] = content;
						regsrc[i] = "";
					}
				}
				for (int i = 1; i < 6; i ++) {
					if (my_rs[i][6].equals(fname)) {
						my_rs[i][4] = content;
						my_rs[i][6] = "";
					}
					if (my_rs[i][7].equals(fname)) {
						my_rs[i][5] = content;
						my_rs[i][7] = "";
					}
				}
				my_rs[rsidx][2] = "no";
				my_rs[rsidx][3] = "";
				my_rs[rsidx][4] = "";
				my_rs[rsidx][5] = "";
				wbtime[idx] = cnow;
				my_inst_type[idx + 1][3] = String.valueOf(wbtime[idx]);
			}
		}
	}

	public void core()
	{
		int pos = inst_pos;
		boolean exe[] = new boolean[pos];
		issue();
		for (int i = 0; i < pos; i ++) {
			exe[i] = execute(i);
		}
		for (int i = 0; i < pos; i ++) {
			if (exe[i]) {
				writeResult(i);
			}
		}
	}

	public static void main(String[] args) {
		new Tomasulo();
	}

}
