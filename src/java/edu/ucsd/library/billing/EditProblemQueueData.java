package edu.ucsd.library.billing;

import java.io.IOException;
import java.io.PrintWriter;

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
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import javax.naming.NamingException;

import edu.ucsd.library.util.sql.ConnectionManager;

public class EditProblemQueueData extends HttpServlet {
	private static Logger log = Logger.getLogger( EditProblemQueueData.class );
	boolean foundSessionData = true;
	boolean foundRequestData = true;
	JSONObject results =new JSONObject() ;
	JSONArray newPatron = null;
	JSONArray pending = null;
	JSONArray problem = null;
	HttpSession session = null;
	String invoiceNumString = null;
	String  invoiceNo= null;
	String pid = null;
	String note = null;
	String patronNo =  null;
	String flagPID = null;
	String flagNote = null;
	String oldPatronNo = null;
	String flagPatron = null;
	
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
	
		log.info("EditProblemQueueData: $$$$$$$$$ BEGIN $$$$$$$$$$$$$$");
		session = request.getSession();
		problem = (session.getAttribute("problemData") != null ) ? (JSONArray)session.getAttribute("problemData") : null;
		newPatron = (session.getAttribute("newPatronData") != null ) ? (JSONArray)session.getAttribute("newPatronData") : null;
		 pending = (session.getAttribute("pendingData") != null ) ? (JSONArray)session.getAttribute("pendingData") : null;
		 
		log.info("EditProblemQueueData: Found data from session");
		
