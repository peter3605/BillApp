import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class GUI {	
	private DatabaseConnector myDb;
	private JList<String> billingList;
	public static JFrame mainFrame;
	private JPanel homePanel,tennantPanel,panel,billingPanel;
	private JTabbedPane tabbedPane;
	private JButton tennantChangeButton, tennantNewButton, tennantDeleteButton;
	private JTable resultTable;
	private JScrollPane resultArea,billingScrollPane,finalBillPane;
	private DefaultTableModel resultTableModel;
	private String[][] tennantData;
	private JTextField waterTField,electricityTField;
	private JComboBox dueDates;
	private JRadioButton button1, button2;
	private ButtonGroup buttonGroup;

	private String[] tennantColumnNames = new String[]{"Apartment","Tennant Name","Email Address","Last Bill"};
	private int tennantCurrentRow = 0;
	String[] stringOptions = {"1 Day","2 Days","3 Days","4 Days","5 Days","6 Days","1 Week", "2 Weeks", "3 Weeks","1 Month"};
	int[] intOptions = {1,2,3,4,5,6,7,14,21,30};
	private String billDate = "";
	private static int intDueDate = 1;
	private String stringDueDate = "";

	GUI(DatabaseConnector db) {
		System.out.println("Launching GUI");
		myDb = db;

		try {
			initialize();

			System.out.println("Successfully launched GUI");
		}
		catch(Exception e) {
			System.out.println("ERROR - unable to launch GUI - "+e);
		}
	}

	//create everything for the GUI
	private void initialize() {
		drawHomePanel();
		drawTennantPanel();
		drawBillingPanel();

		tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(new Dimension(800,800));
		tabbedPane.addTab("Home",homePanel);
		tabbedPane.addTab("Manage Tennant Information",tennantPanel);
		tabbedPane.addTab("Billing Application",finalBillPane);

		mainFrame = new JFrame("Property Management Interface");
		mainFrame.setResizable(false);
		mainFrame.getContentPane().add(tabbedPane);
		mainFrame.setVisible(true);
		mainFrame.pack();

		WindowAdapter exitListener = new WindowAdapter()  {
			public void windowClosing(WindowEvent e) {      
				System.out.println("Closed GUI");
			}
		};		
		mainFrame.addWindowListener(exitListener);
	}

	private void drawHomePanel() {
		homePanel = new JPanel();
		homePanel.setLayout(new BoxLayout(homePanel,BoxLayout.PAGE_AXIS));
		JLabel title = new JLabel();
		title.setText("Chateau Ridge LLC");
		title.setFont(new Font(Font.SERIF,Font.BOLD,80));

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		homePanel.setLayout(gridbag);

		c.fill = GridBagConstraints.HORIZONTAL; 
		c.ipady = 40;
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 1;
		gridbag.setConstraints(title, c);
		homePanel.add(title);

	}

	private void drawTennantPanel() {
		tennantChangeButton = new JButton("Change Tennant Information");
		tennantChangeButton.setEnabled(false);
		tennantChangeButton.setActionCommand("change");
		tennantChangeButton.addActionListener(new TennantButtonListener());

		tennantNewButton = new JButton("Add New Tennant");
		tennantNewButton.setActionCommand("new");
		tennantNewButton.addActionListener(new TennantButtonListener());

		tennantDeleteButton = new JButton("Delete Tennant Record");
		tennantDeleteButton.setEnabled(false);
		tennantDeleteButton.setActionCommand("delete");
		tennantDeleteButton.addActionListener(new TennantButtonListener());

		tennantPanel = new JPanel();
		tennantPanel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				resultTable.clearSelection();
				tennantChangeButton.setEnabled(false);
				tennantDeleteButton.setEnabled(false);
			}
		});

		JPanel titlePanel = new JPanel();
		createTennantTitlePanel(titlePanel);

		JPanel resultPanel = new JPanel();
		createTennantResultPanel(resultPanel);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		tennantPanel.setLayout(gridbag);

		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		gridbag.setConstraints(titlePanel,c);
		tennantPanel.add(titlePanel);

		c.fill = GridBagConstraints.NONE;
		c.weightx = 1;
		c.weighty = 5;
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 2;
		gridbag.setConstraints(resultPanel,c);
		tennantPanel.add(resultPanel);

		c.gridx = 1; 
		c.gridy = 4;
		c.gridwidth = 2; 
		c.gridheight = 1;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(tennantChangeButton, c);
		tennantPanel.add(tennantChangeButton);	

		c.gridx = 1; 
		c.gridy = 3;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(tennantDeleteButton, c);
		tennantPanel.add(tennantDeleteButton);	

		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(tennantNewButton, c);
		tennantPanel.add(tennantNewButton);
	}

	private void createTennantTitlePanel(JPanel panel) {
		JLabel title = new JLabel();
		title.setText("Tennant Information");
		title.setFont(new Font(Font.SERIF,Font.BOLD,40));
		panel.add(title);
	}

	private void createTennantResultPanel(JPanel panel) {	
		tennantData = myDb.getTennantTable();

		resultTableModel = new ResultTableModel(tennantData,tennantColumnNames);

		resultTable = new JTable(resultTableModel);
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		resultTable.getColumnModel().getColumn(0).setPreferredWidth(1);
		resultTable.getColumnModel().getColumn(2).setPreferredWidth(150);
		resultTable.setRowHeight(50);
		resultTable.setAutoCreateRowSorter(true);
		resultTable.setVisible(true);
		resultTable.setColumnSelectionAllowed(false);
		resultTable.setFillsViewportHeight(true);
		resultTable.setPreferredSize(new Dimension(800,700));

		resultArea = new JScrollPane(resultTable);
		resultArea.setPreferredSize(new Dimension(700,500));
		resultArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		resultTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JTable target = (JTable)e.getSource();
				int row  = target.getSelectedRow(); 
				setCurrentRow(row);

				tennantChangeButton.setEnabled(true);
				tennantDeleteButton.setEnabled(true);
			}
		});

		panel.add(resultArea);
	}

	public void setCurrentRow(int i) {
		tennantCurrentRow = i;
	}

	public int getCurrentRow() {
		return tennantCurrentRow;
	}

	public void refreshTennantTable() {
		tennantData = myDb.getTennantTable();

		resultTableModel = new ResultTableModel(tennantData,tennantColumnNames);

		resultTable.setModel(resultTableModel);
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		resultTable.getColumnModel().getColumn(0).setPreferredWidth(1);
		resultTable.getColumnModel().getColumn(2).setPreferredWidth(150);
		resultTable.setRowHeight(50);
		resultTable.setAutoCreateRowSorter(true);
		resultTable.setVisible(true);
		resultTable.setColumnSelectionAllowed(false);
		resultTable.setFillsViewportHeight(true);
		resultTable.setPreferredSize(new Dimension(800,700));

		tennantChangeButton.setEnabled(false);
		tennantDeleteButton.setEnabled(false);
	}

	private void drawBillingPanel() {
		String today = FileDownload.createNewFileName().substring(0,10).replaceAll("_", "-");

		billingPanel = new JPanel();
		billingPanel.setPreferredSize(new Dimension(700,1000));

		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.PAGE_AXIS));
		JLabel title = new JLabel();
		title.setText("Billing");
		title.setFont(new Font(Font.SERIF,Font.BOLD,40));
		titlePanel.add(title);
		JLabel date = new JLabel("Today is: "+today);
		date.setFont(new Font(Font.SERIF,Font.PLAIN,20));
		titlePanel.add(date);

		billingPanel.add(titlePanel);

		JPanel mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(700,300));
		JPanel buttonPanel = new JPanel();
		buttonGroup = new ButtonGroup();
		button1 = new JRadioButton("All Tennants");
		button1.setActionCommand("all");
		button1.addActionListener(new RadioButtonListener());
		button1.setSelected(true);
		button2 = new JRadioButton("Select Tennants");
		button2.setActionCommand("some");	
		button2.addActionListener(new RadioButtonListener());
		buttonGroup.add(button1);
		buttonGroup.add(button2);
		buttonPanel.add(button1);
		buttonPanel.add(button2);
		mainPanel.add(buttonPanel);
		createMainBillingPanel();
		mainPanel.add(panel);

		billingPanel.add(mainPanel);

		billingPanel.add(new JLabel("    Select date you want to bill to (if no date is selected, then it will bill until today)    "));

		new Calendar(billingPanel,this);

		JPanel dueDatePanel = new JPanel();
		dueDatePanel.setPreferredSize(new Dimension(700,50));
		JLabel label = new JLabel("Select when the bill is due: ");
		createDueDates();
		dueDatePanel.add(label);
		dueDatePanel.add(dueDates);

		billingPanel.add(dueDatePanel);

		JPanel enterPanel = new JPanel();
		JLabel label1 = new JLabel("Enter water unit cost: ");
		JLabel label2 = new JLabel("Enter electricity unit cost: ");
		waterTField = new JTextField(myDb.getWaterUnitCost());
		waterTField.setPreferredSize(new Dimension(200,30));
		electricityTField = new JTextField(myDb.getElectricityUnitCost());
		electricityTField.setPreferredSize(new Dimension(200,30));
		enterPanel.add(label1);
		enterPanel.add(waterTField);
		enterPanel.add(label2);
		enterPanel.add(electricityTField);

		billingPanel.add(enterPanel);

		JPanel runPanel = new JPanel();
		runPanel.setPreferredSize(new Dimension(200,50));
		JButton run = new JButton("Run");
		run.setActionCommand("water");
		run.addActionListener(new RunButtonListener());
		run.setPreferredSize(new Dimension(200,40));
		runPanel.add(run);

		billingPanel.add(runPanel);

		finalBillPane = new JScrollPane(billingPanel);
		finalBillPane.setPreferredSize(new Dimension(800,800));
		finalBillPane.getVerticalScrollBar().setUnitIncrement(16);
	}

	private void createMainBillingPanel() {
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(700,200));

		String[] names = new String[tennantData.length];
		for(int i=0;i<tennantData.length;i++) {
			names[i] = tennantData[i][0]+" - "+tennantData[i][1];
		}
		DefaultListModel<String> listModel = new DefaultListModel<>();
		for(int i=0;i<names.length;i++) {
			listModel.addElement(names[i]);
		}

		billingList = new <String>JList(listModel);
		billingList.setPreferredSize(new Dimension(100,(30*(names.length+1))));
		billingList.setFixedCellHeight(30);
		int start = 0;
		int end = billingList.getModel().getSize() - 1;
		if (end >= 0) {
			billingList.setSelectionInterval(start, end);
		}
		billingList.setEnabled(false);

		billingScrollPane = new JScrollPane(billingList);
		billingScrollPane.setPreferredSize(new Dimension(200,150));
		panel.add(billingScrollPane);
	}

	private void refreshBillingList() {
		String[] names = new String[tennantData.length];
		for(int i=0;i<tennantData.length;i++) {
			names[i] = tennantData[i][0]+" - "+tennantData[i][1];
		}

		DefaultListModel<String> listModel = new DefaultListModel<>();

		for(int i=0;i<names.length;i++) {
			listModel.addElement(names[i]);
		}

		billingList.setModel(listModel);
		billingList.setEnabled(false);
	}	

	private void createOptionFrame(String string) {
		JFrame optionFrame = new JFrame("Error");

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(400,50));
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
		JLabel label = new JLabel(string);
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(label);
		JButton button = new JButton("Ok");
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(button2.isSelected()) {
					billingList.clearSelection();
				}
				waterTField.setText(myDb.getWaterUnitCost());
				electricityTField.setText(myDb.getElectricityUnitCost());
				optionFrame.dispose();
			}
		});

		panel.add(button);

		optionFrame.setContentPane(panel);
		optionFrame.setVisible(true);;
		optionFrame.pack();
		optionFrame.setLocationRelativeTo(mainFrame);
	}

	public void setBillDate(String s) {
		billDate = s;
	}

	public static String getDueDate() {
		String s = LocalDate.now(ZoneId.of("America/New_York")).plusDays(intDueDate).toString();
		return s.substring(5) + "-" + s.substring(0, 4);
	}

	private void createDueDates() {
		dueDates = new JComboBox(stringOptions);
		dueDates.setSelectedIndex(0);
		dueDates.addActionListener(new DueDateListener());
	}

	private class ResultTableModel extends DefaultTableModel {
		ResultTableModel(String[][] data, String[] columns) {
			super(data,columns);
		}
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	private class TennantButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command.equals("delete")) {
				int selected = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this tennant record?","Warning", JOptionPane.YES_NO_OPTION);
				if(selected==JOptionPane.YES_OPTION) {
					myDb.deleteTennantRecord(tennantData[tennantCurrentRow][1]);
					refreshTennantTable();
					refreshBillingList();
				}
			}
			else if(command.equals("new")) {
				JFrame frame = new JFrame("Add New Tennant");

				JPanel container = new JPanel();
				container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));

				JPanel panel = new JPanel();
				//panel.setPreferredSize(new Dimension(500,100));
				panel.setLayout(new GridLayout(0,2));

				JLabel[] labels = new JLabel[3];
				JTextField[] tFields = new JTextField[3];
				for(int i=0;i<3;i++) {
					labels[i] = new JLabel(tennantColumnNames[i]);
					tFields[i] = new JTextField();
				}

				panel.add(labels[0]);
				panel.add(tFields[0]);
				panel.add(labels[1]);
				panel.add(tFields[1]);
				panel.add(labels[2]);
				panel.add(tFields[2]);

				JPanel calendarPanel = new JPanel();
				calendarPanel.setPreferredSize(new Dimension(400,400));
				JLabel label = new JLabel();
				label.setText("Select the last time they were billed");
				calendarPanel.add(label);
				Calendar calendar = new Calendar(calendarPanel,"");

				JButton done = new JButton("Done");
				done.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String[] newData = new String[4];
						for(int i=0;i<3;i++) {
							newData[i] = tFields[i].getText();
						}
						newData[3] = calendar.selectedDate;
						if(newData[3].equals("")){
							createOptionFrame("Please select a last billing date");
						}
						else {
							frame.dispose();
							myDb.addTennantRecord(newData);
							refreshTennantTable();
							refreshBillingList();
						}
					}
				});
				container.add(panel);
				container.add(new JLabel("      "));
				container.add(new JLabel("      "));
				container.add(calendarPanel);
				container.add(done);

				frame.setContentPane(container);
				frame.setVisible(true);
				frame.pack();
			}
			else if(command.equals("change")) {
				JFrame frame = new JFrame("Change Tennant Information");

				JPanel container = new JPanel();
				container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));

				JPanel panel = new JPanel();
				panel.setLayout(new GridLayout(0,2));

				JLabel[] labels = new JLabel[4];
				JTextField[] tFields = new JTextField[3];
				for(int i=0;i<3;i++) {
					labels[i] = new JLabel(tennantColumnNames[i]);
					tFields[i] = new JTextField(tennantData[tennantCurrentRow][i]);
				}

				panel.add(labels[0]);
				panel.add(tFields[0]);
				tFields[0].setEditable(false);
				panel.add(labels[1]);
				panel.add(tFields[1]);
				panel.add(labels[2]);
				panel.add(tFields[2]);

				JPanel calendarPanel = new JPanel();
				calendarPanel.setPreferredSize(new Dimension(400,400));
				JLabel label = new JLabel();
				label.setText("Select the last time they were billed");
				calendarPanel.add(label);
				Calendar calendar = new Calendar(calendarPanel,tennantData[tennantCurrentRow][3]);

				JButton done = new JButton("Done");
				done.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						frame.dispose();
						String[] newData = new String[3];
						newData[0] = tFields[1].getText();
						newData[1] = tFields[2].getText();
						newData[2] = calendar.selectedDate;
						myDb.updateTennantRecord(tennantData[tennantCurrentRow][0], newData);
						refreshTennantTable();
						refreshBillingList();
					}
				});
				container.add(panel);
				container.add(new JLabel("          "));
				container.add(new JLabel("          "));
				container.add(calendarPanel);
				container.add(done);

				frame.setContentPane(container);
				frame.setVisible(true);
				frame.pack();
			}
		}
	}

	class RadioButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command.equals("some")) {
				billingList.setEnabled(true);
				billingList.clearSelection();
			}
			else if(command.equals("all")) {
				int start = 0;
				int end = billingList.getModel().getSize() - 1;
				if (end >= 0) {
					billingList.setSelectionInterval(start, end);
				}
				billingList.setEnabled(false);
			}
		}
	}

	private class DueDateListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox)e.getSource();
			String date = (String)cb.getSelectedItem();
			int num = 0;
			for(int i=0;i<stringOptions.length;i++) {
				if(date.equals(stringOptions[i])) {
					num = i;
				}
			}
			intDueDate = intOptions[num];
		}
	}

	class RunButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			System.out.println("Run button has been clicked");
			if(!waterTField.getText().equals("") && !electricityTField.getText().equals("")) {
				String text1 = waterTField.getText();
				String text2 = electricityTField.getText();
				double waterUnitCost = 0 ;
				double electricityUnitCost = 0;

				//get the unit costs entered by the user - throw error message if not entered or in wrong format
				try {
					waterUnitCost = Double.parseDouble(text1); 
					electricityUnitCost = Double.parseDouble(text2); 
					System.out.println("Water and electricity unit cost formats are okay");					
					try {
						//get the tennants selected by the user
						final List<String> selectedValuesList = billingList.getSelectedValuesList();
						System.out.println("Retrieved selected tennants to bill");
						Object[] selected = selectedValuesList.toArray();
						String[] apartments = new String[selected.length];

						//get array of apartments corresponding to the selected tennants
						for(int i=0;i<selected.length;i++) {
							String s = (String)selected[i];
							int index = s.indexOf(" ");
							apartments[i] = s.substring(0,index);
						}

						System.out.println("Due date: "+getDueDate());

						//update the water and electricity unit cost
						myDb.updateUnitCost(waterUnitCost, electricityUnitCost);

						//send the info to calculate costs class
						//if no bill date is selected, then assume you are billing to the current date
						if(billDate.equals("")) {
							String today = FileDownload.createNewFileName().substring(0,10).replaceAll("_", "-");
							new CalculateCosts(waterUnitCost,electricityUnitCost,apartments,today,myDb);
						}
						else {
							new CalculateCosts(waterUnitCost,electricityUnitCost,apartments,billDate,myDb);
						}

						//clear billingpanel
						panel.setPreferredSize(new Dimension(400,400));
						if(button2.isSelected())
							billingList.clearSelection();
						waterTField.setText(myDb.getWaterUnitCost());
						electricityTField.setText(myDb.getElectricityUnitCost());

					}
					catch(NumberFormatException nfe) {
						//this exception occurs when there is no record from the selected bill date
						System.out.println("ERROR - no records from selected date - "+e);
						createOptionFrame("There are no water and electricity records from selected date in the database");	
					}
					catch(Exception x) {
						System.out.println("ERROR - occured during run button listener - "+x);
					}
				}
				catch (NumberFormatException nfe) {
					System.out.println("ERROR - unit costs are in wrong format - "+e);
					createOptionFrame("Please enter the unit costs as a number(can be a decimal)");	
				}
			}
			else {
				System.out.println("ERROR - unit costs have not been entered");
				createOptionFrame("Please enter unit costs");
			}
		}
	}
}