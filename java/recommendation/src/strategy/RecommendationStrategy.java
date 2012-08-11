package strategy;

import graphframework.TrustGraph;

import java.io.File;
import java.util.Map;
import java.util.Set;

import dataframework.User;

/**
 * 
 * @author mizrachi
 *
 */
public interface RecommendationStrategy {

	/**
	 * Receives TrustGraph with set of voters and non-voters and outputs the recommendation to
	 * file. Graph structure is not manipulated by any means but users values will change to
	 * the recommended value. Output is in CSV format where the first column is the user id and
	 * the second is its recommendation.
	 * @param graph the trust graph.
	 * @param votes a map from a (voter) user id to its vote.
	 * @param queries set of non-voters to fill the recommendation for. Result may contains recommendation
	 * to additional nodes.
	 * @param output file to hold the progress and final result. File is in CSV format, the first line contains the
	 * user id and each following line contains the value of the corresponding user in a specific step.
	 * Last line holds the final result. File will be overridden if exist.
	 */
	public void recommend(TrustGraph graph, Map<Integer, Double> votes, Set<User> queries, File output);
}
