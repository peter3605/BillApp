import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
/*
 * DOWNLOADS MOST RECENT CSV FILE IN SERVER - RUN METHOD RETURNS STRING PATH TO THAT FILE
 */
public class FileDownload {
	private FTPClient client;
	private Configuration configuration;
	public String newName;

	FileDownload(FTPClient client, Configuration config) {
		this.client = client;
		configuration = config;
	}
	
	//returns the current date in the format mm_dd_yyyy-hh_mm
	public  static String createNewFileName() {
		Calendar date = new GregorianCalendar();
		String year = String.valueOf(date.get(Calendar.YEAR));
		String month = getMonthString(date.get(Calendar.MONTH));
		String day = getDayString(date.get(Calendar.DATE));

		return month+"_"+day+"_"+year;
	}
	
	/*
	 * these methods are used to correctly format the date
	 */
	public static String getMonthString(int zeroBasedMonth) {
		String month = ""; 
		int monthOfYear =  zeroBasedMonth + 1;         
		if (monthOfYear < 10)
			month = "0" + monthOfYear;
		else 
			month = String.valueOf(monthOfYear);
		return month;
	}
	
	public static String getDayString(int aNumber) {
		String answer = "";
		if (aNumber < 10)
			answer = "0"+aNumber;
		else
			answer = String.valueOf(aNumber);
		return answer;
	}

	//returns the file name - for 510 st paul
	public String downloadFile510() throws IOException {
		newName = createNewFileName();
		FTPFile[] files = null;
		try {
			files = client.listFiles(configuration.getFTPFolder());
			System.out.println("Successful obtained list of files from the server");
		} 
		catch (IOException e) {
			System.out.println("ERROR - unable to obtain list of files from the server");
			e.printStackTrace();
		}
		System.out.println("Server folder size: "+files.length);

		//get the name of the most recent file
		String fileName = files[files.length-1].getName();
		
		//create a new file with the given file path
		File newFile = new File(configuration.getFTPDownloadPath()  + "/" + newName + ".csv");
		try {
			//download the file from the server
			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(newFile));
			boolean b = client.retrieveFile(fileName, outputStream);
			outputStream.close();

			System.out.println("Successfully downloaded file from server: "+fileName);
			System.out.println("   New file name: "+configuration.getFTPDownloadPath() + newName);

			return newFile.getAbsolutePath();
		}
		catch(IOException e) {
			System.out.print("ERROR - Unable to download file from server - ");
			e.printStackTrace();
		}
		return "";
	}

	//returns the folder name - for 1933 st paul
	public String downloadFile1933() throws IOException {
		String[] monthName = {"January", "February",
				"March", "April", "May", "June", "July",
				"August", "September", "October", "November",
		"December"};
		String[] targetFileNames = {"mb-001","mb-002","mb-003","mb-004","mb-005",
				"mb-006","mb-007","mb-008","mb-032"};

		FTPFile[] files = null;
		try {
			files = client.listFiles(configuration.getFTPFolder());
			System.out.println("Successful obtained list of files from the server");
		} 
		catch (IOException e) {
			System.out.println("ERROR - unable to obtain list of files from the server");
			e.printStackTrace();
		}

		System.out.println("Server folder size: "+files.length);
		String folderPath = "";
		try {
			//creating new folder
			folderPath = configuration.getFTPDownloadPath() + "/" + monthName[Calendar.getInstance().get(Calendar.MONTH)];
			File newFolder = new File(folderPath);
			newFolder.mkdir();
			folderPath += "/" + createNewFileName();
			newFolder = new File(folderPath);
			newFolder.mkdir();
			System.out.println("Successfully created new folder at: "+folderPath);
		} catch(Exception e) {
			System.out.println("ERROR - unable to create folder" + e);
		}
		for(String targetFile : targetFileNames) {
			try {
				//download the file from the server
				String fileName = folderPath + "/" + targetFile + ".csv";
				File file = new File(fileName);
				OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileName));
				boolean b = client.retrieveFile(targetFile+".log", outputStream);
				outputStream.close();

				System.out.println("Successfully downloaded file from server: "+ targetFile+".log");
				System.out.println("   New file name: "+ fileName);

			}
			catch(IOException e) {
				System.out.print("ERROR - Unable to download file from server - ");
				e.printStackTrace();
			}
		}
		return folderPath; 		
	}

}