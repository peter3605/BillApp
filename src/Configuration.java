import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Configuration extends java.util.Properties {
	private final String FILEPATH = "config.properties";

	private String FTPServer;
	private String FTPUsername;
	private String FTPPassword;
	private String FTPFolder;

	private String databaseName;
	private String schemaName = "";
	private String databaseUsername;
	private String databasePassword;
	private String emailUsername;
	private String emailPassword;
	private String streetName;
	private String endAddress;
	private String billTemplate;
	private String billFolder = System.getProperty("user.dir") + "/Bills/";
	private String FTPDownloadPath = System.getProperty("user.dir") + "/Meter Files/";
	private String reportFileFolder = System.getProperty("user.dir") + "/Report Files/";



	public Configuration(String building){
		if(building.equals("1933StPaul"))
			FTPServer = "73.128.40.28";
		else 
			FTPServer = "73.132.143.131";
		FileInputStream in = null;
		try {
			in = new FileInputStream(new java.io.File("config.properties"));
		}
		catch (Exception e) {
			System.out.println("Error finding configuration file config.properties: " + e.getMessage());
		}

		try {
			load(in);
			FTPUsername = getProperty("FTPUsername");
			FTPPassword = getProperty("FTPPassword");
			FTPFolder = getProperty("FTPFolder");
			databaseName = getProperty("databaseName");
			databaseUsername = getProperty("databaseUsername");
			databasePassword = getProperty("databasePassword");
			emailUsername = getProperty("emailUsername");
			emailPassword = getProperty("emailPassword");

			billTemplate = (billFolder + "\\bill_template.pdf");

			System.out.println("Successfully read properties file");

			if(FTPServer.equals("73.128.40.28")){
				FTPDownloadPath += "1933StPaul";
				billFolder += "1933StPaul\\";
				reportFileFolder += "1933StPaul\\";
				schemaName = "1933stpaul";
				streetName = "1933 St.Paul Street";
				endAddress = "Baltimore, MD 21218";
			}else {
				FTPDownloadPath += "510StPaul";
				billFolder += "510StPaul\\";
				reportFileFolder += "510StPaul\\";
				schemaName = "510stpaul";
				streetName = "510 St.Paul Place";
				endAddress = "Baltimore, MD 21202";
			}
		}
		catch (Exception e) {
			System.out.println("Error loading configuration settings: " + e.getMessage());
			e.printStackTrace();
			try {
				in.close(); 
			} catch (IOException localIOException) {} 
		} finally { 
			try { 
				in.close();
			} catch (IOException localIOException1) {}
		}
	}

	public String getFTPServer() { return FTPServer; }
	public String getFTPUsername() { return FTPUsername; }
	public String getFTPPassword() { return FTPPassword; }
	public String getFTPFolder() { return FTPFolder; }
	public String getFTPDownloadPath() { return FTPDownloadPath; }
	public String getDatabaseName() { return databaseName; }
	public String getSchemaName() { return schemaName; }
	public String getDatabaseUsername() { return databaseUsername; }
	public String getDatabasePassword() { return databasePassword; }
	public String getEmailUsername() { return emailUsername; }
	public String getEmailPassword() { return emailPassword; }
	public String getStreetName() { return streetName; }
	public String getEndAddress() { return endAddress; }
	public String getBillTemplate() { return billTemplate; }
	public String getBillFolder() { return billFolder; }
	public String getReportFileFolder() { return reportFileFolder; }
}
