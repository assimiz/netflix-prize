package strategy;

import graphframework.TrustGraph;
import dbanalysis.NetflixDB;

/**
 * Votes mapper that initializes all its mappings on initialization. After
 * initialization is done every call to {@link #map(int, int, int)} is being
 * retrieved from the mappings repository.
 * 
 * @author Assaf Mizrachi
 *
 */
public abstract class AbstractDesignatedVotesMapper implements VotesMapper {

	protected NetflixDB db;
	
	protected TrustGraph networkGraph;
	
	protected CluelessRecommendationStrategy clulessStrategy;
	
	
	/**
	 * @param db
	 * @param networkGraph
	 */
	public AbstractDesignatedVotesMapper(NetflixDB db, TrustGraph networkGraph,
			CluelessRecommendationStrategy clulessStrategy) {
		this.db = db;
		this.networkGraph = networkGraph;
		this.clulessStrategy = clulessStrategy;
		init();
	}

	protected abstract void init();
	
	/**
	 * Calculates the percentage of valuable mappings out of total mappings. Mapping for item Y is
	 * valuable if source user has rated X at least one item that destination user rated Y.
	 * 
	 * @return the percentage of valuable mappings.
	 */
	public abstract double getValuableMappingDistribution();

	@Override
	public abstract int map(int sourceUserId, int destUserId, int sourceUserVote);
	
	/**
	 * Strategy for cluess recommendation mapping
	 * @author mizrachi
	 *
	 */
	public static enum CluelessRecommendationStrategy {
		//Map clueless recommendation to the median vote (to be
		//used only in case the number of votes is odd)
		MAP_TO_MEDIAN_VOTE, 
		//Distribute the clueless recommendation  equally among
		//all votes.
		DISTRIBUTE
	}

}
