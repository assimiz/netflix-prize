package dbanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

public class NetflixAnalyzer {

	public static File OUT_DIR;
	public static DateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
	public static void main(String[] args) {
		String dbFileName = args[0];
		NetflixDB db = loadDB(dbFileName);
		String outputDirName = args[1];
		OUT_DIR = new File(outputDirName, FILE_DATE_FORMAT.format(Calendar.getInstance().getTime()));
		if (!OUT_DIR.exists()) {
			OUT_DIR.mkdir();
		} else {
			OUT_DIR.delete();
		}
		analyzeRatios(db);
		analyzeRents(db);
		analyzeQuestions(db);
		analyseRates(db);
		analyzeStdDevs(db);		
		analyzeNormalizedRentsStdDevs(db);
		analyzeNormalizedQuestionsStdDevs(db);
		
		System.out.println("Analysis finished");
	}
	
	private static void analyzeNormalizedQuestionsStdDevs(NetflixDB db) {
		System.out.println("Saving movies questions stddevs values to file");
		saveMatrixToFile(getQuestionsSdtDevsMatrix(db.getAllMovies()),
				"MoviesQuestionsStdDevs.txt");
		
		System.out.println("Saving users questions stddevs values to file");
		saveMatrixToFile(getQuestionsSdtDevsMatrix(db.getAllUsers()),
				"UsersQuestionsStdDevs.txt");
	}

	private static void analyzeNormalizedRentsStdDevs(NetflixDB db) {
		System.out.println("Saving movies rents stddevs values to file");
		saveMatrixToFile(getRentsSdtDevsMatrix(db.getAllMovies()),
				"MoviesRentsStdDevs.txt");
		
		System.out.println("Saving users rents stddevs values to file");
		saveMatrixToFile(getRentsSdtDevsMatrix(db.getAllUsers()),
				"UsersRentsStdDevs.txt");
	}
	
	private static void analyzeStdDevs(NetflixDB db) {
		System.out.println("Saving movies rents stddevs values to file");
		saveVectorToFile(getSdtDevsVector(db.getAllMovies()),
				"MoviesStdDevs.txt");
		
		System.out.println("Saving users rents stddevs values to file");
		saveVectorToFile(getSdtDevsVector(db.getAllUsers()),
				"UsersStdDevs.txt");
	}

	private static void analyseRates(NetflixDB db) {
		System.out.println("Saving movies rates values to file");
		saveVectorToFile(getAvgRatesVector(db.getAllMovies()),
				"MoviesRates.txt");
		
		System.out.println("Saving users rates values to file");
		saveVectorToFile(getAvgRatesVector(db.getAllUsers()),
				"UsersRates.txt");
	}

	private static void analyzeRents(NetflixDB db) {
		System.out.println("Saving movies rents values to file");
		saveVectorToFile(getRentsVector(db.getAllMovies()),
				"MoviesRents.txt");
		
		System.out.println("Saving users rents values to file");
		saveVectorToFile(getRentsVector(db.getAllUsers()),
				"UsersRents.txt");
	}
	
	private static void analyzeQuestions(NetflixDB db) {
		System.out.println("Saving movies questions values to file");
		saveVectorToFile(getQuestionsVector(db.getAllMovies()),
				"MoviesQuestions.txt");
		
		System.out.println("Saving users questions values to file");
		saveVectorToFile(getQuestionsVector(db.getAllUsers()),
				"UsersQuestions.txt");
	}

	private static Collection<Integer> getRentsVector(NetflixObject[] objects) {
		ArrayList<Integer> rents = new ArrayList<Integer>(objects.length);
		for (NetflixObject obj : objects) {
			if (obj != null) {
				rents.add(obj.getNumOfRents());
			}
		}
		return rents;
	}
	
	private static Collection<Integer> getQuestionsVector(NetflixObject[] objects) {
		ArrayList<Integer> questions = new ArrayList<Integer>(objects.length);
		for (NetflixObject obj : objects) {
			if (obj != null) {
				questions.add(obj.getNumOfQuestions());
			}
		}
		return questions;
	}
	
	private static Collection<Float> getAvgRatesVector(NetflixObject[] objects) {
		ArrayList<Float> rates = new ArrayList<Float>(objects.length);
		for (NetflixObject obj : objects) {
			if (obj != null) {
				rates.add(obj.getAvgRate());
			}
		}
		return rates;
	}
	
	private static Float[][] getRentsSdtDevsMatrix(NetflixObject[] objects) {
		Float[][] matrix = new Float[objects.length][2];
		for (NetflixObject obj : objects) {
			if (obj != null) {
				matrix[obj.getId()][0] = obj.getStdDev();
				matrix[obj.getId()][1] = (float) obj.getNumOfRents();
			}
		}
		return matrix;
	}
	
	private static Float[][] getQuestionsSdtDevsMatrix(NetflixObject[] objects) {
		Float[][] matrix = new Float[objects.length][2];
		for (NetflixObject obj : objects) {
			if (obj != null) {
				matrix[obj.getId()][0] = obj.getStdDev();
				matrix[obj.getId()][1] = (float) obj.getNumOfQuestions();
			}
		}
		return matrix;
	}
	
