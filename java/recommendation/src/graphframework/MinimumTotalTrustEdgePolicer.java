package graphframework;

import org.jgrapht.Graph;

import dataframework.Trust;
import dataframework.User;

/**
 * Edge Policer for a trust graph that bases its decision upon the trust
 * between two users. Edge will be allowed to participate in the graph only
 * if the total common trust of a user is below a specified threshold.
 * 
 * @author Assaf Mizrachi
 *
 */
public class MinimumTotalTrustEdgePolicer extends AbstractEdgeAdditionPolicer<User, Trust> {

	private double minTotalTrust;
	
	private double totalTrust;
	
	private TrustCalculator calc;
	
	/**
	 * Create a Policer for specified Netflix DB and with a specified {@link TrustCalculator}
	 * 
	 * @param minTotalTrust the total trust threshold
	 * @param calc the calculator
	 * 
	 */
	public MinimumTotalTrustEdgePolicer(double minTotalTrust, TrustCalculator calc) {
		this(minTotalTrust, calc, null);
	}
	
	public MinimumTotalTrustEdgePolicer(double minTotalTrust, TrustCalculator calc, EdgeAdditionPolicer<User, Trust> decorator) {
		super(decorator);
		this.minTotalTrust = minTotalTrust;
		this.calc = calc;
		this.totalTrust = 0;
	}
	
	@Override
	public boolean caculate(User from, User to, Graph<User, Trust> graph) {
		if (totalTrust >= minTotalTrust) {
			return false;
		} else {
			totalTrust += calc.getTrust(from, to);
			return true;
		}
	}
}

