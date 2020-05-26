
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
/*
 * Creates the calendar used in the GUI 
 * Adds the calendar to the jpanel that is passed to it in its constructor 
 */
public class Calendar
{
	private JLabel lblMonth;
	private JButton btnPrev, btnNext;
	private JTable tblCalendar;
	private JFrame frmMain;
	private Container pane;
	private DefaultTableModel mtblCalendar; //Table model
	private JScrollPane stblCalendar; //The scrollpane
	private JPanel pnlCalendar;

	private int selectedRow = -1;
	private int selectedColumn = -1;
	protected String selectedDate = "";
	private GUI gui;

	//real year and real month are for the actual date
	//current year and current month are for what the user is looking at
	private int realYear, realMonth, realDay, currentYear, currentMonth;

	Calendar(JPanel panel, GUI g)
	{
		initialize(panel);
		gui = g;
	}
	Calendar(JPanel panel, String date){
		selectedDate = date;
		initialize(panel);
		gui = null;
		if(!(date.equals("")))
		{
			convertDate(date);
		}
	}

	private void convertDate(String date){
		String month;
		if(date.substring(0,1).equals("0"))
		{
			month = date.substring(1,2);
		}
		else
		{
			month = date.substring(0,2);
		}
		String day;
		if(date.substring(3,4).equals("0"))
		{
			day = date.substring(4,5);
		}
		else
		{
			day = date.substring(3,5);
		}
		String year = date.substring(6);

		refreshCalendar(Integer.valueOf(month)-1,Integer.valueOf(year));
		for(int x=0;x<tblCalendar.getRowCount();x++)
		{
			for(int y=0;y<tblCalendar.getColumnCount();y++)
			{
				if(tblCalendar.getValueAt(x,y) == Integer.valueOf(day))
				{
					selectedRow = x;
					selectedColumn = y;
				}
			}
		}
		refreshCalendar(Integer.valueOf(month)-1,Integer.valueOf(year));
	}

