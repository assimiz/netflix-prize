package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;

import dbanalysis.DBMovie;
import dbanalysis.DBUser;
import dbanalysis.FastNetflixDB;
import dbanalysis.FastestNetflixDB;
import dbanalysis.NetflixAnalyzer;
import dbanalysis.NetflixDB;
import dbanalysis.Rent;

public class Utilities {

	/**
	 * Takes a questions file name and remove the questions that has no mention in
	 * a specified {@link NetflixDB}, e.g. question about a movie or user that do
	 * exist in the database.
	 * 
	 * @param baseQuestionsFileName the questions file name
	 * @param fastNetflixDbFileName the Netflix DB file name.
	 */
	public static void shrinkQuestionsFile(String baseQuestionsFileName, String fastNetflixDbFileName) {
		NetflixDB db = NetflixAnalyzer.loadDB(fastNetflixDbFileName);
		File dbFile = new File(fastNetflixDbFileName);
		
		shrinkQuestionsFile(baseQuestionsFileName, dbFile.getName() + "_probe_shrinked.txt", db);
	}
	
	/**
	 * Takes a questions file name and remove the questions that has no mention in
	 * a specified {@link NetflixDB}, e.g. question about a movie or user that do
	 * exist in the database.
	 * 
	 * @param baseQuestionsFileName the questions file name
	 * @param db the database
	 */
	public static void shrinkQuestionsFile(String baseQuestionsFileName, String destQuestionsFileName, NetflixDB db) {
		
		File destQuestionsFile = new File(destQuestionsFileName);
		
		try {
			FileWriter writer = new FileWriter(destQuestionsFile);

			QuestionsFileIterator iter = new QuestionsFileIterator(baseQuestionsFileName);
			int currentMovieId = -1;
			Rent rent;
			String movieIdSuffix = ":";
			String endOfLine = System.getProperty("line.separator");
			StringBuilder builder = new StringBuilder();
			while(iter.hasNext()) {
				rent = iter.next();
				if(rent.getMovieId() != currentMovieId) {
					currentMovieId = rent.getMovieId();
					if (db.readMovie(currentMovieId) != null) {
						String toWrite = builder.toString().trim();
						if (toWrite.length() > 0 && toWrite.charAt(toWrite.length() - 1) != ':') {
							writer.write(builder.toString());
						}
						builder = new StringBuilder();
						builder.append(String.valueOf(currentMovieId) + movieIdSuffix);		
						builder.append(endOfLine);
					}
				}
				if (db.readMovie(currentMovieId) == null) {
					System.out.println("Dumping rent " + rent + ". Movie id is not in the DB");
				} else if (db.readUser(rent.getUserId()) == null) {
					System.out.println("Dumping rent " + rent + ". User id is not in the DB");					
				} else {
					builder.append(String.valueOf(rent.getUserId()));
					builder.append(endOfLine);
				}
			}
			writer.close();
		} catch (IOException e) {
			System.err.println("IOException when trying to create shrinked questions file. " + e.getMessage());
		}
	}
	
	public static void copyDB(NetflixDB source, NetflixDB dest) {
		DBUser currentUser = null;
		DBMovie currentMovie = null;
		int i = 0;
		for (DBUser user : source.getAllUsers()) {
			System.out.println("Copying user " + ++i);
			currentUser = dest.readUser(user.getId());
			if (currentUser == null) {
				currentUser = new DBUser(user.getId());
			}
			for (Rent rent : user.getRents()) {				
				currentMovie = dest.readMovie(rent.getMovieId());
				if (currentMovie == null) {
					currentMovie = new DBMovie(rent.getMovieId());
				}
				currentUser.updateRent(rent);
				currentMovie.updateRent(rent);
				dest.writeMovie(currentMovie);
				dest.writeUser(currentUser);
				dest.writeRent(rent);
			}
		}
	}
	
	public static void saveObjectToFile(Object obj, String fileName) {
		ObjectOutputStream oos;
		try {
			System.out.println("Writing db to persistent storage.");					
			File file = new File(fileName);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(obj);
			oos.close();
			System.out.println("Finished.");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void shrinkQuestionsFileTest() {
		FastNetflixDB db = new FastNetflixDB();
		
		DBUser X = new DBUser(1);
		DBUser Y = new DBUser(2);
		DBUser A = new DBUser(3);
		DBUser B = new DBUser(4);
		DBUser C = new DBUser(5);
		
		db.writeUser(new DBUser(X.getId()));
		db.writeUser(new DBUser(Y.getId()));
		db.writeUser(new DBUser(A.getId()));
		db.writeUser(new DBUser(B.getId()));
		db.writeUser(new DBUser(C.getId()));
		
		db.writeMovie(new DBMovie(1));
		db.writeRent(new Rent(1, X.getId(), new Date(), 1));
		db.writeRent(new Rent(1, Y.getId(), new Date(), 2));
		db.writeRent(new Rent(1, A.getId(), new Date(), 2));
		
		db.writeMovie(new DBMovie(2));
		db.writeRent(new Rent(2, X.getId(), new Date(), 3));
		db.writeRent(new Rent(2, C.getId(), new Date(), 4));
		db.writeRent(new Rent(2, A.getId(), new Date(), 2));
		
		db.writeMovie(new DBMovie(3));
		db.writeRent(new Rent(3, X.getId(), new Date(), 3));
		db.writeRent(new Rent(3, Y.getId(), new Date(), 4));		
		db.writeRent(new Rent(3, B.getId(), new Date(), 5));
		
		String baseQuestionsFileName = System.getProperty("user.dir") + "\\src\\test\\questionsFileDemo.txt";
		String destQuestionsFileName = baseQuestionsFileName + "_shrinked.txt";
		Utilities.shrinkQuestionsFile(baseQuestionsFileName, destQuestionsFileName, db);
	}
	
	public static void main(String[] args) {
//		shrinkQuestionsFileTest();
//		String baseQuestionsFileName = "C:\\Documents and Settings\\mizrachi\\My Documents\\Assaf\\" +
//				"Private\\Master\\Thesis\\Files\\NetflixDB\\probe.txt";
//		String dbFileName = "C:\\Documents and Settings\\mizrachi\\My Documents\\Assaf\\Private\\Master\\" +
//				"Thesis\\assafm\\thesis\\Java\\Netflix\\NetflixDB_10000_Users.obj";
//		Utilities.shrinkQuestionsFile(baseQuestionsFileName, dbFileName);
		
		NetflixDB fast = NetflixAnalyzer.loadDB(args[0]);
		NetflixDB fastest = new FastestNetflixDB();
		copyDB(fast, fastest);
		saveObjectToFile(fastest, args[0] + ".fastest");
	}
}