	private static Collection<Float> getSdtDevsVector(NetflixObject[] objects) {
		ArrayList<Float> rates = new ArrayList<Float>(objects.length);
		for (NetflixObject obj : objects) {
			if (obj != null) {
				rates.add(obj.getStdDev());
			}
		}
		return rates;
	}

	public static void analyzeRatios(NetflixDB db) {
		System.out.println("Saving movies rents-questions valid values to file");
		saveVectorToFile(getRentsQuestionsValidRatioVector(db.getAllMovies()),
				"ValidMoviesRentsToQuestionsRatios.txt");
		
		System.out.println("Saving movies rents-questions invalid values to file");
		saveVectorToFile(getRentsQuestionsInvalidRatioVector(db.getAllMovies()),
				"InvalidMoviesRentsToQuestionsRatios.txt");
		
		System.out.println("Saving users rents-questions valid values to file");
		saveVectorToFile(getRentsQuestionsValidRatioVector(db.getAllUsers()),
				"ValidUsersRentsToQuestionsRatio.txt");
		
		System.out.println("Saving users rents-questions invalid values to file");
		saveVectorToFile(getRentsQuestionsInvalidRatioVector(db.getAllUsers()),
				"InvalidUsersRentsToQuestionsRatio.txt");
		
		System.out.println("Saving movies questions-rents valid values to file");
		saveVectorToFile(getQuestionsRentsValidRatioVector(db.getAllMovies()),
				"ValidMoviesQuestionsToRentsRatios.txt");
		
		System.out.println("Saving movies questions-rents invalid values to file");
		saveVectorToFile(getQuestionsRentsInvalidRatioVector(db.getAllMovies()),
				"InvalidMoviesQuestionsToRentsRatios.txt");
		
		System.out.println("Saving users questions-rents valid values to file");
		saveVectorToFile(getQuestionsRentsValidRatioVector(db.getAllUsers()),
				"ValidUsersQuestionsToRentsRatio.txt");
		
		System.out.println("Saving users questions-rents invalid values to file");
		saveVectorToFile(getQuestionsRentsInvalidRatioVector(db.getAllUsers()),
				"InvalidUsersQuestionsToRentsRatio.txt");
		
		System.out.println("Ratios analysis finished");
	}
	
	public static void saveVectorToFile(Collection<? extends Number> values, String fileName) {			
		try {
			//creating output directory with current time
			File file = new File(OUT_DIR, fileName);
			if (!file.exists()) {
				file.createNewFile();
				BufferedWriter out = new BufferedWriter(new FileWriter(file));	
				for (Number val : values) {
					out.write(String.valueOf(val));
					out.write(' ');
				}
				out.newLine();
				out.close();
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private static void saveMatrixToFile(Number[][] values,
			String fileName) {
		try {
			File file = new File(OUT_DIR, fileName);
			if (!file.exists()) {
				file.createNewFile();
				BufferedWriter out = new BufferedWriter(new FileWriter(file));	
				boolean newLine;
				for (int i = 0; i < values.length; i++) {
					newLine = false;
					for (int j = 0; j < values[i].length; j ++) {
						if (values[i][j] != null) {
							out.write(String.valueOf(values[i][j]));
							out.write(' ');
							newLine = true;
						}
					}
					if (newLine) {
						out.newLine();
					}
				}				
				out.close();
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public static Collection<Float> getRentsQuestionsValidRatioVector(NetflixObject[] objects) {
		ArrayList<Float> ratioList = new ArrayList<Float>(objects.length);
		for (NetflixObject obj : objects) {
			if (obj != null && obj.getNumOfQuestions() != 0) {
				ratioList.add((float) obj.getNumOfRents() 
						/ (float) obj.getNumOfQuestions());
			}
		}
		return ratioList;
	}
	
	public static Collection<Float> getRentsQuestionsInvalidRatioVector(NetflixObject[] objects) {
		ArrayList<Float> ratioList = new ArrayList<Float>(objects.length);
		for (NetflixObject obj : objects) {
			if (obj != null && obj.getNumOfQuestions() == 0) {
				ratioList.add((float) obj.getNumOfRents());
			}
		}
		return ratioList;
	}
	
	public static Collection<Float> getQuestionsRentsValidRatioVector(NetflixObject[] objects) {
		ArrayList<Float> ratioList = new ArrayList<Float>(objects.length);
		for (NetflixObject obj : objects) {
			if (obj != null && obj.getNumOfRents() != 0) {
				ratioList.add((float) obj.getNumOfQuestions() 
						/ (float) obj.getNumOfRents());
			}
		}
		return ratioList;
	}
	
	public static Collection<Float> getQuestionsRentsInvalidRatioVector(NetflixObject[] objects) {
		ArrayList<Float> ratioList = new ArrayList<Float>(objects.length);
		for (NetflixObject obj : objects) {
			if (obj != null && obj.getNumOfRents() == 0) {
				ratioList.add((float) obj.getNumOfRents());
			}
		}
		return ratioList;
	}

	public static NetflixDB loadDB(String dbFileName) {
		NetflixDB db = null;
		System.out.println("Loading db from persistent storage");
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new FileInputStream(dbFileName));
			db = (NetflixDB) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return db;
	}
}
