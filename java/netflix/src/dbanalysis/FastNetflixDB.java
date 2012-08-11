package dbanalysis;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;



public class FastNetflixDB implements Serializable, NetflixDB {

	private static final long serialVersionUID = 6493166459041873657L;

	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	private DBMovie[] movies;
	
	private Map<Integer, DBUser> users;
	
	private ArrayList<Rent> rents;
	
	private ArrayList<Rent> questions;
	
	public FastNetflixDB() {
		//MovieIDs range from 1 to 17770 sequentially.		
		movies = new DBMovie[17771];
		//CustomerIDs range from 1 to 2649429, with gaps. There are 480189 users.
		users = new TreeMap<Integer, DBUser>();
		
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
		return users.get(userId);
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
		users.get(rent.getUserId()).addRent(rent);
		movies[rent.getMovieId()].addRent(rent);
	}

	@Override
	public void writeUser(DBUser user) {
		users.put(user.getId(), user);
	}

	@Override
	public DBMovie[] getAllMovies() {
		return movies;
	}

	@Override
	public DBUser[] getAllUsers() {
		return users.values().toArray(new DBUser[0]);
	}
	
	@Override
	public int getNumberOfMovies() {
		return movies.length;
	}

	@Override
	public int getNumberOfUsers() {
		return users.size();
	}

	@Override
	public Iterator<Rent> rentsIterator() {
		return rents.iterator();
	}	

}
