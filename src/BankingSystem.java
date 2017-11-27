import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Manage connection to database and perform SQL statements.
 */
public class BankingSystem {
	// Connection properties
	private static String driver;
	private static String url;
	private static String username;
	private static String password;

	// JDBC Objects
	private static Connection testCon;
	private static Connection con;
	private static Statement stmt;
	private static ResultSet rs;

	private static int menuInput;
	private static Scanner reader;
	private static String customerId;
	private static boolean batchBypass = true;

	//gui helper stuff
	private static int newCustomerId;
	private static int newAccountNumber;
	private static boolean newCustomerSuccess;
	private static boolean openAccountSuccess;
	private static boolean closeAccountSuccess;
	private static boolean depositSuccess;
	private static boolean withdrawSuccess;
	private static boolean transferSuccess;
	private static boolean accountSummarySuccess;
	private static boolean reportASuccess;
	private static boolean reportBSuccess;
	private static String accountSummary = "";
	private static String reportAText = "";
	private static String reportBText = "";

	/**
	 * Initialize database connection given properties file.
	 * @param filename name of properties file
	 */
	public static void init(String filename) {
		try {
			Properties props = new Properties();						// Create a new Properties object
			FileInputStream input = new FileInputStream(filename);	// Create a new FileInputStream object using our filename parameter
			props.load(input);										// Load the file contents into the Properties object
			driver = props.getProperty("jdbc.driver");				// Load the driver
			url = props.getProperty("jdbc.url");						// Load the url
			username = props.getProperty("jdbc.username");			// Load the username
			password = props.getProperty("jdbc.password");			// Load the password
			//new stuff
			Class.forName(driver); // load the driver
			con = DriverManager.getConnection(url, username, password); // Create the connection
			stmt = con.createStatement(); // Create a statement
			reader = new Scanner(System.in);

		} catch (Exception e) {
			System.out.println("Exception in init()");
			e.printStackTrace();
		}
	}

	/**
	 * Test database connection.
	 */
	public static void testConnection() {
		System.out.println(":: TEST - CONNECTING TO DATABASE");
		try {
			Class.forName(driver);
			testCon = DriverManager.getConnection(url, username, password);
			testCon.close();
			System.out.println(":: TEST - SUCCESSFULLY CONNECTED TO DATABASE");
		} catch (Exception e) {
			System.out.println(":: TEST - FAILED CONNECTED TO DATABASE");
			e.printStackTrace();
		}
	}

	public static void closeConnections() {
		try {
			stmt.close();
			con.close(); // Close the connection after we are done with
		} catch (SQLException e) {
			e.printStackTrace();
		} // Close the statement after we are done with the statement
		reader.close();
	}

	public static void promptMainMenu() {
		batchBypass = false;
		System.out.println("\nWelcome to the Self Banking System! \n" +
				"1. New Customer \n" +
				"2. Customer Login \n" +
				"3. Exit \n");

		System.out.print("Please enter your choice: ");
		menuInput = reader.nextInt(); // Scans the next token of the input as an int.

		if (menuInput == 1) {
			promptNewCustomer();
		} else if (menuInput == 2) {
			promptLogin();
		} else if (menuInput == 3) {
			System.out.println("Good Bye!\n");
			closeConnections();
			return;
		} else {
			System.out.println("Input Not Recognized. Going Back To Main Menu");
			promptMainMenu();
		}
	}

	public static void promptLogin() {
		System.out.print("\nPlease enter your Customer ID: ");
		String idNumber = reader.next();
		System.out.print("Please enter your Pin number: ");
		String pinNumber = reader.next();

		// Administrator log in
		if (idNumber.equals("0") && pinNumber.equals("0")) {
			administratorMainMenu();
		} else { // Regular log in
			try {
				String query = "SELECT ID FROM P1.Customer WHERE ID = " + idNumber + " AND Pin = " + pinNumber;
				System.out.println();

				ResultSet rs = stmt.executeQuery(query);
				if (rs.next()) {
					customerId = idNumber;
					System.out.println("\nLog into ID " + customerId + " successful\n");
					customerMainMenu();
				} else {
					System.out.println("Login Failed, ID or PIN is incorrect, going back to main menu");
					promptMainMenu();
					return;
				}
				rs.close();
			} catch (SQLException e) {
				System.out.println("Login failed, ID or PIN is incorrect \n");
				promptMainMenu();
				// e.printStackTrace();
			}
		}
	}

