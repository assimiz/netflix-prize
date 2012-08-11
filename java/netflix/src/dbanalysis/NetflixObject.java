package dbanalysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import common.Compartors;


public abstract class NetflixObject implements Serializable, Comparable<NetflixObject>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2864329175899729374L;

	private int id;
	
	private int numOfRents;
	
	private int numOfQuestions;
	
	private int accRate;
	
	private int powAccRate;
	
	private List<Rent> rents;
	
	private List<Rent> questions;
	
	public NetflixObject(int id) {
		this.id = id;
		numOfRents = 0;
		numOfQuestions = 0;
		accRate = 0;
		powAccRate = 0;
		rents = new ArrayList<Rent>();
		questions = new ArrayList<Rent>();
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}



	/**
	 * @return the numOfRents
	 */
	public int getNumOfRents() {
		return numOfRents;
	}



	/**
	 * @return the numOfQuestions
	 */
	public int getNumOfQuestions() {
		return numOfQuestions;
	}



	/**
	 * @return the avgRate
	 */
	public float getAvgRate() {
		return (float) ((float) accRate / (float) numOfRents);
	}
	
	/**
	 * 
	 * @return the minimum rate granted by/for this object
	 */
	public int getMinRate() {
		int minRate = Integer.MAX_VALUE;
		for (Rent r : rents) {
			if (r.getRate() < minRate) {
				minRate = r.getRate();
			}
		}
		return minRate;
	}
	
	/**
	 * 
	 * @return the minimum rate granted by/for this object
	 */
	public int getMaxRate() {
		int maxRate = Integer.MIN_VALUE;
		for (Rent r : rents) {
			if (r.getRate() > maxRate) {
				maxRate = r.getRate();
			}
		}
		return maxRate;
	}
	
	/**
	 * @return the avgRate
	 */
	public float getStdDev() {
		return (float) Math.sqrt(((float) ((float) powAccRate / (float) numOfRents))
				- (float) Math.pow(getAvgRate(), 2));
	}
	
	/**
	 * 
	 * @param rent
	 */
	public void updateRent(Rent rent) {
		//check overflow
		if (accRate + rent.getRate() < accRate) {
			throw new ValueOverflowException(accRate);
		}
		accRate += rent.getRate();
		
		//check overflow
		if (powAccRate + Math.pow(rent.getRate(), 2) < powAccRate) {
			throw new ValueOverflowException(powAccRate);
		}
		powAccRate += Math.pow(rent.getRate(), 2);
		numOfRents++;
	}
	
	/**
	 * 
	 * @param rent
	 */
	public void updateQuestion(Rent rent) {
		numOfQuestions++;
	}
	
	public void addRent(Rent rent) {
		rents.add(rent);
	}
	
	public void addQuestion(Rent unratedRent) {
		questions.add(unratedRent);
	}
	
	public List<Rent> getRents() {
		return rents;
	}

	public List<Rent> getQuestions() {
		return questions;
	} 
	
	/**
	 * Collects and returns all the rents of given rate.
	 * 
	 * @param rate the rate
	 * @return all the rents of the specified rate
	 */
	public List<Rent> getRentsForRate(int rate) {
		return getRentsForRate(getRents(), rate);
	}
	
	/**
	 * Collects and returns all the rents of given rate from a specified list.
	 * 
	 * @param target the list to be searched
	 * @param rate the rate
	 * @return all the rents of the specified rate
	 */
	public static List<Rent> getRentsForRate(List<Rent> target, int rate) {
		//first we sort all rents by their rate
		Rent[] allRents = target.toArray(new Rent[0]);
		Comparator<Rent> byRate = Compartors.createRateComparator();
		Arrays.sort(allRents, byRate);		
		//then we create a matching array containing only the rates.
		//This array will be used for the binary search
		int[] allRates = new int[allRents.length];
		int i = 0;
		for (Rent rent: allRents) {
			allRates[i++] = rent.getRate();
		}
		//now we execute the binary search and finds a single instance 
		int instance = Arrays.binarySearch(allRates, rate);
		//there may be more so we have to search for them too
		ArrayList<Integer> allInstancesOfTheSameRate = new ArrayList<Integer>();
		//but there may be none
		if (instance >= 0) {
			allInstancesOfTheSameRate.add(instance);
			//since the array is ordered we can be sure that all other matches are
			//in adjacent cells
			//so we search forward		
			for (int j = instance + 1; j < allRates.length; j++) {
				if (allRates[j] == rate) {
					allInstancesOfTheSameRate.add(j);
				} else {
					break;
				}
			}
			//and backward
			for (int j = instance - 1; j >= 0; j--) {
				if (allRates[j] == rate) {
					allInstancesOfTheSameRate.add(j);
				} else {
					break;
				}
			}
		}
		//finally we add all instances to the result 
		ArrayList<Rent> matches = new ArrayList<Rent>();
		for (Integer k : allInstancesOfTheSameRate) {
			matches.add(allRents[k]);
		}
		return matches;
	}
	
	@Override
	public boolean equals(Object obj) {
		return ((NetflixObject) obj).getId() == this.id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return "Id = " + id + ", numOfRents = " + numOfRents
		+ ", numOfQuestions = " + numOfQuestions + ", avgRate = " + getAvgRate();
	}

	@Override
	public int compareTo(NetflixObject o) {
		return getId() - o.getId();
	}
}
