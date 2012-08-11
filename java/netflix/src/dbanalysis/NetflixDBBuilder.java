package dbanalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import common.DBFolderIterator;
import common.QuestionsFileIterator;


/**
 * Builds fast netflix DB from the set of files. User need to specify the
 * directory and the place of the questions file (not mandatory).
 * 
 * @author mizrachi
 *
 */
public class NetflixDBBuilder {
	
	//githeb test
	private static volatile int rentNum;
	
	private static volatile int questionNum;
	
	private static volatile boolean finished;

	public static void main(final String[] args) throws IOException {
		final NetflixDBBuilder builder = new NetflixDBBuilder();
		Runnable builderThread = new Runnable() {
			
			@Override
			public void run() {				
				System.out.println("Started DB building.");
				builder.buildDB(args[0], args[1], args.length == 3 ? args[2] : null);
				System.out.println("Finished DB building.");
			}
		};
		new Thread(builderThread).start();
	}

	public static void buildDB(String className, String dbFolder, String questionsFileName) {

		rentNum = 0;
		questionNum = 0;
		finished = false;
		
		NetflixDB db;
		try {
			db = (NetflixDB) Class.forName(className).newInstance();
		} catch (InstantiationException ie) {
			System.err.println("Unable to instatiate object " + className);
			return;
		} catch (IllegalAccessException iae) {
			System.err.println("Illegal when trying to instatiate object " + className);
			return;
		} catch (ClassNotFoundException cnfe) {
			System.err.println("Class not found when trying to instatiate object " + className);
			return;
		}
		Iterator<Rent> rents = new DBFolderIterator(dbFolder);
		DateFormat dateFormat = db.getDateFormat();

		Rent rent;
		DBUser user;
		DBMovie movie;

		//reading rents		
		System.out.println("Started reading rents.");
		while (rents.hasNext()) {
			rent = rents.next();
			user = db.readUser(rent.getUserId());
			if (user == null) {
				user = new DBUser(rent.getUserId());
			}
			movie = db.readMovie(rent.getMovieId());
			if (movie == null) {
				movie = new DBMovie(rent.getMovieId());
			}
			user.updateRent(rent);
			movie.updateRent(rent);
			db.writeMovie(movie);
			db.writeUser(user);
			db.writeRent(rent);
			rentNum++;
		}

		System.out.println("Finished reading rents.");
		
		if (questionsFileName != null) {
			//reading questions
			Iterator<Rent> questions = new QuestionsFileIterator(questionsFileName);
			System.out.println("Started reading questions.");
			while (questions.hasNext()) {
				rent = questions.next();
				user = db.readUser(rent.getUserId());
				if (user == null) {
					System.err.println("Could not find user with id: " + rent.getUserId());
				} else {
					user.updateQuestion(rent);
					db.writeUser(user);
				}
				movie = db.readMovie(rent.getMovieId());
				if (movie == null) {
					System.err.println("Could not find movie with id: " + rent.getMovieId());
				} else {
					movie.updateQuestion(rent);
					db.writeMovie(movie);
				}		
				questionNum++;
			}
			((QuestionsFileIterator) questions).finish();
			System.out.println("Finished reading questions.");
		}
		//now saving objects to persistent storage
		ObjectOutputStream oos;
		try {
			System.out.println("Writing db to persistent storage");
			Date currentDate = Calendar.getInstance().getTime();			
			File file = new File(dbFolder, "NetflixDB_" + 
					dateFormat.format(currentDate) +".obj");
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(db);
			oos.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finished = true;
	}
	
	public boolean isFinished() {
		return finished;
	}
}