	public static void customerMainMenu() {
		System.out.println("Customer Main Menu \n" +
				"1. Open Account\n" +
				"2. Close Account \n" +
				"3. Deposit \n" +
				"4. Withdraw \n" +
				"5. Transfer \n" +
				"6. Account Summary \n" +
				"7. Exit \n");

		System.out.print("Please enter your choice: ");
		menuInput = reader.nextInt();

		if (menuInput == 1) {
			System.out.print("\nPlease enter a Customer ID: ");
			String checkCustomerId = reader.next();
			System.out.print("Please enter a account type (C for Checking or S for Saving): ");
			String accountType = reader.next().toUpperCase();
			if(!accountType.equals("C") && !accountType.equals("S")) {
				System.out.println("Account Type Can Only Be 'C' OR 'S', Going back to customer main menu");
			}
			System.out.print("Please enter a balance (Initial Deposit): ");
			String intialBalance = reader.next();
			try{
				Integer.parseInt(intialBalance);
				if(Integer.parseInt(intialBalance) < 0) throw new NumberFormatException();
			} catch(Exception e) {
				System.out.println("Initial Balance can only be a positive integer, Going back to customer main menu");
				customerMainMenu();
				return;
			}
			openAccount(checkCustomerId, accountType, intialBalance);
		} else if (menuInput == 2) {
			System.out.print("\nPlease enter your Account Number to close: ");
			String accountNumber = reader.next();
			try{
				Integer.parseInt(accountNumber);
				if(Integer.parseInt(accountNumber) < 0) throw new NumberFormatException();
			} catch(Exception e) {
				System.out.println("Account can only be a positive integer, Going back to customer main menu");
				customerMainMenu();
				return;
			}
			closeAccount(accountNumber);
		} else if (menuInput == 3) {
			System.out.print("\nPlease enter Account Number To Deposit Into: ");
			String accountNumber = reader.next();
			try{
				Integer.parseInt(accountNumber);
				if(Integer.parseInt(accountNumber) < 0) throw new NumberFormatException();
			} catch(Exception e) {
				System.out.println("Account can only be a positive integer, Going back to customer main menu");
				customerMainMenu();
				return;
			}
			System.out.print("\nPlease enter your Deposit Amount: ");
			String deposit = reader.next();
			try{
				Integer.parseInt(deposit);
				if(Integer.parseInt(deposit) < 0) throw new NumberFormatException();
			} catch(Exception e) {
				System.out.println("Deposit can only be a positive integer, Going back to customer main menu");
				customerMainMenu();
				return;
			}
			deposit(accountNumber, deposit);
		} else if (menuInput == 4) {
			System.out.print("\nPlease enter your Account Number: ");
			String accountNumber = reader.next();
			try{
				Integer.parseInt(accountNumber);
				if(Integer.parseInt(accountNumber) < 0) throw new NumberFormatException();
			} catch(Exception e) {
				System.out.println("Account can only be a positive integer, Going back to customer main menu");
				customerMainMenu();
				return;
			}
			System.out.print("\nPlease enter your Withdraw Amount: ");
			String withdraw = reader.next();
			try{
				Integer.parseInt(withdraw);
				if(Integer.parseInt(withdraw) < 0) throw new NumberFormatException();
			} catch(Exception e) {
				System.out.println("Withdraw amount can only be a positive integer, Going back to customer main menu");
				customerMainMenu();
				return;
			}
			withdraw(accountNumber, withdraw);

		} else if (menuInput == 5) {
			System.out.print("\nPlease enter your Source's Account Number: ");
			String sourceAccountNumber = reader.next();
			try{
				Integer.parseInt(sourceAccountNumber);
				if(Integer.parseInt(sourceAccountNumber) < 0) throw new NumberFormatException();
			} catch(Exception e) {
				System.out.println("Source Account # can only be a positive integer, Going back to customer main menu");
				customerMainMenu();
				return;
			}
			System.out.print("\nPlease enter your Destination's Account Number: ");
			String destinationAccountNumber = reader.next();
			try{
				Integer.parseInt(destinationAccountNumber);
				if(Integer.parseInt(destinationAccountNumber) < 0) throw new NumberFormatException();
			} catch(Exception e) {
				System.out.println("Destination Account # can only be a positive integer, Going back to customer main menu");
				customerMainMenu();
				return;
			}
			System.out.print("\nPlease enter the amount to transfer: ");
			String transferAmount = reader.next();
			try{
				Integer.parseInt(transferAmount);
				if(Integer.parseInt(transferAmount) < 0) throw new NumberFormatException();
			} catch(Exception e) {
				System.out.println("Transfer amount can only be a positive integer, Going back to customer main menu");
				customerMainMenu();
				return;
			}
			transfer(sourceAccountNumber, destinationAccountNumber, transferAmount);
		} else if (menuInput == 6) {
			accountSummary(customerId);
		} else if (menuInput == 7) {
			promptMainMenu();
			return;
		} else {
			System.out.println("Input Not Recognized. Going Back To Customer Main Menu");
		}
		customerMainMenu();
	}

		public static void promptNewCustomer() {
			System.out.print("\nPlease enter your name: ");
			reader.nextLine();
			String name = reader.nextLine();
			if(name.isEmpty()) {
				System.out.println("Name cannot be blank, going back to main menu");
				promptMainMenu();
				return;
			}
			System.out.print("Please enter your gender (M or F): ");
			String gender = reader.next().toUpperCase();
			if(!gender.equals("M") && !gender.equals("F")) {
				System.out.println("Gender can only be 'M' or 'F', going back to main menu");
				promptMainMenu();
				return;
			}
			System.out.print("Please enter your age: ");
			String age = reader.next();
			try{
				Integer.parseInt(age);
				if(Integer.parseInt(age) < 0) throw new NumberFormatException();
			} catch(Exception e) {
				System.out.println("Age can only be a positive integer, going back to main menu");
				promptMainMenu();
				return;
			}
			System.out.print("Please enter your pin: ");
			String pin = reader.next();
			try{
				Integer.parseInt(pin);
				if(Integer.parseInt(pin) < 0) throw new NumberFormatException();
			} catch(Exception e) {
				System.out.println("Pin can only be a positive integer, going back to main menu");
				promptMainMenu();
				return;
			}
			newCustomer(name, gender, age, pin);
			promptMainMenu();
		}


	/**
	 * Create a new customer.
	 * @param name customer name
	 * @param gender customer gender
	 * @param age customer age
	 * @param pin customer pin
	 */
	public static void newCustomer(String name, String gender, String age, String pin) {
		System.out.println("\n:: CREATE NEW CUSTOMER - RUNNING");
		try {
			PreparedStatement customerInfo = con
					.prepareStatement("INSERT INTO P1.Customer (Name, Gender, Age, Pin) VALUES (?, ?, ?, ?)");
			customerInfo.setString(1, name);
			customerInfo.setString(2, gender);
			customerInfo.setString(3, age);
			customerInfo.setString(4, pin);
			customerInfo.executeUpdate();

			String query = "SELECT IDENTITY_VAL_LOCAL() AS NewCustomerId FROM SYSIBM.SYSDUMMY1";
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				newCustomerId = rs.getInt(1);
				if(!batchBypass) {
					System.out.println("Account Creation Successful, Your Customer ID Is: " + newCustomerId);
				}
				System.out.println(":: CREATE NEW CUSTOMER - SUCCESS");
				newCustomerSuccess = true;
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println(e);
			System.out.println("Inserting new customer failed \n"
					+ "Gender Must Be 'M' or 'F' \n"
					+ "Age Must Be A Positive Integer \n"
					+ "Pin Must Be A Positive Integer \n"
					+ "Returning to Main Menu \n");
			// e.printStackTrace();
		}
	}