	//this method takes a jpanel as a parameter and adds the calendar panel to that jpanel
	public void initialize(JPanel panel)
	{
		//Look and feel
		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch (ClassNotFoundException e) {}
		catch (InstantiationException e) {}
		catch (IllegalAccessException e) {}
		catch (UnsupportedLookAndFeelException e) {}

		//initialize jpanel that calendar will be on
		pane = new JPanel();
		pane.setPreferredSize(new Dimension(330,375));
		pane.setLayout(null);

		//Create controls
		lblMonth = new JLabel ("January");
		btnPrev = new JButton ("<<");
		btnNext = new JButton (">>");
		mtblCalendar = new DefaultTableModel(){public boolean isCellEditable(int rowIndex, int mColIndex){return false;}};
		tblCalendar = new JTable(mtblCalendar);
		stblCalendar = new JScrollPane(tblCalendar);
		pnlCalendar = new JPanel(null);

		//Set border
		pnlCalendar.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY,1));

		//Register action listeners
		btnPrev.addActionListener(new btnPrev_Action());
		btnNext.addActionListener(new btnNext_Action());

		//Add controls to pane
		pane.add(pnlCalendar);
		pnlCalendar.add(lblMonth);
		pnlCalendar.add(btnPrev);
		pnlCalendar.add(btnNext);
		pnlCalendar.add(stblCalendar);

		//Set bounds
		pnlCalendar.setBounds(0, 0, 320, 335);
		lblMonth.setBounds(160-lblMonth.getPreferredSize().width/2, 25, 100, 25);
		btnPrev.setBounds(10, 25, 50, 25);
		btnNext.setBounds(260, 25, 50, 25);
		stblCalendar.setBounds(10, 50, 300, 250);


		//Get real month/year
		GregorianCalendar cal = new GregorianCalendar(); //Create calendar
		realDay = cal.get(GregorianCalendar.DAY_OF_MONTH); //Get day
		realMonth = cal.get(GregorianCalendar.MONTH); //Get month
		realYear = cal.get(GregorianCalendar.YEAR); //Get year
		currentMonth = realMonth; //Match month and year
		currentYear = realYear;

		//Add headers
		String[] headers = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}; //All headers
		for (int i=0; i<7; i++)
		{
			mtblCalendar.addColumn(headers[i]);
		}

		tblCalendar.getParent().setBackground(tblCalendar.getBackground()); //Set background

		//No resize/reorder
		tblCalendar.getTableHeader().setResizingAllowed(false);
		tblCalendar.getTableHeader().setReorderingAllowed(false);

		//Single cell selection
		tblCalendar.setColumnSelectionAllowed(true);
		tblCalendar.setRowSelectionAllowed(true);
		tblCalendar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		//Set row/column count
		tblCalendar.setRowHeight(38);
		mtblCalendar.setColumnCount(7);
		mtblCalendar.setRowCount(6);

		/*
		 * when the mouse is clicked, it looks for which day on the calendar has been selected, and highlights that day
		 * it also sets the selected row and selected column variables
		 * also sets the bill date in the GUI class
		 */
		tblCalendar.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(tblCalendar.getValueAt(tblCalendar.getSelectedRow(), tblCalendar.getSelectedColumn())!=null)
				{
					selectedRow = tblCalendar.getSelectedRow();
					selectedColumn = tblCalendar.getSelectedColumn();
					refreshCalendar(currentMonth,currentYear);
					System.out.println("Selected: "+FileDownload.getMonthString((currentMonth)) + "-" + FileDownload.getDayString((int)tblCalendar.getValueAt(selectedRow,selectedColumn)) + "-" + currentYear);
					if(gui==null){
						selectedDate = FileDownload.getMonthString((currentMonth)) + "-" + FileDownload.getDayString((int)tblCalendar.getValueAt(selectedRow,selectedColumn)) + "-" + currentYear;
					}
					else{
						gui.setBillDate(FileDownload.getMonthString((currentMonth)) + "-" + FileDownload.getDayString((int)tblCalendar.getValueAt(selectedRow,selectedColumn)) + "-" + currentYear);

					}
				}
			}
		});

		refreshCalendar (realMonth, realYear);

		//adds the calendar panel to the jpanel
		panel.add(pane);
	}

	/*
	 * is called when the user clicks on the button to change the month or the user clicks on a day on the calendar
	 */
	public void refreshCalendar(int month, int year)
	{
		//Variables
		String[] months =  {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
		int nod, som; //Number Of Days, Start Of Month

		//Allow/disallow buttons
		btnPrev.setEnabled(true);
		btnNext.setEnabled(true);
		if (month == 0 && year <= realYear-10){btnPrev.setEnabled(false);} //Too early
		if (month == 11 && year >= realYear+100){btnNext.setEnabled(false);} //Too late
		lblMonth.setText(months[month]); //Refresh the month label (at the top)
		lblMonth.setBounds(160-lblMonth.getPreferredSize().width/2, 25, 180, 25); //Re-align label with calendar

		//Clear table
		for (int i=0; i<6; i++)
		{
			for (int j=0; j<7; j++)
			{
				mtblCalendar.setValueAt(null, i, j);
			}
		}

		//Get first day of month and number of days
		GregorianCalendar cal = new GregorianCalendar(year, month, 1);
		nod = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
		som = cal.get(GregorianCalendar.DAY_OF_WEEK);

		//Draw calendar
		for (int i=1; i<=nod; i++)
		{
			int row = new Integer((i+som-2)/7);
			int column  =  (i+som-2)%7;
			mtblCalendar.setValueAt(i, row, column);
		}

		//Apply renderers
		tblCalendar.setDefaultRenderer(tblCalendar.getColumnClass(0), new tblCalendarRenderer());
	}

	//tblCalendarRenderer is what draws/colors each cell in the table
	//it highlights the day that has been clicked on by users
	class tblCalendarRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent (JTable table, Object value, boolean selected, boolean focused, int row, int column)
		{
			super.getTableCellRendererComponent(table, value, selected, focused, row, column);
			if (column == 0 || column == 6)
			{ //Week-end
				setBackground(new Color(255, 220, 220));
			}
			else
			{ //Weekday
				setBackground(new Color(255, 255, 255));
			}
			if(selectedRow != -1 && selectedColumn != -1)
			{
				if(row == selectedRow && column == selectedColumn)
				{
					setBackground(Color.YELLOW);
				}
			}

			setBorder(null);
			setForeground(Color.black);
			return this;
		}
	}
	/*
	 * actionlisteners for the buttons to change months
	 */
	class btnPrev_Action implements ActionListener
	{
		public void actionPerformed (ActionEvent e)
		{
			if (currentMonth == 0)
			{ //Back one year
				currentMonth = 11;
				currentYear -= 1;
			}
			else
			{ //Back one month
				currentMonth -= 1;
			}
			refreshCalendar(currentMonth, currentYear);
		}
	}
	class btnNext_Action implements ActionListener
	{
		public void actionPerformed (ActionEvent e)
		{
			if (currentMonth == 11)
			{ //Foward one year
				currentMonth = 0;
				currentYear += 1;
			}
			else
			{ //Foward one month
				currentMonth += 1;
			}
			refreshCalendar(currentMonth, currentYear);
		}
	}
}