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

public class GetSearchResults extends HttpServlet {
	private static Logger log = Logger.getLogger( GetSearchResults .class );
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
		log.info("$$$$$$$$$ GetSearchResults BEGIN $$$$$$$$$$$$$$$$ ");
		session = request.getSession();
		try{
			searchval = request.getParameter("searchval");
			log.info("$$$$ searchval:"+searchval);
			searchCriteria = request.getParameter("searchCriteria");
			log.info("$$$$ searchCriteria:"+searchCriteria);
			
		}
		catch(Exception e2)
		{
			foundRequestData = false;
			log.error("NO data got from request in GetSearchResults servlet: searchval, searchCriteria are expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from GetSearchResults servlet", e);
				return;
			}
			
		}
		try{
		JSONArray tempArray =getResults(searchval,searchCriteria);
		results.put("searchCriteria",searchCriteria);
		results.put("searchResultArray",tempArray);
		results.put("searchResultArraySize",tempArray.size());
		log.info("$$ size of the searchResultArray:"+tempArray.size());
		response.setContentType("text/plain;charset=UTF-8");
		response.addHeader("Pragma", "no-cache");
		response.setStatus(200);
		PrintWriter writer = new PrintWriter(response.getOutputStream());
		writer.write(results.toString());
		writer.close();
		
		
		log.info("$$$$$$$$$ GetSearchResults END $$$$$$$$$$$$$$$$ ");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		log.info("Error sending back queue data from GetSearchResults", e);
	}
		
	}
	
	public JSONArray getResults(String searchval, String searchCriteria)
	{
		JSONArray res = new JSONArray();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String query = null;
		String patronNo = null;
		String pid = null;
		String patronName = null;
		boolean flag = false;
		if (searchCriteria.equalsIgnoreCase("Name"))
		{
			 String name = searchval.toUpperCase();
			query = " SELECT PATRONNO,PID,PATRONNAME FROM PATRONS WHERE upper(PATRONNAME) LIKE"+"'" +name+"%' ORDER BY UPPER(PATRONNAME)";
		}
		else if (searchCriteria.equalsIgnoreCase("PID"))
		{
			 String pidd = searchval.toUpperCase();
			query = " SELECT PATRONNO,PID,PATRONNAME FROM PATRONS WHERE upper(PID) LIKE"+"'" +pidd+"%'";
		}
		else if (searchCriteria.equalsIgnoreCase("PatronRecNo"))
			query = " SELECT PATRONNO,PID,PATRONNAME FROM PATRONS WHERE PATRONNO LIKE"+"'" +searchval+"%'";
		else if (searchCriteria.equalsIgnoreCase("InvoiceNo"))
		{
			flag = true;
			query = "SELECT PATRONNO, INVOICENO FROM TRANSACTIONS WHERE INVOICENO LIKE"+"'" +searchval+"%'";
		}
		log.info("$$$$$$$$$$ query is:"+query);
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			if(flag){
			while (rs.next()) {
				
				String patronno = rs.getString(1);
				String num = rs.getString(2);
				JSONObject obj = new JSONObject();
				 obj.put("patronNo",patronno);
				 obj.put("invoiceNo",num);
				 log.info("$$$$ INSIDE if -INVOICE $$$$");
				 log.info("$$$$ patronno:"+patronno);
				 log.info("$$$$ invoice:"+num);
				 res.add(obj);
				 
				
			}
			}
			else
			{
				log.info("$$$$ INSIDE else -INVOICE $$$$");
				while (rs.next()) {
				String patronno = rs.getString(1);
				String pidd = rs.getString(2);
				String pName = rs.getString(3);
				log.info("$$$$ patronNo:"+patronno);
				log.info("$$$$ pid:"+pidd);
				log.info("$$$$ pName:"+pName);
				 JSONObject obj = new JSONObject();
				 obj.put("patronNo",patronno);
				 obj.put("PID",pidd);
				 obj.put("patronName",pName);
				 res.add(obj);
				}
			}
			conn.close();
		}
		 catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				log.error("NumberFormatException", e);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				log.error("SQLException", e);
			} catch (NamingException e) {
				log.error("JNDI Lookup failed for DB2 connection", e);
			}
		
		
		
		
		
		
		
		return res;
	}
	
	
}