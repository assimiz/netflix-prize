package graphframework;

import org.jgrapht.Graph;

/**
 * Accepts or denies new edge to graph
 * 
 * @author mizrachi
 *
 */
public interface EdgeAdditionPolicer<V, E> {

	/**
	 * Checks if a new edge to be added from the source to destination node
	 * should be accepted to the graph.
	 * 
	 * @param from the edge source
	 * @param to the edge destination
	 * @param graph the graph to add the edge to
	 * 
	 * @return <code>true</code> if edge is allowed to be aded to graph
	 * , <code>false</code> otherwise.
	 */
	public boolean isEdgeAllowed(V from, V to, Graph<V, E> graph);
}
