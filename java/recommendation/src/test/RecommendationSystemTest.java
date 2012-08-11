package test;

import graphframework.TrustGraph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import misc.BidirectionalLinearGraphGenerator;

import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GridGraphGenerator;

import strategy.AveragingRecommendationSystem;
import strategy.BeliefPropagationRecommendationSystem;
import strategy.MinRmseVotesMapper;
import strategy.AbstractDesignatedVotesMapper.CluelessRecommendationStrategy;
import dataframework.Trust;
import dataframework.User;
import dbanalysis.DBMovie;
import dbanalysis.DBUser;
import dbanalysis.FastNetflixDB;
import dbanalysis.Rent;

public class RecommendationSystemTest {
	
	public static int VOTE_X = 2;
	public static int VOTE_Y = 5;
	
	public static int VOTE_A = 4;
	public static int VOTE_B = 2;
	public static int VOTE_C = 3;

	public static void main(String[] args) {
		
		line(10);
		grid(5, 5);
		beforeManipulation();
		afterManipulation();
		weightedGraph();
		singletonCluelessGraph();
	}
	
	private static void singletonCluelessGraph() {
		System.out.println("\nTest for singleton graph started...");
		TrustGraph graph = new TrustGraph();
		User X = new User(0);
		User Y = new User(1);
		graph.addVertex(X);
		graph.addVertex(Y);
		Map<Integer, Double> votes = new HashMap<Integer, Double>();
		votes.put(Y.getId(), (double) VOTE_Y);
		Set<User> queries = new HashSet<User>();
		queries.add(X);
		FastNetflixDB db = new FastNetflixDB();
		
		int totalMessagesBudget = 100;
		
		BeliefPropagationRecommendationSystem bps = new BeliefPropagationRecommendationSystem(totalMessagesBudget);
		System.out.println("BP system for the singleton graph started");
		bps.recommend(graph, votes, queries, null);
		System.out.println("BP System reccommendation:");
		printRecommendations(graph);
		
		MinRmseVotesMapper mapper = new MinRmseVotesMapper(db, graph, CluelessRecommendationStrategy.DISTRIBUTE);
		System.out.println("MinRmseVotesMapper percentage of valuable mappings is " + mapper.getValuableMappingDistribution());
		
		BeliefPropagationRecommendationSystem rmsebp = new BeliefPropagationRecommendationSystem(
				totalMessagesBudget, mapper);
		System.out.println("BP system with min RMSE votes mapper for the singleton graph started");		
		rmsebp.recommend(graph, votes, queries, null);
		System.out.println("BP System with min RMSE votes mapper reccommendation:");
		printRecommendations(graph);
		
		AveragingRecommendationSystem avgs = new AveragingRecommendationSystem(totalMessagesBudget);
		System.out.println("AVG system for the singleton graph started");
		avgs.recommend(graph, votes, queries, null);
		System.out.println("AVG System reccommendation:");
		printRecommendations(graph);
		
		AveragingRecommendationSystem rmseavg = new AveragingRecommendationSystem(
				totalMessagesBudget, false, mapper);
		System.out.println("AVG system with min RMSE votes mapper for the singleton graph started");
		rmseavg.recommend(graph, votes, queries, null);
		System.out.println("AVG System with min RMSE votes mapper reccommendation:");
		printRecommendations(graph);
	}

