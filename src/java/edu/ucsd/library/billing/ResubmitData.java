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
import edu.ucsd.library.util.sql.ConnectionManager;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import javax.naming.NamingException;


public class ResubmitData extends HttpServlet {
	private static Logger log = Logger.getLogger( ResubmitData.class );
	boolean foundSessionData = true;
	boolean foundRequestData = true;
	JSONObject results =new JSONObject() ;
	JSONArray dummy = null;
	JSONArray deletedArray = null;
	JSONArray addedArray = null;
	JSONArray pending = null;
	JSONArray problem = null;
	JSONArray newPatron = null;
	JSONArray sessionArray = null;
	JSONArray newPending = new JSONArray();
	HttpSession session = null;
	String invoiceNumString = null;
	String  invoiceNoList= null;
	boolean flag = true;
	String errorMessage = null;
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request,HttpServletResponse response){
		log.info("ResubmitData: $$$$$$$$$ BEFORE $$$$$$$$$$$$$$");
		session = request.getSession();
		 pending = (session.getAttribute("pendingData") != null ) ? (JSONArray)session.getAttribute("pendingData") : null;
		 problem = (session.getAttribute("problemData") != null ) ? (JSONArray)session.getAttribute("problemData") : null;
		 newPatron = (session.getAttribute("newPatronData") != null ) ? (JSONArray)session.getAttribute("newPatronData") : null;
		 sessionArray = (session.getAttribute("sessionData") != null ) ? (JSONArray)session.getAttribute("sessionData") : null;
		 log.info("MoveToOtherQueues: Found data from session");
		
		if((pending == null )|| (problem == null )||(newPatron == null ))
				{
			//commented aug 19th
			//foundSessionData = false;
			//flag = false;
			//errorMessage = "Pending Queue  is empty! Load today's file first!";
			pending = new JSONArray();
			problem = new JSONArray();
			newPatron = new JSONArray();
				}
		if(sessionArray == null )
				{
			foundSessionData = false;
			flag = false;
			errorMessage = "Session Data  is empty! Try loading again!";
				}
		
			try{
				invoiceNoList = request.getParameter("invoiceArr");
				log.info("$$$$ invoiceNoList:"+invoiceNoList);
				
			}
			catch(Exception e2)
			{
				foundRequestData = false;
				flag = false;
				errorMessage = "No data came from selected record!!";
				log.error("NO data got from request in ResubmitData servlet: PENDINGID is expected");
				try {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
				} catch (IOException e) {
					log.error("There was an error sending error message back in response from ResubmitData servlet", e);
					return;
				}
				
			}
			
			if(foundSessionData && foundRequestData){
				try {
					
					log.info("$$$$ size of the pending queue in sesssion:"+pending.size());
					log.info("$$$$ size of the newPatron queue in sesssion:"+newPatron.size());
					log.info("$$$$ size of the problem queue in sesssion:"+problem.size());
					String [] temp = invoiceNoList.split(",");
					addedArray =addRecords(sessionArray,pending,temp);
					deletedArray = deleteRecords(sessionArray,temp);
					JSONObject rows = new JSONObject();
					rows.put("rows",deletedArray);
					rows.put("total",deletedArray.size());	
					
					JSONObject queues = new JSONObject();
					
					results.put("pending", addedArray);
					results.put("problem", problem);
					results.put("newPatron", newPatron);
					results.put("pendingTotal",addedArray.size());
					results.put("problemTotal",problem.size());
					results.put("newPatronTotal",newPatron.size());
					
					
					session.setAttribute("problemData", problem);
					session.setAttribute("pendingData", addedArray);
					session.setAttribute("newPatronData", newPatron);
					session.setAttribute("sessionData", deletedArray);
					
					results.put("sessionData",rows);
					//results.put("queues",queues);						
					results.put("result", "success");
					results.put("flag", flag);
					results.put("errorMessage", errorMessage);
					response.setContentType("text/plain;charset=UTF-8");
					response.addHeader("Pragma", "no-cache");
					response.setStatus(200);
					PrintWriter writer = new PrintWriter(response.getOutputStream());
					writer.write(results.toString());
					writer.close();
					
			
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.info("Error sending back queue data from MoveToOtherQueues", e);
				}
			}
			
			else
			{
				//send error message and flag
				try{
				results.put("result", flag);
				results.put("errorMessage", errorMessage);
				response.setContentType("text/plain;charset=UTF-8");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(200);
				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write(results.toString());
				writer.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.info("Error sending back queue data from MoveToOtherQueues", e);
			}
			}
			
			
		
		
		
		log.info("ResubmitData: $$$$$$$$$ END $$$$$$$$$$$$$$");
		
	}
	
	public JSONArray addRecords(JSONArray from,JSONArray to,String[] temp)
	{
		boolean flag = false;
		JSONArray newArray = to;
		int tokencount= temp.length;
		for(int i=0; i < from.size();i++)
		  {
			 flag = false;
			  JSONObject obj = (JSONObject)from.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				 
				  if((obj.get("pendingID")).equals(temp[j]))
				  {
					  flag = true;
					  newArray.add(obj); 
				  }
			  }
			  
			  			  
		  }
		return newArray;
	}
	
	public JSONArray deleteRecords(JSONArray from,String[] temp)
	{
		boolean flag = false;
		JSONArray newArray = new JSONArray();
		int tokencount= temp.length;
		for(int i=0; i < from.size();i++)
		  {
			 flag = false;
			  JSONObject obj = (JSONObject)from.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				 
				  if((obj.get("pendingID")).equals(temp[j]))
				  {
					  flag = true;
					  break;
				  }
			  }
			  
			  if(!flag)
			  {
				  newArray.add(obj);  
			  }
			  
		  }
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManager.getConnection("billing");
			//stmt = conn.createStatement();
			for(int k=0;k<tokencount;k++)
			{
			//stmt.executeUpdate(" DELETE FROM BILLING.PENDING_HISTORY WHERE PENDINGID =" + "'" + temp[k] + "'");
				    PreparedStatement pstmt = conn.prepareStatement("DELETE FROM PENDING_HISTORY WHERE PENDINGID =?");
				    pstmt.setString( 1,temp[k] );
				    pstmt.execute();
				    
				    conn.commit();
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
	
		return newArray;
	}
	
}