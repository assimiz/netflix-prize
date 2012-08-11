package dbanalysis;

/**
 * File to load all netflix data to sql DB. According to
 * http://setupandconfig.blogspot.com/2008/04/loading-up-netflix-prize-data-into.html
 * @author mizrachi
 *
 */
public class CreateSqlLoader {  
	 
	   public static void main (String[] arg) {  
	 
	       System.out.printf("use netflix\n");  
	 
	       for (int i = 1;  i <= 17770; i++) {  
	 
	           System.out.printf("load data local infile 'C:/Documents and Settings/mizrachi/My Documents/Assaf/Private/Master/Thesis/Files/NetflixDB/training_set/mv_%07d.txt' into table rating_all fields terminated by ',' lines terminated by '\\n' ignore 1 lines (@userid,@rating,@date) set movieid=%d, userid=@userid, rating=@rating, date=@date;\n",i,i);  
	 
	                
	 
	       }  
	 
	   }  
	 
	}  
