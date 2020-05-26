import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

public class Parse1933StPaul {
	private final String[] WATER_COLUMNS = new String[]{
			"Water-Public",
			"Water-Basement",
			"Water-4A",
			"Water-3A",
			"Water-2F",
			"Water-2R",
			"Water-1A",
			"Water-CH",
	};
	private final String[] ELECTRICITY_COLUMNS = new String[]{
			"Electricity-Public",
			"Electricity-Basement",
			"Electricity-4A",
			"Electricity-3A",
			"Electricity-2F",
			"Electricity-2R",
			"Electricity-1A",
			"Electricity-CH",
	};
	private String dateOfReading;
	private int[] waterValues;
	private int[] electricityValues;
	private DatabaseConnector myDb;

	/*
	 * Method to parse files that have been manually downloaded - must specify folder path
	 * 
	 * 
	 *
		public static void main(String[] arg) {
			Configuration configuration = new Configuration("1933StPaul");
			DatabaseConnector myDb = new DatabaseConnector(configuration);
			String folderPath = "C:\\Users\\peter\\Desktop\\Work\\BillApp(Updated)\\Meter Files\\1933StPaul\\November\\11_01_2019";
			new Parse1933StPaul(folderPath, myDb);
		}*/

	Parse1933StPaul(String folderName, DatabaseConnector db){
		System.out.println("Parsing 1933StPaul : "+folderName);
		myDb = db;

		waterValues = new int[8];
		electricityValues = new int[8];
		String begining = "/mb-00";

		//read water and electricity values from the files
		for(int i = 1 ; i <= 8 ; i++) {
			parseEnergyData(folderName + begining + i + ".csv", i-1);
		}
		parseWaterData(folderName + "/mb-032.csv");

		/*
		 * re-adjusting electricity values:
		 * 1st floor = 1st floor - (carriage house + public)
		 * 3rd floor = 3rd floor - basement
		 * 4th floor = 4th floor - 2R
		 */

		electricityValues[6] = electricityValues[6] - (electricityValues[7] + electricityValues[0]);
		electricityValues[3] = electricityValues[3] - electricityValues[1];
		electricityValues[2] = electricityValues[2] - electricityValues[5];

		/*
		 * re-adjusting water values:
		 * total consumption = all apartments + public
		 * individual apartment = (total consumption) x (individual apartment/all apartments)
		 */

		double totalConsumption = waterValues[0] - Integer.valueOf(myDb.getWaterValue(myDb.getLastTennantBill("1A"), "Public"));
		double allApartments = 0;
		double totalCalculated = 0;
		for(int i = 1; i < waterValues.length; i++) {
			totalConsumption += waterValues[i];
			allApartments += waterValues[i];
		}
		for(int i = 1; i < waterValues.length; i++) {
			double ratio = ((double)waterValues[i])/allApartments;
			double apartment = totalConsumption * ratio;
			waterValues[i] = (int) Math.round(apartment);
			totalCalculated += (int) Math.round(apartment);
		}
		System.out.println("   TOTAL WATER: " + totalConsumption);
		System.out.println("   TOTAL CALCULATED: "+totalCalculated);

		//inserting into database
		System.out.println("Inserting into database: " + dateOfReading);
		if(!myDb.checkIfRecordExists("water", dateOfReading))
			myDb.insertNewWaterRecord(dateOfReading, waterValues);
		else
			System.out.println("Water record already exists");
		
		if(!myDb.checkIfRecordExists("electricity", dateOfReading))
			myDb.insertNewElectricityRecord(dateOfReading, electricityValues);
		else
			System.out.println("Electricity record already exists");	 
	}

	private void parseEnergyData(String fileName, int num) {
		BufferedReader br = null;
		String line = "";
		String splitBy = ",";
		String[] elements = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				elements = line.split(splitBy);
			}
		}
		catch (FileNotFoundException e) {
			System.out.print("ERROR - couldn't find file - "+e);
		} 
		catch (IOException e) {
			System.out.print("ERROR - unable to pull most recent data set - "+e );
		}
		//get the date that the dataset is from
		String s = elements[0].substring(1,11);
		dateOfReading = s.substring(5)+"-"+s.substring(0, 4);

		double electricityNum = Double.valueOf(elements[37]);
		electricityValues[num] = (int)electricityNum;
	}

	private void parseWaterData(String fileName) {
		BufferedReader br = null;
		String line = "";
		String splitBy = ",";
		String[] elements = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				elements = line.split(splitBy);
			}
		}
		catch (FileNotFoundException e) {
			System.out.print("ERROR - couldn't find file - "+e);
		} 
		catch (IOException e) {
			System.out.print("ERROR - unable to pull most recent data set - "+e );
		}
		int count = 0;
		for(int i = 4; i < 20; i+=2) {
			int waterNum = Integer.valueOf(elements[i]);
			waterValues[count] = (waterNum);
			count++;
		}
	}
}