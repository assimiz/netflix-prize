package strategy;


/**
 * A key for a message that is passed between users in a message passing strategy.
 * @author mizrachi
 *
 */
public class MessageKey implements Comparable<MessageKey> {

	private int sourceUserId;
	private int destUserId;
	/**
	 * @param sourceUserId
	 * @param destUserId
	 */
	public MessageKey(int sourceUserId, int destUserId) {
		this.sourceUserId = sourceUserId;
		this.destUserId = destUserId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + destUserId;
		result = prime * result + sourceUserId;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageKey other = (MessageKey) obj;
		if (destUserId != other.destUserId)
			return false;
		if (sourceUserId != other.sourceUserId)
			return false;
		return true;
	}
	@Override
	public int compareTo(MessageKey o) {
		//FIXME this was not tested to see if correctly implements the comparable i/f.
		return hashCode() * sourceUserId - o.hashCode() * o.sourceUserId
			+ hashCode() * destUserId - o.hashCode() * o.destUserId;
	}
	@Override
	public String toString() {
		return "MessageKey [sourceUserId=" + sourceUserId + ", destUserId="
				+ destUserId + "]";
	}	
}
