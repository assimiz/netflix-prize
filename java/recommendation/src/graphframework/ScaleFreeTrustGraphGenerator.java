package graphframework;


import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import dataframework.Trust;
import dataframework.User;
import dbanalysis.DBUser;
import dbanalysis.NetflixDB;

/**
 * Generates directed or undirected <a href =
 * "http://mathworld.wolfram.com/Scale-FreeNetwork.html">scale-free network</a>
 * of any size. Scale-free network is a connected graph, where degrees of
 * vertices are distributed in unusual way. There are many vertices with small
 * degrees and only small amount of vertices with big degrees.
 * 
 * @author Assaf Mizrachi
 */
public class ScaleFreeTrustGraphGenerator {
	
	private NetflixDB db;
	
	private TrustCalculator edgeWeightCalculator;
	
	private boolean createBidirectionalEdges;
	
	private int scalingFactor;

	/**
	 * Constructs a Scale-free graph generator for all the users in a specified
	 * Netflix DB. in the graph to be generated, the higher degree vertices are
	 * the most heavy (in terms of number of rents) users.
	 * 
	 * @param db the Netflix database
	 * @param edgeWeightCalculator the edge weight calculation authority
	 * @param ensureConnectivity set this value to <code>true</code> if a strong connectivity
	 * of the trust-graph is desired. Set it to <code>false</code> otherwise.
	 *
	 */
	public ScaleFreeTrustGraphGenerator(NetflixDB db, TrustCalculator edgeWeightCalculator,
			int scalingFactor, boolean ensureStrongConnectivity) {
		this.db = db;
		this.edgeWeightCalculator = edgeWeightCalculator;
		this.scalingFactor = scalingFactor;
		this.createBidirectionalEdges = ensureStrongConnectivity;
	}

	/**
	 * Generates scale-free network with <tt>db</tt> passed to the
	 * constructor. The graph is generated from all the users in a specified
	 * Netflix DB, where the higher degree vertices are the most heavy (in terms
	 * of number of rents) users. Each call of this method produces identical output except for
	 * the directions of edges that are lost lost).
	 * 
	 * @param db the Netflix database
	 * 
	 */
	public TrustGraph generateGraph() {
		TrustGraph result = new TrustGraph();
		Random random = new Random();
		int vRents, uRents, rentSum;
		boolean isFirst = true;
		DBUser[] users = db.getAllUsers();
		Arrays.sort(users, new Comparator<DBUser>() {

			@Override
			public int compare(DBUser o1, DBUser o2) {
				return o2.getNumOfRents() - o1.getNumOfRents();
			}
		});
		int normalizer = users[0].getNumOfRents() * users[1].getNumOfRents();
		int i = 0;
		for (DBUser u : users) {
			System.out.println("Handling user " + i++ + " out of " + users.length);
			User newVertex = new User(u.getId());
			uRents = u.getNumOfRents();
			result.addVertex(newVertex);
			boolean isConnected = false;			
			while (!isConnected && !isFirst) // we want our graph to be connected

			{
				rentSum = 0;
				for (User v : result.vertexSet()) {
					vRents = db.readUser(v.getId()).getNumOfRents();
					if (v != newVertex && ((rentSum == 0)
							|| (random.nextInt(normalizer / scalingFactor) <= (vRents * uRents)))) {
						rentSum += (vRents);
						if (createBidirectionalEdges) {
							isConnected |= addEdge(result, v, newVertex);
							isConnected |= addEdge(result, newVertex, v);
						} else {
							if (random.nextInt(2) == 0) {
								isConnected |= addEdge(result, v, newVertex);
							} else {
								isConnected |= addEdge(result, newVertex, v);
							}
						}
					}
				}
			}
			isFirst = false;
		}
		return result;
	}
	
	private boolean addEdge(TrustGraph graph, User source, User dest) {
		double trust = edgeWeightCalculator.getTrust(source, dest);
		if (trust > 0) {
			Trust edge = graph.addEdge(source, dest);
			graph.setEdgeWeight(edge, edgeWeightCalculator.getTrust(source, dest));
			return true;
		} else {
			return false;
		}
	}
}

