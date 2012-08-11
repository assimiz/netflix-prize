package test;

import graphframework.BestOfBreedTrustGraphGenerator;
import graphframework.CommonItemsBasedTrustCalculator;
import graphframework.TrustEdgeNameProvider;
import graphframework.UserIdNameProvider;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.jgrapht.Graph;
import org.jgrapht.ext.DOTExporter;

import dataframework.Trust;
import dataframework.User;
import dbanalysis.NetflixAnalyzer;
import dbanalysis.NetflixDB;
import dbanalysis.Rent;

public class GraphBuildTest {

	public static Graph<User, Trust> buildGraph() throws IOException {
//		String dbFolder = args[0];
//		String dbFolder = "C:\\Documents and Settings\\mizrachi\\My Documents\\Assaf\\Private\\Master\\Thesis\\Files\\TestDB";
//		String dbFolder = "C:\\Documents and Settings\\mizrachi\\My Documents\\Assaf\\Private\\Master\\Thesis\\Files\\NetflixDB\\debug_set";
		//TODO make real folder
		String dbFileName = "C:\\Documents and Settings\\mizrachi\\My Documents\\Assaf\\Private\\Master\\Thesis\\" +
				"assafm\\thesis\\Java\\Netflix\\NetflixDB_1000_Users_2.obj";
		NetflixDB db = NetflixAnalyzer.loadDB(dbFileName);
		Iterator<Rent> rents = db.rentsIterator();
		CommonItemsBasedTrustCalculator calc = new CommonItemsBasedTrustCalculator(db);
		//TODO make DBIterator from NetflixDB
//		Graph<User, Trust> graph = new DefaultTrustGraphGenerator(rents, calc, new MinimumTrustEdgePolicer(
//				0.0, calc, new MinimumCommonItemsEdgePolicer(db, 1))).generateGraph();
		
		Graph<User, Trust> graph = new BestOfBreedTrustGraphGenerator(rents, calc, 1.0).generateGraph();
		
		System.out.println("Exporting graph to dot file.");
		DOTExporter<User, Trust> exporter = new DOTExporter<User, Trust>(new UserIdNameProvider(), 
				new UserIdNameProvider(), new TrustEdgeNameProvider());
		exporter.export(new FileWriter("graph.dot"), graph);		
		return graph;
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		buildGraph();
	}

}