	private static void weightedGraph() {
		System.out.println("\nTest for weighted graph started...");
		
		TrustGraph graph = new TrustGraph();
		User X = new User(0);
		User Y = new User(1);
		User A = new User(2);
		User B = new User(3);
		User C = new User(4);
		
		graph.addVertex(X);
		graph.addVertex(Y);
		graph.addVertex(A);
		graph.addVertex(B);
		graph.addVertex(C);
		
		Trust t;
		t = graph.addEdge(X, B);
		graph.setEdgeWeight(t, 50);
		
		t = graph.addEdge(X, C);
		graph.setEdgeWeight(t, 20);
		
		t = graph.addEdge(X, Y);
		graph.setEdgeWeight(t, 10);
		
		t = graph.addEdge(Y, X);
		graph.setEdgeWeight(t, 20);
		
		t = graph.addEdge(Y, A);
		graph.setEdgeWeight(t, 10);
		
		Map<Integer, Double> votes = new HashMap<Integer, Double>();
		votes.put(A.getId(), (double) VOTE_A);
		votes.put(B.getId(), (double) VOTE_B);
		votes.put(C.getId(), (double) VOTE_C);
		Set<User> queries = new HashSet<User>();
		queries.add(X);
		queries.add(Y);
		
		int totalMessagesBudget = 100;
		
		BeliefPropagationRecommendationSystem bps = new BeliefPropagationRecommendationSystem(totalMessagesBudget);
		System.out.println("BP system for the weighted graph started");
		bps.recommend(graph, votes, queries, null);
		System.out.println("BP System reccommendation:");
		printRecommendations(graph);
		
		BeliefPropagationRecommendationSystem bpswvm = new BeliefPropagationRecommendationSystem(totalMessagesBudget);
		System.out.println("BP system with transparent votes mapper for the weighted graph started");
		bpswvm.recommend(graph, votes, queries, null);
		System.out.println("BP System with transparent votes mapper reccommendation:");
		printRecommendations(graph);
		
		FastNetflixDB db = new FastNetflixDB();
		db.writeUser(new DBUser(X.getId()));
		db.writeUser(new DBUser(Y.getId()));
		db.writeUser(new DBUser(A.getId()));
		db.writeUser(new DBUser(B.getId()));
		db.writeUser(new DBUser(C.getId()));
		
		db.writeMovie(new DBMovie(1));
		db.writeRent(new Rent(1, X.getId(), new Date(), 1));
		db.writeRent(new Rent(1, Y.getId(), new Date(), 2));		
		db.writeRent(new Rent(1, B.getId(), new Date(), 3));
		db.writeRent(new Rent(1, C.getId(), new Date(), 4));
		db.writeRent(new Rent(1, A.getId(), new Date(), 2));
		
		db.writeMovie(new DBMovie(2));
		db.writeRent(new Rent(2, X.getId(), new Date(), 3));
		db.writeRent(new Rent(2, Y.getId(), new Date(), 4));		
		db.writeRent(new Rent(2, B.getId(), new Date(), 3));
		db.writeRent(new Rent(2, C.getId(), new Date(), 4));
		db.writeRent(new Rent(2, A.getId(), new Date(), 2));
		
		db.writeMovie(new DBMovie(3));
		db.writeRent(new Rent(3, X.getId(), new Date(), 3));
		db.writeRent(new Rent(3, Y.getId(), new Date(), 4));		
		db.writeRent(new Rent(3, B.getId(), new Date(), 5));
		db.writeRent(new Rent(3, C.getId(), new Date(), 1));
		db.writeRent(new Rent(3, A.getId(), new Date(), 4));
		
		MinRmseVotesMapper mapper = new MinRmseVotesMapper(db, graph, CluelessRecommendationStrategy.DISTRIBUTE);
		System.out.println("MinRmseVotesMapper percentage of valuable mappings is " + mapper.getValuableMappingDistribution());
		
		BeliefPropagationRecommendationSystem rmsebp = new BeliefPropagationRecommendationSystem(
				totalMessagesBudget, mapper);
		System.out.println("BP system with min RMSE votes mapper for the weighted graph started");
		rmsebp.recommend(graph, votes, queries, null);
		System.out.println("BP System with min RMSE votes mapper reccommendation:");
		printRecommendations(graph);
		
		AveragingRecommendationSystem avgs = new AveragingRecommendationSystem(totalMessagesBudget);
		System.out.println("AVG system for the weighted graph started");
		avgs.recommend(graph, votes, queries, null);
		System.out.println("AVG System reccommendation:");
		printRecommendations(graph);
		
		AveragingRecommendationSystem avgswvm = new AveragingRecommendationSystem(totalMessagesBudget);
		System.out.println("AVG system with transparent votes mapper for the weighted graph started");
		avgswvm.recommend(graph, votes, queries, null);
		System.out.println("AVG System with transparent votes mapper reccommendation:");
		printRecommendations(graph);
		
		AveragingRecommendationSystem rmseavg = new AveragingRecommendationSystem(
				totalMessagesBudget, false, mapper);
		System.out.println("AVG system with min RMSE votes mapper for the weighted graph started");
		rmseavg.recommend(graph, votes, queries, null);
		System.out.println("AVG System with min RMSE votes mapper reccommendation:");
		printRecommendations(graph);
	}

