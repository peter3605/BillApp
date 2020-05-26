import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
public class CalculateCosts {

	private String billDate;
	private DatabaseConnector myDb;
	private String[] apartments;
	private Configuration configuration;

	private String[] names;
	private String[] lastBills;
	private double[] waterUsage;
	private double[] electricityUsage;
	private double[] waterCosts;
	private double[] electricityCosts;
	private double waterUnitCost;
	private double electricityUnitCost;
	private int totalWater;
	private int totalElectricity;
	private Record[] records;

	public CalculateCosts(double wuc, double euc, String[] aparts, String date, DatabaseConnector db) {
		configuration = new Configuration(Main.answer);

		System.out.println("About to calculate costs...");

		myDb = db;
		apartments = aparts;
		waterUnitCost = wuc;
		electricityUnitCost = euc;
		billDate = date;

		names = new String[apartments.length];
		for (int i = 0; i < names.length; i++) {
			System.out.println(apartments[i]);
			names[i] = myDb.getTennantNameByApartment(apartments[i]);
		}

		lastBills = new String[apartments.length];
		for (int i = 0; i < apartments.length; i++) {
			lastBills[i] = myDb.getLastTennantBill(apartments[i]);
		}

		try {
			waterCosts = calculateWater();
			electricityCosts = calculateElectricity();

			records = createRecords();

			createReportFile();

			new GenerateBills(waterUnitCost, electricityUnitCost, lastBills, records, billDate, myDb);
		}
		catch (NumberFormatException e) {
			System.out.println("No records from that bill date - " + e);
			createOptionFrame("Sorry, there are no records from that date. Please enter another bill date");
		}
	}

	/** 
	 * Calculate the water usage and cost for each apartment
	 */
	private double[] calculateWater() {
		double[] cost = new double[apartments.length];
		waterUsage = new double[apartments.length];
		totalWater = 0;
		for (int i = 0; i < apartments.length; i++) {
			if (myDb.getWaterValue(billDate, apartments[i]).equals("")) {
				throw new NumberFormatException();
			}
			int current = Integer.valueOf(myDb.getWaterValue(billDate, apartments[i])).intValue();
			int last = Integer.valueOf(myDb.getWaterValue(lastBills[i], apartments[i])).intValue();
			double total = (current - last) * 1.1D;
			waterUsage[i] = total;
			totalWater = (int)(totalWater + waterUsage[i]);

			cost[i] = (waterUsage[i] * waterUnitCost);
		}
		System.out.println("Calculated water totals: " + totalWater);
		return cost;
	}

	/** 
	 * Calculate the electricity usage and cost for each apartment
	 */
	private double[] calculateElectricity() {
		double[] cost = new double[apartments.length];
		electricityUsage = new double[apartments.length];
		totalElectricity = 0;
		for (int i = 0; i < apartments.length; i++) {
			if (myDb.getElectricityValue(billDate, apartments[i]).equals("")) {
				throw new NumberFormatException();
			}
			int current = Integer.valueOf(myDb.getElectricityValue(billDate, apartments[i])).intValue();
			int last = Integer.valueOf(myDb.getElectricityValue(lastBills[i], apartments[i])).intValue();
			double total = (current - last) * 1.1D;
			electricityUsage[i] = total;
			totalElectricity = (int)(totalElectricity + electricityUsage[i]); 

			cost[i] = (electricityUsage[i] * electricityUnitCost);
		}
		System.out.println("Calculated electricity totals: " + totalElectricity);
		return cost;
	}


	/** 
	 * Creates a .csv file with all of the utilities data from the time period
	 */
	private void createReportFile() {
		int currentWater = Integer.valueOf(myDb.getWaterValue(billDate, "public")).intValue();
		int lastWater = Integer.valueOf(myDb.getWaterValue(lastBills[0], "public")).intValue();
		int publicWaterUsage = currentWater - lastWater;

		int currentElectricity = Integer.valueOf(myDb.getElectricityValue(billDate, "public")).intValue();
		int lastElectricity = Integer.valueOf(myDb.getElectricityValue(lastBills[0], "public")).intValue();
		int publicElectricityUsage = currentElectricity - lastElectricity;

		String fileName = configuration.getReportFileFolder() + GenerateBills.getMonthFromDate(billDate) + "-Report.csv";
		PrintWriter pw = null;
		System.out.println("Creating report file: "+fileName);
		try {
			pw = new PrintWriter(new File(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("ERROR - report file was not found" + e);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Appartment");
		sb.append(",");
		sb.append("Electricity Usage");
		sb.append(",");
		sb.append("Electricity Cost");
		sb.append(",");
		sb.append("Water Usage");
		sb.append(",");
		sb.append("Water Cost");
		sb.append("\n");

		sb.append("Public");
		sb.append(",");
		sb.append(publicElectricityUsage);
		sb.append(",");
		sb.append(publicElectricityUsage * electricityUnitCost);
		sb.append(",");
		sb.append(publicWaterUsage);
		sb.append(",");
		sb.append(0);
		sb.append("\n");

		for(int i = 0; i < records.length; i++) {
			sb.append(records[i].getApartments());
			sb.append(",");
			sb.append(records[i].getElectricityUsage());
			sb.append(",");
			sb.append(records[i].getElectricityCost());
			sb.append(",");
			sb.append(records[i].getWaterUsage());
			sb.append(",");
			sb.append(records[i].getWaterCost());
			sb.append("\n");
		}

		sb.append("\n");
		sb.append("Total Water Usage");
		sb.append(",");
		sb.append(totalWater);
		sb.append("\n");
		sb.append("Total Water Cost");
		sb.append(",");
		sb.append(totalWater * waterUnitCost);
		sb.append("\n");
		sb.append("Total Electricity Usage");
		sb.append(",");
		sb.append(totalElectricity + publicElectricityUsage);
		sb.append("\n");
		sb.append("Total Electricity Cost");
		sb.append(",");
		sb.append((totalElectricity + publicElectricityUsage) * electricityUnitCost);
		sb.append("\n");
		sb.append("\n");
		sb.append("*Total electricity usage and cost includes public");
		sb.append("\n");
		sb.append("*Total water usage and cost does not include public");

		pw.write(sb.toString());
		pw.close();
		System.out.println("Successfully created report file");
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
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				optionFrame.dispose();
			}
		});
		panel.add(button);

		optionFrame.setContentPane(panel);
		optionFrame.setVisible(true);;
		optionFrame.pack();
		optionFrame.setLocationRelativeTo(GUI.mainFrame);
	}

	/** 
	 * Move all of the data in to Record data types
	 */
	private Record[] createRecords() {
		int size = apartments.length;

		Record[] r = new Record[size];
		for (int i = 0; i < size; i++) {
			r[i] = new Record(names[i], apartments[i], waterUsage[i], electricityUsage[i], waterCosts[i], electricityCosts[i]);
		}
		return r;
	}
}
