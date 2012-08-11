package graphframework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jgrapht.Graph;

import dataframework.Trust;
import dataframework.User;
import dbanalysis.DBMovie;
import dbanalysis.DBUser;
import dbanalysis.NetflixDB;
import dbanalysis.Rent;
import dbanalysis.SqlReadOnlyNetflixDB;

/**
 * Edge Policer that bases its decision upon preliminary RW on the bi-partite Users-Movies graph.
 * Mainains counters for the number of times a user was hit starting a 2-step random walk from
 * another user. Only those user-to-user edges who mostly traveled will be allowed to participate in the
 * graph. Computation is done on initialization.
 * 
 * @author Assaf Mizrachi
 *
 */
public class RandomWalkBasedEdgePolicer extends
		AbstractEdgeAdditionPolicer<User, Trust> {	
	
	private static int RW_LENGTH = 50;
	
	private static int NUM_OF_WALKS = 5;

	private NetflixDB db;
	
	private boolean ready;
	
	private int maxEdges;
	
	private int numberOfUsersWithMoreThanMaxEdges = 0;
	
	//maps from source user (id) to all his neighbors. The neighbors are also
	//mapped to the number of times they were encountered
	private Map<Integer, List<DBUserCounter>> adjacencyMap;
	
	/**
	 * Create a Policer for specified Netflix DB and for a specified number
	 * of edges.
	 * 
	 * @param db the Netflix database
	 * @param maxEdges threshold number of out-edges per user
	 */
	public RandomWalkBasedEdgePolicer(NetflixDB db, int maxEdges) {
		this(db, maxEdges, null);
	}
	
	/**
	 * Create a Policer for specified Netflix DB and for a specified number
	 * of edges
	 * 
	 * @param db the Netflix database
	 * @param maxEdges threshold number of out-edges per user
	 * 
	 */
	public RandomWalkBasedEdgePolicer(NetflixDB db, int maxEdges, EdgeAdditionPolicer<User, Trust> decorator) {
		super(decorator);
		this.db = db;
		this.ready = false;
		this.adjacencyMap = new TreeMap<Integer, List<DBUserCounter>>();
		this.maxEdges = maxEdges;
		this.init();
	}
	
	private void init() {
		DBMovie movie;
		DBUser current, previous;
		for (int round = 0; round < NUM_OF_WALKS; round++) {
			for (int movieId = 1; movieId <= 17770; movieId++) {
				System.out.println("Running RW on bi-partite graph. Round is " + round + ", Movie id is " + movieId);
				movie = db.readMovie(movieId);
				current = chooseRandomUserOf(movie);
				for (int i = 0; i < RW_LENGTH; i++) {
					System.out.println("RW step #" + i);
					previous = current;
					current = chooseRandomUserOf(chooseRandomMovieOf(current));
					
					List<DBUserCounter> friends = adjacencyMap.get(previous.getId());
					if (friends == null) {
						friends = new ArrayList<DBUserCounter>();
					}
					
					DBUserCounter counter = null;
					for (DBUserCounter c : friends) {
						if (c.getUser().getId() == current.getId()) {
							counter = c;
							break;
						}
					}
					
					if (counter == null) {
						counter = new DBUserCounter(current);
					}
					
					friends.add(counter.increment());
					
					adjacencyMap.put(previous.getId(), friends);
				}
			}
		}
		ready = true;
	}
	
	private DBMovie chooseRandomMovieOf(DBUser user) {
		List<Rent> rents = user.getRents();
		int size = rents.size();
		int chosen = (int) Math.round(Math.random() * (size - 1));
		return db.readMovie(rents.get(chosen).getMovieId());
	}
	
	private DBUser chooseRandomUserOf(DBMovie movie) {
		List<Rent> rents = movie.getRents();
		int size = rents.size();
		int chosen = (int) Math.round(Math.random() * (size - 1));
		return db.readUser(rents.get(chosen).getUserId());
	}
	
	@Override
	protected boolean caculate(User from, User to, Graph<User, Trust> graph) {
		List<DBUserCounter> friends = this.adjacencyMap.get(from.getId());
		DBUserCounter[] array = friends.toArray(new DBUserCounter[0]);
		Arrays.sort(array);
		for (int i = 0; i < maxEdges; i++) {
			if (to.getId() == array[i].getUser().getId()) {
				System.out.println("User " + from + " has more than " + maxEdges
						+ " neighbors. (total number of such users found so far = "
						+ ++numberOfUsersWithMoreThanMaxEdges + ")");				
				return true;
			}
		}
		return false;
	}
	
	public boolean isReady() {
		return ready;
	}
	
	private class DBUserCounter implements Comparable<DBUserCounter> {
		private DBUser user;				

		private int counter;
		/**
		 * @param user
		 */
		public DBUserCounter(DBUser user) {
			this.user = user;
			this.counter = 0;
		}
		
		public DBUserCounter increment() {
			counter++;
			return this;
		}

		

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((user == null) ? 0 : user.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DBUserCounter other = (DBUserCounter) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (user == null) {
				if (other.user != null)
					return false;
			} else if (!user.equals(other.user))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DBUserCounter: Id = " + user.getId() + ", counter = " + counter;
		}

		private RandomWalkBasedEdgePolicer getOuterType() {
			return RandomWalkBasedEdgePolicer.this;
		}

		@Override
		public int compareTo(DBUserCounter o) {
			return this.counter - o.counter;
		}
		
		public DBUser getUser() {
			return user;
		}

	}
	
	public static void main(String[] args) {
		SqlReadOnlyNetflixDB db = new SqlReadOnlyNetflixDB(false);
		RandomWalkBasedEdgePolicer policer = new RandomWalkBasedEdgePolicer(db, 30);
	}
}
