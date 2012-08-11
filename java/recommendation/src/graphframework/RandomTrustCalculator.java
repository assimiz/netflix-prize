package graphframework;

import dataframework.User;

/**
 * Generates random trust between two users. 
 * 
 * @author mizrachi
 *
 */
public class RandomTrustCalculator extends CacheTrustCalculator {

	
	/**
	 * 
	 */
	public RandomTrustCalculator() {
		super();
	}

	/**
	 * @param cachSize
	 */
	public RandomTrustCalculator(int cachSize) {
		super(cachSize);
	}

	@Override
	protected double calcTrust(User from, User to) {
		return (double) Math.random();
	}

}
