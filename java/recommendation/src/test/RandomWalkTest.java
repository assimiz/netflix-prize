package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;

import algframework.BoundedStepsRandomWalkIterator;
import algframework.SinkVertexTraversalEvent;
import dataframework.Trust;
import dataframework.User;

public class RandomWalkTest {

	public static void main(String[] args) throws IOException {
		Graph<User, Trust> graph = GraphBuildTest.buildGraph();
		BoundedStepsRandomWalkIterator<User, Trust> iter = new 
			BoundedStepsRandomWalkIterator<User, Trust>(graph, null, true, 10000);
		iter.setReuseEvents(true);
		
		SteadyStateDistributionCalculator<User, Trust> calc = 
			new SteadyStateDistributionCalculator<User, Trust>();
		iter.addTraversalListener(calc);
		
		int i = 0;
		while (iter.hasNext()) {
			i++;
			iter.next();
			StringBuilder builder = new StringBuilder();
			builder.append("step " + i + ": ");
			for (User user : graph.vertexSet()) {
				builder.append(user + " = " + calc.getSteadyStateDistribution(user) + ", ");
			}
			System.out.println(builder.toString());
		}
	}
	
	private static class SteadyStateDistributionCalculator<V, E> extends TraversalListenerAdapter<V, E> {

		private Map<V, Integer> vertexToTraveralsMap = new HashMap<V, Integer>();
		
		private int totalSteps = 0;
		
		@Override
		public void vertexTraversed(VertexTraversalEvent<V> e) {
			if (e instanceof SinkVertexTraversalEvent<?>) {
				System.out.println("############################ Sink vertex reached ############################");
			}
			
			totalSteps++;
			Integer previousVal = vertexToTraveralsMap.get(e.getVertex());
			if (previousVal != null) {
				vertexToTraveralsMap.put(e.getVertex(), previousVal + 1);
			} else {
				vertexToTraveralsMap.put(e.getVertex(), 1);
			}
			
		}
		
		public float getSteadyStateDistribution(V v) {
			Integer val = vertexToTraveralsMap.get(v);
			if (val != null) {
				return val / (float) totalSteps;
			} else {
				return 0;
			}
		}
	}
}
