package graphframework;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

/**
 * Edge Policer that bases its decision upon the current number of outgoing
 * edges. No more than predefined number of edges are allowed to participate
 * in the graph.
 * 
 * @author Assaf Mizrachi
 *
 * @param <V> the vertex object
 * @param <E> the edge object
 */
public class LimitedOutDegreeEdgePolicer<V, E> extends AbstractEdgeAdditionPolicer<V, E>{
	
	private int maxDegree;
	
	
	
	/**
	 * Creates a Policer for a specified max out-going degree.
	 * 
	 * @param maxDegree the maximum degree
	 */
	public LimitedOutDegreeEdgePolicer(int maxDegree) {
		this(maxDegree, null);
	}
	
	public LimitedOutDegreeEdgePolicer(int maxDegree, EdgeAdditionPolicer<V, E> decorator) {
		super(decorator);
		this.maxDegree = maxDegree;		
	}

	@Override
	protected boolean caculate(V from, V to, Graph<V, E> graph) {
		return ((DirectedGraph<V, E>) graph).outDegreeOf(from) < maxDegree;
	}

}
