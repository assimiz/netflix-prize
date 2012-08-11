package strategy;

/**
 * Message that is passed between users in a message passing strategy.
 * @author mizrachi
 *
 */
public interface Message {

	public boolean isValuable();
	
	public double toRecommendation();
}
