package misc;

import graphframework.TrustGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.StringTokenizer;

import strategy.RecommendationStrategy;
import test.NetflixRecommender;
import dataframework.Trust;
import dataframework.User;
import dbanalysis.FastNetflixDB;
import dbanalysis.NetflixDB;

/**
 * Contains useful methods for statistic analysis of a recommendation system output.
 * @author mizrachi
 *
 */
public class StatisticAnalyzer {

	private static final int MAX_ROUNDS = 100;
	/**
	 * Analyzes the average convergence ratio for a list of files in a specific directory.
	 * 
	 * @param progressFilesPath path to a directory of progress files that are out put of a
	 * {@link RecommendationStrategy#recommend(TrustGraph, java.util.Map, Set, File)}.
	 */
	public static void analyzeConvergenceRatio(String progressFilesPath) {
		
		File folder = new File(progressFilesPath);
		File[] files = folder.listFiles();

		System.out.println("Analysing convergence ratio for folder " + folder);
		System.out.println("Folder contains " + files.length + " files.");

		FileReader input = null;
		BufferedReader bufRead = null;
		String delim = ",";
		StringTokenizer lineTokenizer;
		StringTokenizer prevLineineTokenizer;
		short rec, prevRec;
		int recChangedCount, recNoChangedCount;
		long[] totalRecNoChangedCount = new long[MAX_ROUNDS];
		long[] totalRecChangedCount = new long[MAX_ROUNDS];
		Arrays.fill(totalRecNoChangedCount, 0);
		Arrays.fill(totalRecChangedCount, 0);
		int fileCount = 0;
		for (File file : files) {
			
			System.out.println("Handling file " + ++fileCount + " out of " + files.length + " files.");
			try {

				input = new FileReader(file);
				bufRead = new BufferedReader(input);

				String line, prevLine;
				//this is the first line containing the users ids
				bufRead.readLine();
				//this is the second line containing the initialization phase
				prevLine = bufRead.readLine();
				int round = 0; 
				int votersCounter = 0;

				while ((line = bufRead.readLine()) != null && round < MAX_ROUNDS) {
					lineTokenizer = new StringTokenizer(line, delim);
					prevLineineTokenizer = new StringTokenizer(prevLine, delim);
					if (lineTokenizer.countTokens() != prevLineineTokenizer.countTokens()) {
						throw new IOException("Line do not contain the same number of elements." +
								" Line = " + line + "; Previous line = " + prevLine);
					}
					
					if (round == 0) {
						StringTokenizer toCountVoters = new StringTokenizer(prevLine, delim);
						while (toCountVoters.hasMoreElements()) {
							if (Double.valueOf(toCountVoters.nextToken()) > 0) {
								votersCounter++;
							}
						}
					}
					
					recChangedCount = recNoChangedCount = 0;					
					while (lineTokenizer.hasMoreElements()) {
						rec = (short) Math.round(Double.valueOf(lineTokenizer.nextToken()));
						prevRec = (short) Math.round(Double.valueOf(prevLineineTokenizer.nextToken()));
						
						if (rec == prevRec) {
							recNoChangedCount++;
						} else {
							recChangedCount++;
						}
					}
					//we don't take counters in this calculations since they do not 'change their mind'
					//(i.e. their recommendation is fixed).
					recNoChangedCount -= votersCounter;
					totalRecChangedCount[round] += recChangedCount;
					totalRecNoChangedCount[round] += recNoChangedCount;
					prevLine = line;
					round++;
				}

				bufRead.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Total recommendation not changed array:\n" + Arrays.toString(totalRecNoChangedCount));
		System.out.println("Total recommendation changed array:\n" + Arrays.toString(totalRecChangedCount));
	}
	
	/**
	 * Analyzes the RMSE of a recommendation system vs. the number of. information it has
	 * (in/out edges/information).
	 * 
	 * @param sortedByUserFinalRecommendationsFile file name that is output of {@link NetflixRecommender} sorted by user id}.
	 * @param trustGraphFile file of serialized {@link TrustGraph} object.
	 * @param db file of serialized {@link FastNetflixDB} object.
	 */
	public static void analyzeRmseVsDegree(String sortedByUserFinalRecommendationsFile, String trustGraphFile,
			NetflixDB db) {
		
		System.out.println("Analysing Rmse vs. Degree for rec file " + sortedByUserFinalRecommendationsFile +
				" and graph file " + trustGraphFile);
		System.out.println("Loading graph from persistent storage");
		ObjectInputStream ois;
		TrustGraph graph = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(trustGraphFile));
			graph = (TrustGraph) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		User[] users = graph.vertexSet().toArray(new User[0]);
		Arrays.sort(users);
		
		StringBuilder info = new StringBuilder();
		String delim = ",";
		info.append("User Id").append(delim) 
		.append("Total outgoing edges weight").append(delim)
		.append("Total outgoing edges number").append(delim)
		.append("Total incoming edges weight").append(delim)
		.append("Total incoming edges number").append(delim)
		.append("Total Pow. Error").append(delim)
		.append("Number of Questions").append(delim)
		.append("User Std. dev.").append(delim)
		.append("Total Questions Std. dev.").append("\n");
		
		try {
			BufferedReader recFileBufRead = new BufferedReader(new FileReader(sortedByUserFinalRecommendationsFile));

			String recFileLine;
			//this is the first line containing the users ids
			recFileBufRead.readLine();
			
			StringTokenizer recFileLineStringTokenizer;
			int userIndex = -1;
			int userId = 0, prevUserId = 0, sqErr = 0, numQuestions = 0;
			double totalQuestionsStdDev = 0.0;
			
			while ((recFileLine = recFileBufRead.readLine()) != null) {
				recFileLineStringTokenizer = new StringTokenizer(recFileLine, delim);
				//striping line to get all values
				prevUserId = userId;
				int movieId = Integer.valueOf(recFileLineStringTokenizer.nextToken());				
				userId = Integer.valueOf(recFileLineStringTokenizer.nextToken());
				double finalValue = Double.valueOf(recFileLineStringTokenizer.nextToken());
				int finalRec = Integer.valueOf(recFileLineStringTokenizer.nextToken());
				int actualRec = Integer.valueOf(recFileLineStringTokenizer.nextToken());	
				totalQuestionsStdDev += db.readMovie(movieId).getStdDev();
				
				//we know both file and file list are sorted by the movie id
				if (userId != prevUserId) {
					if (userIndex >= 0) {
						System.out.println("Handling user " + userIndex + " out of " + users.length + " users.");

						Set<Trust> outEdges = graph.outgoingEdgesOf(users[userIndex]);
						int totalOutInformation = 0;
						for (Trust edge : outEdges) {
							totalOutInformation += graph.getEdgeWeight(edge);
						}
						Set<Trust> inEdges = graph.incomingEdgesOf(users[userIndex]);
						int totalInInformation = 0;
						for (Trust edge : inEdges) {
							totalInInformation += graph.getEdgeWeight(edge);					
						}
						info.append(prevUserId).append(delim) 
							.append(totalOutInformation).append(delim)
							.append(outEdges.size()).append(delim)
							.append(totalInInformation).append(delim)
							.append(inEdges.size()).append(delim)
							.append(sqErr).append(delim)
							.append(numQuestions).append(delim)
							.append(db.readUser(prevUserId).getStdDev()).append(delim)
							.append(totalQuestionsStdDev).append("\n");
					}
					userIndex++;
					sqErr = 0;
					numQuestions = 0;
					totalQuestionsStdDev = 0.0;
				}
				sqErr += Integer.valueOf(recFileLineStringTokenizer.nextToken());		
				numQuestions++;

			}

			recFileBufRead.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(info.toString());
	}
	
	public static void analyzeRmseVsNumberOfMovieRents(String sortedByMovieFinalRecommendationsFile, NetflixDB db) {
		System.out.println("Analysing RMSE vs the number of movie rents for " + sortedByMovieFinalRecommendationsFile);
		
		try {
			BufferedReader recFileBufRead = new BufferedReader(new FileReader(sortedByMovieFinalRecommendationsFile));

			String recFileLine;
			//this is the first line containing the users ids
			recFileBufRead.readLine();
			
			StringTokenizer recFileLineStringTokenizer;
			int movieId = -1, prevMovieId = -1;
			String delim = ",";
			int totalSqrErrPrMovie = 0, numQuestions = 0, totalQuestionsStdDev = 0;
			StringBuilder info = new StringBuilder();
			info.append("Movie Id").append(delim) 			
			.append("Number of Questions").append(delim)
			.append("Total Pow. Error").append(delim)
			.append("Movie Std. dev.").append(delim)
			.append("Movie Rents (Number of Voters)").append(delim)
			.append("Total Questions Std. dev.").append("\n");
			
			while ((recFileLine = recFileBufRead.readLine()) != null) {
				recFileLineStringTokenizer = new StringTokenizer(recFileLine, delim);
				//striping line to get all values
				prevMovieId = movieId;
				movieId = Integer.valueOf(recFileLineStringTokenizer.nextToken());	
				
				if (prevMovieId != movieId && prevMovieId >= 0) {
					info
					.append(prevMovieId).append(delim)
					.append(numQuestions).append(delim)
					.append(totalSqrErrPrMovie).append(delim)
					.append(db.readMovie(prevMovieId).getStdDev()).append(delim)
					.append(db.readMovie(prevMovieId).getNumOfRents()).append(delim)
					.append(totalQuestionsStdDev).append("\n");
					
					totalSqrErrPrMovie = numQuestions = totalQuestionsStdDev = 0;
				}
				int userId = Integer.valueOf(recFileLineStringTokenizer.nextToken());
				double finalValue = Double.valueOf(recFileLineStringTokenizer.nextToken());
				int finalRec = Integer.valueOf(recFileLineStringTokenizer.nextToken());
				int actualRec = Integer.valueOf(recFileLineStringTokenizer.nextToken());
				int sqErr = Integer.valueOf(recFileLineStringTokenizer.nextToken());
				totalSqrErrPrMovie += sqErr;
				totalQuestionsStdDev += db.readUser(userId).getStdDev();
				numQuestions++;
			}
			
			System.out.println(info.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void analyzeRmseVsNumberOfUserRents(String sortedByUserFinalRecommendationsFile, NetflixDB db) {
		System.out.println("Analysing RMSE vs the number of user rents for " + sortedByUserFinalRecommendationsFile);
		
		try {
			BufferedReader recFileBufRead = new BufferedReader(new FileReader(sortedByUserFinalRecommendationsFile));

			String recFileLine;
			//this is the first line containing the users ids
			recFileBufRead.readLine();
			
			StringTokenizer recFileLineStringTokenizer;
			int userId = -1, prevUserId = -1;
			String delim = ",";
			int totalSqrErrPrUser = 0, numQuestions = 0, totalQuestionsStdDev = 0;
			StringBuilder info = new StringBuilder();
			info.append("User Id").append(delim) 			
			.append("Number of Questions").append(delim)
			.append("Total Pow. Error").append(delim)
			.append("User Std. dev.").append(delim)
			.append("User Rents (Number of Voters)").append(delim)
			.append("Total Questions Std. dev.").append("\n");
			
			while ((recFileLine = recFileBufRead.readLine()) != null) {
				recFileLineStringTokenizer = new StringTokenizer(recFileLine, delim);
				//striping line to get all values
				int movieId = Integer.valueOf(recFileLineStringTokenizer.nextToken());	
				prevUserId = userId;				
				userId = Integer.valueOf(recFileLineStringTokenizer.nextToken());
				if (prevUserId != userId && prevUserId >= 0) {
					info
					.append(prevUserId).append(delim)
					.append(numQuestions).append(delim)
					.append(totalSqrErrPrUser).append(delim)
					.append(db.readUser(prevUserId).getStdDev()).append(delim)
					.append(db.readUser(prevUserId).getNumOfRents()).append(delim)
					.append(totalQuestionsStdDev).append("\n");
					
					totalSqrErrPrUser = numQuestions = totalQuestionsStdDev = 0;
				}
				double finalValue = Double.valueOf(recFileLineStringTokenizer.nextToken());
				int finalRec = Integer.valueOf(recFileLineStringTokenizer.nextToken());
				int actualRec = Integer.valueOf(recFileLineStringTokenizer.nextToken());
				int sqErr = Integer.valueOf(recFileLineStringTokenizer.nextToken());
				totalSqrErrPrUser += sqErr;
				totalQuestionsStdDev += db.readMovie(movieId).getStdDev();
				numQuestions++;
			}
			
			System.out.println(info.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Analyzes the RMSE average convergence ratio for a list of files in a specific directory.
	 * 
	 * @param sortedByMovieFinalRecommendationsFile file name that is output of {@link NetflixRecommender} sorted by movie id}.
	 * @param progressFilesPath path to a directory of progress files that are out put of a
	 * {@link RecommendationStrategy#recommend(TrustGraph, java.util.Map, Set, File)}.
	 */
	public static void analyzeRMSEConvergenceRatio(String sortedByMovieFinalRecommendationsFile, String progressFilesPath) {
		
		File folder = new File(progressFilesPath);
		File[] files = folder.listFiles();
		
		System.out.println("Analysing RMSE convergence ratio for folder " + folder);
		System.out.println("Folder contains " + files.length + " files.");
		
		class MovieIdFileComparator implements Comparator<File> {
			@Override
			public int compare(File o1, File o2) {
				StringTokenizer toGetTheMovieId = new StringTokenizer(o1.getName(), "_");
				toGetTheMovieId.nextElement();
				int movieId1 = Integer.valueOf(toGetTheMovieId.nextToken());
				
				toGetTheMovieId = new StringTokenizer(o2.getName(), "_");
				toGetTheMovieId.nextElement();
				int movieId2 = Integer.valueOf(toGetTheMovieId.nextToken());
				
				return movieId1 - movieId2;
			}
		}
		//sorting the file list by movie id
		Arrays.sort(files, new MovieIdFileComparator());
		
		String delim = ",";
		double[] totalPowErr = new double[MAX_ROUNDS];
		Arrays.fill(totalPowErr, 0);
		int[] totalItems = new int[MAX_ROUNDS];
		Arrays.fill(totalItems, 0);
		
		try {
			BufferedReader recFileBufRead = new BufferedReader(new FileReader(sortedByMovieFinalRecommendationsFile));

			String recFileLine;
			//this is the first line containing the users ids
			recFileBufRead.readLine();
			
			StringTokenizer recFileLineStringTokenizer;
			int fileIndex = -1;
			int movieId = 0, prevMovieId = 0;
			
			while ((recFileLine = recFileBufRead.readLine()) != null) {
				recFileLineStringTokenizer = new StringTokenizer(recFileLine, delim);
				//striping line to get all values
				prevMovieId = movieId;
				movieId = Integer.valueOf(recFileLineStringTokenizer.nextToken());				
				int userId = Integer.valueOf(recFileLineStringTokenizer.nextToken());
				double finalValue = Double.valueOf(recFileLineStringTokenizer.nextToken());
				int finalRec = Integer.valueOf(recFileLineStringTokenizer.nextToken());
				int actualRec = Integer.valueOf(recFileLineStringTokenizer.nextToken());
				int sqErr = Integer.valueOf(recFileLineStringTokenizer.nextToken());
				
				//we know both file and file list are sorted by the movie id
				if (movieId != prevMovieId) {
					fileIndex++;
				}
				File file = files[fileIndex];
				
				FileReader input = null;
				BufferedReader bufRead = null;
				
				System.out.println("Handling file " + fileIndex + " out of " + files.length + " files.");
				try {

					input = new FileReader(file);
					bufRead = new BufferedReader(input);

					String line;
					//this is the first line containing the users ids
					line = bufRead.readLine();
					String[] usersIdsArr = line.split(",");
					int userIndex = -1;
					for (int i = 0; i < usersIdsArr.length; i++) {
						if (Integer.valueOf(usersIdsArr[i]) == userId) {
							userIndex = i;
							break;
						}
					}
					if (userIndex < 0) {
						throw new IllegalArgumentException("Unable to find user id " + userId + " in file " + file.getName());
					}
											
					//this is the second line containing the initialization phase
					bufRead.readLine();
					
					int round = 0;				
					while ((line = bufRead.readLine()) != null && round < MAX_ROUNDS) {
						String[] values = line.split(",");
						double value = Double.valueOf(values[userIndex]);
						totalPowErr[round] += Math.pow(Math.round(value) - actualRec, 2);
						totalItems[round]++;
						round++;
					}

					bufRead.close();

				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			recFileBufRead.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
				
		System.out.println("Total powered error array:\n" + Arrays.toString(totalPowErr));
		System.out.println("Total items array:\n" + Arrays.toString(totalItems));
	}
	
	public static void main(String[] args) {
//		analyzeConvergenceRatio("C:/Documents and Settings/mizrachi/My Documents/Assaf/Private/Master/Thesis/assafm/thesis/Java/RecommendationSystem/" +
//				"1000_users_scale_free_connected_new_method_omit_clueless_rec/1000_Users_Avg");
		analyzeRMSEConvergenceRatio(
				"C:/Documents and Settings/mizrachi/My Documents/Assaf/Private/Master/Thesis/assafm/thesis/Java/RecommendationSystem/" +
				"1000_users_scale_free_connected_new_method_omit_clueless_rec/rec_1000_users_Avg_sorted_by_movie.csv", 
				"C:/Documents and Settings/mizrachi/My Documents/Assaf/Private/Master/Thesis/assafm/thesis/Java/RecommendationSystem/" +
				"1000_users_scale_free_connected_new_method_omit_clueless_rec/1000_Users_Avg");
//		analyzeRmseVsDegree(
//				"C:/Documents and Settings/mizrachi/My Documents/Assaf/Private/Master/Thesis/assafm/thesis/Java/RecommendationSystem/1000_users_scale_free_connected_new_method_omit_clueless_rec/rec_scaling_factor_10_final_sorted_by_user.csv",
//				"C:/Documents and Settings/mizrachi/My Documents/Assaf/Private/Master/Thesis/assafm/thesis/Java/RecommendationSystem/1000_users_scale_free_connected_new_method_omit_clueless_rec/graph_scaling_factor_10.obj",
//				NetflixAnalyzer.loadDB("C:/Documents and Settings/mizrachi/My Documents/Assaf/Private/Master/Thesis/assafm/thesis/Java/Netflix/NetflixDB_1000_Users_2.obj"));
//		analyzeRmseVsNumberOfMovieRents(
//				"C:/Documents and Settings/mizrachi/My Documents/Assaf/Private/Master/Thesis/assafm/thesis/Java/RecommendationSystem/1000_users_scale_free_connected_new_method_omit_clueless_rec/rec_scaling_factor_10_final_sorted_by_movie.csv",				
//				NetflixAnalyzer.loadDB("C:/Documents and Settings/mizrachi/My Documents/Assaf/Private/Master/Thesis/assafm/thesis/Java/Netflix/NetflixDB_1000_Users_2.obj"));
//		analyzeRmseVsNumberOfUserRents(
//				"C:/Documents and Settings/mizrachi/My Documents/Assaf/Private/Master/Thesis/assafm/thesis/Java/RecommendationSystem/1000_users_scale_free_connected_new_method_omit_clueless_rec/rec_scaling_factor_10_final_sorted_by_user.csv",				
//				NetflixAnalyzer.loadDB("C:/Documents and Settings/mizrachi/My Documents/Assaf/Private/Master/Thesis/assafm/thesis/Java/Netflix/NetflixDB_1000_Users_2.obj"));
	}
}