	/**
	 * Open a new account.
	 * @param id customer id
	 * @param type type of account
	 * @param amount initial deposit amount
	 */
	public static void openAccount(String id, String type, String amount)
	{
		System.out.println("\n:: OPEN ACCOUNT - RUNNING");
		try {
			PreparedStatement openAccountInfo = con
					.prepareStatement("INSERT INTO P1.Account(ID, Balance, Type, Status) VALUES (?, ?, ?, ?)");
			openAccountInfo.setString(1, id);
			openAccountInfo.setString(2, amount);
			openAccountInfo.setString(3, type);
			openAccountInfo.setString(4, "A");
			openAccountInfo.executeUpdate();

			// Retrieve new auto_update key
			String query = "SELECT IDENTITY_VAL_LOCAL() AS NewAccountNumber FROM SYSIBM.SYSDUMMY1";
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				newAccountNumber = rs.getInt(1);
				if(!batchBypass) {
					System.out.println("Account Creation Successful, Your Newly Opened Account Number Is: " + newAccountNumber);
				}
				System.out.println(":: OPEN ACCOUNT - SUCCESS");
				openAccountSuccess = true;
			}
			rs.close();
		} catch (SQLException e) {
			System.out.println(e);
			System.out.println("Open Account Failed");
			// e.printStackTrace();
		}
	}

	/**
	 * Close an account.
	 * @param accNum account number
	 */
	public static void closeAccount(String accNum)
	{
		System.out.println("\n:: CLOSE ACCOUNT - RUNNING");
		String query = "SELECT ID FROM P1.Account WHERE ID = " + customerId + " AND Number = " + accNum;
		try {
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				query = "UPDATE P1.Account SET status = 'I' WHERE ID = " + customerId + " AND Number = " + accNum;
				stmt.executeUpdate(query);
				System.out.println("Account Number " + accNum + " Has Been Successfully Closed\n");
				System.out.println(":: CLOSE ACCOUNT - SUCCESS");
				closeAccountSuccess = true;

			} else if (batchBypass) {
				query = "UPDATE P1.Account SET status = 'I' WHERE Number = " + accNum;
				stmt.executeUpdate(query);
				System.out.println(":: CLOSE ACCOUNT - SUCCESS");

			} else {
				System.out.println("Account ID " + accNum + " Is Not Linked To " + customerId + " or Doesn't Exist, Going Back To Customer Main Menu");
			}
      rs.close();
		} catch (SQLException e) {
			// e.printStackTrace();
			System.out.println("Close Account Error");
		}
	}

	/**
	 * Deposit into an account.
	 * @param accNum account number
	 * @param amount deposit amount
	 */
	public static void deposit(String accNum, String amount) {
		System.out.println("\n:: DEPOSIT - RUNNING");
		if(Integer.parseInt(amount) < 0) return;
		String query = "SELECT ID FROM P1.Account WHERE Number = " + accNum + " AND STATUS = 'A'";
		try {
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				query = "UPDATE P1.Account SET Balance = (SELECT Balance FROM P1.Account WHERE Number = " + accNum
						+ ") + " + amount + " WHERE Number = " + accNum;
				stmt.executeUpdate(query);
				if(!batchBypass) {
					System.out.println("$" + amount + " Has been succesfully deposited into Account number " + accNum);
				}
				System.out.println(":: DEPOSIT - SUCCESS");
				depositSuccess = true;
			} else {
				System.out.println("Account ID To deposit to Not Found Or Is Inactive Going Back To Customer Main Menu");
			}
			rs.close();
		} catch (SQLException e) {
			// e.printStackTrace();
			System.out.println("Deposit Account Error");
		}
	}

	/**
	 * Withdraw from an account.
	 * @param accNum account number
	 * @param amount withdraw amount
	 */
	public static void withdraw(String accNum, String amount) {
		System.out.println("\n:: WITHDRAW - RUNNING");
		if(Integer.parseInt(amount) < 0) return;
		String query = "SELECT ID FROM P1.Account WHERE ID = " + customerId + " AND Number = " + accNum + " AND STATUS = 'A'";
		try {
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				query = "UPDATE P1.Account SET Balance = (SELECT Balance FROM P1.Account WHERE ID = " + customerId
						+ " AND Number = " + accNum + ") - " + amount + " WHERE ID = " + customerId + " AND Number = "
						+ accNum;
				stmt.executeUpdate(query);
				System.out.println("$" + amount + " Has been Succesfully Withdrawn From Account number " + accNum);
				System.out.println(":: WITHDRAW - SUCCESS");
				withdrawSuccess = true;

			} else if(batchBypass) {
				query = "UPDATE P1.Account SET Balance = (SELECT Balance FROM P1.Account WHERE Number = " +
						accNum + ") - " + amount + " WHERE Number = " + accNum;
				stmt.executeUpdate(query);
				System.out.println(":: WITHDRAW - SUCCESS");
			} else {
				System.out.println("Account ID To Withdraw From Not Found Going Back To Customer Main Menu");
			}
			rs.close();
		} catch (SQLException e) {
			// e.printStackTrace();
			System.out.println("Withdraw Account Error, Maybe Account Number is Inactive");
		}
	}

	/**
	 * Transfer amount from source account to destination account.
	 * @param srcAccNum source account number
	 * @param destAccNum destination account number
	 * @param amount transfer amount
	 */
	public static void transfer(String srcAccNum, String destAccNum, String amount) {
		System.out.println("\n:: TRANSFER - RUNNING");
		if(!isActiveAccount(srcAccNum) && !isActiveAccount(destAccNum)) {
			System.out.println("Source Account or Destination Account is not active, returning to customer main menu");
			return;
		}
		String query = "SELECT ID FROM P1.Account WHERE ID = " + customerId + " AND Number = " + srcAccNum;
		try {
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				query = "UPDATE P1.Account SET Balance = (SELECT Balance FROM P1.Account WHERE ID = " + customerId
						+ " AND Number = " + srcAccNum + ") - " + amount + " WHERE ID = " + customerId
						+ " AND Number = " + srcAccNum;
				stmt.executeUpdate(query);

				query = "UPDATE P1.Account SET Balance = (SELECT Balance FROM P1.Account WHERE Number = " + destAccNum
						+ ") + " + amount + " WHERE Number = " + destAccNum;
				int temp = stmt.executeUpdate(query);
				if (temp == 1) {
					System.out.println("$" + amount + " Has been Successfully Transfer From Account number " + srcAccNum + " to " + destAccNum);
					System.out.println(":: TRANSFER - SUCCESS");
					transferSuccess = true;
				} else {
					System.out.println("Transfer failed, destination account does not exist");
					query = "UPDATE P1.Account SET Balance = (SELECT Balance FROM P1.Account WHERE ID = " + customerId
							+ " AND Number = " + srcAccNum + ") + " + amount + " WHERE ID = " + customerId
							+ " AND Number = " + srcAccNum;
					stmt.executeUpdate(query);
				}

			} else if(batchBypass) {
				query = "UPDATE P1.Account SET Balance = (SELECT Balance FROM P1.Account WHERE Number = " + srcAccNum + ") - " + amount + " WHERE Number = " + srcAccNum;
				stmt.executeUpdate(query);

				query = "UPDATE P1.Account SET Balance = (SELECT Balance FROM P1.Account WHERE Number = " + destAccNum
						+ ") + " + amount + " WHERE Number = " + destAccNum;
				stmt.executeUpdate(query);
				System.out.println(":: TRANSFER - SUCCESS");
			} else {
				System.out.println("Source Account Number: " + srcAccNum + " Is Not Linked To Customer ID: "
						+ customerId + " Returning To Customer Menu\n");
			}
			rs.close();
		} catch (SQLException e) {
			// e.printStackTrace();
			System.out.println("Withdraw Account Error");
		}
	}
	
	public static boolean isActiveAccount(String accNum) {
		String query = "SELECT ID FROM P1.Account WHERE Number = " + accNum  + " AND STATUS = 'A'";
		try {
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				rs.close();
				return true;
			}

		} catch (SQLException e) {
			// e.printStackTrace();
			System.out.println("Withdraw Account Error");
		}
		return false;
	}


	/**
	 * Display account summary.
	 * @param accNum account number
	 */
	public static void accountSummary(String cusID) {
		System.out.println("\n:: ACCOUNT SUMMARY - RUNNING");
		accountSummary = "";
		boolean accountExist = false;
		String query = "SELECT Number FROM P1.Account WHERE ID = " + cusID + " AND Status = 'A'"; // The query to run
		try {
			ResultSet rs = stmt.executeQuery(query);
			System.out.println("NUMBER      BALANCE");
			System.out.println("----------- -----------");
			int count = 1;
			Statement stmt2 = con.createStatement();
			while (rs.next()) { // Loop through result set and retrieve contents of each row
				int currentAccountNumber = rs.getInt(1);
				String query2 = "SELECT Balance FROM P1.Account WHERE Number = " + currentAccountNumber + " AND Status = 'A'"; // The query to  run
				ResultSet temp = stmt2.executeQuery(query2); // Executing the query and storing the results in a Result Set
				int currentAccountBalance = 0;
				accountExist = true;
				if(temp.next()) {
					currentAccountBalance = temp.getInt(1);
				}
				String formatted = String.format("%11s %11s", currentAccountNumber,currentAccountBalance);
				System.out.println(formatted);
//				System.out.println(count + ") Account #" + currentAccountNumber + ", " + "Balance: " + currentAccountBalance); // Print out each row's values to the screen
				accountSummary = accountSummary + count + ") Account #" + currentAccountNumber + ", " + "Balance: " + currentAccountBalance + "\n";
				count++;
				temp.close();
			}
			stmt2.close();
			query = "SELECT sum(Balance) FROM P1.Account WHERE ID = " + cusID + " AND Status = 'A'"; // The query to  run
			rs = stmt.executeQuery(query); // Executing the query and storing the results in a Result Set
			if (rs.next() && accountExist) { // Loop through result set and retrieve contents of each row
				int totalBalance = rs.getInt(1);
				accountSummarySuccess = true;
				System.out.println("-----------------------");
//				System.out.println("\nThe Total Balance Of All Accounts Listed Above: $" + totalBalance + "\n");
				String formatted = String.format("Total%18s", totalBalance);
				System.out.println(formatted);
				System.out.println(":: ACCOUNT SUMMARY - SUCCESS");
				accountSummary = accountSummary + "\nThe Total Balance Of All Accounts Listed Above: $" + totalBalance + "\n";
			} else {
				System.out.println("Account # to get summary does not exist");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void administratorMainMenu() {
		System.out.println("\nAdministrator Main Menu! \n"
				+ "1. Account Summary for a Customer \n"
				+ "2. Report A :: Customer Information with Total Balance in Decreasing Order \n"
				+ "3. Report B :: Find the Average Total Balance Between Age Groups \n"
				+ "4. Exit \n");

		System.out.print("Please enter your choice: ");
		menuInput = reader.nextInt(); // Scans the next token of the input as an int.

		if (menuInput == 1) {
			System.out.print("\nPlease enter the Customer's Account Number: ");
			String customerAccountNumber = reader.next();
			accountSummary(customerAccountNumber);
		} else if (menuInput == 2) {
			reportA();
		} else if (menuInput == 3) {
			System.out.print("\nPlease enter the minimum age: ");
			String minAge = reader.next();
			try{
				Integer.parseInt(minAge);
				if(Integer.parseInt(minAge) < 0) throw new NumberFormatException();
			} catch(Exception e) {
				System.out.println("Minimum age can only be a positive integer, Going back to Admin main menu");
				administratorMainMenu();
				return;
			}
			System.out.print("\nPlease enter the maximum age: ");
			String maxAge = reader.next();
			try{
				Integer.parseInt(maxAge);
				if(Integer.parseInt(maxAge) < 0) throw new NumberFormatException();
			} catch(Exception e) {
				System.out.println("Maximum age can only be a positive integer, Going back to Admin main menu");
				administratorMainMenu();
				return;
			}
			reportB(minAge, maxAge);
		} else if (menuInput == 4) {
			customerMainMenu();
			return;
		} else {
			System.out.println("Input Not Recognized. Going Back To Administrator Main Menu");
		}
		administratorMainMenu();
	}

	/**
	 * Display Report A - Customer Information with Total Balance in Decreasing Order.
	 */
	public static void reportA() {
		System.out.println("\n:: REPORT A - RUNNING");
		try {
			String query = "SELECT ID, TotalBalance FROM SumBalance ORDER BY TotalBalance DESC"; //The query to run
			rs = stmt.executeQuery(query); //Executing the query and storing the results in a Result Set
			Statement stmt2 = con.createStatement();
			System.out.println("ID          NAME            GENDER AGE         TOTAL");
			System.out.println("----------- --------------- ------ ----------- -----------");
			while (rs.next()) { //Loop through result set and retrieve contents of each row
				String currentId = rs.getString(1);
				String totalBalance = rs.getString(2);

				String query2 = "SELECT ID, Name, Gender, Age, Pin FROM P1.Customer WHERE ID = " + currentId;
				ResultSet temp = stmt2.executeQuery(query2);
				if (temp.next()) {
					String name = temp.getString(2);
					String gender = temp.getString(3);
					int age = temp.getInt(4);
					String formatted = String.format("%11s %-15s %-6s %11s %11s", currentId,name,gender,age,totalBalance);
					System.out.println(formatted);
					reportAText = reportAText + "Customer ID: " + currentId + ", Name: " + name + ", Age: " + age
							+ ", Gender: " + gender + ", Total Balance: " + totalBalance + "\n";
				} else {
					String name = temp.getString(2);
					String gender = temp.getString(3);
					int age = temp.getInt(4);
					String formatted = String.format("%11s %-15s %-6s %11s %11s", currentId,name,gender,age,0);
					System.out.println(formatted);
					reportAText = reportAText + "Customer ID: " + currentId + ", Name: " + name + ", Age: " + age
							+ ", Gender: " + gender + ", Total Balance: " + 0 + "\n";
				}

				temp.close();
				reportASuccess = true;
			}
			System.out.println(":: REPORT A - SUCCESS");
			reportASuccess = true;
			stmt2.close();
			rs.close();

		} catch (SQLException e) {
			System.out.println("report A failed \n");
			e.printStackTrace();
		}
	}


	/**
	 * Display Report B - Average Balance Of All Users Within Min and Max Age.
	 * @param min minimum age
	 * @param max maximum age
	 */
	public static void reportB(String min, String max) {
		System.out.println("\n:: REPORT B - RUNNING");
		try {
			String query = "SELECT ID FROM P1.Customer WHERE Age >= " + min + " AND Age <= " + max; //The query to run
			rs = stmt.executeQuery(query); // Executing the query and storing the results in a Result Set
			Statement stmt2 = con.createStatement();
			int totalBalance = 0;
			int totalCustomers = 0;
			System.out.println("Average");
			System.out.println("-----------");
			while (rs.next()) { //Loop through result set and retrieve contents of each row
				int currentAccountNumber = rs.getInt(1);
				totalCustomers++;
				String query2 = "SELECT totalBalance FROM SumBalance WHERE ID = " + currentAccountNumber;
				ResultSet temp = stmt2.executeQuery(query2);
				if (temp.next()) {
					totalBalance += temp.getInt(1);
				}
				temp.close();
				reportBSuccess = true;
			}

			String formatted = String.format("%11s", totalBalance/totalCustomers);
			System.out.println(formatted);
//			System.out.println("The Average Balance For All Users Between The Age of " + min + " And " + max + " Is: $"
//					+ totalBalance.divide(new BigDecimal(totalCustomers), 2, RoundingMode.HALF_UP));
			reportBText = reportBText + "The Average Balance For All Users Between The Age of " + min + " And " + max + " Is: $"
					+ totalBalance/totalCustomers;
			System.out.println(":: REPORT B - SUCCESS");
			stmt2.close();
			rs.close();

		} catch (SQLException e) {
			System.out.println("report B failed \n");
			e.printStackTrace();
		}
	}

/*************************************************************************************************************************
 *
 *
 * 	GUI STUFF BELOW
 *
 *
 * ************************************************************************************************************************
 */



	public static void gui() {
		JFrame frame = new JFrame("Banking System");

		JPanel panel = new JPanel();
		panel.setSize(450, 450);
		panel.setLayout(new GridLayout(3, 1));

		JButton newCustomerButton = new JButton("New Customer");
		newCustomerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				newCustomerFrame(frame);
			}
		});

		JButton customerLoginButton = new JButton("Customer Login");
		customerLoginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				customerLoginFrame(frame);
			}
		});

		JButton exitButton = new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Good Bye!");
				System.exit(0);
			}
		});

		panel.add(newCustomerButton);
		panel.add(customerLoginButton);
		panel.add(exitButton);

		frame.add(panel);

		frame.setSize(450, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void newCustomerFrame(JFrame first) {
		JFrame frame = new JFrame("New Customer");

		JPanel panel = new JPanel();
		panel.setSize(450, 450);
		panel.setLayout(new GridLayout(5, 2));

		JLabel name = new JLabel("Name", JLabel.CENTER);
		JTextField nameBox = new JTextField();
		nameBox.setHorizontalAlignment(JTextField.CENTER);
		JLabel gender = new JLabel("Gender", JLabel.CENTER);

		JPanel radioPanel = new JPanel(new GridLayout(1,2));
		JRadioButton maleButton = new JRadioButton("Male");
		maleButton.setSelected(true);
		maleButton.setActionCommand(maleButton.getText());
		JRadioButton femaleButton = new JRadioButton("Female");
		femaleButton.setActionCommand(femaleButton.getText());
		ButtonGroup group = new ButtonGroup();
		group.add(maleButton);
		group.add(femaleButton);
		radioPanel.add(maleButton);
		radioPanel.add(femaleButton);

		JLabel age = new JLabel("Age", JLabel.CENTER);
		JTextField ageBox = new JTextField();
		ageBox.setHorizontalAlignment(JTextField.CENTER);
		JLabel pin = new JLabel("Pin", JLabel.CENTER);
		JTextField pinBox = new JTextField();
		pinBox.setHorizontalAlignment(JTextField.CENTER);

		JButton submitButton = new JButton("Submit");
		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(group.getSelection().getActionCommand());
				newCustomerSuccess = false;
				newCustomer(nameBox.getText(), group.getSelection().getActionCommand().substring(0, 1), ageBox.getText(), pinBox.getText());
				if(newCustomerSuccess) {
					JOptionPane.showMessageDialog(null, "Account Creation Successful, Your Customer ID Is: " + newCustomerId);
				}
				else {
					JOptionPane.showMessageDialog(null, "Account Creation FAILED\nAge and Pin Must be a positive Integer\nGoing Back To Main Menu");
				}
				newCustomerSuccess = false;
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		panel.add(name);
		panel.add(nameBox);
		panel.add(gender);
		panel.add(radioPanel);
		panel.add(age);
		panel.add(ageBox);
		panel.add(pin);
		panel.add(pinBox);
		panel.add(submitButton);
		panel.add(cancelButton);

		frame.add(panel);
		frame.setSize(450, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void customerLoginFrame(JFrame first) {
		JFrame frame = new JFrame("New Customer");

		JPanel panel = new JPanel();
		panel.setSize(450, 450);
		panel.setLayout(new GridLayout(3, 2));

		JLabel id = new JLabel("ID", JLabel.CENTER);
		JTextField idBox = new JTextField();
		idBox.setHorizontalAlignment(JTextField.CENTER);
		JLabel pin = new JLabel("Pin", JLabel.CENTER);
		JTextField pinBox = new JTextField();
		pinBox.setHorizontalAlignment(JTextField.CENTER);

		JButton submitButton = new JButton("Login");
		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (idBox.getText().equals("0") && pinBox.getText().equals("0")) {
//					frame.setVisible(false);
					frame.dispose();
					JOptionPane.showMessageDialog(null, "Admin Login Success");
					administratorWelcomeFrame(first);
				} else { // Regular log in
					try {
						String query = "SELECT ID FROM P1.Customer WHERE ID = " + idBox.getText() + " AND Pin = " + pinBox.getText();
						System.out.println();

						ResultSet rs = stmt.executeQuery(query);
						if (rs.next()) {
							customerId = idBox.getText();
							System.out.println("\nLog into ID " + customerId + " successful\n");
							JOptionPane.showMessageDialog(null, "Login Success");
//							frame.setVisible(false);
							frame.dispose();
							customerWelcomeFrame(first);
						}
						else {
							JOptionPane.showMessageDialog(null, "Customer Login Failed, Please Retry ID and PIN");
						}
						rs.close();
					} catch (SQLException e1) {
						System.out.println("Login failed, ID or PIN is incorrect \n");
						JOptionPane.showMessageDialog(null, "Customer Login Failed, Please Retry ID and PIN");
						// e1.printStackTrace();
					}
				}

			}
		});

		panel.add(id);
		panel.add(idBox);
		panel.add(pin);
		panel.add(pinBox);
		panel.add(submitButton);
		panel.add(cancelButton);

		frame.add(panel);

		frame.setSize(450, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void customerWelcomeFrame(JFrame first) {
		JFrame frame = new JFrame("Customer Welcome Frame");

		JPanel panel = new JPanel();
		panel.setSize(450, 450);
		panel.setLayout(new GridLayout(7, 1));

		JButton openAccountButton = new JButton("Open Account");
		JButton closeAccountButton = new JButton("Close Account");
		JButton depositButton = new JButton("Deposit");
		JButton withdrawButton = new JButton("Withdraw");
		JButton transferButton = new JButton("Transfer");
		JButton accountSummaryButton = new JButton("Account Summary");
		JButton exitButton = new JButton("Exit");

		openAccountButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				openAccountFrame(frame);
			}
		});

		closeAccountButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				closeAccountFrame(frame);
			}
		});

		depositButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				depositFrame(frame);
			}
		});

		withdrawButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				withdrawFrame(frame);
			}
		});

		transferButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				transferFrame(frame);
			}
		});

		accountSummaryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				accountSummaryFrame(frame, customerId);
			}
		});

		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				JOptionPane.showMessageDialog(null, "Good Bye");
