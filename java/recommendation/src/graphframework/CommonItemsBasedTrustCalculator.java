package graphframework;

import java.util.Arrays;

import dataframework.User;
import dbanalysis.NetflixDB;

/**
 * Calculates trust based on the number of common items rated by two users.
 * User A trusts user B more than user C if the number of common items rated 
 * by users A and B is bigger than the number of common items rated by A and C.
 * The trust can be normalized by the total number of items rated by user A.
 *  
 * @author mizrachi
 *
 */
public class CommonItemsBasedTrustCalculator extends CacheTrustCalculator {

	//no normalization on the total common items
	public static final int NORMALIZE_NONE = 1;
	
	//normalization by the total items of the 'from' user
	public static final int NORMALIZE_BY_FROM = 2;
	
	//normalization by the total items of the 'to' user
	public static final int NORMALIZE_BY_TO = 3;
	
	private int normalize;
	
	private NetflixDB db;
	
	private double power;
	
	public CommonItemsBasedTrustCalculator(NetflixDB db) {
		this(db, NORMALIZE_NONE);
	}
	
	public CommonItemsBasedTrustCalculator(NetflixDB db, int normalize) {
		this(db, normalize, 1);
	}
	
	public CommonItemsBasedTrustCalculator(NetflixDB db, int normalize, double power) {
		this.db = db;
		this.normalize = normalize;
		this.power = power;
	}
	
	public CommonItemsBasedTrustCalculator(NetflixDB db, int normalize, double power, int cacheSize) {
		super(cacheSize);
		this.db = db;
		this.normalize = normalize;
		this.power = power;
	}
	
	@Override
	protected double calcTrust(User from, User to) {
		int commonItems = 0;
		
		int[] fromUserMovies = db.readUser(from.getId()).getAllMoviesIds();
		int[] toUserMovies = db.readUser(to.getId()).getAllMoviesIds();		
		int[] biggerArray = fromUserMovies.length > toUserMovies.length ? fromUserMovies : toUserMovies;
		int[] smallerArray = fromUserMovies == biggerArray ? toUserMovies : fromUserMovies;
		
		//array must be sorted before the binary search
		Arrays.sort(biggerArray);		
		for (int id : smallerArray) {
			if (Arrays.binarySearch(biggerArray, id) >= 0) {
				commonItems++;
			}
		}
		
		if (normalize == NORMALIZE_BY_TO) {
			return (double) commonItems / 
				(double) db.readUser(to.getId()).getNumOfRents();
		} else if (normalize == NORMALIZE_BY_FROM) {
			return (double) commonItems / 
				(double) db.readUser(from.getId()).getNumOfRents();
		} else {
			return Math.pow(commonItems, power);
		}
	}

}
