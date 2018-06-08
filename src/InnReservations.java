import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.*;
import java.util.ArrayList;
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
		System.out.println();
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
		
		try{
			String sql = "select kkurashi.lab7_rooms.*, _ as popularity, _ as availCheckin, _ as lastStayDuration\n" +
						 "from kkurashi.lab7_rooms JOIN kkurashi.lab7_reservations\n" + 
						 "WHERE Room = RoomCode";
			System.out.println(sql);

			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet result = stmt.executeQuery();
			
			if (result.next()) {
				System.out.println("+----------+--------------------------+------+---------+--------+-----------+-------------+------------+------------+----------------+------------------+");
				System.out.printf("| %8s | %24s | %4s | %7s | %6s | %9s | %11s | %10s | %10s | %14s | %16 | \n", 
				"RoomCode", "RoomName", "Beds", "bedType", "maxOcc", "basePrice", "decor", "popularity", "availCheckin", "lastStayDuration");
				System.out.println("+----------+--------------------------+------+---------+--------+-----------+-------------+------------+------------+----------------+------------------+");
				do {
					System.out.printf("| %8s", result.getString(1));
					System.out.printf(" | %24s", result.getInt(2));
					System.out.printf(" | %4s", result.getString(3));
					System.out.printf(" | %7s", result.getString(4));
					System.out.printf(" | %6s", result.getString(5));
					System.out.printf(" | %9s", result.getString(6));
					System.out.printf(" | %11s", result.getString(7));
					System.out.printf(" | %10s", result.getString(8));
					System.out.printf(" | %10s", result.getInt(9));
					System.out.printf(" | %14s |\n", result.getInt(10));
					System.out.printf(" | %16s |\n", result.getInt(10));
				} while (result.next());

				System.out.println("+----------+--------------------------+------+---------+--------+-----------+-------------+------------+------------+----------------+------------------+");
			}
		}
		catch(SQLException e){
			e.printStackTrace();
		}

	}
	
	public static void reservations(Connection c, Scanner s) {
		System.out.println("");
		String string;
		
		System.out.print("First name:");
		string = s.nextLine();
		String firstname = string;
		
		System.out.print("Last name:");
		string = s.nextLine();
		String lastname = string;
		
		System.out.print("Room code:");
		string = s.nextLine().toLowerCase();
		if (string.equals("any")) string = "%";
		String roomcode = string;
		
		System.out.print("Bed type:");
		string = s.nextLine().toLowerCase();
		if (string.equals("any")) string = "%";
		String bedtype = string;

		Date checkin;
		while (true) {
			System.out.print("Check-in (yyyy-mm-dd):");
			string = s.nextLine();
			try {
				checkin = Date.valueOf(string);
				break;
			} catch (Exception e) {}
		}

		Date checkout;
		while (true) {
			System.out.print("Check-out (yyyy-mm-dd):");
			string = s.nextLine();
			try {
				checkout = Date.valueOf(string);
				if (checkout.after(checkin)) break;
			} catch (Exception e) {}
		}
		
		int children;
		while (true) {
			System.out.print("# of children:");
			string = s.nextLine();
			try {
				children = Integer.parseInt(string);
				break;
			} catch (Exception e) {}
		}

		int adults;
		while (true) {
			System.out.print("# of adults:");
			string = s.nextLine();
			try {
				adults = Integer.parseInt(string);
				break;
			} catch (Exception e) {}
		}
		
		try {
			Statement idstmt = c.createStatement();
			ResultSet result = idstmt.executeQuery("select max(code) + 1\n" + 
					"from kkurashi.lab7_reservations");
			result.next();
			int code = result.getInt(1);
			
			String sql = "select *\n" + 
					"from kkurashi.lab7_rooms\n" + 
					"where roomcode like ?\n" + 
					"    and bedtype like ?\n" + 
					"    and maxocc >= ?\n" + 
					"    and roomcode not in (\n" + 
					"        select distinct room\n" + 
					"        from kkurashi.lab7_reservations\n" + 
					"        where checkin between ? and date_sub(?, interval 1 day)\n" + 
					"        or date_sub(checkout, interval 1 day) between ? and ?\n" + 
					"    )";
			PreparedStatement stmt = c.prepareStatement(sql);
			stmt.setString(1, roomcode);
			stmt.setString(2, bedtype);
			stmt.setInt(3, adults + children);
			stmt.setDate(4, checkin);
			stmt.setDate(5, checkout);
			stmt.setDate(6, checkin);
			stmt.setDate(7, checkout);
			
			result = stmt.executeQuery();
			int i = 1;
			ArrayList<String> list = new ArrayList<String>();
			ArrayList<Double> pricelist = new ArrayList<Double>();
			if (result.next()) {
				System.out.println("Choose your room:");
				do {
					string = result.getString(1);
					System.out.printf("%2d: %s\n", i, string);
					string += " " + result.getString(2) + " " + result.getString(4);
					list.add(string);
					pricelist.add(result.getDouble(6));
					i++;
				} while (result.next());
			} else {
				sql = "select *, count(*) priority\n" + 
						"from (\n" + 
						"    select *\n" + 
						"    from kkurashi.lab7_rooms a\n" + 
						"    union all\n" + 
						"    select *\n" + 
						"    from kkurashi.lab7_rooms b\n" + 
						"    where decor in (\n" + 
						"        select decor\n" + 
						"        from kkurashi.lab7_rooms\n" + 
						"        where roomcode = ?\n" + 
						"    )\n" + 
						"    union all\n" + 
						"    select *\n" + 
						"    from kkurashi.lab7_rooms c\n" + 
						"    where bedtype = ?\n" + 
						"    ) rooms\n" + 
						"where maxocc >= ?\n" + 
						"    and roomcode not in (\n" + 
						"        select distinct room\n" + 
						"        from kkurashi.lab7_reservations\n" + 
						"        where checkin between ? and date_sub(?, interval 1 day)\n" + 
						"        or date_sub(checkout, interval 1 day) between ? and ?\n" + 
						"    )\n" + 
						"group by roomcode, roomname, beds, bedtype, maxocc, baseprice, decor\n" + 
						"order by priority desc\n" + 
						"limit 5";
				stmt = c.prepareStatement(sql);
				stmt.setString(1, roomcode);
				stmt.setString(2, bedtype);
				stmt.setInt(3, adults + children);
				stmt.setDate(4, checkin);
				stmt.setDate(5, checkout);
				stmt.setDate(6, checkin);
				stmt.setDate(7, checkout);
				
				result = stmt.executeQuery();
				if (result.next()) {
					System.out.println("No exact matches found.");
					System.out.println("These are our available options:");
					do {
						string = result.getString(1);
						System.out.printf("%2d: %s\n", i, string);
						string += " " + result.getString(2) + " " + result.getString(4);
						list.add(string);
						pricelist.add(result.getDouble(6));
						i++;
					} while (result.next());
				} else {
					System.out.println("No room can accomodate your request");
					return;
				}
			}
			
			int room;
			while (true) {
				System.out.print("Choice:");
				string = s.nextLine();
				try {
					room = Integer.parseInt(string);
					if (room < i && room > 0) break;
				} catch (Exception e) {}
			}
			
			double price = 0.0;
			double days = 0.0;
			LocalDate checkinDate = checkin.toLocalDate();
			LocalDate checkoutDate = checkout.toLocalDate();
			while (checkinDate.isBefore(checkoutDate)) {
				if (checkinDate.getDayOfWeek().equals(DayOfWeek.SUNDAY) || checkinDate.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
					price += pricelist.get(room - 1) * 1.1;
				} else {
					price += pricelist.get(room - 1);
				}
				checkinDate = checkinDate.plusDays(1);
				days += 1.0;
			}
			price *= 1.18;
			
			System.out.println(firstname + " " + lastname);
			System.out.println(list.get(room - 1));
			System.out.println(checkin.toString() + " to " + checkout.toString());
			System.out.println("Adults:" + adults);
			System.out.println("Children:" + children);
			System.out.printf("Cost:$%.2f\n", price);
			
			sql = "insert into `kkurashi`.`lab7_reservations`\n" + 
					"    (`CODE`, `Room`, `CheckIn`, `Checkout`, `Rate`, `LastName`, `FirstName`, `Adults`, `Kids`)\n" + 
					"    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
			stmt = c.prepareStatement(sql);
			stmt.setInt(1, code);
			stmt.setString(2, list.get(room - 1).substring(0, 3));
			stmt.setDate(3, checkin);
			stmt.setDate(4, checkout);
			stmt.setDouble(5, price / days);
			stmt.setString(6, lastname);
			stmt.setString(7, firstname);
			stmt.setInt(8, adults);
			stmt.setInt(9, children);
			
			while(true) {
				System.out.print("Confirm Reservation (confirm/cancel):");
				string = s.nextLine().toLowerCase();
				if (string.equals("confirm")) {
					int update = stmt.executeUpdate();
					if (update > 0) {
						System.out.println("Success!");
					} else {
						System.out.println("Unable to add Reservation");
					}
					break;
				}
				if (string.equals("cancel")) {
					System.out.println("Reservation Cancelled!");
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void reservation_change(Connection c, Scanner s) {
		System.out.print("To begin, please enter your reservation code: ");
		int resCode;

		while(true){

			//obtain resCode for query use
			String resCodeStr = s.nextLine();
			try {
				resCode = Integer.parseInt(resCodeStr);
			} catch (Exception e) {
				System.out.println("Error: Please provide a valid reservation number.");
				System.out.print("Please enter your reservation code: ");
				continue;
			}
			//check if resCode contains 5 digits
			if(resCode < 10000 || resCode > 99999){ //resCode is less than 5 digits OR resCode exceeds 5 digits
				System.out.println("Error: Reservation code must be 5 digits. Please try again.");
				System.out.print("Please enter your reservation code: ");
			}
			else{ //valid resCode given so continue to next prompt
				break;
			}
		}

		boolean isDone = false;
		boolean isInvalid = false;

		String firstName = "<no change>";
		String lastName = "<no change>";
		Date newBeginDate = null;
		Date newEndDate = null;
		int numChildren = -1;
		int numAdults = -1;

		//for seeing if fields are left blank
		boolean haveFirst = false;
		boolean haveLast = false;
		boolean haveBeginDate = false;
		boolean haveEndDate = false; 
		boolean haveChildren = false;
		boolean haveAdults = false;

		boolean noChange = true;

		while(!isDone){
			System.out.println("What would you like to change?");
			System.out.println("1. First name -> " + firstName);
			System.out.println("2. Last name  -> " + lastName);
			System.out.println("3. Begin date -> " + (newBeginDate == null? "<no change>" : newBeginDate));
			System.out.println("4. End date   -> " + (newEndDate == null? "<no change>" : newEndDate));
			System.out.println("5. # children -> " + (numChildren == -1? "<no change>" : numChildren));
			System.out.println("6. # adults   -> " + (numAdults == -1? "<no change>" : numAdults));
			System.out.println("7. EXIT RESERVATION CHANGE");

			System.out.print("Choose number: ");
			String numChosenStr = s.nextLine();
			int numChosen;
			try {
				numChosen = Integer.parseInt(numChosenStr);
			} catch (Exception e) {
				System.out.println("Error: Please provide a valid number from the list.\n");
				continue;
			}
			System.out.println(); //add a new line for readability

			//for user to change begin and end date
			Date resCheckIn = null;
			Date resCheckOut = null;
			String resRoom = "";
		    String dateSQL = "SELECT * from kkurashi.lab7_reservations WHERE CODE = ?;";
		    try (PreparedStatement pstmtDate = c.prepareStatement(dateSQL)) {
		    	pstmtDate.setInt(1, resCode);

				ResultSet resultForDate = pstmtDate.executeQuery(); // Send SQL statement to DBMS
				if(resultForDate.next()){
					resRoom = resultForDate.getString(2); //Room under reservation
			    	resCheckIn = resultForDate.getDate(3); //curr checkIn for res
					resCheckOut = resultForDate.getDate(4); //curr checkOut for res
				}
		    }
		    catch(SQLException e){
		    	e.printStackTrace();
		    }

			//for querying currently reserved dates 
			// FOR TESTING: use res CODE 49886
			// -2019-01-12 and any later date will allow user to change checkIn,
			// -and almost any date before will result in date conflicts for user
			String newSQl = "SELECT * from kkurashi.lab7_reservations\n" +
							"WHERE ? >= CheckIn AND ? < Checkout\n" +
							"AND CODE != ?\n" + //exclude users own reservation from search
							"AND Room = '" + resRoom + "';"; //only select room that person wants to change check-in/check-out for

			PreparedStatement pstmt;
			ResultSet result;

			String newString = "";
			switch(numChosen){
				case 1:
					System.out.print("New first name:");
					newString = s.nextLine();
					firstName = newString;
					break;
				case 2:
					System.out.print("New last name:");
					newString = s.nextLine();
					lastName = newString;
					break;
				case 3:
					while(true){
						System.out.println("Current check-in date: " + resCheckIn);
						System.out.println("Current check-out date: " + resCheckOut);
						System.out.print("New begin date (yyyy-mm-dd):");
						newString = s.nextLine();
						try {
							newBeginDate = Date.valueOf(newString);
							if(newBeginDate.after(resCheckOut)){ //check if newEndDate comes before current CheckIn
								System.out.println("Error: Your check-in date can not be after your check-out date. Change check-out date first.\n");
								newBeginDate = null;
								isInvalid = true;
								break;
							}
							pstmt = c.prepareStatement(newSQl);
							pstmt.setDate(1, newBeginDate);
							pstmt.setDate(2, newBeginDate);
							pstmt.setInt(3, resCode);
							result = pstmt.executeQuery();
							if(result.next()){ //if queries with matching dates were found
								System.out.println("Date conflicts with current reservation in our system. Please try again.");
								newBeginDate = null;
								isInvalid = true;
							}
							else{//there are no current res with date, so allow user to use date
								break;
							}
						} catch (Exception e) {
							System.out.println("Please provide a valid date in the proper format.");
						}
					}
					break;
				case 4:
					while(true){
						System.out.println("Current check-in date: " + resCheckIn);
						System.out.println("Current check-out date: " + resCheckOut);
						System.out.print("New end date (yyyy-mm-dd):");
						newString = s.nextLine();
						try {
							newEndDate = Date.valueOf(newString);
							if(!newEndDate.after(resCheckIn)){ //check if newEndDate comes before current CheckIn
								System.out.println("Error: Your check-out date can not be before your check-in date. Change check-in date first.");
								newEndDate = null;
								isInvalid = true;
								break;
							}

							pstmt = c.prepareStatement(newSQl);
							pstmt.setDate(1, newEndDate);
							pstmt.setDate(2, newEndDate);
							pstmt.setInt(3, resCode);
							result = pstmt.executeQuery();
							if(result.next()){ //if queries with matching dates were found
								newEndDate = null;
								System.out.println("Date conflicts with current reservation in our system. Please try again.");
								isInvalid = true;
							}
							else{//there are no current res with date, so allow user to use date
								System.out.println();
								break;
							}
						} catch (Exception e) {
							System.out.println("Please provide a valid date in the proper format.");
						}
					}
					break;
				case 5:
					while(true){
						System.out.print("New # of children:");
						newString = s.nextLine();
						try {
							numChildren = Integer.parseInt(newString);
							break;
						} catch (Exception e) {
							System.out.println("Please provide a valid number.");
						}
					}
					break;
				case 6:
					while(true){
						System.out.print("New # of adults:");
						newString = s.nextLine();
						try {
							numAdults = Integer.parseInt(newString);
							break;
						} catch (Exception e) {
							System.out.println("Please provide a valid number.");
						}
					}
					break;
				case 7:
					isDone = true; //doesn't do anything but for readability
					break; //break out of main loop and return to initial prompt
				default:
					System.out.print("Error: Invalid number. Please try again.");
					isInvalid = true;
					break; //prompt user for res change
			}

			if(isDone){ //break out of main loop back to init prompt
				break;
			}
			else if(isInvalid){
				isInvalid = false;
				continue;
			}

			while(true){
				System.out.print("Are you sure you want to make this change: '" + newString + "'? " + 
								 "(No will exit reservation change)\n(Y)es/(N)o: ");
				String response = s.nextLine();
				String lcResponse = response.toLowerCase(); //input is NOT case sensitive
				if(lcResponse.equals("y") || lcResponse.equals("yes")){
					noChange = false;
					break;
				}
				else if(lcResponse.equals("n") || lcResponse.equals("no")){
					isDone = true; //end the main outer loop and return to initial prompt
					System.out.println("Reservation change has been cancelled.");
					break;
				}
				else{
					System.out.println("Error: Invalid input. Please try again.");
				}
			}

			if(isDone) //break out of main loop back to init prompt
				return;

			while(true){
				System.out.print("Would you like to make an additional change?\n(Y)es/(N)o: ");
				String answer = s.nextLine();
				String lcAnswer = answer.toLowerCase(); //input is NOT case sensitive
				if(lcAnswer.equals("y") || lcAnswer.equals("yes")){
					System.out.println("");
					isDone = false; //continue the main outer loop
					break;
				}
				else if(lcAnswer.equals("n") || lcAnswer.equals("no")){
					System.out.println("\nReservation has been updated. Thank you :)");
					isDone = true; //end the main outer loop and return to initial prompt
					break;
				}
				else{
					System.out.println("Error: Invalid input. Please try again.");
				}
			}

			System.out.println(); //new line for readability
		}

		//update user reservation in database
		try {

			String firstNameQuery = "FirstName = ?";
			String lastNameQuery = ",\n    lastName = ?";
			String beginDateQuery = ",\n    CheckIn = ?";
			String endDateQuery = ",\n    Checkout = ?";
			String numChildQuery = ",\n    Kids = ?";
			String numAdultQuery = ",\n    Adults = ?";

			//sql for reservation update
			String sql1 = "UPDATE kkurashi.lab7_reservations\n" +
						  "SET " +
						  ((!firstName.equals("<no change>"))? firstNameQuery : "FirstName=FirstName") + 
						  ((!lastName.equals("<no change>"))? lastNameQuery : "") +
						  ((newBeginDate != null)? beginDateQuery : "") +  
						  ((newEndDate != null)? endDateQuery : "") + 
						  ((numChildren != -1)? numChildQuery : "") +
						  ((numAdults != -1)? numAdultQuery : "") +
						  "\nWHERE CODE = ?;";

			PreparedStatement stmt1 = c.prepareStatement(sql1);

			//sql for updated record
			String sql2 = "SELECT * from kkurashi.lab7_reservations\n" +
						  "WHERE CODE = ?;";		 

			//if user for some reason was able to make it here without making a change, leave res change
			if(noChange){
				return;
			}

			int queryPointer = 1; //start at 1
			//check if fields were left blank and if not, add to sql statement
			if(!firstName.equals("<no change>"))
				stmt1.setString(queryPointer++, firstName);
			if(!lastName.equals("<no change>"))
				stmt1.setString(queryPointer++, lastName);
			if(!(newBeginDate == null))
				stmt1.setDate(queryPointer++, newBeginDate);
			if(!(newEndDate == null))
				stmt1.setDate(queryPointer++, newEndDate);
			if(!(numChildren == -1))
				stmt1.setInt(queryPointer++, numChildren);
			if(!(numAdults == -1))
				stmt1.setInt(queryPointer++, numAdults);

			stmt1.setInt(queryPointer++, resCode); //add conditional for record update

			//update reservation
			int rowsUpdated = stmt1.executeUpdate();
			
			//get newly updated record in reservations for user to see
			PreparedStatement pstmt2 = c.prepareStatement(sql2);
			pstmt2.setInt(1, resCode);
			ResultSet result2 = pstmt2.executeQuery();

			System.out.println("UPDATED RESERVATION:");
			System.out.println("+-------+------+------------+------------+---------+---------------+------------+--------+------+");
			System.out.printf("| %5s | %4s | %10s | %10s | %7s | %13s | %10s | %6s | %4s |\n", 
			"CODE", "Room", "CheckIn", "Checkout", "Rate", "LastName", "FirstName", "Adults", "Kids");
			System.out.println("+-------+------+------------+------------+---------+---------------+------------+--------+------+");
			if(result2.next()){
				System.out.printf("| %5d", result2.getInt(1));
				System.out.printf(" | %4s", result2.getString(2));
				System.out.printf(" | %10s", result2.getString(3));
				System.out.printf(" | %10s", result2.getString(4));
				System.out.printf(" | %7s", result2.getString(5));
				System.out.printf(" | %13s", result2.getString(6));
				System.out.printf(" | %10s", result2.getString(7));
				System.out.printf(" | %6d", result2.getInt(8));
				System.out.printf(" | %4d |\n", result2.getInt(9));
			}
			System.out.println("+-------+------+------------+------------+---------+---------------+------------+--------+------+");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public static void reservation_cancellation(Connection c, Scanner s) {
		String string;
		int code;
		
		while (true) {
			System.out.print("Enter reservation code:");
			string = s.nextLine();
			try {
				code = Integer.parseInt(string);
				break;
			} catch (Exception e) {}
		}
		
		while(true) {
			System.out.print("Are you sure? (yes/no):");
			string = s.nextLine().toLowerCase();
			if (string.equals("yes")) {
				String sql = "delete from kkurashi.lab7_reservations where code = ?";
				try {
					PreparedStatement stmt = c.prepareStatement(sql);
					stmt.setInt(1, code);
					if (stmt.executeUpdate() == 0) {
						System.out.println("No such reservation.");
					} else {
						System.out.println("Reservation cancelled.");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				break;
			}
			if (string.equals("no")) {
				break;
			}
		}
	}
	
		public static void detailed_reservation_information(Connection c, Scanner s) {
		System.out.println("5");
		boolean isDone = false;
		boolean isInvalid = false;

		String firstName = "<Any>";
		String lastName = "<Any>";
		String dateRange = "<Any>";
		String roomCode = "<Any>";
		int resCode = -1;

		//for both dates in date range
		String[] splitDates = new String[2];

		while(!isDone){
			System.out.println("What would you like to search for?");
			System.out.println("1. First name: " + firstName);
			System.out.println("2. Last name: " + lastName);
			System.out.println("3. Range of dates: " + dateRange);
			System.out.println("4. Room code (i.e. AOB): " + roomCode);
			System.out.println("5. Reservation code: " + (resCode == -1? "<Any>" : resCode));
			System.out.println("6. Clear search fields");
			System.out.println("7. Search reservations with given fields");
			System.out.println("8. EXIT RESERVATION SEARCH");


			System.out.print("Choose number: ");
			String numChosenStr = s.nextLine();
			int numChosen;
			try {
				numChosen = Integer.parseInt(numChosenStr);
			} catch (Exception e) {
				System.out.println("Error: Please provide a valid number from the list.\n");
				continue;
			}
			System.out.println(); //add a new line for readability
			
			String newString = "";
			switch(numChosen){
				case 1:
					System.out.print("First name:");
					firstName = s.nextLine();
					break;
				case 2:
					System.out.print("New last name:");
					lastName = s.nextLine();
					break;
				case 3:
					System.out.print("Search Date Range (yyyy-mm-dd:yyyy-mm-dd):");
					dateRange = s.nextLine();
					if(dateRange.contains(":")){
						splitDates = dateRange.split(":");
						try{
							Date testDate1 = Date.valueOf(splitDates[0]);
							Date testDate2 = Date.valueOf(splitDates[1]);
						}catch(Exception e){
							dateRange = "<Any>";
							System.out.println("Error: Please provide a valid range of dates\n");
							break;
						}
						System.out.println("beginDate: " + splitDates[0]);
						System.out.println("endDate: " + splitDates[1]);
					}
					else{
						dateRange = "<Any>";
						System.out.println("Error: Please provide a valid range of dates.\n");
					}
					break;
				case 4:
					// while(true){
					// 	System.out.print("Search room code:");
					// 	roomCode = s.nextLine();
					// 	if(roomCode.length() == 3){
					// 		break;
					// 	}
					// 	else{
					// 		System.out.println("Please provide a valid room code (i.e. AOB)");
					// 	}
					// }
					System.out.print("Search room code:");
					roomCode = s.nextLine();
					break;
				case 5:
					while(true){
						System.out.print("Search reservation code:");
						newString = s.nextLine();
						try {
							resCode = Integer.parseInt(newString);
							break;
						} catch (Exception e) {
							System.out.println("Please provide a valid 5-digit reservation number.");
						}
					}
					break;
				case 6:
					firstName = "<Any>";
					lastName = "<Any>";
					dateRange = "<Any>";
					roomCode = "<Any>";
					resCode = -1;
					break;
				case 7:
					//---------------------------------------------------------
					//query specified tables based on the different search fields
					try {
						String dateQuery = "AND ((CheckIn BETWEEN ? AND ?)\n" + //5, 6
								     	   "    OR (Checkout BETWEEN ? AND ?));"; //7, 8

						String sql = "select kkurashi.lab7_rooms.RoomName, kkurashi.lab7_reservations.*\n" +
									 "from kkurashi.lab7_rooms JOIN kkurashi.lab7_reservations\n" + 
									 "WHERE Room = RoomCode\n" +
									 "AND FirstName LIKE ?\n" + //1
									 "AND LastName LIKE ?\n" +  //2
									 "AND RoomCode LIKE ?\n" + //3
									 "AND CONVERT(CODE, CHAR(5)) LIKE ?\n" //4
								 	 + ((!dateRange.equals("<Any>"))? dateQuery : "");

						PreparedStatement stmt = c.prepareStatement(sql);

						//check if fields were left blank and make sql statement accordingly
						if(!firstName.equals("<Any>"))
							stmt.setString(1, firstName + "%");
						else
							stmt.setString(1, "%");

						// sql += "\nAND FirstName LIKE '" + firstName + "%'";

						if(!lastName.equals("<Any>"))
							stmt.setString(2, lastName + "%");
						else
							stmt.setString(2, "%");
							// sql += "\nAND LastName LIKE '" + lastName + "%'";

						if(!roomCode.equals("<Any>"))
							stmt.setString(3, roomCode + "%");
						else
							stmt.setString(3, "%");
						// sql += "\nAND RoomCode LIKE '" + roomCode + "%'";

						if(!(resCode == -1))
							stmt.setString(4, resCode + "%");
						else
							stmt.setString(4, "%");
						// sql += "\nAND CONVERT(CODE, CHAR(5)) LIKE '" + resCode + "%'";

						if(!dateRange.equals("<Any>")){
							String startDate = splitDates[0];
							String endDate = splitDates[1];

							stmt.setString(5, startDate);
							stmt.setString(6, endDate);
							stmt.setString(7, startDate);
							stmt.setString(8, endDate);
							// sql += "\nAND ((CheckIn BETWEEN '" + startDate + "' AND '" + endDate + "')" +
							// 	   	"\n    OR (Checkout BETWEEN '" + startDate + "' AND '" + endDate + "'))";
						}

						// PreparedStatement stmt = c.prepareStatement(sql);
						ResultSet result = stmt.executeQuery();
						
						if (result.next()) {
							System.out.println("+--------------------------+-------+------+------------+------------+---------+---------------+------------+--------+------+");
							System.out.printf("| %24s | %5s | %4s | %10s | %10s | %7s | %13s | %10s | %6s | %4s |\n", 
							"RoomName", "CODE", "Room", "CheckIn", "Checkout", "Rate", "LastName", "FirstName", "Adults", "Kids");
							System.out.println("+--------------------------+-------+------+------------+------------+---------+---------------+------------+--------+------+");
							do {
								System.out.printf("| %24s", result.getString(1));
								System.out.printf(" | %5d", result.getInt(2));
								System.out.printf(" | %4s", result.getString(3));
								System.out.printf(" | %10s", result.getString(4));
								System.out.printf(" | %10s", result.getString(5));
								System.out.printf(" | %7s", result.getString(6));
								System.out.printf(" | %13s", result.getString(7));
								System.out.printf(" | %10s", result.getString(8));
								System.out.printf(" | %6d", result.getInt(9));
								System.out.printf(" | %4d |\n", result.getInt(10));
							} while (result.next());

							System.out.println("+--------------------------+-------+------+------------+------------+---------+---------------+------------+--------+------+");
						}
						else{
							System.out.println("No results found.");
						}

					} catch (SQLException e) {
						e.printStackTrace();
					}
					System.out.println("Search complete.\n");
					//---------------------------------------------------------

					//check to see if user wants to perform another search
					while(true){
						System.out.print("Would you like to perform another search?\n(Y)es/(N)o?");
						String answer = s.nextLine();
						String lcAnswer = answer.toLowerCase(); //input is NOT case sensitive
						if(lcAnswer.equals("y") || lcAnswer.equals("yes")){
							isDone = false; //continue the main outer loop
							break;
						}
						else if(lcAnswer.equals("n") || lcAnswer.equals("no")){
							System.out.println("Reservation has been updated. Thank you :)");
							isDone = true; //end the main outer loop and return to initial prompt
							break;
						}
						else{
							System.out.println("Error: Invalid input. Please try again.");
						}
					}

					break;
				case 8:
					isDone = true; //doesn't do anything but for readability
					break; //break out of main loop and return to initial prompt
				default:
					System.out.println("Error: Invalid number. Please try again.");
					isInvalid = true;
					break; //prompt user for res change
			}

			if(isDone){ //break out of main loop back to init prompt
				break;
			}
			else if(isInvalid){
				isInvalid = false;
				continue;
			}

		}
	}
	
	
	public static void revenue(Connection c, Scanner s) {
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
					"    round(sum(if(month(checkout) = 1, rate * datediff(checkout, checkin), 0)), 0) January,\n" + 
					"    round(sum(if(month(checkout) = 2, rate * datediff(checkout, checkin), 0)), 0) February,\n" + 
					"    round(sum(if(month(checkout) = 3, rate * datediff(checkout, checkin), 0)), 0) March,\n" + 
					"    round(sum(if(month(checkout) = 4, rate * datediff(checkout, checkin), 0)), 0) April,\n" + 
					"    round(sum(if(month(checkout) = 5, rate * datediff(checkout, checkin), 0)), 0) May,\n" + 
					"    round(sum(if(month(checkout) = 6, rate * datediff(checkout, checkin), 0)), 0) June,\n" + 
					"    round(sum(if(month(checkout) = 7, rate * datediff(checkout, checkin), 0)), 0) July,\n" + 
					"    round(sum(if(month(checkout) = 8, rate * datediff(checkout, checkin), 0)), 0) August,\n" + 
					"    round(sum(if(month(checkout) = 9, rate * datediff(checkout, checkin), 0)), 0) September,\n" + 
					"    round(sum(if(month(checkout) = 10, rate * datediff(checkout, checkin), 0)), 0) October,\n" + 
					"    round(sum(if(month(checkout) = 11, rate * datediff(checkout, checkin), 0)), 0) November,\n" + 
					"    round(sum(if(month(checkout) = 12, rate * datediff(checkout, checkin), 0)), 0) December,\n" + 
					"    round(sum(rate * datediff(checkout, checkin)), 0) Total\n" + 
					"from kkurashi.lab7_reservations join kkurashi.lab7_rooms\n" + 
					"on room = roomcode\n" + 
					"where year(checkout) = ?\n" + 
					"group by room\n" + 
					"order by room";
			PreparedStatement stmt = c.prepareStatement(sql);
			stmt.setInt(1, year);
			
			ResultSet result = stmt.executeQuery();
			
			if (result.next()) {
				int[] totals = new int[13];
				int revenue;
				System.out.printf("%5s | %9s | %9s | %9s | %9s | %9s | %9s | %9s | %9s | %9s | %9s | %9s | %9s | %9s\n",
						"Room", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December", "Year");
				do {
					System.out.printf("%5s", result.getString(1));
					for (int i = 2; i <= 14; i++) {
						revenue = result.getInt(i);
						totals[i - 2] += revenue;
						System.out.printf(" | %9d", revenue);
					}
					System.out.println();
				} while (result.next());
				System.out.printf("%5s", "Total");
				for (int i = 0; i <= 12; i++) {
					System.out.printf(" | %9d", totals[i]);
				}
				System.out.println();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
