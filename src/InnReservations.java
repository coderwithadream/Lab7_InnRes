import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Scanner;
import java.lang.String;

public class InnReservations {
	public static void main(String[] args) {
		if (args.length != 3) {
			return;
		}
		String dburl = "jdbc:mysql://" + args[0] + "?autoReconnect=true&verifyServerCertificate=false&useSSL=true";
		String dbuser = args[1];
		String dbpass = args[2];
		try {
			Connection c = DriverManager.getConnection(dburl, dbuser, dbpass);
			Scanner s = new Scanner(System.in);
			while (prompt(c, s));
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean prompt(Connection c, Scanner s) {
		System.out.println("1: Rooms and Rates");
		System.out.println("2: Reservations");
		System.out.println("3: Reservation Change");
		System.out.println("4: Reservation Cancellation");
		System.out.println("5: Detailed Reservation Information");
		System.out.println("6: Revenue");
		System.out.println("7: Quit");
		System.out.print("Enter #");
		String string = s.nextLine();
		if (string.length() == 1) {
			int choice =  Character.getNumericValue(string.charAt(0));
			switch (choice) {
			case 1:
				rooms_and_rates(c, s);
				break;
			case 2:
				reservations(c, s);
				break;
			case 3:
				reservation_change(c, s);
				break;
			case 4:
				reservation_cancellation(c, s);
				break;
			case 5:
				detailed_reservation_information(c, s);
				break;
			case 6:
				revenue(c, s);
				break;
			case 7:
				return false;
			default:
				break;
			}
		}
		return true;
	}
	
	//1, 3, 5 - RW
	//2, 4, 6 - KK
	public static void rooms_and_rates(Connection c, Scanner s) {
		System.out.println("1");
	}
	
	public static void reservations(Connection c, Scanner s) {
		System.out.println("2");
		String string = "";
		boolean cont = true;
		
		System.out.print("First name:");
		if (s.hasNextLine()) {
			string = s.nextLine();
		}
		String firstname = string;
		
		System.out.print("Last name:");
		if (s.hasNextLine()) {
			string = s.nextLine();
		}
		String lastname = string;
		
		System.out.print("Room code:");
		if (s.hasNextLine()) {
			string = s.nextLine();
		}
		String roomcode = string;
		
		System.out.print("Bed type:");
		if (s.hasNextLine()) {
			string = s.nextLine();
		}
		String bedtype = string;

		cont = true;
		Date checkin = Date.valueOf("0000-01-01");
		while (cont) {
			System.out.print("Check-in (yyyy-mm-dd):");
			if (s.hasNextLine()) {
				string = s.nextLine();
				try {
					checkin = Date.valueOf(string);
					cont = false;
				} catch (Exception e) {}
			}
		}

		cont = true;
		Date checkout = Date.valueOf("0000-01-01");
		while (cont) {
			System.out.print("Check-out (yyyy-mm-dd):");
			if (s.hasNextLine()) {
				string = s.nextLine();
				try {
					checkout = Date.valueOf(string);
					cont = false;
				} catch (Exception e) {}
			}
		}
		
		cont = true;
		int children = 0;
		while (cont) {
			System.out.print("# of children:");
			if (s.hasNextLine()) {
				string = s.nextLine();
				try {
					children = Integer.parseInt(string);
					cont = false;
				} catch (Exception e) {}
			}
		}
		
		cont = true;
		int adults = 0;
		while (cont) {
			System.out.print("# of adults:");
			if (s.hasNextLine()) {
				string = s.nextLine();
				try {
					adults = Integer.parseInt(string);
					cont = false;
				} catch (Exception e) {}
			}
		}
		
		System.out.println(firstname);
		System.out.println(lastname);
		System.out.println(roomcode);
		System.out.println(bedtype);
		System.out.println(checkin.toString());
		System.out.println(checkout.toString());
		System.out.println(children);
		System.out.println(adults);
	}
	
	public static void reservation_change(Connection c, Scanner s) {
		System.out.println("3");
	}
	
	public static void reservation_cancellation(Connection c, Scanner s) {
		System.out.println("4");
		System.out.print("Enter reservation code:");
		if (s.hasNextLine()) {
			String code = s.nextLine();
		}
		System.out.println("Are you sure? (yes/no)");
		if (s.hasNextLine()) {
			if (s.nextLine().toLowerCase().equals("yes")) {
				//remove row
			}
		}
	}
	
	public static void detailed_reservation_information(Connection c, Scanner s) {
		System.out.println("5");
	}
	
	public static void revenue(Connection c, Scanner s) {
		System.out.println("6");
		
	}
	
}
