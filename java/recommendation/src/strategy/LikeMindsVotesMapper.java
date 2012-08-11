package strategy;

import graphframework.LikeMindsTrustCalculator;
import graphframework.TrustGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dataframework.User;
import dbanalysis.NetflixDB;


/**
 * Votes mapper who's based on LikeMinds method to provide recommendations.
 * Currently it is dedicated for 5 scale ratings and the closeness function
 * is fixed. Should be used along with {@link LikeMindsTrustCalculator}.
 * 
 * @author mizrachi
 * @see http://www.greening.org/talks/consumertrust/index.htm
 *
 */

public class LikeMindsVotesMapper extends AbstractDesignatedVotesMapper {

	private Map<Integer, UserValues> valuesMap;
	
	public LikeMindsVotesMapper(NetflixDB db, TrustGraph networkGraph,
			CluelessRecommendationStrategy clulessStrategy) {
		super(db, networkGraph, clulessStrategy);				
	}	

	@Override
	public double getValuableMappingDistribution() {
		return 100;
	}

	@Override
	protected void init() {
		valuesMap = new HashMap<Integer, UserValues>();
		
		Set<User> users = networkGraph.vertexSet();
		int i = 0;
		for (User u : users) {
			System.out.println("Calculates mapping for user " + i++ + " out of " + users.size());
			valuesMap.put(u.getId(), new UserValues(db.readUser(u.getId()).getMinRate(),
					db.readUser(u.getId()).getMaxRate()));
		}
	}

	@Override
	//						k				i				r_kj
	public int map(int sourceUserId, int destUserId, int sourceUserVote) {
		double s_kj = calcScalingScalar(sourceUserId, destUserId, sourceUserVote);
		return (int) Math.round(s_kj);
	}
	
	//										k				i				r_kj
	private double calcScalingScalar(int sourceUserId, int destUserId, int sourceUserVote) {
		int r_kj = sourceUserVote;
		UserValues srcUserValues = valuesMap.get(sourceUserId);
		UserValues dstUserValues = valuesMap.get(destUserId);
		int min_i = dstUserValues.minVote;
		int max_i = dstUserValues.maxVote;
		int min_k = srcUserValues.minVote;
		int max_k = srcUserValues.maxVote;
		//TODO this function is weird. what if max_k == min_k? also it can map
		//out of the scope of the ladder
		double s_kj = min_i + ((r_kj - min_k) * (max_i - min_i) / (max_k - min_k));
		return s_kj;
	}
	
	private static class UserValues {
		public final int minVote;
		public final int maxVote;
		
		/**
		 * @param minVote
		 * @param maxVote
		 */
		public UserValues(int minVote, int maxVote) {
			this.minVote = minVote;
			this.maxVote = maxVote;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + maxVote;
			result = prime * result + minVote;
			return result;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UserValues other = (UserValues) obj;
			if (maxVote != other.maxVote)
				return false;
			if (minVote != other.minVote)
				return false;
			return true;
		}
		
		
	}
}
