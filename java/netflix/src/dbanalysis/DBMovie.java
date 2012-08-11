package dbanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import common.Compartors;

public class DBMovie extends NetflixObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2359251774842631477L;

	public DBMovie(int id) {
		super(id);
	}
	
	/**
	 * Collects and returns all the rents of given user id.
	 * 
	 * @param target the list to be searched
	 * @param user the user id
	 * @return all the rents for specified user id.
	 */
	public List<Rent> getRentsForUserId(int userId) {
		return getRentsForUserId(getRents(), userId);
	}
	
	/**
	 * Collects and returns all the rents of given user id for a specified list.
	 * 
	 * @param target the list to be searched
	 * @param user the user id
	 * @return all the rents for specified user id.
	 */
	public static List<Rent> getRentsForUserId(List<Rent> target, int userId) {
		//first we sort all rents by movie id
		Rent[] allRents = target.toArray(new Rent[0]);
		Comparator<Rent> byUserId = Compartors.createUserIdComparator();
		Arrays.sort(allRents, byUserId);		
		//then we create a matching array containing only the movie Ids.
		//This array will be used for the binary search
		int[] allUserIds = new int[allRents.length];
		int i = 0;
		for (Rent rent: allRents) {
			allUserIds[i++] = rent.getUserId();
		}
		//now we execute the binary search and finds a single instance 
		int instance = Arrays.binarySearch(allUserIds, userId);
		//there might be more so we have to search for them too
		ArrayList<Integer> allInstancesOfTheUserId = new ArrayList<Integer>();
		//but there may be none
		if (instance >= 0) {
			allInstancesOfTheUserId.add(instance);
			//since the array is ordered we can be sure that all other matches are
			//in adjacent cells
			//so we search forward		
			for (int j = instance + 1; j < allUserIds.length; j++) {
				if (allUserIds[j] == userId) {
					allInstancesOfTheUserId.add(j);
				} else {
					break;
				}
			}
			//and backward
			for (int j = instance - 1; j >= 0; j--) {
				if (allUserIds[j] == userId) {
					allInstancesOfTheUserId.add(j);
				} else {
					break;
				}
			}
		}
		//finally we add all instances to the result 
		ArrayList<Rent> matches = new ArrayList<Rent>();
		for (Integer k : allInstancesOfTheUserId) {
			matches.add(allRents[k]);
		}
		return matches;
	}
	
	/**
	 * Collects all users IDs of this movie.
	 * 
	 * @return array contains all IDs of user rented this movie.
	 */
	public int[] getAllUsersIds() {
		HashSet<Integer> ids = new HashSet<Integer>();
		for (Rent rent : getRents()) {
			ids.add(rent.getUserId());			
		}
		int[] result = new int[ids.size()];
		int i = 0;
		for (Integer id : ids) {
			result[i++] = id;
		}
		return result;
	}
}
