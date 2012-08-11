package algframework;

import org.jgrapht.Graph;

public class InfiniteRandomWalkIterator<V, E> extends RandomWalkIterator<V, E> {

	/**
	 * {@inheritDoc}
	 */
	public InfiniteRandomWalkIterator(Graph<V, E> graph, V startVertex,
			boolean isWeighted) {
		super(graph, startVertex, isWeighted);
	}

	/**
	 * {@inheritDoc}
	 */
	public InfiniteRandomWalkIterator(Graph<V, E> graph, V startVertex) {
		super(graph, startVertex);
	}

	@Override
	protected void encounterVertex(Object vertex, Object edge) {
		// I don't care
	}

	@Override
	protected boolean isExhausted() {
		// I'm never exhausted
		return false;
	}

}
