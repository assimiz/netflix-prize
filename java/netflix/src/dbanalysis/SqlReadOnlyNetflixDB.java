package dbanalysis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * SQL based DB proxy. DB is in read-only mode. Any attempt to use manipulating (e.g. write...)
 * methods will throw exception. Heavy performance methods (e.g. getAll...) are also unimplemented
 * and will throw exception.
 * 
 * Class was tested on mySql DB. Proxy assumes root user with no password.
 * 
 * @author mizrachi
 *
 */
public class SqlReadOnlyNetflixDB implements NetflixDB {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	private static final String USER_ID_COL = "userid";
	
	private static final String MOVIE_ID_COL = "movieid";
	
	private static final String RATING_COL = "rating";
	
	private static final String DATE_COL = "date";

	private Connection connection = null;
	
	private boolean includeProbeData;
	
	private String ratingTableName;
	
	/**
	 * Creates SQL DB Proxy with or without probe data. One may like probe data
	 * out of the results while generating recommendations in order not to 
	 * "unfairly" improve his accuracy. On the other hand, he may like it in when
	 * analyzing the DB or when checking his results for the probe data.
	 * 
	 * @param includeProbeData <code>true</code> if probe data should be included in the
	 * results, <code>false</code>, otherwise.
	 */
	public SqlReadOnlyNetflixDB(boolean includeProbeData) {
		// The newInstance() call is a work around for some
		// broken Java implementations
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException ex) {
			System.err.println("InstantiationException while tryinh to create JDBC driver: " + ex.getMessage());
		} catch (IllegalAccessException ex) {
			System.err.println("IllegalAccessException while tryinh to create JDBC driver: " + ex.getMessage());
		} catch (ClassNotFoundException ex) {
			System.err.println("ClassNotFoundException while tryinh to create JDBC driver: " + ex.getMessage());
		}

