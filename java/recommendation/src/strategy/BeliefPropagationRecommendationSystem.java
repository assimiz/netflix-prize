package strategy;


/**
 * Averaging Recommendation System with a minor twist in its behavior, vertices
 * ignore other's recommendation when updating each other.
 * 
 * @author mizrachi
 *
 */
public class BeliefPropagationRecommendationSystem extends AveragingRecommendationSystem {

	
	public BeliefPropagationRecommendationSystem(int totalMessagesBudget) {
		//we do not take in account messsages sent from the neighbor that
		//is currently being updated.
		super(totalMessagesBudget, true, new TransparentVotesMapper());
	}
	
	public BeliefPropagationRecommendationSystem(int totalMessagesBudget, VotesMapper votesMapper) {
		//we do not take in account messsages sent from the neighbor that
		//is currently being updated.
		super(totalMessagesBudget, true, votesMapper);
	}
	
}
