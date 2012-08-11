package graphframework;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import common.Compartors;

import dataframework.User;
import dbanalysis.NetflixDB;
import dbanalysis.Rent;

/**
 * Trust calculator that base its calculation on the Pearson coefficient
 * between the ratings of common items of two users (actually it is the
 * Pearson coefficient itself). The coefficient may be standard or constrained.
 * The later ensures that the its value increases only when both users rated an
 * item negatively (below the mid subjective rate) or positively (above the mid subjective rate).
 * 
 * @author Assaf Mizrachi
 *
 */
public class PearsonCorrelationTrustCalculator extends CacheTrustCalculator {

	private NetflixDB db;
	private boolean constrained;
	private int scale;
	
	public PearsonCorrelationTrustCalculator(NetflixDB db, boolean constrained, int scale) {
		this.db = db;
		this.constrained = constrained;
		this.scale = scale;
	}
	
	public PearsonCorrelationTrustCalculator(NetflixDB db, boolean constrained, int scale, int cacheSize) {
		super(cacheSize);
		this.db = db;
		this.constrained = constrained;
		this.scale = scale;
	}
	
	@Override
	protected double calcTrust(User from, User to) {
		return calcPearsonCorrelation(from.getId(), to.getId());
	}

	private double calcPearsonCorrelation(int userId, int anotherUserId) {
		List<Rent> srcUserRents = db.readUser(userId).getCommonMoviesWith(db.readUser(anotherUserId));
		List<Rent> dstUserRents = db.readUser(anotherUserId).getCommonMoviesWith(db.readUser(userId));

		Comparator<Rent> byMovieId = Compartors.createMovieIdComparator();
		Collections.sort(srcUserRents, byMovieId);
		Collections.sort(dstUserRents, byMovieId);

		if (srcUserRents.size() != dstUserRents.size()) {
			throw new RuntimeException("source user and destination user are reporting different " +
					"size of common items when calculating pearson correlation for source "
					+ userId + " and destination " + anotherUserId);
		}

		if (constrained && scale % 2 == 0) {
			throw new IllegalArgumentException("Constrained Pearson correlation can be calculated only for odd scale");
		}
		//calculating the Pearson coefficient
		Rent srcRent, dstRent;
		double numerator = 0, denum1 = 0, denum2 = 0;
		int shift = constrained ? scale / 2 + 1 : 0;
		for (int i = 0; i < srcUserRents.size(); i++) {
			srcRent = srcUserRents.get(i);
			dstRent = dstUserRents.get(i);

			if (srcRent.getMovieId() != dstRent.getMovieId()) {
				throw new RuntimeException("unexpexted difference in movie id when calculating" +
						" cvt for source " + userId + " and destination " + anotherUserId);
			}

			numerator += (srcRent.getRate() - shift) * (dstRent.getRate() - shift);
			denum1 += Math.pow((srcRent.getRate() - shift), 2);
			denum2 += Math.pow((dstRent.getRate() - shift), 2);
		}

		double result = numerator / Math.sqrt(denum1 * denum2);
		return result;		
	}
}
