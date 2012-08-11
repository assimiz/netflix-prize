package strategy;

import java.util.Arrays;

/**
 * Message containing distribution on all votes from 1 to 5 ladder.
 * 
 * @author mizrachi
 *
 */
public class VotesDistributionMessage implements Message {
	
	public final static int MIN_VOTE = 1;
	
	public final static int MAX_VOTE = 5;
	
	public final static int CLUELESS_REC = Integer.MAX_VALUE;

	//votes are on 1 to MAX_VOTE ladder. place in the array stands for the vote value.
	private double[] votesDistribution = new double[MAX_VOTE];
	//Message is valuable if at least one cell of the distribution array has a value
	//(note that the summust be 100).
	private boolean valuable = false;
	
	public VotesDistributionMessage(int vote) {
		if (vote == CLUELESS_REC) {
			valuable = false;
		} else {
			for (int i = 0; i < MAX_VOTE; i++) {
				if (i + 1 == vote) {
					votesDistribution[i] = 100;
				} else {
					votesDistribution[i] = 0;
				}
			}
			valuable = true;
		}
	}
	
	
	
	/**
	 * @param votesDistribution
	 */
	public VotesDistributionMessage(double[] votesDistribution) {		
		for (double d : votesDistribution) {
			if (d != 0.0) {				
				this.votesDistribution = votesDistribution;
				this.valuable = true;
				break;
			}
		}
	}



	@Override
	public boolean isValuable() {
		return valuable;
	}
	
	@Override
	public double toRecommendation() {
		double recommendation = 0;
		for (int i = 0; i < MAX_VOTE; i++) {
			recommendation += (i + 1) * votesDistribution[i] / 100.d;
		}
		return recommendation;
	}
	
	public double[] getVotesDisribution() {
		return votesDistribution;
	}



	@Override
	public String toString() {
		return Arrays.toString(votesDistribution);
	}
	
	
}