	private static void beforeManipulation() {
		System.out.println("\nTest for special graph (before manipulation) started...");
		
		TrustGraph graph = new TrustGraph();
		User X = new User(0);
		User Y = new User(4);
		User A = new User(1);
		User B = new User(2);
		User C = new User(3);
		graph.addVertex(X);
		graph.addVertex(Y);
		graph.addVertex(A);
		graph.addVertex(B);
		graph.addVertex(C);
		graph.addEdge(A, X);
		graph.addEdge(A, B);
		graph.addEdge(B, C);
		graph.addEdge(C, Y);
		graph.addEdge(C, A);
		Map<Integer, Double> votes = new HashMap<Integer, Double>();
		votes.put(X.getId(), (double) VOTE_X);
		votes.put(Y.getId(), (double) VOTE_Y);
		Set<User> queries = new HashSet<User>();
		queries.add(A);
		queries.add(B);
		queries.add(C);
		
		int totalMessagesBudget = 100000;
//		RandomWalkRecommendationSystem rws = new RandomWalkRecommendationSystem(totalMessagesBudget);
//		System.out.println("RW system for the line graph started");
//		rws.recommend(graph, votes, queries, null);
//		System.out.println("RW System reccommendation:");
//		printRecommendations(graph);
		
		BeliefPropagationRecommendationSystem bps = new BeliefPropagationRecommendationSystem(totalMessagesBudget);
		System.out.println("BP system for the special graph (before manipulation) started");
		bps.recommend(graph, votes, queries, null);
		System.out.println("BP System reccommendation:");
		printRecommendations(graph);
		
		AveragingRecommendationSystem avgs = new AveragingRecommendationSystem(totalMessagesBudget);
		System.out.println("AVG system for the special graph (before manipulation) started");
		avgs.recommend(graph, votes, queries, null);
		System.out.println("AVG System reccommendation:");
		printRecommendations(graph);
	}
	
	private static void afterManipulation() {
		System.out.println("\nTest for special graph (after manipulation) started...");
		
		TrustGraph graph = new TrustGraph();
		User X = new User(0);
		User Y = new User(4);
		User A = new User(1);
		User B = new User(2);
		User C = new User(3);
		graph.addVertex(X);
		graph.addVertex(Y);
		graph.addVertex(A);
		graph.addVertex(B);
		graph.addVertex(C);
		graph.addEdge(A, X);
		graph.addEdge(A, C);
		graph.addEdge(B, C);
		graph.addEdge(C, Y);
		graph.addEdge(C, A);
		Map<Integer, Double> votes = new HashMap<Integer, Double>();
		votes.put(X.getId(), (double) VOTE_X);
		votes.put(Y.getId(), (double) VOTE_Y);
		Set<User> queries = new HashSet<User>();
		queries.add(A);
		queries.add(B);
		queries.add(C);
		
		int totalMessagesBudget = 100000;
//		RandomWalkRecommendationSystem rws = new RandomWalkRecommendationSystem(totalMessagesBudget);
//		System.out.println("RW system for the line graph started");
//		rws.recommend(graph, votes, queries, null);
//		System.out.println("RW System reccommendation:");
//		printRecommendations(graph);
		
		BeliefPropagationRecommendationSystem bps = new BeliefPropagationRecommendationSystem(totalMessagesBudget);
		System.out.println("BP system for the special graph (after manipulation) started");
		bps.recommend(graph, votes, queries, null);
		System.out.println("BP System reccommendation:");
		printRecommendations(graph);
		
		AveragingRecommendationSystem avgs = new AveragingRecommendationSystem(totalMessagesBudget);
		System.out.println("AVG system for the special graph (after manipulation) started");
		avgs.recommend(graph, votes, queries, null);
		System.out.println("AVG System reccommendation:");
		printRecommendations(graph);
	}

