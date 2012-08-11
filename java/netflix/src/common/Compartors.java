package common;

import java.util.Comparator;

import dbanalysis.Rent;

/**
 * Useful comparetors.
 * 
 * @author Assaf Mizrachi
 *
 */
public class Compartors {

	/**
	 * Create {@link Rent} comparator by the movie id.
	 * 
	 * @return {@link Rent} comparator by the movie id.
	 */
	public static Comparator<Rent> createMovieIdComparator() {
		return new MovieIdComparator();
	}
	private static class MovieIdComparator implements Comparator<Rent> {

		@Override
		public int compare(Rent r1, Rent r2) {
			return r1.getMovieId() - r2.getMovieId();
		}
		
	}
	
	/**
	 * Create {@link Rent} comparator by the user id.
	 * 
	 * @return {@link Rent} comparator by the user id.
	 */
	public static Comparator<Rent> createUserIdComparator() {
		return new UserIdComparator();
	}
	private static class UserIdComparator implements Comparator<Rent> {

		@Override
		public int compare(Rent r1, Rent r2) {
			return r1.getUserId() - r2.getUserId();
		}
		
	}
	
	/**
	 * Create {@link Rent} comparator by the rate.
	 * 
	 * @return {@link Rent} comparator by the rate.
	 */
	public static Comparator<Rent> createRateComparator() {
		return new RateComparator();
	}
	private static class RateComparator implements Comparator<Rent> {

		@Override
		public int compare(Rent r1, Rent r2) {
			return r1.getRate() - r2.getRate();
		}
		
	}
}
