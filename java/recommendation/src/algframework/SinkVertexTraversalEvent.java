package algframework;

import org.jgrapht.event.VertexTraversalEvent;

/**
 * A traversal event for a graph sink vertex.
 * Will be fired if a specified sink (no outgoing edges
 * exists) vertex have been visited during the graph traversal. Other vertices
 * will not be traversed after a sink traversal event so further events can be
 * only regular VertexTraversalEvent of the sink vertex. This event will, however,
 * be fired only once.
 *
 * @param e the vertex traversal event.
 */
public class SinkVertexTraversalEvent<V> extends VertexTraversalEvent<V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7217953776366430420L;

	
	public SinkVertexTraversalEvent(Object eventSource, V vertex) {
		super(eventSource, vertex);
	}

}
