
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.html.parser.Element;
/*
 * THIS CLASS PARSES THE NEW CSV FILE DOWNLOADED FROM FILEDOWNLOAD
 * AND INSERTS ANY NEW DATA INTO THE DATABASE
 * 
 */ 
public class Parse510StPaulFile {	
	private final String[] WATER_COLUMNS = new String[]{
			"Water-Public",
			"Water-1A",
			"Water-1B",
			"Water-2A",
			"Water-2B",
			"Water-2C",
			"Water-3A",
			"Water-3B",
			"Water-3C"
	};
	private final String[] ELECTRICITY_COLUMNS = new String[]{
			"Electricity-Public",
			"Electricity-Retail",
			"Electricity-1A",
			"Electricity-1B",
			"Electricity-2A",
			"Electricity-2B",
			"Electricity-2C",
			"Electricity-3A",
			"Electricity-3B",
			"Electricity-3C"
	};

	private String fileName;
	private int[] waterValues;
	private int[] electricityValues;
	private DatabaseConnector myDb;

	Parse510StPaulFile(String fileName, DatabaseConnector db) {	
		System.out.println("Parsing csv file: " + fileName);

		myDb = db;
		this.fileName = fileName;

		parseData(fileName);
	}

	//gets the most recent set of data from the file
	private void parseData(String fileName) {
		BufferedReader br = null;
		String line = "";
		String splitBy = ",";
		String[] elements = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				elements = line.split(splitBy);
			}
			System.out.println("Successfully pulled most recent data set");
		} 
		catch (FileNotFoundException e) {
			System.out.print("ERROR - couldn't find file - "+e);
		} 
		catch (IOException e) {
			System.out.print("ERROR - unable to pull most recent data set - "+e );
		} 

		try {
			br.close();
		} 
		catch (IOException e) {
		}
		splitData(elements);
	}

	//splits data into water or electricity
	//if the record is new, the it inserts the record into the database
	private void splitData(String[] data) {
		System.out.println("Splitting data...");

		//get the date that the dataset is from
		String s = data[0].substring(1,11);
		String dateOfReading =s.substring(5)+"-"+s.substring(0, 4);
		try {
			ArrayList<String> dataSet = new <String>ArrayList();
			int count = 4;
			while(count < data.length && dataSet.size() != 19) {
				//dont add the date to the arraylist - only data
				if(Double.valueOf(data[count])!=0) {
					dataSet.add(data[count]);
				}
				count += 2;
			}
			//there should be 19 elements - 9 water and 10 electricity
			if(dataSet.size()==19) {
				waterValues = new int[9];
				electricityValues = new int[10];
				for(int i = 0; i < 9; i++) {
					waterValues[i] = Integer.valueOf(dataSet.get(i));
					System.out.println("WATER VALUE - "+ i + " - "+waterValues[i]);	    	
				}
				for(int i = 9; i < dataSet.size(); i++) {
					electricityValues[i-9] = Integer.valueOf(dataSet.get(i));
					System.out.println("ELECTRICITY VALUE - "+ i + " - "+electricityValues[i-9]);	  
				}
				System.out.println("Successfully split data into water and electricity");

				//check if the record already exists for the reading date - if not, then insert a new record
				if(!myDb.checkIfRecordExists("water", dateOfReading)) 
					myDb.insertNewWaterRecord(dateOfReading, waterValues);
				else 
					System.out.println("Water record already exists");
				if(!myDb.checkIfRecordExists("electricity", dateOfReading))
					myDb.insertNewElectricityRecord(dateOfReading, electricityValues);
				else
					System.out.println("Electricity record already exists");
			}
			else {
				System.out.println("Dataset is wrong size - " + dataSet.size());	    	
			}
		}
		catch(Exception e) {
			System.out.print("ERROR - unable to split data - "+e);
		}
	}
}