package algframework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.traverse.AbstractGraphIterator;

public abstract class RandomWalkIterator<V, E> extends AbstractGraphIterator<V, E> {

	public RandomWalkIterator(Graph<V, E> graph, V startVertex) {
		this(graph, startVertex, true);
	}
	
	public RandomWalkIterator(Graph<V, E> graph, V startVertex, boolean isWeighted) {
		setCrossComponentTraversal(false);
		this.graph = graph;
		this.isWeighted = isWeighted;
		this.isDirected = graph instanceof DirectedGraph<?, ?>;
		reusableEdgeEvent = new FlyweightEdgeEvent<V, E>(this, null);
        reusableVertexEvent = new FlyweightVertexEvent<V>(this, null);
		if (startVertex == null) {
			if (graph.vertexSet().size() > 0) {
				currentVertex = graph.vertexSet().iterator().next();
			}
		} else if (graph.containsVertex(startVertex)){
			currentVertex = startVertex;			
		} else {
			throw new IllegalArgumentException("graph must contain the start vertex");
		}
		sinkReached = false;
	}

	/**
	 * Check if this walk is exhausted. Calling {@link #next()} on
	 * exhausted iterator will throw {@link NoSuchElementException}.
	 * 
	 * @return <code>true</code>if this iterator is exhausted,
	 * <code>false</code> otherwise.
	 */
	protected abstract boolean isExhausted();
	
	/**
     * Update data structures every time we see a vertex.
     *
     * @param vertex the vertex encountered
     * @param edge the edge via which the vertex was encountered, or null if the
     * vertex is a starting point
     */
    protected abstract void encounterVertex(V vertex, E edge);

	@Override
	public boolean hasNext() {
		//TODO what if the node is sink?
		return currentVertex != null && !isExhausted();
	}

	@Override
	public V next() {
		Set<E> potentialEdges;
		V nextVertex;
		if (isDirected) {
			potentialEdges = ((DirectedGraph<V, E>) graph).outgoingEdgesOf(currentVertex);
		} else{
			potentialEdges = graph.edgesOf(currentVertex);
		}
		E drawnEdge = drawEdge(potentialEdges);
		if (drawnEdge != null) {
			if (isDirected) {
				nextVertex = graph.getEdgeTarget(drawnEdge);
			} else {
				nextVertex = graph.getEdgeTarget(drawnEdge) != currentVertex ? 
						graph.getEdgeTarget(drawnEdge) : graph.getEdgeSource(drawnEdge);
			}		
			encounterVertex(nextVertex, drawnEdge);
			fireEdgeTraversed(createEdgeTraversalEvent(drawnEdge));
			fireVertexTraversed(createVertexTraversalEvent(nextVertex));
			currentVertex = nextVertex;
			return nextVertex;
		} else {
			if (sinkReached) {
				fireVertexTraversed(createVertexTraversalEvent(currentVertex));
			} else {
				sinkReached = true;
				fireVertexTraversed(createSinkVertexTraversalEvent(currentVertex));
			}
			//this means we reached sink vertex (no out going edges)
			return currentVertex;
		}
	}

	private EdgeTraversalEvent<V, E> createEdgeTraversalEvent(E edge) {
		if (isReuseEvents()) {
			reusableEdgeEvent.setEdge(edge);

			return reusableEdgeEvent;
		} else {
			return new EdgeTraversalEvent<V, E>(this, edge);
		}
	}

	private VertexTraversalEvent<V> createVertexTraversalEvent(V vertex) {
		if (isReuseEvents()) {
			reusableVertexEvent.setVertex(vertex);

			return reusableVertexEvent;
		} else {
			return new VertexTraversalEvent<V>(this, vertex);
		}
	}
	
	private SinkVertexTraversalEvent<V> createSinkVertexTraversalEvent(V vertex) {
		if (isReuseEvents()) {
			reusableSinkVertexEvent.setVertex(vertex);

			return reusableSinkVertexEvent;
		} else {
			return new SinkVertexTraversalEvent<V>(this, vertex);
		}
	}
	
	private E drawEdge(Set<E> edges) {
		if (edges.isEmpty()) {
			return null;
		}
		
		int drawn;
		List<E> list = new ArrayList<E>(edges);		
		//TODO check if correct
		if (isWeighted) {
			Iterator<E> safeIter = list.iterator();
			double border = Math.random() * getTotalWeight(list);
			double d = 0;
			drawn = -1;
			do {
				d += graph.getEdgeWeight(safeIter.next());
				drawn++;
				//TODO check if < is correct
			} while (d < border);
		} else {
			drawn = (int) Math.floor(Math.random() * list.size());
		}
		return list.get(drawn);
	}
	
	private double getTotalWeight(Collection<E> edges) {
		double total = 0;
		for (E e : edges) {
			total += graph.getEdgeWeight(e);
		}
		return total;
	}
	
	/**
     * A reusable vertex event.
     *
     * @author Barak Naveh
     * @since Aug 11, 2003
     */
    static class FlyweightVertexEvent<VV> extends VertexTraversalEvent<VV> {
		private static final long serialVersionUID = 3834024753848399924L;

		/**
		 * @see VertexTraversalEvent#VertexTraversalEvent(Object, Object)
		 */
		public FlyweightVertexEvent(Object eventSource, VV vertex) {
			super(eventSource, vertex);
		}

		/**
		 * Sets the vertex of this event.
		 * 
		 * @param vertex
		 *            the vertex to be set.
		 */
		protected void setVertex(VV vertex) {
			this.vertex = vertex;
		}
	}
    
    /**
     * A reusable vertex event.
     *
     * @author Barak Naveh
     * @since Aug 11, 2003
     */
    static class FlyweightSinkVertexEvent<VV> extends SinkVertexTraversalEvent<VV> {
		private static final long serialVersionUID = 3834024753848399924L;

		/**
		 * @see SinkVertexTraversalEvent#SinkVertexTraversalEvent(Object, Object)
		 */
		public FlyweightSinkVertexEvent(Object eventSource, VV vertex) {
			super(eventSource, vertex);
		}

		/**
		 * Sets the vertex of this event.
		 * 
		 * @param vertex
		 *            the vertex to be set.
		 */
		protected void setVertex(VV vertex) {
			this.vertex = vertex;
		}
	}
    
    /**
	 * A reusable edge event.
	 * 
	 * @author Barak Naveh
	 * @since Aug 11, 2003
	 */
    static class FlyweightEdgeEvent<VV, localE> extends
			EdgeTraversalEvent<VV, localE> {
		private static final long serialVersionUID = 4051327833765000755L;

		/**
		 * @see EdgeTraversalEvent#EdgeTraversalEvent(Object, Edge)
		 */
		public FlyweightEdgeEvent(Object eventSource, localE edge) {
			super(eventSource, edge);
		}

		/**
		 * Sets the edge of this event.
		 * 
		 * @param edge
		 *            the edge to be set.
		 */
		protected void setEdge(localE edge) {
			this.edge = edge;
		}
	}

	private V currentVertex;
	private final Graph<V, E> graph;
	private final boolean isDirected;
	private final boolean isWeighted;
	private FlyweightVertexEvent<V> reusableVertexEvent;
	private FlyweightSinkVertexEvent<V> reusableSinkVertexEvent;
	private FlyweightEdgeEvent<V, E> reusableEdgeEvent;
	private boolean sinkReached;
}
