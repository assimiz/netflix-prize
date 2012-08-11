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

public class QuestionsFileIterator implements Iterator<Rent> {

	
	private BufferedReader br;
	private String line;
	private StringTokenizer lineTokenizer;
	private String lineDelimiter = ",";
	DateFormat dateFormat;

	private int movieId;

	public QuestionsFileIterator(String fileName) {
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
		if (line.charAt(line.length() - 1) == ':') {
			movieId = Integer.valueOf(line.substring(0, line.length() - 1));
			try {
				line = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		lineTokenizer = new StringTokenizer(line, lineDelimiter);
		int userId = Integer.valueOf(lineTokenizer.nextToken());
		Date date = null;
		if (lineTokenizer.hasMoreTokens()) {
			try {
				date = dateFormat.parse(lineTokenizer.nextToken());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Rent rent = new Rent(movieId, userId, date, 0);
		// reading next line
		try {
			line = br.readLine();
		} catch (IOException e) {
			line = null;
			e.printStackTrace();
		}
		return rent;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Netflix DB is ReadOnly");
	}

	public void finish() {
		try {
			line = null;
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
