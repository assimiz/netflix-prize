package strategy;


/**
 * Maps source vote to other vote. For example, this may be used to say something
 * like say when user X votes A, User Y votes B according to some mapping strategy.
 * 
 * @author mizrachi
 *
 */
public interface VotesMapper {

	/**
	 * Maps a vote for a specified movie from one user to another.
	 * 
	 * @param sourceUserId the user to map from
	 * @param destUserId the user to map to
	 * @param sourceUserVote the vote of the user to map from
	 * @return the expected vote (recommendation) of the destination user 
	 */
	public int map(int sourceUserId, int destUserId, int sourceUserVote);
}
