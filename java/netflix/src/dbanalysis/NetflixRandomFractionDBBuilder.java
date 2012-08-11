package dbanalysis;

import java.util.Calendar;
import java.util.Date;

import common.Utilities;

/**
 * Builds a fraction fast DB from the full Sql DB. The builder chooses
 * random users up to a configurable quota. Then it builds a fast DB
 * from the subset of the users and their rents. 
 * 
 * @author mizrachi
 *
 */
public class NetflixRandomFractionDBBuilder {

	//CustomerIDs range from 1 to 2649429, with gaps.
	private static final int MAX_USER_ID = 2649429;
	
	private int numOfUsers;
	
	private SqlReadOnlyNetflixDB sqlDb;
	
	private NetflixDB db;

	/**
	 * @param numOfUsers
	 * @param sqlDb
	 * @param fastDb
	 */
	public NetflixRandomFractionDBBuilder(int numOfUsers,
			SqlReadOnlyNetflixDB sqlDb, NetflixDB fastDb) {
		this.numOfUsers = numOfUsers;
		this.sqlDb = sqlDb;
		this.db = fastDb;
	}
	
	public void buildDb() {
		for (int i = 0; i < numOfUsers; i++) {
			System.out.println("Creating user " + i);
			int userId;
			DBUser randomUser = null;
			
			do {
				userId = (int) Math.round(Math.random() * (MAX_USER_ID - 1));
				randomUser = sqlDb.readUser(userId);
			}
			//user with no rents is actually does not exist 
			while (randomUser.getNumOfRents() == 0 || db.readUser(userId) != null);
			
			DBUser currentUser = null;
			DBMovie currentMovie = null;			
			//we actually do not use the user we got but rebuild it along with its rents and
			//related movies.
			for (Rent rent : randomUser.getRents()) {
				currentUser = db.readUser(rent.getUserId());
				if (currentUser == null) {
					currentUser = new DBUser(rent.getUserId());
				}
				currentMovie = db.readMovie(rent.getMovieId());
				if (currentMovie == null) {
					currentMovie = new DBMovie(rent.getMovieId());
				}
				currentUser.updateRent(rent);
				currentMovie.updateRent(rent);
				db.writeMovie(currentMovie);
				db.writeUser(currentUser);
				db.writeRent(rent);
			}
		}
	}
	
	public static void main(String[] args) {
		NetflixDB db;
		try {
			db = (NetflixDB) Class.forName(args[1]).newInstance();
		} catch (InstantiationException ie) {
			System.err.println("Unable to instatiate object " + args[0]);
			return;
		} catch (IllegalAccessException iae) {
			System.err.println("Illegal when trying to instatiate object " + args[0]);
			return;
		} catch (ClassNotFoundException cnfe) {
			System.err.println("Class not found when trying to instatiate object " + args[0]);
			return;
		}
		NetflixRandomFractionDBBuilder builder = new NetflixRandomFractionDBBuilder(Integer.valueOf(args[0]), new SqlReadOnlyNetflixDB(false),
				db);
		builder.buildDb();
		
		//now saving objects to persistent storage
		Date currentDate = Calendar.getInstance().getTime();
		Utilities.saveObjectToFile(db, "NetflixDB_" + 
				db.getDateFormat().format(currentDate) +".obj");
	}
}
