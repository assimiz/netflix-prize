package dbanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import common.Compartors;

public class DBUser extends NetflixObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -405697599467403828L;
	
	public DBUser(int id) {
		super(id);
	}
	
	/**
	 * Collects and returns all the rents of given movie.
	 * 
	 * @param movieId the movie id
	 * @return all the rents for specified movie id.
	 */
	public List<Rent> getRentsForMovieId(int movieId) {
		return getRentsForMovieId(getRents(), movieId);
	}
	
	/**
	 * Collects and returns all the rents of given movie id for a specified list.
	 * 
	 * @param target the list to be searched
	 * @param movieId the movie id
	 * @return all the rents for specified movie id.
	 */
	public static List<Rent> getRentsForMovieId(List<Rent> target, int movieId) {
		//first we sort all rents by movie id
		Rent[] allRents = target.toArray(new Rent[0]);
		Comparator<Rent> byMovieId = Compartors.createMovieIdComparator();
		Arrays.sort(allRents, byMovieId);		
		//then we create a matching array containing only the movie Ids.
		//This array will be used for the binary search
		int[] allMovieIds = new int[allRents.length];
		int i = 0;
		for (Rent rent: allRents) {
			allMovieIds[i++] = rent.getMovieId();
		}
		//now we execute the binary search and finds a single instance 
		int instance = Arrays.binarySearch(allMovieIds, movieId);
		//there might be more so we have to search for them too
		ArrayList<Integer> allInstancesOfTheMovieId = new ArrayList<Integer>();
		//but there may be none
		if (instance >= 0) {
			allInstancesOfTheMovieId.add(instance);
			//since the array is ordered we can be sure that all other matches are
			//in adjacent cells
			//so we search forward		
			for (int j = instance + 1; j < allMovieIds.length; j++) {
				if (allMovieIds[j] == movieId) {
					allInstancesOfTheMovieId.add(j);
				} else {
					break;
				}
			}
			//and backward
			for (int j = instance - 1; j >= 0; j--) {
				if (allMovieIds[j] == movieId) {
					allInstancesOfTheMovieId.add(j);
				} else {
					break;
				}
			}
		}
		//finally we add all instances to the result 
		ArrayList<Rent> matches = new ArrayList<Rent>();
		for (Integer k : allInstancesOfTheMovieId) {
			matches.add(allRents[k]);
		}
		return matches;
	}
	
	public List<Rent> getCommonMoviesWith(DBUser other) {
		ArrayList<Rent> matches = new ArrayList<Rent>();
		
		Rent[] fromUserMovies = this.getRents().toArray(new Rent[0]);
		Rent[] toUserMovies = other.getRents().toArray(new Rent[0]);		
		Rent[] biggerArray = fromUserMovies.length > toUserMovies.length ? fromUserMovies : toUserMovies;
		Rent[] smallerArray = fromUserMovies == biggerArray ? toUserMovies : fromUserMovies;
		
		//array must be sorted before the binary search
		Comparator<Rent> byMovieId = Compartors.createMovieIdComparator();
		Arrays.sort(biggerArray, byMovieId);		
		int index;
		for (Rent rent : smallerArray) {
			index = Arrays.binarySearch(biggerArray, rent, byMovieId);
			if (index >= 0) {
				matches.add(fromUserMovies == biggerArray ? biggerArray[index] : rent);
			}
		}
		
		return matches;
	}
	
	/**
	 * Collects all movies IDs of this user.
	 * 
	 * @return array contains all IDs of movies rented by this user.
	 */
	public int[] getAllMoviesIds() {
		List<Integer> ids = new ArrayList<Integer>(getRents().size());
		for (Rent rent : getRents()) {
			ids.add(rent.getMovieId());			
		}
		int[] result = new int[ids.size()];
		int i = 0;
		for (Integer id : ids) {
			result[i++] = id;
		}
		return result;
	}
}
