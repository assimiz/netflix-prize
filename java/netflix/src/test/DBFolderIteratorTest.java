package test;

import common.DBFolderIterator;

public class DBFolderIteratorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DBFolderIterator iterator = new DBFolderIterator(args[0]);
		
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}
	}

}
