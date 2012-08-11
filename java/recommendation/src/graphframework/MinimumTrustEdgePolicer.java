package graphframework;

import org.jgrapht.Graph;

import dataframework.Trust;
import dataframework.User;

/**
 * Edge Policer for the Netflix graph that bases its decision upon the trust
 * between two users. Edge will be allowed to participate in the graph only if
 * the source user's trust in the target is more than a predefined threshold.
 * 
 * @author Assaf Mizrachi
 *
 */
public class MinimumTrustEdgePolicer extends AbstractEdgeAdditionPolicer<User, Trust> {

	private double minTrust;
	
	private TrustCalculator calc;
	
	/**
	 * Create a Policer for specified Netflix DB and with a specified {@link TrustCalculator}
	 * 
	 * @param db the Netflix database
	 * @param calc the calculator
	 * 
	 */
	public MinimumTrustEdgePolicer(double minTrust, TrustCalculator calc) {
		this(minTrust, calc, null);
	}
	
	public MinimumTrustEdgePolicer(double minTrust, TrustCalculator calc, EdgeAdditionPolicer<User, Trust> decorator) {
		super(decorator);
		this.minTrust = minTrust;
		this.calc = calc;
	}
	
	@Override
	public boolean caculate(User from, User to, Graph<User, Trust> graph) {
		return calc.getTrust(from, to) >= minTrust;
	}
}
