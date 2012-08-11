package dataframework;

import java.io.Serializable;


public class User implements Comparable<User>, Serializable {

	private static final long serialVersionUID = -4248602928925166594L;

	private int id;
	
	//for voters this is their original vote.
	//for non-voters this is the recommended value.
	private double value;
	
	private boolean isVoter;
	
	//the following two variables are used for a 
	//RW based recommendation system.
	private double sigma;
	
	private double hitsNumber;

	/**
	 * @param id
	 */
	public User(int id) {
		this.id = id;
		reset();
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
		//TODO throw excpetion when trying to change the value of a voter?
		this.value = value;
	}
	
	/**
	 * @return the isVoter
	 */
	public boolean isVoter() {
		return isVoter;
	}

	/**
	 * @param isVoter the isVoter to set
	 */
	public void setVoter(boolean isVoter) {
		this.isVoter = isVoter;
	}
	
	/**
	 * 
	 * @param vote
	 */
	public void updateValue(double vote) {
		this.sigma += vote;
		this.hitsNumber++;
		this.value = sigma / hitsNumber;
	}
	
	/**
	 * 
	 */
	public void reset() {
		this.sigma = 0;
		this.hitsNumber = 0;
		this.value = 0;
		this.isVoter = false;
	}
	
	/**
	 * 
	 * @return the number of times a walk started or passed through this user
	 * reached a voter.
	 */
	public double getHitsNumber() {
		return hitsNumber;
	}	

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return ((User) obj).id == this.id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public int compareTo(User other) {
		return this.id - other.id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf(id);
	}
}
