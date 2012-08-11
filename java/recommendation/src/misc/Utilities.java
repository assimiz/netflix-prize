package misc;

import graphframework.TrustCalculator;
import graphframework.TrustGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.alg.StrongConnectivityInspector;

import dataframework.Trust;
import dataframework.User;
import dbanalysis.NetflixDB;
import dbanalysis.Rent;
import dbanalysis.SqlReadOnlyNetflixDB;


public class Utilities {


	public static ConnectedComponentData[] getConnectedComponentsData(DirectedGraph<User, Trust> graph) {
		ConnectivityInspector<User, Trust> inspector = 
			new ConnectivityInspector<User, Trust>(graph);
		
		List<ConnectedComponentData> result = new ArrayList<ConnectedComponentData>();
		
		List<Set<User>> connectedComponents = inspector.connectedSets();
		int totalGraphSize = 0;
		for (Set<User> connectedSet : connectedComponents) {
			totalGraphSize += connectedSet.size();
		}
		for (Set<User> connectedSet : connectedComponents) {
			result.add(new ConnectedComponentData(connectedSet.size(), connectedSet.size() / (float) totalGraphSize * 100));
		}
		
		Collections.sort(result);
		return result.toArray(new ConnectedComponentData[result.size()]);
	}
	
	public static ConnectedComponentData[] getStronglyConnectedComponentsData(DirectedGraph<User, Trust> graph) {
		StrongConnectivityInspector<User, Trust> inspector = 
			new StrongConnectivityInspector<User, Trust>(graph);
		
		List<ConnectedComponentData> result = new ArrayList<ConnectedComponentData>();
		
		List<Set<User>> connectedComponents = inspector.stronglyConnectedSets();
		int totalGraphSize = 0;
		for (Set<User> connectedSet : connectedComponents) {
			totalGraphSize += connectedSet.size();
		}
		for (Set<User> connectedSet : connectedComponents) {
			result.add(new ConnectedComponentData(connectedSet.size(), connectedSet.size() / (float) totalGraphSize * 100));
		}
		
		Collections.sort(result);
		return result.toArray(new ConnectedComponentData[result.size()]);
	}
	
	public static double getGraphDiameter(DirectedGraph<User, Trust> graph) {
		FloydWarshallShortestPaths<User, Trust> diameterCalculator = new FloydWarshallShortestPaths<User, Trust>(graph);
		return diameterCalculator.getDiameter();
	}
	
	public static int getAverageRecommendation(List<Rent> rents) {
		if (rents.size() == 0) {
			throw new IllegalArgumentException("Cannot calculate average for empty list");
		}
		int sum = 0;
		for (Rent rent : rents) {
			sum += rent.getRate();
		}
		return Math.round((float) sum / (float) rents.size());
	}
	
	public static void reassignTrust(TrustGraph graph, TrustCalculator calc) {
		User src, dest;
		double weight;
		Set<Trust> illegalEdges = new HashSet<Trust>();
		for (Trust edge : graph.edgeSet()) {
			src = graph.getEdgeSource(edge);
			dest = graph.getEdgeTarget(edge);
			weight = calc.getTrust(src, dest);
			if (weight > 0) {
				graph.setEdgeWeight(edge, weight);
			} else {
				illegalEdges.add(edge);
			}			
		}
		graph.removeAllEdges(illegalEdges);		
		System.out.println("Removed " + illegalEdges.size() + " illegal weight edges while reassigned trust");
	}
	
	public static void powerTrust(TrustGraph graph, double power) {
		System.out.println("Powering all trusts by " + power);
		for (Trust edge : graph.edgeSet()) {
			graph.setEdgeWeight(edge, Math.pow(edge.getLevel(), 2));
		}
	}
	
	public static void printGraphMeasurements(String graphFileName) {
		System.out.println("Loading graph from persistent storage");
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new FileInputStream(graphFileName));
			final TrustGraph graph = (TrustGraph) ois.readObject();			
			ois.close();

			double totalWeight = 0;
			double totalEdges = 0;
			for (Trust edge : graph.edgeSet()) {
				totalEdges++;
				totalWeight += graph.getEdgeWeight(edge);
			}

