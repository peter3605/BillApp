import java.io.PrintStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailBills {
	private String date;
	private Configuration configuration;

	EmailBills(Record[] records, String[] billNames, String[] lastBills, String s, DatabaseConnector myDb, String m) {
		configuration = new Configuration(Main.answer);
		date = s;

		String[] names = new String[records.length];
		String[] apartments = new String[records.length];

		for (int i = 0; i < records.length; i++) {
			names[i] = records[i].getName();
			apartments[i] = records[i].getApartments();
		}

		String[] emails = getEmails(names, myDb);

		for (int i = 0; i < emails.length; i++) {
			sendEmail(emails[i], names[i], apartments[i], billNames[i], lastBills[i]);
		}
		System.out.println("All emails sent");
	}
	
	/** 
	 * Returns a string with the date spelled ou tin words - used in the email
	 */
	private String dateToString(String dateInNumbers) {
		String dateString = "";
		String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
		int month = Integer.valueOf(dateInNumbers.substring(0, 2)).intValue();
		dateString = dateString + months[(month - 1)] + " ";
		if (Integer.valueOf(dateInNumbers.charAt(3)).intValue() == 0) {
			dateString = dateString + dateInNumbers.charAt(4);
		}
		else {
			dateString = dateString + dateInNumbers.substring(3, 5);
		}
		dateString = dateString + ", " + dateInNumbers.substring(6);

		return dateString;
	}
	
	/** 
	 * Gets the email address of each tenant
	 */
	private String[] getEmails(String[] names, DatabaseConnector myDb) {
		String[] emails = new String[names.length];
		for (int i = 0; i < emails.length; i++) {
			try {
				emails[i] = myDb.getTennantEmailByName(names[i]);
			}
			catch (Exception e) {
				System.out.println("ERROR - unable to get email of: " + names[i] + " - " + e);
			}
		}

		System.out.println("Successfully got all tennant emails");
		return emails;
	}
	
	/** 
	 * Send an email to a tenant
	 */
	public void sendEmail(String email, String name, String apartment, String billName, String lastBill) {
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() 
		{
			protected PasswordAuthentication getPasswordAuthentication() 
			{
				return new PasswordAuthentication(configuration.getEmailUsername(), configuration.getEmailPassword());
			}
		});

		try {
			
			//create email object
			Message message = new MimeMessage(session);
			
			//if there is more than one tenant email
			if(email.contains("&")) {
				String[] emails = email.split("&");
				InternetAddress[] addresses = new InternetAddress[emails.length];
				for (int i = 0; i < emails.length; i++) {
					addresses[i] = new InternetAddress(emails[i]);
				}
				message.setRecipients(Message.RecipientType.TO, addresses);  
			} else {
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));  
			}
			
			//email subject header
			message.setSubject("Apartment " + apartment + " Utility Bill");   
			
			//main body text
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText("Dear " + name + " ," + 
					"\n\n\nAttached is your utilities bill from " + dateToString(lastBill) + " - " + dateToString(date) + ". Please pay by " + dateToString(GUI.getDueDate()) + " through either a check or with your rent." + 
					"\n\nFor any questions or concerns, please contact us at chateauridge.pm@gmail.com" + 
					"\n\n\nThanks," + 
					"\nChateau Ridge Property Management" + 
					"\n443-410-4234" + 
					"\nhttp://www.chateauridgegroup.com/");     
			
			//attach bill file
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(billName);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(apartment + " Utility Bill.pdf");
			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);
			
			//cc email to chateauridge.pm
			message.addRecipient(Message.RecipientType.CC, new InternetAddress("chateauridge.pm@gmail.com"));

			Transport.send(message);

			System.out.println("Email sent to: " + email);
		}
		catch (Exception e) {
			System.out.println("ERROR - unable to send email to: " + email + " - ");
			e.printStackTrace();
		}
	}
}
