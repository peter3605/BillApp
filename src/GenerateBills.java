import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

public class GenerateBills {  
	private Record[] records;

	private String[] lastBills;

	private String[] billNames;

	private String date;

	private String month;

	private double waterUnitCost;
	private double electricityUnitCost;
	private DatabaseConnector myDb;
	private String combinedPDFPath;
	private JFrame previewFrame;
	private JPanel optionPanel;
	private Configuration configuration;

	GenerateBills(double wuc, double euc, String[] lb, Record[] r, String s, DatabaseConnector db) {
		configuration = new Configuration(Main.answer);

		System.out.println("Generating bills...");

		waterUnitCost = wuc;
		electricityUnitCost = euc;
		records = r;
		myDb = db;
		lastBills = lb;
		date = s;

		billNames = new String[records.length];

		month = getMonthFromDate(date);
		File dir = new File(configuration.getBillFolder() + month);
		dir.mkdir();

		try {
			for (int i = 0; i < records.length; i++) {
				billNames[i] = run(i);
			}
			System.out.println("Successfully created ALL bills");
		}
		catch (Exception e) {
			System.out.println("ERROR - occured while creating bills - " + e);
		}
		
		mergePDF(billNames);
		createOptionFrame();
		previewPDF();
	}

	public static String getMonthFromDate(String date) {
		String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
		int monthNum = Integer.valueOf(date.substring(0, 2)).intValue();
		return months[(monthNum - 1)];
	}

	private void mergePDF(String[] PDFNames) {
		System.out.println("Merging pdf documents...");

		combinedPDFPath = (configuration.getBillFolder() + month + "\\" + "All Utility Bills.pdf");
		new File(combinedPDFPath);

		PDFMergerUtility PDFmerger = new PDFMergerUtility();
		PDFmerger.setDestinationFileName(combinedPDFPath);

		for (int i = 0; i < PDFNames.length; i++) {
			File file = new File(PDFNames[i]);
			try {
				PDDocument doc = PDDocument.load(file);
				PDFmerger.addSource(file);
				doc.close();
			}
			catch (IOException e) {
				System.out.println("ERROR - unable to load pdf - " + e);
			}
		}

		try {
			PDFmerger.mergeDocuments();
			System.out.println("Successfully merged pdf documents: " + combinedPDFPath);
		}
		catch (IOException e) {
			System.out.println("ERROR - unable to merge pdf documents - " + e);
		}
	}

