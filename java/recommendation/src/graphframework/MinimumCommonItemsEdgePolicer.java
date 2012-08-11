package graphframework;

import org.jgrapht.Graph;

import dataframework.Trust;
import dataframework.User;
import dbanalysis.NetflixDB;
import dbanalysis.Rent;

/**
 * Edge Policer for the Netflix graph that bases its decision upon the number of common items
 * between two users. Only edges from users that share more than a predefined number of common
 * items will be allowed to participate in the graph.
 * 
 * @author Assaf Mizrachi
 *
 */
public class MinimumCommonItemsEdgePolicer extends AbstractEdgeAdditionPolicer<User, Trust> {

	private int minCommonItems;
	
	private NetflixDB db;
	
	/**
	 * Create a Policer for specified Netflix DB and for a specified number
	 * of common items
	 * 
	 * @param db the Netflix database
	 * @param minCommonItems threshold number of common items
	 * 
	 */
	public MinimumCommonItemsEdgePolicer(NetflixDB db, int minCommonItems) {
		this(db, minCommonItems, null);
	}
	
	public MinimumCommonItemsEdgePolicer(NetflixDB db, int minCommonItems, EdgeAdditionPolicer<User, Trust> decorator) {
		super(decorator);
		this.db = db;
		this.minCommonItems = minCommonItems;
	}
	
	@Override
	protected boolean caculate(User from, User to,
			Graph<User, Trust> graph) {
		int commonItems = 0;
		for (Rent fromRent : db.readUser(from.getId()).getRents()) {
			for (Rent toRent : db.readUser(to.getId()).getRents()) {
				if (fromRent.getMovieId() == toRent.getMovieId()) {
					commonItems++;
					if (commonItems >= minCommonItems) {
						return true;
					}
				}
			}
		}
		return commonItems >= minCommonItems;
	}

}