	private static void line(int size) {
		System.out.println("\nTest for line graph started...");
		
		TrustGraph graph = new TrustGraph();
		BidirectionalLinearGraphGenerator<User, Trust> graphGenerator = 
			new BidirectionalLinearGraphGenerator<User, Trust>(size);
		Map<String, User> resultMap = new HashMap<String, User>();
		graphGenerator.generateGraph(graph, uniqueUserVertexFeactory(),
				resultMap);
		Map<Integer, Double> votes = new HashMap<Integer, Double>();
		Set<User> queries = new HashSet<User>();
		for (User user : graph.vertexSet()) {
			//making both sides of the lines voters.
			if (Arrays.asList(1).contains(user.getId())) {
				votes.put(user.getId(), (double) VOTE_X);
			} else if (Arrays.asList(size).contains(user.getId())) {
				votes.put(user.getId(), (double) VOTE_Y);
			} else {
				queries.add(user);
			}
		}
		
		int totalMessagesBudget = 100000;
//		RandomWalkRecommendationSystem rws = new RandomWalkRecommendationSystem(totalMessagesBudget);
//		System.out.println("RW system for the line graph started");
//		rws.recommend(graph, votes, queries, null);
//		System.out.println("RW System reccommendation:");
//		printRecommendations(graph);
		
		BeliefPropagationRecommendationSystem bps = new BeliefPropagationRecommendationSystem(totalMessagesBudget);
		System.out.println("BP system for the line graph started");
		bps.recommend(graph, votes, queries, null);
		System.out.println("BP System reccommendation:");
		printRecommendations(graph);
		
		BeliefPropagationRecommendationSystem bpswvm = new BeliefPropagationRecommendationSystem(totalMessagesBudget);
		System.out.println("BP system with transparent votes mapper for the line graph started");
		bpswvm.recommend(graph, votes, queries, null);
		System.out.println("BP System with transparent votes mapper reccommendation:");
		printRecommendations(graph);
		
		AveragingRecommendationSystem avgs = new AveragingRecommendationSystem(totalMessagesBudget);
		System.out.println("AVG system for the line graph started");
		avgs.recommend(graph, votes, queries, null);
		System.out.println("AVG System reccommendation:");
		printRecommendations(graph);
		
		AveragingRecommendationSystem avgswvm = new AveragingRecommendationSystem(totalMessagesBudget);
		System.out.println("AVG system with transparent votes mapper for the line graph started");
		avgswvm.recommend(graph, votes, queries, null);
		System.out.println("AVG System with transparent votes mapper reccommendation:");
		printRecommendations(graph);
	}
	
