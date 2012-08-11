package test;

import graphframework.BestOfBreedTrustGraphGenerator;
import graphframework.CommonItemsBasedTrustCalculator;
import graphframework.DefaultTrustGraphGenerator;
import graphframework.LikeMindsTrustCalculator;
import graphframework.MinimumCommonItemsEdgePolicer;
import graphframework.MinimumTrustEdgePolicer;
import graphframework.PearsonCorrelationTrustCalculator;
import graphframework.RmseTrustCalculator;
import graphframework.ScaleFreeTrustGraphGenerator;
import graphframework.TrustCalculator;
import graphframework.TrustEdgeNameProvider;
import graphframework.TrustGraph;
import graphframework.UserIdNameProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import misc.Utilities;

import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;

import strategy.AveragingRecommendationSystem;
import strategy.BeliefPropagationRecommendationSystem;
import strategy.LikeMindsVotesMapper;
import strategy.MinRmseVotesMapper;
import strategy.RandomWalkRecommendationSystem;
import strategy.RecommendationStrategy;
import strategy.VotesDistributionMessage;
import strategy.AbstractDesignatedVotesMapper.CluelessRecommendationStrategy;

import common.QuestionsFileIterator;

import dataframework.Trust;
import dataframework.User;
import dbanalysis.NetflixAnalyzer;
import dbanalysis.NetflixDB;
import dbanalysis.Rent;
import dbanalysis.SqlReadOnlyNetflixDB;

/*
 * TODO:
 * 1.5 Make Systems to stop when convergence achieved (instead of using messages budget). 
 * 2. Make message passing systems handle edge cases (like disconnected non-voter):
 * 	2.1 First make them to stop (avoid the endless while loop)
 *  2.2 Assign 'Don't know' recommendation for all clue-less users - ??? not sure it is desired.
 * */

/**
 * Outputs recommendations to an output file for a specified {@link NetflixDB},
 * questions file and system.
 * 
 * @author mizrachi
 */
public class NetflixRecommender {
	public static final int CLUELESS_RECOMMENDATION = 3;
	
	public static CluelessRecommendationStrategy CLUELESS_STRATEGY = CluelessRecommendationStrategy.DISTRIBUTE;
	
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd_HH-mm");

