import org.apache.commons.net.ftp.FTPClient;

public class Scheduler {
	public static void main(String args[]) {
		runFTP("1933StPaul");
		runFTP("510StPaul");
		
	}
	/**
	 * Connects to the ftp file, downloads the file, and then parses the file
	 * @param none
	 */
	public static void runFTP(String building) {
		System.out.println("Begining FTP connection...");
		
		Configuration configuration = new Configuration(building);
		DatabaseConnector myDb = new DatabaseConnector(configuration);
		
		FTPClient ftpClient = null;
		FTPConnector connector;
		try {
			try {
				// Create the ftp client
				ftpClient = new FTPClient();
				
				// Create an instance of ftp connector
				connector = new FTPConnector(configuration);
				
				// Log the client into the server
				connector.logInToServer(ftpClient);
			}
			catch(Exception w) {
				System.out.println("ERROR - ftp connection");
			}
			
			// Download and parse the files
			FileDownload fileDownload = new FileDownload(ftpClient, configuration);
			if(configuration.getFTPServer().equals("73.128.40.28")) {
				/* For 1933 St Paul */
				
				// Download files from ftp server
				String newFolderName = fileDownload.downloadFile1933();
				
				// Parse the downloaded files
				new Parse1933StPaul(newFolderName, myDb);
			}else {
				/* For 510 St Paul */
				
				// Download file from ftp server
				String newFileName = fileDownload.downloadFile510();
				
				// Parse the downloaded file
				new Parse510StPaulFile(newFileName, myDb);
			}
			
			System.out.println("Download and parsing complete");
			
		}
		catch(Exception e)
		{
			//sendEmail(configuration.getEmailUsername(), e.getMessage());
		}
	}
}