	private static void grid(int rows, int cols) {
		System.out.println("\nTest for grid graph started...");
		
		TrustGraph graph = new TrustGraph();		
		GridGraphGenerator<User, Trust> graphGenerator = new GridGraphGenerator<User, Trust>(rows, cols);
		Map<String, User> resultMap = new TreeMap<String, User>();
		graphGenerator.generateGraph(graph, uniqueUserVertexFeactory(),
				resultMap);
		Map<Integer, Double> votes = new HashMap<Integer, Double>();
		Set<User> queries = new HashSet<User>();
		
		//setting the voters.
		for (User user : graph.vertexSet()) {
			if (Arrays.asList(1).contains(user.getId())) {
				votes.put(user.getId(), (double) VOTE_X);
			} else if (Arrays.asList(25).contains(user.getId())) {
				votes.put(user.getId(), (double) VOTE_Y);
			} else {
				queries.add(user);
			}
		}
		
		int totalMessagesBudget = 1000000;
//		RandomWalkRecommendationSystem rws = new RandomWalkRecommendationSystem(totalMessagesBudget);
//		System.out.println("RW system for the grid graph started");
//		rws.recommend(graph, votes, queries, new File("RW-" + rows + "x" + cols + "gridReport.csv"));
//		System.out.println("RW System reccommendation:");
//		printRecommendations(graph);
//		outputRecommendationsForGridGraph(graph, new File("RW-" + rows + "x" + cols + "grid.csv"), rows);
		
		BeliefPropagationRecommendationSystem bps = new BeliefPropagationRecommendationSystem(totalMessagesBudget);
		System.out.println("BP system for the grid graph started");
		bps.recommend(graph, votes, queries, new File("BP-" + rows + "x" + cols + "gridReport.csv"));
		System.out.println("BP System reccommendation:");
		printRecommendations(graph);
		outputRecommendationsForGridGraph(graph, new File("BP-" + rows + "x" + cols + "grid.csv"), rows);
		
		BeliefPropagationRecommendationSystem bpswvm = new BeliefPropagationRecommendationSystem(totalMessagesBudget);
		System.out.println("BP system with transparent votes mapper for the grid graph started");
		bpswvm.recommend(graph, votes, queries, new File("BPWVM-" + rows + "x" + cols + "gridReport.csv"));
		System.out.println("BP System with transparent votes mapper reccommendation:");
		printRecommendations(graph);
		outputRecommendationsForGridGraph(graph, new File("BPWVM-" + rows + "x" + cols + "grid.csv"), rows);		
		
		AveragingRecommendationSystem avgs = new AveragingRecommendationSystem(totalMessagesBudget);
		System.out.println("AVG system for the grid graph started");
		avgs.recommend(graph, votes, queries, new File("AVG-" + rows + "x" + cols + "gridReport.csv"));
		System.out.println("AVG System reccommendation:");
		printRecommendations(graph);
		outputRecommendationsForGridGraph(graph, new File("AVG-" + rows + "x" + cols + "grid.csv"), rows);
		
		AveragingRecommendationSystem avgswvm = new AveragingRecommendationSystem(totalMessagesBudget);
		System.out.println("AVG system with transparent votes mapper for the grid graph started");
		avgswvm.recommend(graph, votes, queries, new File("AVGWVM-" + rows + "x" + cols + "gridReport.csv"));
		System.out.println("AVG System with transparent votes mapper reccommendation:");
		printRecommendations(graph);
		outputRecommendationsForGridGraph(graph, new File("AVGWVM-" + rows + "x" + cols + "grid.csv"), rows);
	}
	
	private static void printRecommendations(TrustGraph graph) {
		for (User user : new TreeSet<User>(graph.vertexSet())) {
			if (user.isVoter()) {
				//this is not an error, I just want it to be painted in red on eclipse.
				System.out.println("User " + user.getId() + ", Vote = " + user.getValue());
			} else {
				System.out.println(user.getValue());
			}
		}
	}
	
	private static void outputRecommendationsForGridGraph(TrustGraph graph, File output, int rows) {
		try {
			FileWriter writer = new FileWriter(output);
			User[] users  = graph.vertexSet().toArray(new User[0]);
			Arrays.sort(users);
			int i = 0;
			String lineEnd = "\n";
			char delimiter = ',';
			for (User user : users) {
				writer.write(String.valueOf(user.getValue()));
				writer.write(delimiter);
				i++;
				if (i % rows == 0) {
					writer.write(lineEnd);
				}
			}		
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static VertexFactory<User> uniqueUserVertexFeactory() {
		return new VertexFactory<User>() {

			private int index = 1;
			@Override
			public User createVertex() {
				return new User(index++);
			}
			
		};
	}
}