	private void createOptionFrame() {
		optionPanel = new JPanel();
		optionPanel.setPreferredSize(new Dimension(300,50));

		JPanel labelPanel = new JPanel();
		JLabel label = new JLabel("Email bills to tennants?");
		labelPanel.add(label);
		JPanel buttonPanel = new JPanel();
		JButton yes = new JButton("Yes");
		yes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				previewFrame.dispose();

				System.out.println("Updating last bill dates...");

				for(int i=0;i<records.length;i++) {
					myDb.updateBillDates(records[i].getApartments(), date);
				}

				System.out.println("Sending emails...");
				new EmailBills(records,billNames,lastBills,date,myDb,getMonthFromDate(date));
			}
		});
		
		JButton no = new JButton("No");
		no.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				previewFrame.dispose();
				System.out.println("Emails are not being sent");
			}
		});
		buttonPanel.add(yes);
		buttonPanel.add(no);

		optionPanel.add(labelPanel);
		optionPanel.add(buttonPanel);

		System.out.println("Created option panel");
	}

	private void previewPDF() {
		System.out.println("Opening preview frame...");

		try {
			String path = configuration.getBillFolder() + month + "\\" + "All Utility Bills.pdf";
			SwingController controller = new SwingController();

			// Build a SwingViewFactory configured with the controller
			SwingViewBuilder factory = new SwingViewBuilder(controller);

			// Use the factory to build a JPanel that is pre-configured
			//with a complete, active Viewer UI.
			JPanel mainPanel = factory.buildViewerPanel();

			// add interactive mouse link annotation support via callback
			controller.getDocumentViewController().setAnnotationCallback(
					new org.icepdf.ri.common.MyAnnotationCallback(
							controller.getDocumentViewController()));

			JPanel container = new JPanel();
			container.setLayout(new BoxLayout(container, 3));
			container.add(optionPanel);
			container.add(mainPanel);
			controller.openDocument(path);

			previewFrame = new JFrame("Preview Bills");
			previewFrame.setContentPane(container);
			previewFrame.setVisible(true);
			previewFrame.setAlwaysOnTop(true);
			previewFrame.pack();

			System.out.println("Successfully opened preview frame");
		}
		catch (Exception e) {
			System.out.println("ERROR - unable to open preview frame - " + e);
		}
	}

	private String run(int num) throws Exception {
		String file_name;
		boolean gas = false;
		boolean internet = false;
		if(configuration.getSchemaName().equals("1933stpaul") && 
				records[num].getApartments().equals("1A") || records[num].getApartments().equals("4A")) {
			int length = configuration.getBillTemplate().length();
			file_name = configuration.getBillTemplate().substring(0, length-4) + "_with_gas.pdf";
			gas = true;
		}
		else {
			if(configuration.getSchemaName().equals("510stpaul") && records[num].getApartments().equals("2B")) {
				int length = configuration.getBillTemplate().length();
				file_name = configuration.getBillTemplate().substring(0, length-4) + "_with_internet.pdf";
				internet = true;
			} else {
				file_name = configuration.getBillTemplate();
			}
		}
		try(PDDocument pdfDocument = PDDocument.load(new File(file_name))){
			PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();
			if (acroForm != null) {
				System.out.println("Filling in fields for " + records[num].getApartments() + " bill");

				PDTextField field = (PDTextField)acroForm.getField("tennant_name");
				field.setValue(records[num].getName());
				PDTextField field1 = (PDTextField)acroForm.getField("street_name");
				field1.setValue(configuration.getStreetName());
				PDTextField field3 = (PDTextField)acroForm.getField("apartment");
				field3.setValue("Apartment-" + records[num].getApartments());
				PDTextField field4 = (PDTextField)acroForm.getField("end_address");
				field4.setValue(configuration.getEndAddress());
				PDTextField field5 = (PDTextField)acroForm.getField("electricity_usage");
				field5.setValue(String.format("%.2f", new Object[] { records[num].getElectricityUsage()}) + " KWH");
				PDTextField field6 = (PDTextField)acroForm.getField("water_usage");
				field6.setValue(String.format("%.2f", new Object[] { records[num].getWaterUsage()}) + " Gallons");
				PDTextField field7 = (PDTextField)acroForm.getField("electricity_unit_price");
				field7.setValue(String.format("%.6f", new Object[] { Double.valueOf(electricityUnitCost) }));
				PDTextField field8 = (PDTextField)acroForm.getField("water_unit_price");
				field8.setValue(String.format("%.6f", new Object[] { Double.valueOf(waterUnitCost) }));
				PDTextField field9 = (PDTextField)acroForm.getField("electricity_total");
				field9.setValue("$" + String.format("%.2f", new Object[] { Double.valueOf(records[num].getElectricityCost()) }));
				PDTextField field10 = (PDTextField)acroForm.getField("water_total");
				field10.setValue("$" + String.format("%.2f", new Object[] { Double.valueOf(records[num].getWaterCost()) }));
				/*PDTextField field11 = (PDTextField)acroForm.getField("total");
	          	field11.setValue("$" + String.format("%.2f", new Object[] { Double.valueOf(records[num].getWaterCost() + records[num].getElectricityCost()) }));
				*/
				PDTextField field12 = (PDTextField)acroForm.getField("start_date");
				field12.setValue(String.valueOf(lastBills[num]));
				PDTextField field13 = (PDTextField)acroForm.getField("end_date");
				field13.setValue(date);
				PDTextField field14 = (PDTextField)acroForm.getField("due_date");
				field14.setValue(GUI.getDueDate());

				if(gas) {
					double gas_usage = 15.82;
					double gas_unit_cost = 1.430233;
					PDTextField field15 = (PDTextField)acroForm.getField("gas_usage");
					field15.setValue(String.format("%.2f", new Object[] {gas_usage}) + " Therms");
					PDTextField field16 = (PDTextField)acroForm.getField("gas_unit_cost");
					field16.setValue(String.format("%.6f", new Object[] {gas_unit_cost}));
					PDTextField field17 = (PDTextField)acroForm.getField("gas_total");
					field17.setValue("$" + String.format("%.2f", new Object[] {gas_usage * gas_unit_cost}));
					PDTextField field11 = (PDTextField)acroForm.getField("total");
					field11.setValue("$" + String.format("%.2f", new Object[] { Double.valueOf(records[num].getWaterCost() + records[num].getElectricityCost() + (gas_usage * gas_unit_cost)) }));
				} else if(internet) {
					System.out.println("A");
					PDTextField field15 = (PDTextField)acroForm.getField("internet_total");
					System.out.println("C");
					field15.setValue("$10.00");
					System.out.println("B");
					PDTextField field11 = (PDTextField)acroForm.getField("total");
					field11.setValue("$" + String.format("%.2f", new Object[] { Double.valueOf(10 + records[num].getWaterCost() + records[num].getElectricityCost()) }));
				} else {	
					PDTextField field11 = (PDTextField)acroForm.getField("total");
					field11.setValue("$" + String.format("%.2f", new Object[] { Double.valueOf(records[num].getWaterCost() + records[num].getElectricityCost()) }));
				}
			}
			acroForm.flatten();
			String name = configuration.getBillFolder() + month + "\\" + records[num].getApartments() + " Utility Bill.pdf";
			pdfDocument.save(new File(name));
			System.out.println("Successfully created bill for: " + records[num].getApartments());

			return name;
		}
	}
}