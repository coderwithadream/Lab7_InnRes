import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class InnReservations {
	public static void main(String[] args) {
		String dburl = "";
		String dbuser = "";
		String dbpass = "";
		try {
			Connection c = DriverManager.getConnection(dburl, dbuser, dbpass);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
