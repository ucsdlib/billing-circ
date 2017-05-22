package edu.ucsd.library.billing;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.text.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import javax.naming.NamingException;

import edu.ucsd.library.util.sql.ConnectionManager;

public class GetInvoiceNotes extends HttpServlet {
	private static Logger log = Logger.getLogger( GetInvoiceNotes.class );
	String  invoiceNoList= null;
	JSONObject results =new JSONObject() ;
	
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		log.info("GetInvoiceNotes: $$$$$$$$$ BEGIN $$$$$$$$$$$$$$");
		try{
			invoiceNoList = request.getParameter("invNo");
			log.info("$$$$ invNo:"+invoiceNoList);
			
		}
		catch(Exception e2)
		{
		log.error("NO data got from request in GetInvoiceNotes servlet: invoiceNOARRAY is expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from GetInvoiceNotes servlet", e);
				return;
			}
		
		}
		try{
		String [] temp = invoiceNoList.split(",");
		log.info("####  temp[0]:"+temp[0]);
		JSONArray invoiceNoteArray =getInvoiceNotes(temp[0]);
		results.put("invoiceNoteArray",invoiceNoteArray);
		results.put("sizeInvoiceNoteArray",invoiceNoteArray.size());
		log.info("$$ size of the invoiceNoteArray:"+invoiceNoteArray.size());
		response.setContentType("text/plain;charset=UTF-8");
		response.addHeader("Pragma", "no-cache");
		response.setStatus(200);
		PrintWriter writer = new PrintWriter(response.getOutputStream());
		writer.write(results.toString());
		writer.close();
		invoiceNoteArray=null;
		log.info("GetInvoiceNotes: $$$$$$$$$ END $$$$$$$$$$$$$$");
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			log.info("Error sending back queue data from GetInvoiceNotes", e);
		}
	}
	
	public JSONArray getInvoiceNotes(String invoiceNo)
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		String invoice = null;
		String date = null;
		String userid = null;
		String notes = null;
		String barcode = null;
		String commId =null;
		String responseId = null;
		String expId = null;
		
		double finalAmt;
		 String username = null;
		DecimalFormat df = new DecimalFormat("0.00");		
		JSONArray invoiceNoteArray = new JSONArray();  	
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
		
		        
		        	 rs = stmt.executeQuery(" SELECT INVOICENO,DATEADDED,USERID ,ADDITIONALNOTES,EXPLANATIONID,RESPONSEID,COMMUNICATIONID" +
		 					"	FROM INVOICENOTES " +
		 					"WHERE INVOICENO  ="+ "'"+invoiceNo+"'");
					while (rs.next()) {
						 invoice = rs.getString(1);
						 date = rs.getString(2);
						 userid = rs.getString(3);
						 notes = rs.getString(4);
						 expId= rs.getString(5);
						 responseId= rs.getString(6);
						 commId= rs.getString(7);
					
				     // rs1 = stmt.executeQuery("SELECT username FROM USERS WHERE USERID ="+"'"+userid+"'");
						
						/*while (rs1.next()) {
						username = rs1.getString(1);
						log.info("username in getInvoiceNotes :"+username);
						}*/
						 String expDesc = getExplanation(expId);
						 String comDesc = getCommunication(commId);
						 String resDesc = getResponse(responseId);
						 
						 if(expDesc == null)
						 {
							 expDesc = " ";
							 
						 }
						 if(comDesc == null)
						 {
							 comDesc = " ";
							 
						 }
						 if(resDesc == null)
						 {
							 resDesc = " ";
							 
						 }
						
						 username = getUsername (userid);
						 log.info("username in getInvoiceNotes :"+username);
						 //String newDate =DateFormat.getInstance().format(date) ;
						 JSONObject obj = new JSONObject();
						 obj.put("invoiceNo",invoice);
						 obj.put("date",date);
						 log.info("#####  date : "+date);
						 obj.put("userid",userid);
						 log.info("#### user id:"+userid);
						 obj.put("username",username);
						 log.info("#### username:"+username);
						 obj.put("notes",notes);
						 log.info("#### notes:"+notes);
						 obj.put("resDesc",resDesc);
						 log.info("#### resDesc:"+resDesc);
						 obj.put("comDesc",comDesc);
						 log.info("#### comDesc:"+comDesc);
						 obj.put("expDesc",expDesc);
						 log.info("#### expDesc:"+expDesc);
						 invoiceNoteArray.add(obj);
						 obj = new JSONObject();
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

		return invoiceNoteArray;

		
	}
	
	

	private static String getUsername(String userid) {
		// TODO Auto-generated method stub
		Connection conn = null;
		Statement stmt = null;
		String username1 =null;
		try {
			
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
		
		 ResultSet rs1 = stmt.executeQuery("SELECT username FROM USERS WHERE USERID ="+"'"+userid+"'");
			
			while (rs1.next()) {
			username1 = rs1.getString(1);
			log.info("username in getInvoiceNotes :"+username1);
			// System.out.println("byeee");
			
			}
			rs1.close();
		conn.close();
	} catch (SQLException e) {
		
		log.error("SQLException", e);
	} catch (NamingException e) {
		log.error("JNDI Lookup failed for DB2 connection", e);
	}
	 
		return username1;
	}
	
	private static String getExplanation(String expId)
	{
		Connection conn = null;
		Statement stmt = null;
		String exp =null;
		try {
			
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
		
		 ResultSet rs1 = stmt.executeQuery("SELECT EXPLANATION FROM EXPLANATIONS WHERE EXPLANATIONID ="+"'"+expId+"'");
			
			while (rs1.next()) {
				exp = rs1.getString(1);
			log.info("EXPLANATION in getExplanation :"+exp);
			// System.out.println("byeee");
			
			}
			rs1.close();
		conn.close();
	} catch (SQLException e) {
		
		log.error("SQLException", e);
	} catch (NamingException e) {
		log.error("JNDI Lookup failed for DB2 connection", e);
	}
	 
		return exp;
		
	}
	
	
	private static String getCommunication(String comId)
	{
		Connection conn = null;
		Statement stmt = null;
		String exp =null;
		try {
			
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
		
		 ResultSet rs1 = stmt.executeQuery("SELECT COMMUNICATION FROM COMMUNICATIONS WHERE COMMUNICATIONID ="+"'"+comId+"'");
			
			while (rs1.next()) {
				exp = rs1.getString(1);
			log.info("EXPLANATION in getExplanation :"+exp);
			// System.out.println("byeee");
			
			}
			rs1.close();
		conn.close();
	} catch (SQLException e) {
		
		log.error("SQLException", e);
	} catch (NamingException e) {
		log.error("JNDI Lookup failed for DB2 connection", e);
	}
	 
		return exp;
		
	}
	
	
	private static String getResponse(String resId)
	{
		Connection conn = null;
		Statement stmt = null;
		String exp =null;
		try {
			
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
		
		 ResultSet rs1 = stmt.executeQuery("SELECT RESPONSE FROM RESPONSES WHERE RESPONSEID ="+"'"+resId+"'");
			
			while (rs1.next()) {
				exp = rs1.getString(1);
			log.info("RESPONSE in getExplanation :"+exp);
			// System.out.println("byeee");
			
			}
			rs1.close();
		conn.close();
	} catch (SQLException e) {
		
		log.error("SQLException", e);
	} catch (NamingException e) {
		log.error("JNDI Lookup failed for DB2 connection", e);
	}
	 
		return exp;
		
	}
	
}