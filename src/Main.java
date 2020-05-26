import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.net.ftp.FTPClient;

public class Main 
{	
	private DatabaseConnector myDb;
	protected Configuration configuration;

	// This global variable contains the building that the user selected
	// Default setting is 510stpaul
	static String answer = "510StPaul";

	/**
	 * Creates the dropdown menu to choose which building
	 * @param none
	 */
	private void chooseBuilding() {
		// Building options
		String[] options = new String[] {"510StPaul","1933StPaul"};

		// Creates the jframe and panel 
		JFrame optionFrame = new JFrame();
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(400,100));
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
		JLabel label = new JLabel("Choose which building you are billing:");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(label);

		// Creates the dropdown menu with the building option
		JComboBox buildings = new JComboBox(options);
		buildings.setSelectedIndex(0);
		buildings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				answer = (String)cb.getSelectedItem();
			}
		});
		panel.add(buildings);

		// Creates the button - it calls the initialize() method when clicked
		JButton button = new JButton("Ok");
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				optionFrame.dispose();
				initialize();
			}
		});
		panel.add(button);

		// Frame settings
		optionFrame.setContentPane(panel);
		optionFrame.setVisible(true);;
		optionFrame.pack();
		optionFrame.setLocationRelativeTo(GUI.mainFrame);
	}

	/**
	 * Initializes global variables, runs the FTP process, launches GUI
	 * @param none
	 */
	private void initialize() {
		System.out.println("Building : " + answer);

		// Initialize configuration and database connector
		configuration = new Configuration(answer);
		myDb = new DatabaseConnector(configuration);

		// Run the FTP process
		runFTP();

		// Launch the GUI
		new GUI(myDb);
	}

	/**
	 * Connects to the ftp file, downloads the file, and then parses the file
	 * @param none
	 */
	public void runFTP() {
		System.out.println("Begining FTP connection...");

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

	public Configuration getConfiguration() {
		return configuration;
	}

	public static void main(String[] args) {
		new Main().chooseBuilding();
	}
}
