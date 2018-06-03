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
		
		System.out.print("First name:");
		string = s.nextLine();
		String firstname = string;
		
		System.out.print("Last name:");
		string = s.nextLine();
		String lastname = string;
		
		System.out.print("Room code:");
		string = s.nextLine();
		String roomcode = string;
		
		System.out.print("Bed type:");
		string = s.nextLine();
		String bedtype = string;

		Date checkin = Date.valueOf("0000-01-01");
		while (true) {
			System.out.print("Check-in (yyyy-mm-dd):");
			string = s.nextLine();
			try {
				checkin = Date.valueOf(string);
				break;
			} catch (Exception e) {}
		}

		Date checkout = Date.valueOf("0000-01-01");
		while (true) {
			System.out.print("Check-out (yyyy-mm-dd):");
			string = s.nextLine();
			try {
				checkout = Date.valueOf(string);
				break;
			} catch (Exception e) {}
		}
		
		int children = 0;
		while (true) {
			System.out.print("# of children:");
			string = s.nextLine();
			try {
				children = Integer.parseInt(string);
				break;
			} catch (Exception e) {}
		}

		int adults = 0;
		while (true) {
			System.out.print("# of adults:");
			string = s.nextLine();
			try {
				adults = Integer.parseInt(string);
				break;
			} catch (Exception e) {}
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
		String code = s.nextLine();
		System.out.println("Are you sure? (yes/no)");
		if (s.nextLine().toLowerCase().equals("yes")) {
			//remove row
		}
	}
	
	public static void detailed_reservation_information(Connection c, Scanner s) {
		System.out.println("5");
	}
	
	public static void revenue(Connection c, Scanner s) {
		System.out.println("6");
		String string;
		int year = 0;
		
		while (true) {
			System.out.print("Year:");
			string = s.nextLine();
			try {
				year = Integer.parseInt(string);
				break;
			} catch (Exception e) {}
		}
		
		try {
			String sql = "select room,\n" + 
					"    sum(if(month(checkout) = 1, rate * datediff(checkout, checkin), 0)) January,\n" + 
					"    sum(if(month(checkout) = 2, rate * datediff(checkout, checkin), 0)) February,\n" + 
					"    sum(if(month(checkout) = 3, rate * datediff(checkout, checkin), 0)) March,\n" + 
					"    sum(if(month(checkout) = 4, rate * datediff(checkout, checkin), 0)) April,\n" + 
					"    sum(if(month(checkout) = 5, rate * datediff(checkout, checkin), 0)) May,\n" + 
					"    sum(if(month(checkout) = 6, rate * datediff(checkout, checkin), 0)) June,\n" + 
					"    sum(if(month(checkout) = 7, rate * datediff(checkout, checkin), 0)) July,\n" + 
					"    sum(if(month(checkout) = 8, rate * datediff(checkout, checkin), 0)) August,\n" + 
					"    sum(if(month(checkout) = 9, rate * datediff(checkout, checkin), 0)) September,\n" + 
					"    sum(if(month(checkout) = 10, rate * datediff(checkout, checkin), 0)) October,\n" + 
					"    sum(if(month(checkout) = 11, rate * datediff(checkout, checkin), 0)) November,\n" + 
					"    sum(if(month(checkout) = 12, rate * datediff(checkout, checkin), 0)) December,\n" + 
					"    sum(rate * datediff(checkout, checkin)) Total\n" + 
					"from kkurashi.lab7_reservations join kkurashi.lab7_rooms\n" + 
					"on room = roomcode\n" + 
					"where year(checkout) = ?\n" + 
					"group by room\n" + 
					"order by room";
			PreparedStatement stmt = c.prepareStatement(sql);
			stmt.setInt(1, year);
			
			ResultSet result = stmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
