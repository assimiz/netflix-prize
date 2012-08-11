package graphframework;

import org.jgrapht.Graph;

/**
 * Abstract Edge Policer.
 * 
 * @author Assaf Mizrachi
 *
 * @param <V> the vertex object
 * @param <E> the edge object
 */
public abstract class AbstractEdgeAdditionPolicer<V, E> implements EdgeAdditionPolicer<V, E>{

	protected EdgeAdditionPolicer<V, E> decorator;
	
	/**
	 * Create a Policer with additional decorator policer. Only edges that 
	 * are allowed by both Policers will be allowed to participate in the graph.
	 * 
	 * @param decorator another Policer.
	 */
	public AbstractEdgeAdditionPolicer(EdgeAdditionPolicer<V, E> decorator) {
		this.decorator = decorator;
	}
	
	@Override
	public boolean isEdgeAllowed(V from, V to, Graph<V, E> graph) {
		return caculate(from, to, graph) &&
			(decorator != null ? decorator.isEdgeAllowed(from, to, graph) : true);
	}

	protected abstract boolean caculate(V from, V to, Graph<V, E> graph);
}
