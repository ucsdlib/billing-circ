package edu.ucsd.library.bursar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;


public class ReportUtility {
	private static Logger log = Logger.getLogger( ReportUtility.class );
	private static final String FILE_LOCATION = "java:comp/env/clusterSharedPath";
	private static final String FILE_NAME = "LAST_REPORT_DATE"; //local
	private static final SimpleDateFormat DB2_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static String FILE_PATH = null;
	private static void init()
	{
		if ( FILE_PATH == null )
		{
			try {
				Context initCtx = new InitialContext();
				FILE_PATH = (String)initCtx.lookup(FILE_LOCATION) + "bursar/" + FILE_NAME;
			} catch (NamingException e) {
				log.error("Error initializing Bursar report location", e);
			}
		}
	}
	/**
	 * Write the latest report date 
	 * @param date 
	 * @return
	 */
	public static synchronized boolean writeLastDate(String date){
		log.info("=======WRITING LAST DATE BEGIN===================");
		init();
		if(!validNewDate(getLastDate(), date)){
			return false; //the new date is not greater than old date
		}
		try {
			//remove current file
			ReportUtility.removeOutputFile();
			OutputStream outputFile = new FileOutputStream( FILE_PATH );
			ObjectOutputStream cout = new ObjectOutputStream( outputFile );
			cout.writeObject(date);
			cout.close();
		} catch (FileNotFoundException e) {
			log.error("Bursar report file not found in write()", e);
			return false;
		} catch (IOException e) {
			log.error("Error loading Bursar report file in write()", e);
			return false;
		}
		return true;
	}
	/**
	 * Get the last date the report was ran
	 * @return
	 */
	public static synchronized String getLastDate(){
		log.info("=======GETTING LAST DATE BEGIN===================");
		init();
		if(fileExists()){
			try {
				FileInputStream fis = new FileInputStream(FILE_PATH);
				ObjectInputStream in = new ObjectInputStream(fis);
				return (String)in.readObject();
			} catch (FileNotFoundException e) {
				log.error("Bursar report file not found in read()", e);
			} catch (IOException e) {
				log.error("Error loading Bursar report file in read()", e);
			} catch (ClassNotFoundException e) {
				log.error("Could not case the read data as a String", e);
			}
		}
		log.warn("Error occurred getting Last Date, returning dummy date");
		return "2008-12-01";
		
	}
	/**
	 * if required, remove the file created
	 * @return
	 */
	private static boolean removeOutputFile(){
		File f = new File(FILE_PATH);
		if(!f.exists()){
			//throw some error
			return false;
		}else{
			return f.delete();
		}
	}
	/**
	 * Check that file exists on the system
	 * @return
	 */
	private static boolean fileExists(){
		File f = new File(FILE_PATH);
		return f.exists();
	}
	/**
	 * Make sure new date is greater than old date
	 * @param oldDate
	 * @param newDate
	 * @return
	 */
	private static boolean validNewDate(String oldDate, String newDate){
		Date d1 = null;
		Date d2 = null;
		try {
			d1 = DB2_DATE_FORMAT.parse(oldDate);
			d2 = DB2_DATE_FORMAT.parse(newDate);
		} catch (ParseException e) {
			log.error("Date parse error for: "+d1+" and "+d2, e);
			return false;
		}
		int i = d1.compareTo(d2);
		if(i < 0) {
			return true;
		} 
		else {
			return false;
		}

	}
}
