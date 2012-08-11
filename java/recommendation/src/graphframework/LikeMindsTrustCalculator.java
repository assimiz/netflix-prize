package graphframework;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import common.Compartors;

import dataframework.User;
import dbanalysis.NetflixDB;
import dbanalysis.Rent;

/**
 * Trust calculator that based its calculation upon the 
 * <a href="http://www.greening.org/talks/consumertrust/index.htm">LikeMinds</a> system.
 * Basically, it defines a closeness function that maps from gap between ratings of the same
 * item to a scalar. Then it calculates an agreement scalar based on the aggregated closeness of all 
 * common items rated by the two users. Note that this scalar can be negative and that the system 
 * takes in account the recommendation of 'positive' users only.
 * 
 * @author Assaf Mizrachi
 *
 */
public class LikeMindsTrustCalculator extends CacheTrustCalculator {

	//this stands for the closeness method
	private static int[] closenessMap = new int[]{5, 3, 0, -3, -5};
		
	private NetflixDB db;
	
	public LikeMindsTrustCalculator(NetflixDB db) {
		this.db = db;
	}
	
	public LikeMindsTrustCalculator(NetflixDB db, int cacheSize) {
		super(cacheSize);
		this.db = db;
	}
	
	@Override
	protected double calcTrust(User from, User to) {
		return calcAgreementScalar(from.getId(), to.getId());
	}

	private double calcAgreementScalar(int userId, int anotherUserId) {
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
		
		int card = srcUserRents.size();
		//no common items leads to neutral trust
		if (card == 0) {
			return 0;
		}
		
		//calculating the CVT
		int CVT_ik = 0;
		Rent srcRent, dstRent;
		for (int i = 0; i < srcUserRents.size(); i++) {
			srcRent = srcUserRents.get(i);
			dstRent = dstUserRents.get(i);
			
			if (srcRent.getMovieId() != dstRent.getMovieId()) {
				throw new RuntimeException("unexpexted difference in movie id when calculating" +
						" cvt for source " + userId + " and destination " + anotherUserId);
			}
			
			CVT_ik += closenessMap[Math.abs(srcRent.getRate() - dstRent.getRate())];
		}
		
		//calculating the agreement scalar		
		double AS_ik = (CVT_ik * (Math.log10(card) / Math.log10(2))) / card;
		return AS_ik;		
	}
}
