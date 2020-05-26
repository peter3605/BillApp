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
