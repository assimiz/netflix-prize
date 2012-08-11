package dbanalysis;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;



public class FastestNetflixDB implements Serializable, NetflixDB {

	private static final long serialVersionUID = 8817643692567754106L;

	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	private DBMovie[] movies;
	
	private DBUser[] users;
	
	private ArrayList<Rent> rents;
	
	private ArrayList<Rent> questions;
	
	public FastestNetflixDB() {
		//MovieIDs range from 1 to 17770 sequentially.		
		movies = new DBMovie[17771];
		//CustomerIDs range from 1 to 2649429, with gaps. There are 480189 users.
		users = new DBUser[2649430];
		
		rents = new ArrayList<Rent>();
		questions = new ArrayList<Rent>();
	}
	
	@Override
	public DateFormat getDateFormat() {
		return DATE_FORMAT;
	}

	@Override
	public DBMovie readMovie(int movieId) {
		return movies[movieId];
	}

	@Override
	public Rent readQuestion(int movieId, int userId, Date date) {
		throw new UnsupportedOperationException("Unsupported operation");
	}

	@Override
	public Rent readRent(int movieId, int userId, Date date) {
		throw new UnsupportedOperationException("Unsupported operation");
	}

	@Override
	public DBUser readUser(int userId) {
		return users[userId];
	}

	@Override
	public void writeMovie(DBMovie movie) {
		movies[movie.getId()] = movie;
	}

	@Override
	public void writeQuestion(Rent unratedRent) {
		questions.add(unratedRent);
	}

	@Override
	public void writeRent(Rent rent) {
		rents.add(rent);
		users[rent.getUserId()].addRent(rent);
		movies[rent.getMovieId()].addRent(rent);
	}

	@Override
	public void writeUser(DBUser user) {
		users[user.getId()] = user;
	}

	@Override
	public DBMovie[] getAllMovies() {
		return movies;
	}

	@Override
	public DBUser[] getAllUsers() {
		ArrayList<DBUser> nonNull = new ArrayList<DBUser>(10000);
		for (DBUser user : users) {
			if (user != null) {
				nonNull.add(user);
			}
		}
		return nonNull.toArray(new DBUser[0]);
	}
	
	@Override
	public int getNumberOfMovies() {
		return movies.length;
	}

	@Override
	public int getNumberOfUsers() {
		int nonNull = 0;
		for (DBUser user : users) {
			if (user != null) {
				nonNull++;
			}
		}
		return nonNull;
	}

	@Override
	public Iterator<Rent> rentsIterator() {
		return rents.iterator();
	}	

}
