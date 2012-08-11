package strategy;

import graphframework.TrustGraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;

import algframework.BoundedStepsRandomWalkIterator;
import algframework.PathElement;
import algframework.RandomWalkIterator;
import algframework.SinkVertexTraversalEvent;
import dataframework.Trust;
import dataframework.User;

/**
 * 
 * @author mizrachi
 *
 */
public class RandomWalkRecommendationSystem implements RecommendationStrategy {

	private PathElement<User, Trust> current;
	
	private int totalMessagesBudget;
	
	private int originalTotalMessagesBudget;
	
	private boolean voterEncountered;
	
	private boolean sinkNonVoterEncountered;
	
	private TrustGraph graph;
	
	private WalkerListener walkListener;
	
	private PriorityQueue<User> queriesQueue;
	
	private FileWriter writer;
	
	public RandomWalkRecommendationSystem(int totalMessagesBudget) {
		this.totalMessagesBudget = totalMessagesBudget;
		this.originalTotalMessagesBudget = totalMessagesBudget;
		this.walkListener = new WalkerListener();
	}
		
	@Override
	public void recommend(TrustGraph graph, Map<Integer, Double> votes,
			//TODO export result to non-null output file.
			Set<User> queries, File output) {
		
		if (votes.isEmpty()) {
			throw new IllegalArgumentException("Voters map is empty. Recommendation can be supplied only if at least single voter exists.");
		}
		
		this.writer = null;
		if (output != null) {
			try {
				writer = new FileWriter(output);
			} catch (IOException e) {
				System.err.println("Could not open output file");
				return;
			}
		}
		
		this.graph = graph;
		this.queriesQueue = new PriorityQueue<User>(queries.size(), createQueriesComperator());
		this.queriesQueue.addAll(queries);
		//initializes graph with initial votes
		for (User user : graph.vertexSet()) {
			user.reset();
			if (votes.keySet().contains(user.getId())) {
				user.setVoter(true);
				user.setValue(votes.get(user.getId()));
			} else {
				user.setVoter(false);
				user.setValue(0);
			}
		}
				
		while (totalMessagesBudget > 0) {
			User source = getNextSource();
			current = new PathElement<User, Trust>(source);
			RandomWalkIterator<User, Trust> walker = new BoundedStepsRandomWalkIterator<User, Trust>(
					graph, source, getWalksBudget(source));
			
			walker.addTraversalListener(walkListener);
			voterEncountered = false;
			sinkNonVoterEncountered = false;
			while (!voterEncountered && !sinkNonVoterEncountered && walker.hasNext()) {
				walker.next();
			}
			walker.removeTraversalListener(walkListener);
		}
		
		//report final recommendations and closing file
		report(writer, graph);
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				System.err.println("Unable to close output file. Some of the information might be lost");
			}
		}
	}
	
	private User getNextSource() {
		//we cannot use peek() here since it does not triggers
		//queue reordering so we have to remove and re-add.
		User lessSuccessfulWalksWentTrhough = queriesQueue.remove();
		queriesQueue.add(lessSuccessfulWalksWentTrhough);
		return lessSuccessfulWalksWentTrhough;
	}
	
	private int getWalksBudget(User source) {
		return Math.max(totalMessagesBudget, totalMessagesBudget / this.graph.vertexSet().size());
	}
	
	private Comparator<User> createQueriesComperator() {
		return new FairComparator();
	}
	
	private class WalkerListener extends TraversalListenerAdapter<User, Trust> {

		@Override
		public void vertexTraversed(VertexTraversalEvent<User> e) {
			if (current.getVertex() != e.getVertex()) {
				current = new PathElement<User, Trust>(graph, current, 
					graph.getEdge(current.getVertex(), e.getVertex()));
			}
			totalMessagesBudget--;
			if (totalMessagesBudget % 1600 == 0) {
				report(writer, graph);
			}
			if (e.getVertex().isVoter()) {
				voterEncountered = true;
				updateRecommendations();
//				//outputs current recommendations to file
//				report(writer, graph);
			} else if (e instanceof SinkVertexTraversalEvent<?>) {
				sinkNonVoterEncountered = true;
			}
		}
	}
	
	private void updateRecommendations() {
		double vote = current.getVertex().getValue();
		while (current != null) {
			current = current.getPrevPathElement();
			if (current != null) {
				current.getVertex().updateValue(vote);
				totalMessagesBudget--;
				if (totalMessagesBudget % 1600 == 0) {
					report(writer, graph);
				}
			}
		}
	}
	
	private void report(FileWriter writer, TrustGraph graph) {
		if (writer != null) {
			try {			
				User[] users  = graph.vertexSet().toArray(new User[0]);
				Arrays.sort(users);
				String lineEnd = "\n";
				char delimiter = ',';
				for (User user : users) {
					writer.write(String.valueOf(user.getValue()));
					writer.write(delimiter);		
				}
				writer.write(String.valueOf(originalTotalMessagesBudget - totalMessagesBudget));
				writer.write(lineEnd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Compares between two users according to the number of times they were 
	 * @author mizrachi
	 *
	 */
	private class FairComparator implements Comparator<User> {

		@Override
		public int compare(User o1, User o2) {
			return (int) o1.getHitsNumber() - (int) o2.getHitsNumber();
		}
		
	}
}