			System.out.println("Total Number of Edges is " + totalEdges);
			System.out.println("Total Edges weight " + totalWeight);
			System.out.println("Average edge weight " + totalWeight / totalEdges);

			System.out.println("graph: " + "|V| = " + graph.vertexSet().size() + ", |E| = " + graph.edgeSet().size());
			System.out.println();
//			System.out.println("Diameter is " + getGraphDiameter(graph));
			System.out.println();
			System.out.println("connected:");
			System.out.println(Arrays.toString(Utilities.getConnectedComponentsData(graph)));
			System.out.println("strongly connected:");
			System.out.println(Arrays.toString(Utilities.getStronglyConnectedComponentsData(graph)));

			System.out.println("Sorted according to in-degree:");
			User[] users = graph.vertexSet().toArray(new User[0]);
			Arrays.sort(users, new Comparator<User>() {

				@Override
				public int compare(User o1, User o2) {
					return graph.inDegreeOf(o2) - graph.inDegreeOf(o1);
				}
			});

			File output = new File("output.csv");

			FileWriter writer = null;			

			writer = new FileWriter(output);
			writer.append("User ID, In Degree, Out Degree, #Rents\n");
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < users.length; i++) {
				builder.append(users[i].getId()).append(',').
				append(graph.inDegreeOf(users[i])).append(',').
				append(graph.outDegreeOf(users[i])).append(',').
				//				append(omitProbeDb.readUser(users[i].getId()).getNumOfRents()).append(',').
				append('\n');
			}
