package algframework;

import org.jgrapht.Graph;

public class BoundedStepsRandomWalkIterator<V, E> extends RandomWalkIterator<V, E>{

	private int maxSteps;
	
	public BoundedStepsRandomWalkIterator(Graph<V, E> graph, V startVertex, int maxSteps) {
		this(graph, startVertex, true, maxSteps);
	}
	
	public BoundedStepsRandomWalkIterator(Graph<V, E> graph, V startVertex, boolean isWeighted, int maxSteps) {
		super(graph, startVertex, isWeighted);
		this.maxSteps = maxSteps;
	}
	
	@Override
	protected boolean isExhausted() {
		return maxSteps == 0;
	}

	@Override
	protected void encounterVertex(V vertex, E edge) {
		maxSteps--;
	}

}
