package dbanalysis;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

public interface NetflixDB {

	public abstract void writeMovie(DBMovie movie);

	public abstract void writeUser(DBUser user);
	
	public abstract void writeRent(Rent rent);

	public abstract void writeQuestion(Rent unratedRent);

	public abstract DBMovie readMovie(int movieId);

	public abstract DBUser readUser(int userId);
	
	public abstract Rent readRent(int movieId, int userId, Date date);

	public abstract Rent readQuestion(int movieId, int userId, Date date);
	
	public abstract DateFormat getDateFormat();
	
	public abstract DBMovie[] getAllMovies();
	
	public abstract DBUser[] getAllUsers();
	
	public abstract int getNumberOfMovies();
	
	public abstract int getNumberOfUsers();
	
	public abstract Iterator<Rent> rentsIterator();

}