//			System.out.println(builder.toString());
			writer.write(builder.toString());
			writer.write('\n');
			
			List<Trust> edges = new ArrayList<Trust>();
			edges.addAll(graph.edgeSet());
			Collections.sort(edges, new Comparator<Trust>() {

				@Override
				public int compare(Trust o1, Trust o2) {
					return (int) (o1.getLevel()-o2.getLevel());
				}
			});
			builder = new StringBuilder();
			for (Trust edge : edges) {
				builder.append(edge).append('\n');
			}
			builder.append('\n');
			writer.write(builder.toString());

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void fillRealVotes(File recommendationsOutput) {
		
		System.out.println("Starting filling real votes for " + recommendationsOutput.getName());
		NetflixDB db = new SqlReadOnlyNetflixDB(true);
		try {
			File fixed = new File(recommendationsOutput.getParentFile(), recommendationsOutput.getName() + "_Fixed.csv");
			FileWriter writer = new FileWriter(fixed, false);
			
			BufferedReader br = new BufferedReader(new FileReader(recommendationsOutput));
			StringTokenizer commaTokenizer;
			//writing the header line
			String line = br.readLine();
			writer.append(line).append(", Actual Rate").append(String.valueOf('\n'));
			
			Rent rent;
			while (line != null) {
				line = br.readLine();
				if (line == null) {
					break;
				}
				commaTokenizer = new StringTokenizer(line, String.valueOf(','));
				rent = db.readRent(Integer.valueOf(commaTokenizer.nextToken()),
						Integer.valueOf(commaTokenizer.nextToken()), null);
				writer.append(line + String.valueOf(',') + String.valueOf(rent.getRate()))
					.append(String.valueOf('\n'));
			}
			System.out.println("Finihed filling real votes for " + recommendationsOutput.getName());
			writer.close();
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Calculates the Pearson correlation for two vectors
	 * @param scores1 left
	 * @param scores2 right
	 * 
	 * @return the Pearson correlation
	 */
	public static double getPearsonCorrelation(double[] scores1,double[] scores2){
		double result = 0;
		double sum_sq_x = 0;
		double sum_sq_y = 0;
		double sum_coproduct = 0;
		double mean_x = scores1[0];
		double mean_y = scores2[0];
		for(int i=2;i<scores1.length+1;i+=1){
			double sweep =Double.valueOf(i-1)/i;
			double delta_x = scores1[i-1]-mean_x;
			double delta_y = scores2[i-1]-mean_y;
			sum_sq_x += delta_x * delta_x * sweep;
			sum_sq_y += delta_y * delta_y * sweep;
			sum_coproduct += delta_x * delta_y * sweep;
			mean_x += delta_x / i;
			mean_y += delta_y / i;
		}
		double pop_sd_x = (double) Math.sqrt(sum_sq_x/scores1.length);
		double pop_sd_y = (double) Math.sqrt(sum_sq_y/scores1.length);
		double cov_x_y = sum_coproduct / scores1.length;
		result = cov_x_y / (pop_sd_x*pop_sd_y);
		return result;
	}
	
	/**
	 *  Calculate Pearson product-moment correlation coefficient
	 * @param point1     first point
	 * @param point2     second point
	 * @return     Pearson correlation coefficient
	 */
	public static double getPMCC(double[] point1, double[] point2) {
	    double suma = 0;
	    double sumb = 0;
	    double sumaSq = 0;
	    double sumbSq = 0;
	    double pSum = 0;
	    int n = point1.length;
	    for (int i = 0; i < point1.length; i++) {
	        suma = suma + point1[i];
	        sumb = sumb + point2[i];
	        sumaSq = sumaSq + point1[i] * point1[i];
	        sumbSq = sumbSq + point2[i] * point2[i];
	        pSum = pSum + point1[i] * point2[i];
	    }
	    double numerator = n * pSum - suma * sumb;
	    double denominator = Math.sqrt((n * sumaSq - suma * suma) * (n * sumbSq - sumb * sumb));
	    return numerator / denominator;
	}

	
	public static void main(String[] args) {
//		fillRealVotes(new File("C:\\Documents and Settings\\mizrachi\\My Documents\\Assaf\\" +
//				"Private\\Master\\Thesis\\assafm\\thesis\\Java\\RecommendationSystem" +
//				"\\recommendations_1000_Users.csv"));
		
//		TrustGraph graph = null;
//		String graphFileName = "C:\\Documents and Settings\\mizrachi\\My Documents\\Assaf\\" +
//				"Private\\Master\\Thesis\\assafm\\thesis\\Java\\RecommendationSystem" +
//				"\\graph_08-29_15-37.obj";
//		System.out.println("Loading graph from persistent storage");
//		ObjectInputStream ois;
//		try {
//			ois = new ObjectInputStream(new FileInputStream(graphFileName));
//			graph = (TrustGraph) ois.readObject();
//			ois.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("Calculating connected components...");
//		ConnectedComponentData[] data = getConnectedComponentsData(graph);
//		System.out.println("Finished calculating strongly connected components.");
//		System.out.println(Arrays.toString(data));
//		
//		System.out.println("Calculating strongly connected components...");
//		data = getStronglyConnectedComponentsData(graph);
//		System.out.println("Finished calculating strongly connected components.");
//		System.out.println(Arrays.toString(data));
		
//		double[] right = new double[]{3, 2, 1};
//		double[] left = new double[]{1, 2, 3};
//		System.out.println("The Pearson correlation between the two vectors is " + getPMCC(left, right));
//		System.out.println("The Pearson correlation between the two vectors is " + getPMCC(right, left));
//		
//		System.out.println("The Pearson correlation between the two vectors is " + getPearsonCorrelation(left, right));
//		System.out.println("The Pearson correlation between the two vectors is " + getPearsonCorrelation(right, left));
		
//		printGraphMeasurements("C:/Documents and Settings/mizrachi/My Documents/Assaf/Private/Master/" +
//				"Thesis/assafm/thesis/Java/RecommendationSystem/graph_bob_common_80.obj");
		
		printGraphMeasurements("C:/Documents and Settings/mizrachi/My Documents/Assaf/Private/Master/" +
				"Thesis/assafm/thesis/Java/RecommendationSystem/graph_bob_pearson_40_2.obj");
	}
	
	public static class ConnectedComponentData implements Comparable<ConnectedComponentData> {
		//number of vertices of this component
		private int size;
		//percentage of this component comparing to the size of its graph
		private float percentage;
		
		public ConnectedComponentData(int size, float percentage) {
			super();
			this.size = size;
			this.percentage = percentage;
		}

		public int getSize() {
			return size;
		}

		public float getPercentage() {
			return percentage;
		}

		@Override
		public int compareTo(ConnectedComponentData other) {
			return other.size - this.size;
		}

		@Override
		public String toString() {
			return "size = " + size + " (" + percentage + "%)";
		}
		
		
	}
	
}

