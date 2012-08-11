package graphframework;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import dataframework.Trust;
import dataframework.User;
import dbanalysis.Rent;

/**
 * Generates a trust graph from specified {@link Rent}s set. There will be a vertex for each user
 * appears in one rent or more. Edges will be added in priority based on their weight (trust); 
 * the larger the weight, the higher the possibility of the edge to be added. Edges for each user will
 * be added until reaching a specified threshold; Edges with weight less or equal to zero will not be added.
 * 
 * @author Assaf Mizrachi
 *
 */
public class BestOfBreedTrustGraphGenerator extends GeneralTrustGraphGenerator {
	
	private TrustCalculator edgeWeightCalculator;
	
	private double percentOfTotalTrust;
		
	public BestOfBreedTrustGraphGenerator (Iterator<Rent> rents, TrustCalculator edgeWeightCalculator, double percentOfTotalTrust) {
		super(rents);
		this.edgeWeightCalculator = edgeWeightCalculator;		
		this.percentOfTotalTrust = percentOfTotalTrust;
	}
	
	@Override
	protected void addEdges(TrustGraph graph) {
		Trust trust;
		int k = 0;
		int m = 0;
		int t = 0;
		EdgeAdditionPolicer<User, Trust> edgePolicer;
		for (final User u : graph.vertexSet()) {
			System.out.println("Building the neighborhood of user #" + ++t);
			User[] orderedByTrust = graph.vertexSet().toArray(new User[0]);
			Arrays.sort(orderedByTrust, new Comparator<User>() {

				@Override
				public int compare(User v1, User v2) {
					if ((edgeWeightCalculator.getTrust(u, v2) > edgeWeightCalculator.getTrust(u, v1))) {
						return 1;
					} else if ((edgeWeightCalculator.getTrust(u, v2) < edgeWeightCalculator.getTrust(u, v1))) {
						return -1;
					} else {
						return 0;
					}
				}
			});
			
			edgePolicer = new MinimumTotalTrustEdgePolicer(getThreshold(u, graph), edgeWeightCalculator,
					new MinimumTrustEdgePolicer(0.01, edgeWeightCalculator));
			for (User v : orderedByTrust) {
				if (u != v) {
					if (edgePolicer.isEdgeAllowed(u, v, graph)) {								
						trust = graph.addEdge(u, v);
						graph.setEdgeWeight(trust, edgeWeightCalculator.getTrust(u, v));
						System.out.println("Adding edge " + k++ + " from " + u.toString()
								+ " to " + v.toString() + ". Trust = " + trust);
					} else {
						System.out.println("Reached trust threshold or that all left edges weight's <= 0, throwing additional edges...");
						break;
					}
				}
			}
		}
	}
	
	private double getTotalTrust(User u, TrustGraph graph) {
		double totalTrust = 0;		
		for (User v : graph.vertexSet()) {
			if (u != v) {
				totalTrust += edgeWeightCalculator.getTrust(u, v);
			}
		}
		return totalTrust;
	}
	
	private double getThreshold(User user, TrustGraph graph) {
		return getTotalTrust(user, graph) * percentOfTotalTrust / (double) 100;
	}
}
