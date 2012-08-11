package graphframework;

import java.util.Iterator;

import dataframework.Trust;
import dataframework.User;
import dbanalysis.Rent;

/**
 * Generates a trust graph from specified {@link Rent}s set. There will be a vertex for each user
 * appears in one rent or more. Edges will be added according to specified Policer. Edges are added
 * using brute-force method, i.e. the Policer is being queried for each pair of users (actually it is
 * queried twice for each pair of users since the resulted graph is directed) whether there
 * should or should not be and edge between them.
 * 
 * @author Assaf Mizrachi
 *
 */
public class DefaultTrustGraphGenerator extends GeneralTrustGraphGenerator {
	
	private TrustCalculator edgeWeightCalculator;
	
	private EdgeAdditionPolicer<User, Trust> edgePolicer;
	
	public DefaultTrustGraphGenerator (Iterator<Rent> rents, TrustCalculator edgeWeightCalculator,
			EdgeAdditionPolicer<User, Trust> edgePolicer) {
		super(rents);
		this.edgeWeightCalculator = edgeWeightCalculator;
		this.edgePolicer = edgePolicer;
	}

	@Override
	protected void addEdges(TrustGraph graph) {
		Trust trust;
		int k = 0;
		int m = 0;
		int t = 0;
		for (User u : graph.vertexSet()) {
			System.out.println("Building the neighborhood of user #" + ++t);
			for (User v : graph.vertexSet()) {
				if (u != v) {
					if (edgePolicer.isEdgeAllowed(u, v, graph)) {								
						trust = graph.addEdge(u, v);
						graph.setEdgeWeight(trust, edgeWeightCalculator.getTrust(u, v));
						System.out.println("Adding edge " + k++ + " from " + u.toString()
								+ " to " + v.toString() + ". Trust = " + trust);
					} else {
						System.out.println("Throwing edge " + m++ + " from " + u.toString()
								+ " to " + v.toString() + ". Trust = " + edgeWeightCalculator.getTrust(u, v));
					}
				}
			}
		}
	}
	
}