		try {
			connection =
				DriverManager.getConnection("jdbc:mysql://localhost/netflix?" +
				"user=root&password=");
		} catch (SQLException ex) {
			// handle any errors
			System.err.println("SQLException while tryinh to connect to netflix database: " + ex.getMessage());
			System.err.println("SQLState while tryinh to connect to netflix database: " + ex.getSQLState());
			System.err.println("VendorError while trying to connect to netflix database: " + ex.getErrorCode());
		}
		this.includeProbeData = includeProbeData;
		ratingTableName = includeProbeData ? "rating_all " : "rating ";
	}

	@Override
	public DBMovie[] getAllMovies() {
		throw new UnsupportedOperationException("Operation takes too long time. Use iteration over all movies instead (not recommended).");
	}

	@Override
	public DBUser[] getAllUsers() {
		throw new UnsupportedOperationException("Operation takes too long time. Use iteration over all users instead (not recommended).");
	}
	
	@Override
	public int getNumberOfMovies() {
		throw new UnsupportedOperationException("Operation takes too long time. Use iteration over all users instead (not recommended).");
	}
	
	@Override
	public int getNumberOfUsers() {
		throw new UnsupportedOperationException("Operation takes too long time. Use iteration over all users instead (not recommended).");
	}

	@Override
	public DateFormat getDateFormat() {
		return DATE_FORMAT;
	}

	@Override
	public DBMovie readMovie(int movieId) {
		Statement stmt = null;
		ResultSet rs = null;
		DBMovie movie = null;
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT movieid, userid, rating, date FROM " + ratingTableName + " WHERE movieid = " + movieId);
			movie = createMovie(movieId, rs);
		}
		catch (SQLException ex){
			// handle any errors
			System.err.println("SQLException: " + ex.getMessage());
			System.err.println("SQLState: " + ex.getSQLState());
			System.err.println("VendorError: " + ex.getErrorCode());
		}
		finally {
			// it is a good idea to release
			// resources in a finally{} block
			// in reverse-order of their creation
			// if they are no-longer needed
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) { } // ignore
				rs = null;
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) { } // ignore
				stmt = null;
			}
		}
		return movie;
	}

	@Override
	public Rent readQuestion(int movieId, int userId, Date date) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Rent readRent(int movieId, int userId, Date date) {
		Statement stmt = null;
		ResultSet rs = null;
		Rent rent = null;
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT movieid, userid, rating, date FROM " + ratingTableName + " WHERE " +
					"movieid = " + movieId + " AND " +
					"userid = " + userId);
			if (!rs.isAfterLast()) {
				if (rs.next()) {
					rent = new Rent(rs.getInt(MOVIE_ID_COL), 
							rs.getInt(USER_ID_COL), 
							rs.getDate(DATE_COL), 
							rs.getInt(RATING_COL));
				}
			}
		}
		catch (SQLException ex){
			// handle any errors
			System.err.println("SQLException: " + ex.getMessage());
			System.err.println("SQLState: " + ex.getSQLState());
			System.err.println("VendorError: " + ex.getErrorCode());
		}
		finally {
			// it is a good idea to release
			// resources in a finally{} block
			// in reverse-order of their creation
			// if they are no-longer needed
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) { } // ignore
				rs = null;
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) { } // ignore
				stmt = null;
			}
		}		
		return rent;
	}

	@Override
	public DBUser readUser(int userId) {
		Statement stmt = null;
		ResultSet rs = null;
		DBUser user = null;
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT movieid, userid, rating, date FROM " + ratingTableName + " WHERE userid = " + userId);
			user = createUser(userId, rs);
		}
		catch (SQLException ex){
			// handle any errors
			System.err.println("SQLException: " + ex.getMessage());
			System.err.println("SQLState: " + ex.getSQLState());
			System.err.println("VendorError: " + ex.getErrorCode());
		}
		finally {
			// it is a good idea to release
			// resources in a finally{} block
			// in reverse-order of their creation
			// if they are no-longer needed
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) { } // ignore
				rs = null;
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) { } // ignore
				stmt = null;
			}
		}
		return user;
	}	

	@Override
	public Iterator<Rent> rentsIterator() {
		Statement stmt = null;
		ResultSet rs = null;
		boolean success = false;
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT movieid, userid, rating, date FROM " + ratingTableName);
			success = true;
		}
		catch (SQLException ex){
			// handle any errors
			System.err.println("SQLException: " + ex.getMessage());
			System.err.println("SQLState: " + ex.getSQLState());
			System.err.println("VendorError: " + ex.getErrorCode());
		}
		finally {
			// it is a good idea to release
			// resources in a finally{} block
			// in reverse-order of their creation
			// if they are no-longer needed
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) { } // ignore
				rs = null;
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) { } // ignore
				stmt = null;
			}
		}
		if (success) {
			return new RentsIterator(rs);
		} else {
			return null;
		}
	}

	@Override
	public void writeMovie(DBMovie movie) {
		throw new UnsupportedOperationException("DB is read only.");
	}

	@Override
	public void writeQuestion(Rent unratedRent) {
		throw new UnsupportedOperationException("DB is read only.");
	}

	@Override
	public void writeRent(Rent rent) {
		throw new UnsupportedOperationException("DB is read only.");
	}

	@Override
	public void writeUser(DBUser user) {
		throw new UnsupportedOperationException("DB is read only.");
	}
	
	private static DBMovie createMovie(int movieId, ResultSet rs) {
		DBMovie movie = new DBMovie(movieId);
		try {
			while (rs.next()) {					
				Rent rent = new Rent(rs.getInt(MOVIE_ID_COL), 
						rs.getInt(USER_ID_COL), 
						rs.getDate(DATE_COL), 
						rs.getInt(RATING_COL));
				movie.addRent(rent);
				movie.updateRent(rent);
			}
		} catch (SQLException e) {
			System.err.println("DataBase error when trying to create movie " + movieId + ". " + e.getMessage());
		}
		return movie;
	}

	private static DBUser createUser(int userId, ResultSet rs) {
		DBUser user = new DBUser(userId);
		try {
			while (rs.next()) {					
				Rent rent = new Rent(rs.getInt(MOVIE_ID_COL), 
						rs.getInt(USER_ID_COL), 
						rs.getDate(DATE_COL), 
						rs.getInt(RATING_COL));
				user.addRent(rent);
				user.updateRent(rent);				
			}
		} catch (SQLException e) {
			System.err.println("DataBase error when trying to create user " + userId + ". " + e.getMessage());
		}
		return user;
	}

	private static class RentsIterator implements Iterator<Rent> {
	
		private ResultSet rs;
		
		public RentsIterator(ResultSet rs) {
			this.rs = rs;
		}
		
		@Override
		public boolean hasNext() {
			try {
				return rs.isAfterLast();
			} catch (SQLException e) {
				System.err.println("DataBase error when trying to check for next entry. " + e.getMessage());
				return false;
			}
		}

		@Override
		public Rent next() {
			try {
				if(rs.next()) {
					return new Rent(rs.getInt(MOVIE_ID_COL), 
							rs.getInt(USER_ID_COL), 
							rs.getDate(DATE_COL), 
							rs.getInt(RATING_COL));
				} else {
					System.err.println("No more rows when trying to get next entry");
					return null;
				}
			} catch (SQLException e) {
				System.err.println("DataBase error when trying to check for next entry. " + e.getMessage());
				return null;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
