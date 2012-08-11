package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import dbanalysis.Rent;

public class DBFolderIterator implements Iterator<Rent> {

	private BufferedReader br;
	private File folder;
	private File[] files;
	private int current;
	private String line;
	private StringTokenizer lineTokenizer;
	private String lineDelimiter = ",";
	private String newFileIndicator = ":";
	private DateFormat dateFormat;
	private int movieId;
	
	public DBFolderIterator(String path) {
		try {
			folder = new File(path);
			files = folder.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.contains(".txt");
				}
				
			});
			current = 0;
			br = new BufferedReader(new FileReader(currentFile()));			
			line = br.readLine();
			dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		} catch (IOException e) {
			System.err.println("Error while initializing iterator.");
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean hasNext() {
		return line != null;
	}

	@Override
	public Rent next() {	
		Rent rent = null;
		try {
			if (line.contains(newFileIndicator)) {
				movieId = Integer.valueOf(line.substring(0, line.lastIndexOf(':')));
				line = br.readLine();
			}
			//returning current line
			rent = produceRent();

			//reading next line from current file.
			line = br.readLine();
			if (line == null) {
				br.close();
				//in this case we reached end of current file
				//looking for next file
				if (!filesExhausted()) {
					current++;
					br = new BufferedReader(new FileReader(currentFile()));
					line = br.readLine();											
				}
			}
		} catch (IOException e) {
			line = null;
			System.err.println("Error while reading next line. File = "
					+ currentFile().getName());
			e.printStackTrace();
		}
		return rent;
	}

	private Rent produceRent() {		
		lineTokenizer = new StringTokenizer(line, lineDelimiter);					
		int userId = Integer.valueOf(lineTokenizer.nextToken());
		int rate = Integer.valueOf(lineTokenizer.nextToken());
		Date date = null;
		try {
			date = dateFormat.parse(lineTokenizer.nextToken());
		} catch (ParseException e1) {
			System.err.println("Error while parsing file " + currentFile().getName()
					+ ". line = " + line);
			e1.printStackTrace();
		}
		return new Rent(movieId, userId, date, rate);
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Netflix DB is ReadOnly");
	}
	
	private boolean filesExhausted() {
		return current == files.length - 1;
	}
	
	private File currentFile() {
		return files[current];
	}
}
