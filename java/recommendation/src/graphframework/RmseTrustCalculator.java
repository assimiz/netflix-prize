package graphframework;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import strategy.MinRmseVotesMapper;

import common.Compartors;

import dataframework.User;
import dbanalysis.NetflixDB;
import dbanalysis.Rent;

/**
 * Trust calculator that base its calculation on the RMSE between the rating
 * of all common items of two users. The lower the RMSE - the higher the trust.
 * 
 * @author Assaf Mizrachi
 * @see MinRmseVotesMapper
 *
 */
public class RmseTrustCalculator extends CacheTrustCalculator {

	private NetflixDB db;
	
	public RmseTrustCalculator(NetflixDB db) {
		this.db = db;
	}
	
	public RmseTrustCalculator(NetflixDB db, int cacheSize) {
		super(cacheSize);
		this.db = db;
	}
	
	@Override
	protected double calcTrust(User from, User to) {
		//the bigger the rmse the lower the trust
		return 1 / calcRmse(from.getId(), to.getId());
	}
	
	private double calcRmse(int userId, int anotherUserId) {
		List<Rent> srcUserRents = db.readUser(userId).getCommonMoviesWith(db.readUser(anotherUserId));
		List<Rent> dstUserRents = db.readUser(anotherUserId).getCommonMoviesWith(db.readUser(userId));
		
		Comparator<Rent> byMovieId = Compartors.createMovieIdComparator();
		Collections.sort(srcUserRents, byMovieId);
		Collections.sort(dstUserRents, byMovieId);
		
		if (srcUserRents.size() != dstUserRents.size()) {
			throw new RuntimeException("source user and destination user are reporting different " +
					"size of common items when calculating cvt for source "
					+ userId + " and destination " + anotherUserId);
		}
		
		//calculating sum of powered error
		double sqErr = 0;
		Rent srcRent, dstRent;
		for (int i = 0; i < srcUserRents.size(); i++) {
			srcRent = srcUserRents.get(i);
			dstRent = dstUserRents.get(i);
			
			if (srcRent.getMovieId() != dstRent.getMovieId()) {
				throw new RuntimeException("unexpexted difference in movie id when calculating" +
						" cvt for source " + userId + " and destination " + anotherUserId);
			}
			
			sqErr += Math.pow((srcRent.getRate() - dstRent.getRate()), 2);
		}
		
		//calculating root mean square error
		return Math.sqrt(sqErr/srcUserRents.size());		
	}
}
