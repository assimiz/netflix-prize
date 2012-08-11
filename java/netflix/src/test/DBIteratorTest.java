package test;

import java.util.Iterator;

import dbanalysis.NetflixAnalyzer;
import dbanalysis.NetflixDB;
import dbanalysis.Rent;

public class DBIteratorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NetflixDB db = NetflixAnalyzer.loadDB(args[0]);
		Iterator<Rent> rents = db.rentsIterator();
		while (rents.hasNext()) {
			System.out.println(rents.next());
		}
	}
}
