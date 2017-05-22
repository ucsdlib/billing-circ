package edu.ucsd.library.billing;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import javax.naming.NamingException;

import edu.ucsd.library.util.sql.ConnectionManager;

public class GetSessionDates extends HttpServlet {
	private static Logger log = Logger.getLogger( GetSessionDates .class );
	boolean foundRequestData = true;
	String searchval = null;
	String searchCriteria = null;
	HttpSession session = null;
	JSONObject results =new JSONObject() ;
	
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}
	
	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		log.info("$$$$$$$$$ GetSessionDates BEGIN $$$$$$$$$$$$$$$$ ");
		session = request.getSession();
		try{
			JSONArray sessionDates =getSessionDates();
			log.info("$$$$$$$$$ sessionDates size"+sessionDates.size());
			results.put("sessiondates",sessionDates);
			//log.info("$$ size of the invoiceNoteArray:"+invoiceNoteArray.size());
			response.setContentType("text/plain;charset=UTF-8");
			response.addHeader("Pragma", "no-cache");
			response.setStatus(200);
			PrintWriter writer = new PrintWriter(response.getOutputStream());
			writer.write(results.toString());
			writer.close();
		
		log.info("$$$$$$$$$ GetSessionDates END $$$$$$$$$$$$$$$$ ");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		log.info("Error sending back queue data from GetSessionDates", e);
	}
		
	}
	
	private JSONArray getSessionDates()
	{
		log.info("inside getSessionDates() ");
		JSONArray sessionDates = new JSONArray();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String date = null;
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			 rs = stmt.executeQuery("SELECT DISTINCT TRANSACTIONDATE FROM PENDING_HISTORY ORDER BY TRANSACTIONDATE DESC");
				
				while (rs.next()) {
					
					date = rs.getString(1);
					log.info("dates:"+date);
					JSONObject obj = new JSONObject();
					 obj.put("date",date);
					 sessionDates.add(obj);
				// System.out.println("byeee");
				}
		conn.close();
	} catch (NumberFormatException e) {
		// TODO Auto-generated catch block
		log.error("NumberFormatException", e);
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		log.error("SQLException", e);
	} catch (NamingException e) {
		log.error("JNDI Lookup failed for DB2 connection", e);
	}
		
		return sessionDates;
	}
	
	
	
	
}