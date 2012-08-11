package dataframework;

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * Level of trust between two users.
 * 
 * @author mizrachi
 *
 */
public class Trust extends DefaultWeightedEdge implements Comparable<Trust> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8892524052759699555L;

	/**
	 * @return the level of trust
	 */
	public double getLevel() {
		return getWeight();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf(getWeight());
	}

	@Override
	public int compareTo(Trust o) {
		return (int) (Math.round(this.getWeight() - o.getWeight()));
	}
	
	
}