		if(problem == null ){
			foundSessionData = false;
			log.error("There is no json data in the session");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "json parameter needed");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from EditProblemQueueData servlet", e);
				return;
			}
		}
		
		
		try{
			invoiceNo = request.getParameter("invoiceNo");
			log.info("$$$$ invoiceNo:"+invoiceNo);
			pid = request.getParameter("pid");
			log.info("$$$$ PID:"+pid);
			note= request.getParameter("note");
			log.info("$$$$ note:"+note);
			patronNo= request.getParameter("patronNo");
			log.info("$$$$ patronNo:"+patronNo);
			flagPID= request.getParameter("flagPID");
			log.info("$$$$ flagPID:"+flagPID);
			flagNote= request.getParameter("flagNote");
			log.info("$$$$ flagNote:"+flagNote);
			flagPatron= request.getParameter("flagPatron");
			log.info("$$$$ flagPatron:"+flagPatron);
			oldPatronNo= request.getParameter("oldPatronNo");
			log.info("$$$$ oldPatronNo:"+oldPatronNo);
			
			
		}
		catch(Exception e2)
		{
			foundRequestData = false;
			log.error("NO data got from request in EditProblemQueueData servlet: invoiceNOARRAY is expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from EditProblemQueueData servlet", e);
				return;
			}
			
		}
		
		
		
		if(foundSessionData && foundRequestData){
			try {
				log.info("$$$$ size of the problem queue in sesssion:"+problem.size());
				//String [] temp = invoiceNoList.split(",");
				boolean flag = false;
				if(flagPatron.equalsIgnoreCase("false") && flagPID.equalsIgnoreCase("false") && flagNote.equalsIgnoreCase("false"))
				{
					//no need to update the database :just send the data in session
					 results.put("problem", problem);
				}
				else
				{
					 flag = updateDatabase(patronNo,pid,note,flagPID,flagNote,flagPatron,oldPatronNo);
					 if(flag){
						 //update the problem queue in session
						 JSONArray data = getData(patronNo,problem,pid,oldPatronNo);
						 //test
						 session.setAttribute("problemData", data);
						 results.put("problem", data);
						 results.put("problemTotal",data.size());
						 results.put("status","success");
					 }
					 else{
					 // no need to update problem queue since database update failed.
						 results.put("problem", problem);
						 results.put("status","fail");
					
					 }
				}
				
				
				
				results.put("pending", pending);
				results.put("newPatron", newPatron);
				results.put("pendingTotal",pending.size());
				results.put("newPatronTotal",newPatron.size());
				session.setAttribute("pendingData", pending);
				session.setAttribute("newPatronData", newPatron);
				results.put("result", "success");
				
				
				response.setContentType("text/plain;charset=UTF-8");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(200);
				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write(results.toString());
				writer.close();
				log.info("EditProblemQueueData: END");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.info("Error sending back queue data from EditProblemQueueData", e);
			}
			
			}//end of if(foundSe
		
		
	}//end of post
	
	
	public JSONArray getData(String patronNo,JSONArray from,String pid,String oldPatronNo)
	{
		boolean flag = false;
		JSONObject newjObj = new JSONObject();
		JSONArray newArr = new JSONArray();
		String patron = "p"+oldPatronNo;
		String newPatronNo = "p"+patronNo;
		 log.info("$$$$$ SIZE OF THE PROBLEM ARRAY:"+ from.size());
		for(int i=0; i < from.size();i++)
		  {
			 flag = false;
			  JSONObject obj2 = (JSONObject)from.get(i);
			
				 
				  if((obj2.get("patronRecordNo")).equals(patron))
				  {
					  flag = true;
					  log.info("$$$$$ name:"+ obj2.get("name"));
					  log.info("$$$$$ patronRecordNo:"+ obj2.get("patronRecordNo"));
					  log.info("$$$$$ patron:"+ patron);
					  log.info("$$$$$ pid:"+ obj2.get("pid"));			  
					 String str = (String)obj2.get("invoiceNo");
					  
					  newjObj.put("invoiceNo", str);
					  log.info("$$$$$ invoiceNo:"+ str);
					  newjObj.put("date", obj2.get("date"));
					  newjObj.put("loc", obj2.get("loc"));
					  //newjObj.put("patronRecordNo", obj2.get("patronRecordNo"));
					  newjObj.put("patronRecordNo", newPatronNo);
					  newjObj.put("pid", pid);
					  newjObj.put("name", obj2.get("name"));
					  newjObj.put("address",obj2.get("address"));
					  newjObj.put("pcode1", obj2.get("pcode1"));
					  newjObj.put("pcode2", obj2.get("pcode2"));
					  newjObj.put("patronAffliation", obj2.get("patronAffliation"));
					  newjObj.put("patronType", obj2.get("patronType"));
					  newjObj.put("itemBarcode", obj2.get("itemBarcode"));
					  newjObj.put("title", obj2.get("title"));
					  newjObj.put("callNo",obj2.get("callNo"));
                      newjObj.put("chargeType", obj2.get("chargeType"));
                      newjObj.put("amount1", obj2.get("amount1"));
                      newjObj.put("amount2",obj2.get("amount2"));
                      newjObj.put("amount3",obj2.get("amount3"));
                      newjObj.put("rule",obj2.get("rule"));
                      newArr.add(newjObj);
                      newjObj = new JSONObject();
                      log.info("$$$$ newobj invoice no:"+ newjObj.get("invoiceNo"));
                      log.info("$$$$ ADDED PID CHAGE REOCRD $$$$");
                      
				  }
				  else
				  {
					  newArr.add(obj2);
					 
				  }
			  
			  
			
			  
		  }
		 log.info("$$$$$ SIZE OF THE newArr ARRAY:"+ newArr.size());
		/* for(int i=0;i<newArr.size();i++)
		 {
			  JSONObject obj3 = (JSONObject)newArr.get(i);
			  log.info("$$$$$ ======000000000000000000 =============== $$$$");
			  log.info("$$$$$ patronRecordNo:"+ obj3.get("patronRecordNo"));
			  log.info("$$$$$ PID:"+ obj3.get("pid"));
			  log.info("$$$$$ invoiceNo:"+ obj3.get("invoiceNo"));
			  
		 }*/
		return newArr;
	}
	
	public boolean updateDatabase(String patronNo,String pid, String noteMod,String  flagPID,String flagNote,String flagPatron,String oldPatronNo)
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		boolean result = true;
		String query = null;
		PreparedStatement pstmt = null;
		String note=noteMod.trim();
		
		try {
			conn = ConnectionManager.getConnection("billing");
			//stmt = conn.createStatement();				        
			//stmt.executeUpdate(query);
			//conn.close();
		
	    if (flagPatron.equalsIgnoreCase("false") && flagPID.equalsIgnoreCase("true")&& flagNote.equalsIgnoreCase("true"))
		{
			//query = "UPDATE PATRONS SET PID = "+"'"+pid+"'"+", NOTES = "+"'"+note+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET PID = ?, NOTES = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,pid );
			 pstmt.setString( 2,note ); 
			 pstmt.setString( 3,patronNo ); 
		}
		else if (flagPatron.equalsIgnoreCase("false") && flagPID.equalsIgnoreCase("true")&& flagNote.equalsIgnoreCase("false"))
		{
			//query = "UPDATE PATRONS SET PID = "+"'"+pid+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET PID = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,pid );
			 pstmt.setString( 2,patronNo ); 
		}
		
		else if (flagPatron.equalsIgnoreCase("false") && flagPID.equalsIgnoreCase("false")&& flagNote.equalsIgnoreCase("true"))
		{
			//query = "UPDATE PATRONS SET NOTES = "+"'"+note+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET NOTES = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,note );
			 pstmt.setString( 2,patronNo ); 
		}
		else if (flagPatron.equalsIgnoreCase("true") && flagPID.equalsIgnoreCase("true")&& flagNote.equalsIgnoreCase("true"))
		{
			//query = "UPDATE PATRONS SET NOTES = "+"'"+note+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET PATRONNO=? ,PID = ?, NOTES = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,patronNo );
			 pstmt.setString( 2,pid ); 
			 pstmt.setString( 3,note ); 
			 pstmt.setString( 4,oldPatronNo ); 
		}
		else if (flagPatron.equalsIgnoreCase("true") && flagPID.equalsIgnoreCase("true")&& flagNote.equalsIgnoreCase("false"))
		{
			//query = "UPDATE PATRONS SET PID = "+"'"+pid+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET PATRONNO=? , PID = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,patronNo );
			 pstmt.setString( 2,pid ); 
			 pstmt.setString( 3,oldPatronNo ); 
		}
		
		else if (flagPatron.equalsIgnoreCase("true") && flagPID.equalsIgnoreCase("false")&& flagNote.equalsIgnoreCase("true"))
		{
			//query = "UPDATE PATRONS SET NOTES = "+"'"+note+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET PATRONNO=? , NOTES = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,patronNo );
			 pstmt.setString( 2,note ); 
			 pstmt.setString( 3,oldPatronNo ); 
		}
	    
		else if (flagPatron.equalsIgnoreCase("true") && flagPID.equalsIgnoreCase("false")&& flagNote.equalsIgnoreCase("false"))
		{
			//query = "UPDATE PATRONS SET NOTES = "+"'"+note+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET PATRONNO=? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,patronNo );
			 pstmt.setString( 2,oldPatronNo ); 
			
		}
		//try {
			//conn = ConnectionManager.getConnection("billing");
			//stmt = conn.createStatement();				        
			//stmt.executeUpdate(query);
			 pstmt.execute();			    
			 conn.commit();
			conn.close();
		} catch (NumberFormatException e) {
			result = false;
			// TODO Auto-generated catch block
			log.error("NumberFormatException", e);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			result = false;
			log.error("SQLException", e);
		} catch (NamingException e) {
			result = false;
			log.error("JNDI Lookup failed for DB2 connection", e);
		}
		return result;
	}
	
}