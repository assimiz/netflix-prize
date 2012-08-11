package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;

import dbanalysis.NetflixAnalyzer;
import dbanalysis.NetflixDB;
import dbanalysis.Rent;
import dbanalysis.SqlReadOnlyNetflixDB;

/**
 * Completes additional info required for statistical analysis of a recommendation system performance.
 * Attributes are Actual Rate, Movie Average, etc.
 * 
 * @author Assaf Mizrachi
 *
 */
public class RecommendationSystemAttributeCompleter {
	
	public static final int CLUELESS_RECOMMENDATION = 3;

	
	public static void addResultAnalysisHelpInfo(NetflixDB omitProbeDb, File recommendationsOutput, String system) {

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
			int i = 0;
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
				
				if (++i % 100 == 0) {
					writer.close();
					writer = new FileWriter(fixed, true);
				}
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
	
	public static void main(String[] args) {
		NetflixDB omitProbeDb = NetflixAnalyzer.loadDB(args[0]);
		File recommendationsOutput = new File(args[1]);
		String system = args[2];
		addResultAnalysisHelpInfo(omitProbeDb, recommendationsOutput, system);
	}
}
