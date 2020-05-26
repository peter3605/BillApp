import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DatabaseConnector {

	private static final String CONNECTION_URL = "jdbc:mysql://";

	private static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
	private Connection connection = null;

	private boolean isConnected;

	private String dbUrl;

	private String dbUsername;

	private String dbPassword;

	private Configuration configuration;

	public DatabaseConnector(Configuration config) {
		configuration = config;

		this.dbUrl = configuration.getDatabaseName();
		this.dbUsername = configuration.getDatabaseUsername();
		this.dbPassword = configuration.getDatabasePassword();
		isConnected = false;

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		}
		catch (ClassNotFoundException e) {
			System.out.println("ERROR - couldn't find class: com.mysql.jdbc.Driver - " + e);
		}
		try {
			connect();
			System.out.println("Connected to database: " + dbUrl);
		}
		catch (SQLException e) {
			System.out.println("ERROR - could not connect to database - " + e);
		}
	}

	public void connect() throws SQLException {
		if (connection != null)
			close();
		connection = DriverManager.getConnection("jdbc:mysql://" + dbUrl, dbUsername, dbPassword);
		isConnected = true;
	}

	public void close() {
		try {
			if (connection != null)
				connection.close();
			connection = null;
			isConnected = false;
			System.out.println("The SQL database connection has been closed");
		}
		catch (SQLException e) {
			System.out.println("ERROR - Failed to close the SQL database connection - " + e);
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public boolean checkIfRecordExists(String table, String date) {
		if(table.equals("water")) {
			String query = "SELECT 1 FROM " + configuration.getSchemaName() +".water WHERE date_of_reading = ?";
			PreparedStatement stmt = null;
			try {
				stmt = getConnection().prepareStatement(query);
				stmt.setString(1, date);
				if(isConnected()) {
					ResultSet rs = stmt.executeQuery();
					return rs.next();
				}
			}
			catch(SQLException e) {
				System.out.println("ERROR - occured while checking if record exists - "+e);
			}
		}
		else if(table.equals("electricity")) {
			String query = "SELECT 1 FROM " + configuration.getSchemaName() +".electricity WHERE date_of_reading = ?";
			PreparedStatement stmt = null;
			try {
				stmt = getConnection().prepareStatement(query);
				stmt.setString(1, date);
				if(isConnected()) {
					ResultSet rs = stmt.executeQuery();
					return rs.next();
				}
			}
			catch(SQLException e) {
				System.out.println("ERROR - occured while checking if record exists - "+e);
			}
		}
		return true;
	}

	public void insertNewWaterRecord(String date, int[] waterValues) {
		String query = "INSERT INTO " + configuration.getSchemaName() + ".water(date_of_reading,public,1A,1B,2A,2B,2C,3A,3B,3C) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		int size = 10;
		if(configuration.getFTPServer().equals("73.128.40.28")) {
			query = "INSERT INTO " + configuration.getSchemaName() + ".water(date_of_reading,public,basement,4A,3A,2F,2R,1A,CarriageHouse) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			size = 9;
		}
		PreparedStatement stmt = null;

		try {
			stmt = getConnection().prepareStatement(query);
			stmt.setString(1, date);
			for (int i = 2; i <= size; i++) {
				stmt.setInt(i, waterValues[(i - 2)]);
			}
			if (isConnected()) {
				stmt.executeUpdate();
			}
			System.out.println("Successfully inserted new water record");
		}
		catch (SQLException e) {
			System.out.println("ERROR -  occurred while inserting new record into water table - " + e);
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt - " + e);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					System.out.println("ERROR - occured while closeing stmt - " + e);
				}
			}
		}
	}

	public void insertNewElectricityRecord(String date, int[] electricityValues) {
		String query = "INSERT INTO " + configuration.getSchemaName() + ".electricity(date_of_reading,public,retail,1A,1B,2A,2B,2C,3A,3B,3C) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		int size = 11;
		if(configuration.getFTPServer().equals("73.128.40.28")) {
			query = "INSERT INTO " + configuration.getSchemaName() + ".electricity(date_of_reading,public,basement,4A,3A,2R,2F,1A,CarriageHouse) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			size = 9;
		}
		PreparedStatement stmt = null;
		try {
			stmt = getConnection().prepareStatement(query);
			stmt.setString(1, date);
			for (int i = 2; i <= size; i++) {
				stmt.setInt(i, electricityValues[(i - 2)]);
			}

			if (isConnected()) {
				stmt.executeUpdate();
			}
			System.out.println("Successfully inserted new electricity record");
		}
		catch (SQLException e) {
			System.out.println("ERROR -  occurred while inserting new record into electricity table - " + e);
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt - " + e1);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e2) {
					System.out.println("ERROR - occured while closeing stmt - " + e2);
				}
			}
		}
	}

	public String getWaterValue(String date, String apartment) {
		String answer = "";
		String query = "SELECT " + apartment + " FROM " + configuration.getSchemaName() + ".water WHERE date_of_reading = ?";
		PreparedStatement stmt = null;
		try {
			stmt = getConnection().prepareStatement(query);
			stmt.setString(1, date);
			if (isConnected()) {
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					answer = rs.getString(apartment);
				}
			}
		}
		catch (SQLException e) {
			System.out.println("ERROR - occured while getting water value - " + e);
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt - " + e1);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e2) {
					System.out.println("ERROR - occured while closeing stmt - " + e2);
				}
			}
		}
		return answer;
	}

	public String getElectricityValue(String date, String apartment) {
		String answer = "";
		String query = "SELECT " + apartment + " FROM " + configuration.getSchemaName() + ".electricity WHERE date_of_reading = ?";
		PreparedStatement stmt = null;
		try {
			stmt = getConnection().prepareStatement(query);
			stmt.setString(1, date);
			if (isConnected()) {
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					answer = rs.getString(apartment);
				}
			}
		}
		catch (SQLException e) {
			System.out.println("ERROR - occured while getting electricity value - " + e);      
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt - " + e1);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					System.out.println("ERROR - occured while closeing stmt - " + e);
				}
			}
		}
		return answer;
	}

	public String[][] getTennantTable() {
		String[] names = { "1A", "1B", "2A", "2B", "2C", "3A", "3B", "3C" };
		if(configuration.getFTPServer().equals("73.128.40.28")) {
			names = new String[]{"4A", "3A", "2F", "2R", "1A", "CH"};
		}
		ArrayList<String[]> list = new ArrayList();

		String query = "SELECT * FROM " + configuration.getSchemaName() + ".tennant_information";
		PreparedStatement stmt = null;
		try {
			stmt = getConnection().prepareStatement(query);
			if (isConnected()) {
				ResultSet rs = stmt.executeQuery();
				int i = 0;
				while (rs.next()) {
					String apartment = rs.getString("apartment");
					String name = rs.getString("tennant_name");
					String email = rs.getString("email_address");
					String date1 = rs.getString("last_bill");

					list.add(new String[] { apartment, name, email, date1 });
					i++;
				}
			}
			System.out.println("Successfully got tennant information");
		}
		catch (SQLException e) {
			System.out.println("ERROR - occured while getting tennant information - " + e);
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt - " + e1);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					System.out.println("ERROR - occured while closeing stmt - " + e);
				}
			}
		}


		String[][] results = new String[list.size()][];
		for (int i = 0; i < list.size(); i++) {
			results[i] = ((String[])list.get(i));
		}
		return results;
	}

	public void updateTennantRecord(String apartment, String[] newData) {
		String query = "UPDATE " + configuration.getSchemaName() + ".tennant_information SET tennant_name = ?, email_address = ?, last_bill = ? WHERE apartment = ?";
		PreparedStatement stmt = null;
		try {
			stmt = getConnection().prepareStatement(query);
			stmt.setString(1, newData[0]);
			stmt.setString(2, newData[1]);
			stmt.setString(3, newData[2]);
			stmt.setString(4, apartment);
			if (isConnected()) {
				stmt.executeUpdate();
			}
			System.out.println("Successfully updated tennant record");
		}
		catch (SQLException e) {
			System.out.println("ERROR - occured while updating tennant record - " + e);
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt - " + e1);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					System.out.println("ERROR - occured while closeing stmt - " + e);
				}
			}
		}
	}

	public void addTennantRecord(String[] newData) {
		String query = "INSERT INTO " + configuration.getSchemaName() + ".tennant_information(apartment,tennant_name,email_address,last_bill) VALUES(?, ?, ?, ?)";
		PreparedStatement stmt = null;
		try {
			stmt = getConnection().prepareStatement(query);
			for (int i = 0; i < 4; i++) {
				stmt.setString(i + 1, newData[i]);
			}
			if (isConnected()) {
				stmt.executeUpdate();
			}
			System.out.println("Successfully inserted new tennant record");
		}
		catch (SQLException e) {
			System.out.println("ERROR - occured while adding new tennant record - " + e);
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt " + e1);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					System.out.println("ERROR - occured while closeing stmt " + e);
				}
			}
		}
	}

	public void deleteTennantRecord(String name) {
		String query = "DELETE FROM " + configuration.getSchemaName() + ".tennant_information WHERE tennant_name = ?";
		PreparedStatement stmt = null;
		try {
			stmt = getConnection().prepareStatement(query);
			stmt.setString(1, name);
			if (isConnected()) {
				stmt.executeUpdate();
			}
			System.out.println("Successfully deleted tennant record");
		}
		catch (SQLException e) {
			System.out.println("ERROR - unable to delete tennant record - " + e);
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt " + e1);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					System.out.println("ERROR - occured while closeing stmt " + e);
				}
			}
		}
	}

	public String getLastTennantBill(String apartment) {
		String query = "SELECT last_bill FROM " + configuration.getSchemaName() + ".tennant_information WHERE apartment = ?";
		PreparedStatement stmt = null;
		String answer = "";
		try {
			stmt = getConnection().prepareStatement(query);
			stmt.setString(1, apartment);
			if (isConnected()) {
				ResultSet rs = stmt.executeQuery();
				rs.next();
				answer = rs.getString("last_bill");
			}
		}
		catch (SQLException e) {
			System.out.println("ERROR - unable to get last tennant bill - " + e);
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt " + e1);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					System.out.println("ERROR - occured while closeing stmt " + e);
				}
			}
		}
		return answer;
	}

	public String getTennantNameByApartment(String apartment) {
		String answer = "";
		String query = "SELECT tennant_name FROM " + configuration.getSchemaName() + ".tennant_information WHERE apartment =?";
		PreparedStatement stmt = null;
		try {
			stmt = getConnection().prepareStatement(query);
			stmt.setString(1, apartment);
			if (isConnected()) {
				ResultSet rs = stmt.executeQuery();
				rs.next();
				answer = rs.getString("tennant_name");
			}
		}
		catch (SQLException e) {
			System.out.println("ERROR - unable to get last tennant name - " + e);
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt " + e1);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					System.out.println("ERROR - occured while closeing stmt " + e);
				}
			}
		}
		return answer;
	}

	public String getTennantEmailByName(String name) {
		String answer = "";
		String query = "SELECT email_address FROM " + configuration.getSchemaName() + ".tennant_information WHERE tennant_name = ?";
		PreparedStatement stmt = null;
		try {
			stmt = getConnection().prepareStatement(query);
			stmt.setString(1, name);

			if (isConnected()) {
				ResultSet rs = stmt.executeQuery();
				rs.next();
				answer = rs.getString("email_address");
			}
		}
		catch (SQLException e) {
			System.out.println("ERROR - unable to get tennant email address - " + e);
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt " + e1);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					System.out.println("ERROR - occured while closeing stmt " + e);
				}
			}
		}
		return answer;
	}

	public void updateBillDates(String apartment, String newBillDate) {
		String query = "UPDATE " + configuration.getSchemaName() + ".tennant_information SET last_bill = ? WHERE apartment = ?";
		PreparedStatement stmt = null;
		try {
			stmt = getConnection().prepareStatement(query);
			stmt.setString(1, newBillDate);
			stmt.setString(2, apartment);

			if (isConnected()) {
				stmt.executeUpdate();
			}
			System.out.println("Successfully update last bill dates for " + apartment);
		}
		catch (SQLException e) {
			System.out.println("ERROR - occured while updating last billing dates - " + e);
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt " + e1);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					System.out.println("ERROR - occured while closeing stmt " + e);
				}
			}
		}
	}

	public void updateUnitCost(double waterUnitCost, double electricityUnitCost) {
		String query = "UPDATE " + configuration.getSchemaName() + ".user_information SET water_unit_cost = ?, electricity_unit_cost = ? WHERE id = ?";
		PreparedStatement stmt = null;
		try {
			stmt = getConnection().prepareStatement(query);
			stmt.setDouble(1, waterUnitCost);
			stmt.setDouble(2, electricityUnitCost);
			stmt.setInt(3, 1);

			if (isConnected()) {
				stmt.executeUpdate();
			}
		}
		catch (SQLException e) {
			System.out.println("ERROR - occured while updating unit costs - " + e);
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt " + e1);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					System.out.println("ERROR - occured while closeing stmt " + e);
				} 
			}
		}
	}

	public String getWaterUnitCost() {
		String query = "SELECT water_unit_cost FROM " + configuration.getSchemaName() + ".user_information WHERE id = ?";
		PreparedStatement stmt = null;
		String answer = "";
		try {
			stmt = getConnection().prepareStatement(query);
			stmt.setInt(1, 1);

			if (isConnected()) {
				ResultSet rs = stmt.executeQuery();
				rs.next();
				answer = rs.getString("water_unit_cost");
			}
		}
		catch (SQLException e) {
			System.out.println("ERROR - occured while getting water unit costs - " + e);
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt " + e1);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					System.out.println("ERROR - occured while closeing stmt " + e);
				}
			}
		}
		return answer;
	}

	public String getElectricityUnitCost() { 
		String query = "SELECT electricity_unit_cost FROM " + configuration.getSchemaName() + ".user_information WHERE id = ?";
		PreparedStatement stmt = null;
		String answer = "";
		try {
			stmt = getConnection().prepareStatement(query);
			stmt.setInt(1, 1);
	
			if (isConnected()) {
				ResultSet rs = stmt.executeQuery();
				rs.next();
				answer = rs.getString("electricity_unit_cost");
			}
		}
		catch (SQLException e) {
			System.out.println("ERROR - occured while getting electricity unit costs - " + e);
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e1) {
					System.out.println("ERROR - occured while closeing stmt " + e1);
				}
			}
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					System.out.println("ERROR - occured while closeing stmt " + e);
				}
			}
		}
		return answer;
	}
}
