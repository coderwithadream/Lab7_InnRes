import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class InnReservations {
	public static void main(String[] args) {
		String dburl = "jdbc:mysql://" + args[0] + "?autoReconnect=true&verifyServerCertificate=false&useSSL=true";
		String dbuser = args[1];
		String dbpass = args[2];
		try {
			Connection c = DriverManager.getConnection(dburl, dbuser, dbpass);
			while (true) {
				prompt(c);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void prompt(Connection c) {
		System.out.println("1: Rooms and Rates");
		System.out.println("2: Reservations");
		System.out.println("3: Reservation Change");
		System.out.println("4: Reservation Cancellation");
		System.out.println("5: Detailed Reservation Information");
		System.out.println("6: Revenue");
		System.out.print("Enter #");
		Scanner s = new Scanner(System.in);
		if (s.hasNextInt()) {
			int choice = s.nextInt();
			switch (choice) {
			case 1:
				rooms_and_rates(c);
				break;
			case 2:
				reservations(c);
				break;
			case 3:
				reservation_change(c);
				break;
			case 4:
				reservation_cancellation(c);
				break;
			case 5:
				detailed_reservation_information(c);
				break;
			case 6:
				revenue(c);
				break;
			default:
				break;
			}
		}
		s.close();
	}
	
	public static void rooms_and_rates(Connection c) {
		
	}
	
	public static void reservations(Connection c) {
		
	}
	
	public static void reservation_change(Connection c) {
		
	}
	
	public static void reservation_cancellation(Connection c) {
		
	}
	
	public static void detailed_reservation_information(Connection c) {
		
	}
	
	public static void revenue(Connection c) {
		
	}
	
}
