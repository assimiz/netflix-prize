package graphframework;

import dataframework.User;

/**
 * Calculates trust level between two users.
 * 
 * @author mizrachi
 *
 */
public interface TrustCalculator {

	/**
	 * Calculates the level of trust of user <code>from</code> in user <code>to</code>.
	 * @param from the user that the trust is calculated for.
	 * @param to the user that is being trusted.
	 *  
	 * @return the level of trust of user <code>from</code> in user <code>to</code>.
	 */
	public double getTrust(User from, User to);
}
