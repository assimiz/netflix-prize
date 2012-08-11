package strategy;

import graphframework.TrustGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataframework.Trust;
import dataframework.User;
import dbanalysis.DBUser;
import dbanalysis.NetflixDB;
import dbanalysis.NetflixObject;
import dbanalysis.Rent;

/**
 * Votes mapper that is targeted upon minimizing the RMSE upon mapping.
 * The map from vote <code>m</code>, to recommendation <code>r</code> is the one that minimizes the RMSE
 * if for every time the source user voted <code>m</code>, the other would have been recommended <code>r</code>.
 * In other words, in order to know what the actual mapping from source user’s <code>m</code> vote to 
 * destination user’s recommended <code>r</code> the mapper asks himself the following question: “If every time
 * the source user voted <code>m</code>, I would have recommend <code>r</code> to the destination user, will the
 * calculated RMSE be minimized?”
 * 
 * @author mizrachi
 *
 */
public class MinRmseVotesMapper extends AbstractDesignatedVotesMapper {
	
	private int mappingsTotal;
	
	private int mappingsValuable;
		
	private Map<MessageKey, int[]> votesMap;
	

	public MinRmseVotesMapper(NetflixDB db, TrustGraph networkGraph,
			CluelessRecommendationStrategy clulessStrategy) {
		super(db, networkGraph, clulessStrategy);					
	}
	
	protected void init() {
		mappingsTotal = 0;
		mappingsValuable = 0;
		votesMap = new HashMap<MessageKey, int[]>();
		
		Set<User> users = networkGraph.vertexSet();
		int i = 0;
		for (User source : users) {
			User dest;
			System.out.println("Calculates mapping for user " + i++ + " out of " + users.size());
			Set<Trust> inEdges = networkGraph.incomingEdgesOf(source);
			int j = 0;
			for (Trust edge : inEdges) {
				dest = networkGraph.getEdgeSource(edge);
				int[] voteMapping = calcVotesMapping(source.getId(), dest.getId());
				votesMap.put(new MessageKey(source.getId(), dest.getId()), voteMapping);
			}
		}
	}

	/**
	 * Calculates the RMSE for the specified source vote and its recommendation to
	 * destination user. The RMSE is the gap between the actual vote of the destination user
	 * and the recommendation it got from the source for the all the movies rated <code>sourceUserVote</code>
	 * by the source.
	 * 
	 * @param sourceUserId the source user id
	 * @param destUserId the destination user id
	 * @param sourceUserVote the source user's vote
	 * @param sourceUserRecommendationToDest the source user's recommendation to destination.
	 * @return the calculated RMSE
	 */
	private double calcRMSE(int sourceUserId, int destUserId, int sourceUserVote, int sourceUserRecommendationToDest) {
		int accDelta = 0;
		boolean atLeastOneMatchFound = false;
		List<Rent> commonMovies = db.readUser(sourceUserId).getCommonMoviesWith(db.readUser(destUserId));
		for (Rent sourceUserRent : NetflixObject.getRentsForRate(commonMovies, sourceUserVote)) {
			for (Rent destUserRent : DBUser.getRentsForMovieId(commonMovies, sourceUserRent.getMovieId())) {
				accDelta += Math.pow((destUserRent.getRate() - sourceUserRecommendationToDest), 2);		
				atLeastOneMatchFound = true;
			}
		}
		return atLeastOneMatchFound ? Math.sqrt(accDelta) : Double.MAX_VALUE;
	}

	/**
	 * Calculates vote mapping from source to destination user.
	 * 
	 * @param sourceUserId the source user id
	 * @param destUserId the destination user id
	 * 
	 * @return an array representing the complete vote mapping from source to destination user.
	 */
	protected int[] calcVotesMapping(int sourceUserId, int destUserId) {
		double rmse;			
		int[] mapArray = new int[VotesDistributionMessage.MAX_VOTE];
		for (int sourceUserVote = 0; sourceUserVote < VotesDistributionMessage.MAX_VOTE; sourceUserVote++) {
			double minRmse = Double.MAX_VALUE;
			int minRmseRecommendation = clulessStrategy == CluelessRecommendationStrategy.DISTRIBUTE ?
					VotesDistributionMessage.CLUELESS_REC : (VotesDistributionMessage.MIN_VOTE + VotesDistributionMessage.MAX_VOTE) / 2;
			for (int sourceUserRecommendationToDest = 0; sourceUserRecommendationToDest
				< VotesDistributionMessage.MAX_VOTE; sourceUserRecommendationToDest++) {
				
				rmse = calcRMSE(sourceUserId, destUserId, sourceUserVote + 1, sourceUserRecommendationToDest + 1);
				if (minRmse > rmse) {					
					minRmse = rmse;
					minRmseRecommendation = sourceUserRecommendationToDest + 1;
				}
			}
			mapArray[sourceUserVote] = minRmseRecommendation;
			
			mappingsTotal++;
			if (minRmseRecommendation != VotesDistributionMessage.CLUELESS_REC) {
				mappingsValuable++;
			}
		}
		return mapArray;
	}		

	@Override
	public int map(int sourceUserId, int destUserId, int sourceUserVote) {
		return votesMap.get(new MessageKey(sourceUserId, destUserId))[sourceUserVote - 1];
	}
	
	@Override
	public double getValuableMappingDistribution() {
		if (mappingsTotal > 0) {
			return (double) mappingsValuable / (double) mappingsTotal * 100.d;
		} else {
			return 0;
		}
	}
}
