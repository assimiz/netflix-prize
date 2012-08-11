package dbanalysis;

import java.io.Serializable;
import java.util.Date;


public class Rent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3013236218813948962L;

	private int movieId;
	
	private int userId;
	
	private Date date;
	
	private int rate;

	
	/**
	 * @param movieId
	 * @param userId
	 * @param date
	 * @param rate
	 */
	public Rent(int movieId, int userId, Date date, int rate) {
		this.movieId = movieId;
		this.userId = userId;
		this.date = date;
		this.rate = rate;
	}

	/**
	 * @return the movieId
	 */
	public int getMovieId() {
		return movieId;
	}

	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return the rate
	 */
	public int getRate() {
		return rate;
	}

	@Override
	public boolean equals(Object obj) {
		Rent rent = (Rent) obj;
		return rent.getMovieId() == (this.movieId)
			&& rent.getUserId() == (this.userId)
			&& ((rent.getDate() == null && this.date == null) || rent.getDate().equals(this.date))
			&& rent.getRate() == this.rate;
	}

	@Override
	public int hashCode() {
		return 3 * movieId +
			   7 * userId +
			   11 * (date != null ? date.hashCode() : 0) +
			   13 * rate;
	}

	@Override
	public String toString() {
		return "MovieID = {" + movieId + "}, UserID = {" + userId + "}"
		+ "Rate = " + rate + ", Date = " + date;
	}
}
