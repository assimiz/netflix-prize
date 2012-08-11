package strategy;


/**
 * Maps vote in transparent manner, i.e. each vote is mapped
 * to itself.
 * 
 * @author mizrachi
 *
 */
public class TransparentVotesMapper implements VotesMapper {

	@Override
	public int map(int sourceUserId, int destUserId, int sourceUserVote) {
		return sourceUserVote;
	}

}
