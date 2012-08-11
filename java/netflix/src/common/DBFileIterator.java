package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import dbanalysis.Rent;

public class DBFileIterator implements Iterator<Rent> {


	private BufferedReader br;
	private String line;
	private StringTokenizer lineTokenizer;
	private String lineDelimiter = ",";
	DateFormat dateFormat;
	
	public DBFileIterator(String fileName) {
		try {
			br = new BufferedReader(new FileReader(new File(fileName)));			
			line = br.readLine();
			dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean hasNext() {
		return line != null;
	}

	@Override
	public Rent next() {	
		//returning current line
		lineTokenizer = new StringTokenizer(line, lineDelimiter);			
		int movieId = Integer.valueOf(lineTokenizer.nextToken());
		int userId = Integer.valueOf(lineTokenizer.nextToken());
		int rate = Integer.valueOf(lineTokenizer.nextToken());
		Date date = null;
		try {
			date = dateFormat.parse(lineTokenizer.nextToken());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Rent rent = new Rent(movieId, userId, date, rate);
		
		//reading next line
		try {
			line = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rent;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Netflix DB is ReadOnly");
	}

}