//				System.exit(0);
				first.setVisible(true);
				frame.dispose();

			}
		});

		panel.add(openAccountButton);
		panel.add(closeAccountButton);
		panel.add(depositButton);
		panel.add(withdrawButton);
		panel.add(transferButton);
		panel.add(accountSummaryButton);
		panel.add(exitButton);

		frame.add(panel);

		frame.setSize(450, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}


	public static void openAccountFrame(JFrame first) {
		JFrame frame = new JFrame("Open Account");

		JPanel panel = new JPanel();
		panel.setSize(450, 450);
		panel.setLayout(new GridLayout(4, 2));

		JLabel id = new JLabel("Customer ID", JLabel.CENTER);
		JTextField idBox = new JTextField();
		idBox.setHorizontalAlignment(JTextField.CENTER);

		JLabel accountType = new JLabel("Account Type", JLabel.CENTER);
		JPanel radioPanel = new JPanel(new GridLayout(1,2));
		JRadioButton checkingButton = new JRadioButton("Checking");
		checkingButton.setSelected(true);
		checkingButton.setActionCommand(checkingButton.getText());
		JRadioButton savingsButton = new JRadioButton("Savings");
		savingsButton.setActionCommand(savingsButton.getText());
		ButtonGroup group = new ButtonGroup();
		group.add(checkingButton);
		group.add(savingsButton);
		radioPanel.add(checkingButton);
		radioPanel.add(savingsButton);




		JLabel initialDeposit = new JLabel("Initial Deposit", JLabel.CENTER);
		JTextField initialDepositBox = new JTextField();
		initialDepositBox.setHorizontalAlignment(JTextField.CENTER);

		JButton submitButton = new JButton("Submit");
		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openAccountSuccess = false;
				openAccount(idBox.getText(), group.getSelection().getActionCommand().substring(0, 1), initialDepositBox.getText());
				if(openAccountSuccess) {
					JOptionPane.showMessageDialog(null, "Account Creation Successful, Your Account Number Is: " + newAccountNumber);
				}
				else {
					JOptionPane.showMessageDialog(null, "Account Creation FAILED, Going Back To Main Menu");
				}
				openAccountSuccess = false;
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		panel.add(id);
		panel.add(idBox);
		panel.add(accountType);
		panel.add(radioPanel);
		panel.add(initialDeposit);
		panel.add(initialDepositBox);
		panel.add(submitButton);
		panel.add(cancelButton);

		frame.add(panel);

		frame.setSize(650, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void closeAccountFrame(JFrame first) {
		JFrame frame = new JFrame("Close Account");

		JPanel panel = new JPanel();
		panel.setSize(450, 450);
		panel.setLayout(new GridLayout(2, 2));

		JLabel id = new JLabel("Account Number", JLabel.CENTER);
		JTextField idBox = new JTextField();
		idBox.setHorizontalAlignment(JTextField.CENTER);

		JButton submitButton = new JButton("Submit");
		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeAccountSuccess = false;
				closeAccount(idBox.getText());
				if(closeAccountSuccess) {
					JOptionPane.showMessageDialog(null, idBox.getText() + " Is Now Closed");
				} else {
					JOptionPane.showMessageDialog(null, "Account to close is not yours or non-existant, Going Back To Main Menu");
				}
				closeAccountSuccess = false;
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		panel.add(id);
		panel.add(idBox);
		panel.add(submitButton);
		panel.add(cancelButton);

		frame.add(panel);

		frame.setSize(450, 250);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void depositFrame(JFrame first) {
		JFrame frame = new JFrame("Deposit");

		JPanel panel = new JPanel();
		panel.setSize(450, 450);
		panel.setLayout(new GridLayout(3, 2));

		JLabel id = new JLabel("Account Number", JLabel.CENTER);
		JTextField idBox = new JTextField();
		idBox.setHorizontalAlignment(JTextField.CENTER);
		JLabel depositAmount = new JLabel("Amount to deposit", JLabel.CENTER);
		JTextField depositAmountBox = new JTextField();
		depositAmountBox.setHorizontalAlignment(JTextField.CENTER);

		JButton submitButton = new JButton("Submit");
		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				depositSuccess = false;
				deposit(idBox.getText(), depositAmountBox.getText());
				if(depositSuccess) {
					JOptionPane.showMessageDialog(null, "$" + depositAmountBox.getText() + " Has been succesfully deposited into Account Number " + idBox.getText());
				} else {
					JOptionPane.showMessageDialog(null, "Deposit Failed, Account # Is Invalid Or Deposit Amount is not a positive Integer\nGoing Back To Main Menu");
				}
				depositSuccess = false;
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		panel.add(id);
		panel.add(idBox);
		panel.add(depositAmount);
		panel.add(depositAmountBox);
		panel.add(submitButton);
		panel.add(cancelButton);

		frame.add(panel);

		frame.setSize(650, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void withdrawFrame(JFrame first) {
		JFrame frame = new JFrame("Deposit");

		JPanel panel = new JPanel();
		panel.setSize(450, 450);
		panel.setLayout(new GridLayout(3, 2));

		JLabel id = new JLabel("Account Number", JLabel.CENTER);
		JTextField idBox = new JTextField();
		idBox.setHorizontalAlignment(JTextField.CENTER);
		JLabel depositAmount = new JLabel("Amount to withdraw", JLabel.CENTER);
		JTextField depositAmountBox = new JTextField();
		depositAmountBox.setHorizontalAlignment(JTextField.CENTER);

		JButton submitButton = new JButton("Submit");
		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				withdrawSuccess = false;
				withdraw(idBox.getText(), depositAmountBox.getText());
				if(withdrawSuccess) {
					JOptionPane.showMessageDialog(null, "$" + depositAmountBox.getText() + " Has been succesfully Withdrawn from Account Number " + idBox.getText());
				} else {
					JOptionPane.showMessageDialog(null, "Withdraw Failed (Account Is Not Linked To Your Account or Is Non-existant or Amount is negative\nGoing Back To Main Menu");
				}
				withdrawSuccess = false;
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		panel.add(id);
		panel.add(idBox);
		panel.add(depositAmount);
		panel.add(depositAmountBox);
		panel.add(submitButton);
		panel.add(cancelButton);

		frame.add(panel);

		frame.setSize(650, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void transferFrame(JFrame first) {
		JFrame frame = new JFrame("Transfer Balance");

		JPanel panel = new JPanel();
		panel.setSize(450, 450);
		panel.setLayout(new GridLayout(4, 2));

		JLabel sourceId = new JLabel("Source Account Number", JLabel.CENTER);
		JTextField sourceIdBox = new JTextField();
		sourceIdBox.setHorizontalAlignment(JTextField.CENTER);
		JLabel destinationId = new JLabel("Destination Account Number", JLabel.CENTER);
		JTextField destinationIdBox = new JTextField();
		destinationIdBox.setHorizontalAlignment(JTextField.CENTER);
		JLabel transferAmount = new JLabel("Amount to transfer", JLabel.CENTER);
		JTextField transferAmountBox = new JTextField();
		transferAmountBox.setHorizontalAlignment(JTextField.CENTER);

		JButton submitButton = new JButton("Submit");
		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				transferSuccess = false;
				transfer(sourceIdBox.getText(), destinationIdBox.getText(), transferAmountBox.getText());
				if(transferSuccess) {
					JOptionPane.showMessageDialog(null, "$" + transferAmountBox.getText() + " Has been succesfully transferred \n From Account Number "
							+ sourceIdBox.getText() + " to " + destinationIdBox.getText());
				} else {
					JOptionPane.showMessageDialog(null, "Transfer Failed, Account # Is Invalid or "
							+ "\nSource Account Is Not Yours or "
							+ "\nAmount is not a positive integer or"
							+ "not enough balance\nGoing Back To Main Menu");
				}
				transferSuccess = false;
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		panel.add(sourceId);
		panel.add(sourceIdBox);
		panel.add(destinationId);
		panel.add(destinationIdBox);
		panel.add(transferAmount);
		panel.add(transferAmountBox);
		panel.add(submitButton);
		panel.add(cancelButton);

		frame.add(panel);

		frame.setSize(650, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void accountSummaryFrame(JFrame first, String accountNumber) {
		JFrame frame = new JFrame("Account Summary Frame");

		JPanel panel = new JPanel();
		panel.setSize(450, 450);
		panel.setLayout(new BorderLayout());

		JTextArea summary = new JTextArea("No Accounts");
		summary.setEditable(false);
		JScrollPane scroll = new JScrollPane (summary, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		JButton closeButton = new JButton("close");

		accountSummarySuccess = false;
		accountSummary(accountNumber);
		if(accountSummarySuccess) {
			summary.setText(accountSummary);
		} else {
			JOptionPane.showMessageDialog(null, "Report A Generation Failed");
		}
		accountSummarySuccess = false;

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accountSummary = "";
				first.setVisible(true);
				frame.setVisible(false);
			}
		});

		panel.add(scroll, BorderLayout.CENTER);
		panel.add(closeButton, BorderLayout.SOUTH);

		frame.add(panel);

		frame.setSize(450, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}



	public static void administratorWelcomeFrame(JFrame first) {
		JFrame frame = new JFrame("Administrator Welcome Frame");

		JPanel panel = new JPanel();
		panel.setSize(450, 450);
		panel.setLayout(new GridLayout(4, 1));

		JButton customerSummaryButton = new JButton("Account Summary for a Customer");
		JButton reportAButton = new JButton("Report A :: Customer Information with Total Balance in Decreasing Order");
		JButton reportBButton = new JButton("Report B :: Find the Average Total Balance Between Age Groups");
		JButton exitButton = new JButton("Exit");

		customerSummaryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				administratorSummaryFrame(frame);
			}
		});

		reportAButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				reportAFrame(frame);
			}
		});

		reportBButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				reportBFrame(frame);
			}
		});

		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				first.setVisible(true);
				frame.dispose();
			}
		});

		panel.add(customerSummaryButton);
		panel.add(reportAButton);
		panel.add(reportBButton);
		panel.add(exitButton);

		frame.add(panel);

		frame.setSize(600, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void administratorSummaryFrame(JFrame first) {
		JFrame frame = new JFrame("Administrator Summary Frame");

		JPanel panel = new JPanel();
		panel.setSize(450, 450);
		panel.setLayout(new GridLayout(2, 2));

		JLabel id = new JLabel("Customer ID Number", JLabel.CENTER);
		JTextField idBox = new JTextField();
		idBox.setHorizontalAlignment(JTextField.CENTER);

		JButton submitButton = new JButton("Submit");
		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				accountSummaryFrame(first, idBox.getText());
			}
		});

		panel.add(id);
		panel.add(idBox);
		panel.add(submitButton);
		panel.add(cancelButton);

		frame.add(panel);

		frame.setSize(650, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void reportAFrame(JFrame first) {
		JFrame frame = new JFrame("Report A Frame");

		JPanel panel = new JPanel();
		panel.setSize(450, 450);
		panel.setLayout(new BorderLayout());

		JTextArea reportA = new JTextArea("No Accounts");
		reportA.setEditable(false);
		JScrollPane scroll = new JScrollPane (reportA, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		JButton closeButton = new JButton("close");
		reportAText = "";
		reportASuccess = false;
		reportA();
		if(reportASuccess) {
			reportA.setText(reportAText);
		}
		reportASuccess = false;

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reportAText = "";
				first.setVisible(true);
				frame.setVisible(false);
			}
		});

		panel.add(scroll, BorderLayout.CENTER);
		panel.add(closeButton, BorderLayout.SOUTH);

		frame.add(panel);

		frame.setSize(550, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void reportBFrame(JFrame first) {
		JFrame frame = new JFrame("Report B");

		JPanel panel = new JPanel();
		panel.setSize(450, 450);
		panel.setLayout(new GridLayout(3, 2));

		JLabel minAge = new JLabel("Minimum Age", JLabel.CENTER);
		JTextField minAgeBox = new JTextField();
		minAgeBox.setHorizontalAlignment(JTextField.CENTER);
		JLabel maxAge = new JLabel("Maximum Age", JLabel.CENTER);
		JTextField maxAgeBox = new JTextField();
		maxAgeBox.setHorizontalAlignment(JTextField.CENTER);

		JButton submitButton = new JButton("Submit");
		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reportBText = "";
				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reportBText = "";
				reportASuccess = false;
				reportB(minAgeBox.getText(), maxAgeBox.getText());
				if(reportBSuccess) {
					JOptionPane.showMessageDialog(null, reportBText);
					reportBSuccess = false;
					reportBText = "";
				} else {
					JOptionPane.showMessageDialog(null, "Report B Failed, Age must be a positive Integer, Going Back To Admin Main Menu");
				}

				frame.setVisible(false);
				first.setVisible(true);
			}
		});

		panel.add(minAge);
		panel.add(minAgeBox);
		panel.add(maxAge);
		panel.add(maxAgeBox);
		panel.add(submitButton);
		panel.add(cancelButton);

		frame.add(panel);

		frame.setSize(650, 450);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
