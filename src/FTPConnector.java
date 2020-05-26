import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class FTPConnector 
{
	private Configuration configuration;

	FTPConnector(Configuration config) {
		configuration = config;
	}

	/**
	 * This method connects to the server and logs in using the username and password specified in the config file
	 * @param client - the FTPClient object created in main
	 */
	public void logInToServer(FTPClient client) {
		// Connect to server
		try {
			client.connect(configuration.getFTPServer(),9021);
			System.out.println("Connected to "+configuration.getFTPServer());
		}
		catch(Exception e) {
			System.out.print("ERROR - unable to connect to server - ");
			e.printStackTrace();
		}

		// Login to server
		try {
			client.login(configuration.getFTPUsername(), configuration.getFTPPassword());
			System.out.println("Successfully logged into FTP Server");
		}
		catch(Exception e) {
			System.out.println("ERROR - unable to log into server - "+e);
		}

		// Set up connection
		try {   
			client.enterLocalPassiveMode();
			client.setFileType(FTP.BINARY_FILE_TYPE);
			client.changeWorkingDirectory(configuration.getFTPFolder());
		}
		catch(Exception e) {
			System.out.println("ERROR - occured while setting up FTP client - "+e);
		}
	}
}