	public static void main(String[] args) {
		Iterator<Rent> questions = null;
		RecommendationStrategy system = null;
		NetflixDB omitProbeDb = null;
		TrustGraph graph = null;
		TrustCalculator calc = null;
		String dbFileName = args[0];
		String questionsFileName = args[1];
		String graphBuildStrategy = args[2].toUpperCase();
		String recommendationStrategy = args[3].toUpperCase();
		String trustCalculator = args[4];
		String voteMapper = args[5].toUpperCase();
		int messagedBudget = Integer.valueOf(args[6]);
		double minTrust = Double.valueOf(args[7]);
		int minCommonItems = Integer.valueOf(args[8]);
		int scalingFactor = Integer.valueOf(args[9]);
		double bobPercent = Integer.valueOf(args[10]);
		double trustPower = Integer.valueOf(args[11]);
		String progressFilePolicy = args[12];



		System.out.println("Hi! I'm Netflix recommender.");
		System.out.println("Please confirm recommendation system input.");
		System.out.println("Netflix DB (Probe data omitted) file name: " + dbFileName);
		System.out.println("Questions file: " + questionsFileName);
		System.out.println("Graph build strategy is " + graphBuildStrategy.toUpperCase());
		System.out.println("Recommendation strategy is " + recommendationStrategy.toUpperCase() + 
				(" with " + voteMapper.toUpperCase() + " votes mapper"));			
		System.out.println("Clueless recommendation strategy: " + CLUELESS_STRATEGY);
		System.out.println("Messages budget (per movie session): " + messagedBudget);
		System.out.println("Min trust for edge in the Trust Graph (relevant for POLICE build strategy only): " + minTrust);
		System.out.println("Min common items for edge in the Trust Graph (relevant for POLICE build strategy only): " + minCommonItems);
		//			System.out.println("Recommendation for clueless users: " + CLUELESS_RECOMMENDATION);
		System.out.println("Scaling factor (relevant for SCLFREE build strategy only): " + scalingFactor);
		System.out.println("Percent threshold of total trust (relevant for BoB build strategy only): " + bobPercent);
		System.out.println("Trust power: " + trustPower);
		System.out.println("Progress file policy: " + progressFilePolicy);
		System.out.println("Proceed (y/n)? ");
		
		try {
			int c = System.in.read();
			if (c != 'y') {
				System.out.println("Aborting...");
				System.exit(1);
			}
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		}
		try {
			omitProbeDb = NetflixAnalyzer.loadDB(dbFileName);
			int calculatorCacheSize = omitProbeDb.getNumberOfUsers();
			if (trustCalculator.equalsIgnoreCase("common_none")) {
				calc = new CommonItemsBasedTrustCalculator(omitProbeDb, 
						CommonItemsBasedTrustCalculator.NORMALIZE_NONE, 1, calculatorCacheSize);
			} else if (trustCalculator.equalsIgnoreCase("common_from")) {
				calc = new CommonItemsBasedTrustCalculator(omitProbeDb, 
						CommonItemsBasedTrustCalculator.NORMALIZE_BY_FROM, 1, calculatorCacheSize);
			} else if (trustCalculator.equalsIgnoreCase("common_to")) {
				calc = new CommonItemsBasedTrustCalculator(omitProbeDb, 
						CommonItemsBasedTrustCalculator.NORMALIZE_BY_TO, 1, calculatorCacheSize);
			} else if (trustCalculator.equalsIgnoreCase("pearson")) {
				calc = new PearsonCorrelationTrustCalculator(omitProbeDb,
						true, VotesDistributionMessage.MAX_VOTE, calculatorCacheSize);
			} else if (trustCalculator.equalsIgnoreCase("likeminds")) {
				calc = new LikeMindsTrustCalculator(omitProbeDb, calculatorCacheSize);
			} else if (trustCalculator.equalsIgnoreCase("rmse")) {
				calc = new RmseTrustCalculator(omitProbeDb, calculatorCacheSize);
			}
			
			System.out.println(new Date().toString() + ": Building graph. This may take a while...");
			Iterator<Rent> rents = omitProbeDb.rentsIterator();
			if (graphBuildStrategy.equalsIgnoreCase("police")) {
				graph = new DefaultTrustGraphGenerator(rents, calc, new MinimumCommonItemsEdgePolicer(
						omitProbeDb, minCommonItems, new MinimumTrustEdgePolicer(minTrust, calc))).generateGraph();
			} else if (graphBuildStrategy.equalsIgnoreCase("sclfree")) {
				graph = new ScaleFreeTrustGraphGenerator(omitProbeDb, calc, Integer.valueOf(scalingFactor), false).generateGraph();
			} else if (graphBuildStrategy.equalsIgnoreCase("bob")) {
				graph = new BestOfBreedTrustGraphGenerator(rents, calc, bobPercent).generateGraph();
			} else {
				throw new IllegalArgumentException("Third argument must be one of 'police' or 'sclfree'");
			}
			
			if (trustPower != 1) {
				Utilities.powerTrust(graph, trustPower);
			}
			
			Date now = new Date();			
			System.out.println("Exporting graph to dot file.");
			DOTExporter<User, Trust> exporter = new DOTExporter<User, Trust>(new UserIdNameProvider(), 
					new UserIdNameProvider(), new TrustEdgeNameProvider(), 
					new ComponentAttributeProvider<User>() {

						@Override
						public Map<String, String> getComponentAttributes(
								User component) {
							return Collections.emptyMap();
						}
					}, 
					new ComponentAttributeProvider<Trust>() {

						@Override
						public Map<String, String> getComponentAttributes(
								Trust component) {
							return Collections.emptyMap();
						}
					});
			try {
				exporter.export(new FileWriter("graph_" + DATE_FORMAT.format(now) + ".dot"), graph);
			} catch (IOException e) {
				System.err.println("");
				e.printStackTrace();
			}
			
			System.out.println(now.toString() + ": Finished graph building!");			
			File graphFile = new File("graph_" + DATE_FORMAT.format(now) + ".obj");
			try {
				//now saving graph to persistent storage
				ObjectOutputStream oos;
				System.out.println("Writing graph to persistent storage.");	
				if (graphFile.exists()) {
					graphFile.delete();
				}
				graphFile.createNewFile();
				oos = new ObjectOutputStream(new FileOutputStream(graphFile));
				oos.writeObject(graph);
				oos.close();
				System.out.println("Finished writing graph to persistent storage.");
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				ioe.printStackTrace();
			}		
						
//			ObjectInputStream ois;
//			String graphFileName = "C:/Documents and Settings/mizrachi/My Documents/Assaf/" +
//					"Private/Master/Thesis/assafm/thesis/Results/1000_sf_omit_clueless/" +
//					"graph_scaling_factor_10.obj";			
//			System.out.println("Loading graph from persistent storage: " + graphFileName);			
//			try {
//				ois = new ObjectInputStream(new FileInputStream(graphFileName));
//				graph = (TrustGraph) ois.readObject();
//				ois.close();
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				return;
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				return;
//			} catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				return;
//			}			
			
			questions = new QuestionsFileIterator(questionsFileName);

			system = null;
			if (recommendationStrategy.equalsIgnoreCase("avg")) {
				if (voteMapper.equalsIgnoreCase("minrmse")) {
					System.out.println(DATE_FORMAT.format(new Date())+ ": Building AVG recommendation system with votes mapper. This may take a while...");
					MinRmseVotesMapper mapper = new MinRmseVotesMapper(omitProbeDb, graph, CLUELESS_STRATEGY);
					System.out.println("Percentage of valuable mappings is " + mapper.getValuableMappingDistribution());
					system = new AveragingRecommendationSystem(
							messagedBudget, false, mapper);
					System.out.println("Finished AVG recommendation system with votes mapper building!");
				} else if (voteMapper.equalsIgnoreCase("likeminds")) {
					System.out.println(DATE_FORMAT.format(new Date())+ ": Building AVG recommendation system with LikeMinds votes mapper. This may take a while...");
					LikeMindsVotesMapper mapper = new LikeMindsVotesMapper(omitProbeDb, graph, CLUELESS_STRATEGY);
					System.out.println("Percentage of valuable mappings is " + mapper.getValuableMappingDistribution());
					system = new AveragingRecommendationSystem(
							messagedBudget, false, mapper);
					System.out.println("Finished AVG recommendation system with votes mapper building!");
				} else {
					system = new AveragingRecommendationSystem(messagedBudget);
				}
			} else if (recommendationStrategy.equalsIgnoreCase("bp")) {
				if (voteMapper.equalsIgnoreCase("minrmse")) {
					System.out.println(DATE_FORMAT.format(new Date())+ ": Building BP recommendation system with Min RMSE votes mapper. This may take a while...");
					MinRmseVotesMapper mapper = new MinRmseVotesMapper(omitProbeDb, graph, CLUELESS_STRATEGY);
					System.out.println("Percentage of valuable mappings is " + mapper.getValuableMappingDistribution());
					system = new BeliefPropagationRecommendationSystem(
							messagedBudget, mapper);
					System.out.println("Finished BP recommendation system with votes mapper building!");
				} else if (voteMapper.equalsIgnoreCase("likeminds")) {
					System.out.println(DATE_FORMAT.format(new Date())+ ": Building BP recommendation system with LikeMinds votes mapper. This may take a while...");
					LikeMindsVotesMapper mapper = new LikeMindsVotesMapper(omitProbeDb, graph, CLUELESS_STRATEGY);
					System.out.println("Percentage of valuable mappings is " + mapper.getValuableMappingDistribution());
					system = new BeliefPropagationRecommendationSystem(
							messagedBudget, mapper);
					System.out.println("Finished BP recommendation system with votes mapper building!");
				} else {
					system = new BeliefPropagationRecommendationSystem(messagedBudget);
				}
			} else if (recommendationStrategy.equalsIgnoreCase("rw")) {
				if (!voteMapper.equalsIgnoreCase("no")) {
					throw new IllegalArgumentException("Random walk system is not currently supporting votes mapping.");
				}
				system = new RandomWalkRecommendationSystem(messagedBudget);
			} else {
				throw new IllegalArgumentException("Fifth argument must be one of 'avg', 'bp' or 'rw'");
			}
		} catch (ArrayIndexOutOfBoundsException exc) {
			System.out.println("Usage: NetflixRecommender <OmittedProbeDataDBFileName> <NetflixQuestionsFileName> <sclfree/police> <avg/bp/rw> <minrmse?>");
			exc.printStackTrace();
			return;
		}
			
		File output = new File("recommendations_" + DATE_FORMAT.format(new Date()) + ".csv");		
		
		try {
			FileWriter writer = new FileWriter(output);
			writer.append("Movie ID, User ID, Recommendation\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Map<Integer, Double> votes = new HashMap<Integer, Double>();
		Set<User> queriedUsers = new HashSet<User>();
		int currentMovieId = -1;
		int lastMovieId = -1;
		Rent question;
		
		System.out.println("Starting recommendations!");
		StrongConnectivityInspector<User, Trust> connInspector = new StrongConnectivityInspector<User, Trust>(graph);
		List<Set<User>> connectedComponents = connInspector.stronglyConnectedSets();		
		Set<User> largestConnectedComponent = connectedComponents.get(0);
		for (Set<User> connectedComponent : connectedComponents) {
			if (connectedComponent.size() > largestConnectedComponent.size()) {
				largestConnectedComponent = connectedComponent;
			}
		}
		Set<Integer> largestConnectedComponentIds = new HashSet<Integer>();
		for (User user : largestConnectedComponent) {
			largestConnectedComponentIds.add(user.getId());
		}
		String forProgress = DATE_FORMAT.format(new Date()) + ".csv";
		while (questions.hasNext()) {
			question = questions.next();
			lastMovieId = currentMovieId;
			currentMovieId = question.getMovieId();
			System.out.println(DATE_FORMAT.format(new Date())+ ": Generating recommendation for question " + question);			
			if (graph.vertexSet().contains(new User(question.getUserId()))) {					
				if (currentMovieId != lastMovieId) {
					if (lastMovieId != -1) {
						//adding all voters
						for (User voter : graph.vertexSet()) {
							List<Rent> rents = omitProbeDb.readUser(voter.getId()).getRentsForMovieId(lastMovieId);
							if (rents.size() > 0) {
								votes.put(voter.getId(), (double) Utilities.getAverageRecommendation(rents));
							}
						}		
						File progress;
						if (progressFilePolicy.equalsIgnoreCase("multiple")) {
							progress = new File("progress_" + lastMovieId + "_"+ DATE_FORMAT.format(new Date()) + ".csv");
						} else if (progressFilePolicy.equalsIgnoreCase("single")) {
							progress = new File("progress" + "_" + forProgress);
						} else {
							progress = null;
						}
						boolean atLeastOneVoterBelongsToTheLargestStronglyConnectedComponent = false;
						for (Integer voterId : votes.keySet()) {
							if (largestConnectedComponentIds.contains(voterId)) {
								atLeastOneVoterBelongsToTheLargestStronglyConnectedComponent = true;
								break;
							}
						}
						//we know that the graph structure is one largest component and small
						//number of vertices that are not.
						if (atLeastOneVoterBelongsToTheLargestStronglyConnectedComponent) {
							//we can be sure that all votes and queries are updated
							system.recommend(graph, votes, queriedUsers, progress);
						}
						outputRecommendations(graph, queriedUsers, lastMovieId, output);
						votes.clear();
						queriedUsers.clear();
					}								
				}
				//adding the user to the non-voters
				//for BP and AVG this it is not required since recommendations are got
				//for all non-voters regardless of the specific queried users but it is
				//needed for reporting.
				for (User questioner : graph.vertexSet()) {
					if (questioner.getId() == question.getUserId()) {
						queriedUsers.add(questioner);
						break;
					}
				}				
			} else {
				System.out.println("Skipping question " + question + ". User is not in the trust graph");
			}
		}
		System.out.println("Finished recommendations!");
		
//		addResultAnalysisHelpInfo(omitProbeDb, output, recommendationStrategy);
				
	}

	private static void addResultAnalysisHelpInfo(NetflixDB omitProbeDb, File recommendationsOutput, String system) {

		System.out.println("Starting adding analysis help info for " + recommendationsOutput.getName());
		NetflixDB fullDb = new SqlReadOnlyNetflixDB(true);
		try {
			File fixed = new File(recommendationsOutput.getParentFile(), recommendationsOutput.getName() + "_Fixed.csv");
			FileWriter writer = new FileWriter(fixed, false);

			BufferedReader br = new BufferedReader(new FileReader(recommendationsOutput));
			StringTokenizer commaTokenizer;
			//writing the header line
			String line = br.readLine();
			writer.append(line)
				.append(", Final Rec.(" + system.toUpperCase() + ")")
				.append(", Actual Rate")
				.append(", Movie Avg.")
				.append(", Movie Avg. Rec.")
				.append(", " + system.toUpperCase() + " Sq. Err.")
				.append(", Movie Avg. Sq. Err.")
				.append(String.valueOf('\n'));

			Rent rent;
			int userid, movieid, finalRecommendation, movieAvgRecommendation,
				actualRate, ctr = 1;
			int[] countersSystem = new int[5];
			Arrays.fill(countersSystem, 0);
			int[] countersMovieAvg = new int[5];
			Arrays.fill(countersMovieAvg, 0);
			double movieAvg, rawRecommendation, sumSqrErrSystem = 0, sumSqrErrMovieAvg = 0,
				sqrErrSystem, sqrErrMovieAvg, sumItems = 0;
			while (line != null) {				
				line = br.readLine();
				if (line == null) {
					break;
				}
				commaTokenizer = new StringTokenizer(line, String.valueOf(','));
				movieid = Integer.valueOf(commaTokenizer.nextToken());
				userid = Integer.valueOf(commaTokenizer.nextToken());
				System.out.println(String.valueOf(ctr++) + ": Adding info for movie id " + movieid + " and user id " + userid);
				rawRecommendation = Double.valueOf(commaTokenizer.nextToken());
				finalRecommendation = rawRecommendation > 0 ? (int) Math.round(rawRecommendation) : CLUELESS_RECOMMENDATION;		
				//we want the average to be calculated on the DB subset so comparison will be fair.
				movieAvg = omitProbeDb.readMovie(movieid).getAvgRate();
				movieAvgRecommendation = (int) Math.round(movieAvg);
				rent = fullDb.readRent(movieid, userid, null);			
				if (rent == null) {
					System.err.println("Fatal error. Failed to read rent for movie id " + movieid + " and user id " + userid);
				}
				actualRate = rent != null ? rent.getRate() : CLUELESS_RECOMMENDATION;
				sqrErrSystem = Math.pow((finalRecommendation - actualRate), 2);
				sumSqrErrSystem += sqrErrSystem;
				sqrErrMovieAvg = Math.pow((movieAvgRecommendation - actualRate), 2);
				sumSqrErrMovieAvg += sqrErrMovieAvg;
				countersSystem[Math.abs(finalRecommendation - actualRate)]++;
				countersMovieAvg[Math.abs(movieAvgRecommendation - actualRate)]++;
				sumItems++;
				writer.append(line)
					.append(String.valueOf(',') + String.valueOf(finalRecommendation))
					.append(String.valueOf(',') + String.valueOf(actualRate))
					.append(String.valueOf(',') + String.valueOf(movieAvg))
					.append(String.valueOf(',') + String.valueOf(movieAvgRecommendation))
					.append(String.valueOf(',') + String.valueOf(sqrErrSystem))
					.append(String.valueOf(',') + String.valueOf(sqrErrMovieAvg))
					.append(String.valueOf('\n'));
			}
			writer.append(String.valueOf('\n')).append("Results Analisys:\n");
			writer.append(String.valueOf(',')).append(system.toUpperCase())
				.append(String.valueOf(',')).append("Movie Avg.").append(String.valueOf('\n'));
			writer.append("RMSE").append(String.valueOf(','))
				.append(String.valueOf(Math.sqrt(sumSqrErrSystem / sumItems)))
				.append(String.valueOf(','))
				.append(String.valueOf(Math.sqrt(sumSqrErrMovieAvg / sumItems)))
				.append(String.valueOf('\n'));
			writer.append("#Hits").append(String.valueOf(','))
				.append(String.valueOf(countersSystem[0]))
				.append(String.valueOf(','))
				.append(String.valueOf(countersMovieAvg[0]))
				.append(String.valueOf('\n'));
			writer.append("#Missed by one").append(String.valueOf(','))
				.append(String.valueOf(countersSystem[1]))
				.append(String.valueOf(','))
				.append(String.valueOf(countersMovieAvg[1]))
				.append(String.valueOf('\n'));
			writer.append("#Missed by two").append(String.valueOf(','))
				.append(String.valueOf(countersSystem[2]))
				.append(String.valueOf(','))
				.append(String.valueOf(countersMovieAvg[2]))
				.append(String.valueOf('\n'));
			writer.append("#Missed by three").append(String.valueOf(','))
				.append(String.valueOf(countersSystem[3]))
				.append(String.valueOf(','))
				.append(String.valueOf(countersMovieAvg[3]))
				.append(String.valueOf('\n'));
			writer.append("#Missed by four").append(String.valueOf(','))
				.append(String.valueOf(countersSystem[4]))
				.append(String.valueOf(','))
				.append(String.valueOf(countersMovieAvg[4]))
				.append(String.valueOf('\n'));
			
			System.out.println("Finished adding analysis help info for " + recommendationsOutput.getName());
			writer.close();
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void outputRecommendations(TrustGraph graph, Set<User> queries, int movieID, File output) {
		// TODO write recommendations to output file
		try {
			FileWriter writer = new FileWriter(output, true);
			for (User nonVoter : queries) {
				writer.append(String.valueOf(movieID)).append(String.valueOf(','));				
				writer.append(String.valueOf(nonVoter.getId())).append(String.valueOf(','));
				writer.append(String.valueOf(nonVoter.getValue())).append(String.valueOf("\n"));
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
