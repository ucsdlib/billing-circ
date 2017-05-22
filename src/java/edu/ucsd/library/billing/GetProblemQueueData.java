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
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import javax.naming.NamingException;

import edu.ucsd.library.util.sql.ConnectionManager;

public class GetProblemQueueData extends HttpServlet {
	private static Logger log = Logger.getLogger( GetProblemQueueData.class );
	boolean foundSessionData = true;
	boolean foundRequestData = true;
	JSONObject results =new JSONObject() ;
	JSONArray problem = null;
	JSONArray transArray = new JSONArray();
	JSONArray invoiceNoArray = new JSONArray();
	JSONArray invoiceNoteArray = new JSONArray();
	String patronRecNo = null;
	HttpSession session = null;
	String invoiceNumString = null;
	String  invoiceNoList= null;
	String username = null;
	
	
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
	
		log.info("GetProblemQueueData: $$$$$$$$$ BEGIN $$$$$$$$$$$$$$");
		session = request.getSession();
		problem = (session.getAttribute("problemData") != null ) ? (JSONArray)session.getAttribute("problemData") : null;
		username = (session.getAttribute("username") != null ) ? (String)session.getAttribute("username") : null;
		log.info("GetProblemQueueData: Found data from session");
		log.info("GetProblemQueueData: username:"+username);
		
		if(problem == null ){
			log.error("There is no json data in the session");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "json parameter needed");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from GetProblemQueueData servlet", e);
				return;
			}
		}
		
		
		try{
			invoiceNoList = request.getParameter("invoiceArr");
			log.info("$$$$ invoiceNoList:"+invoiceNoList);
			
		}
		catch(Exception e2)
		{
			foundRequestData = false;
			log.error("NO data got from request in GetProblemQueueData servlet: invoiceNOARRAY is expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from GetProblemQueueData servlet", e);
				return;
			}
			
		}
		
		
		
		if(foundSessionData && foundRequestData){
			try {
				log.info("$$$$ size of the problem queue in sesssion:"+problem.size());
				String [] temp = invoiceNoList.split(",");
				JSONObject data = getData(problem,temp);
				log.info("$$ data:"+data.equals(null));
				java.util.Date today = new java.util.Date();
				Format formatter = new SimpleDateFormat("MM/dd/yy");
			    String strToday = formatter.format(today);
				log.info("$$$ today:"+strToday);
				String userid =getUserID(username);
				results.put("basicData", data);
				JSONArray transactionList = getTransactionList(patronRecNo);
				log.info("$$ size of the transactionList:"+transactionList.size());
				JSONArray explanationIDArray =getExplanationIDList();
				JSONArray communicationIDArray =getCommunicationIDList();
				JSONArray responseIDArray = getResponseIDList();
				log.info("$$ size of the explanationIDArray:"+explanationIDArray.size());
				log.info("$$ size of the communicationIDArray:"+communicationIDArray.size());
				log.info("$$ size of the responseIDArray:"+responseIDArray.size());
				log.info("^%%%%%%%   username"+username);
				results.put("userid",userid);
				results.put("username",username);
				results.put("today",strToday);
				results.put("transactionList",transactionList);
				//results.put("invoiceNoteArray",invoiceNoteArray);
				//results.put("size",invoiceNoteArray.size());
				results.put("explanationIDArray",explanationIDArray);
				results.put("communicationIDArray",communicationIDArray);
				results.put("responseIDArray",responseIDArray);
				//log.info("$$ size of the invoiceNoteArray:"+invoiceNoteArray.size());
				response.setContentType("text/plain;charset=UTF-8");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(200);
				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write(results.toString());
				writer.close();
				log.info("GetProblemQueueData: END");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.info("Error sending back queue data from GetProblemQueueData", e);
			}
			
			}//end of if(foundSe
		
		
	}//end of post
	
	
	public JSONObject getData(JSONArray from,String[] temp)
	{
		boolean flag = false;
		JSONObject newjObj = new JSONObject();
		int tokencount= temp.length;
		//String patronRecNo = null;
		for(int i=0; i < from.size();i++)
		  {
			 flag = false;
			  JSONObject obj2 = (JSONObject)from.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				//=================3/2============
				  String [] arr = temp[j].split("\\|");
				  String invNo = arr[0];
				  String chargeType = arr[1];
				  log.info("$$$$ UUUUUUUUUU getData UUUUUUUUUUUUUUUU");
				  log.info("$$$$ INVno: "+invNo);
				  log.info("$$$$ ChargeType:"+chargeType);
				  //===============================
				  if(((obj2.get("invoiceNo")).equals(invNo)) && ((obj2.get("chargeType")).equals(chargeType)))
				  {
				 // if((obj2.get("invoiceNo")).equals(temp[j]))
				 // {
					  flag = true;
					 // newjObj.put("invoiceNo", obj2.get("invoiceNo"));
					 // newjObj.put("name", obj2.get("name"));
					  String pat = (String)obj2.get("patronRecordNo");
					  log.info("patron Rec No in getData():"+pat);
					  patronRecNo=pat.substring(1);
					  newjObj.put("invoiceNo", obj2.get("invoiceNo"));
					  /*
					  newjObj.put("patronRecordNo", pat.substring(1));
					  newjObj.put("pid", obj2.get("pid"));
					  log.info("$$$$$ name:"+ obj2.get("name"));
					  log.info("$$$$$ patronRecordNo:"+ obj2.get("patronRecordNo"));
					  log.info("$$$$$ pid:"+ obj2.get("pid"));
					  
					  
					  break;
					  */
					  Connection conn = null;
						Statement stmt = null;
						ResultSet rs = null;
						try {
							conn = ConnectionManager.getConnection("billing");
							stmt = conn.createStatement();
							rs = stmt.executeQuery(" SELECT PATRONNO,PID,PATRONNAME,NOTES FROM PATRONS WHERE PATRONNO ="+"'" +patronRecNo+"'");
							while (rs.next()) {
								String patronno = rs.getString(1);
								String pidd = rs.getString(2);
								String pName = rs.getString(3);
								String notes = rs.getString(4);
								newjObj.put("patronRecordNo",patronno);
								newjObj.put("pid", pidd);
								newjObj.put("name", pName);
								if(notes != null)
								{
									newjObj.put("notes", notes.trim());
								}
								else
								{
									newjObj.put("notes", " ");
								}
								log.info("$$$$$$newjObj$$$ "+patronno+" "+pidd+" "+pName+" "+notes);
								// log.info("$$$$$$basic data$$$ "+patronno+" "+pidd+" "+pName);
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
					
					break;
				  }
			  }
			  
			 if(flag)
			 break;
			  
		  }
		return newjObj;
	}
	
	public JSONArray getTransactionList(String patronRecNo)
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String patronNo = patronRecNo;
		String invoice;
		String date;
		double charge;
		double procFee;
		double billingFee;
		String barcode;
		double finalAmt;
		DecimalFormat df = new DecimalFormat("0.00");
		JSONArray transArray = new JSONArray();
		
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(" SELECT T.INVOICENO, T.INVOICEDATE, T.CHARGE,T.PROCESSINGFEE,T.BILLINGFEE ,I.BARCODE" +
					"	FROM TRANSACTIONS T, ITEMS I " +
					"WHERE T.PATRONNO = "+ "'"+patronNo+"'"+" AND I.ITEMNO = T.ITEMNO");
			while (rs.next()) {
				 invoice = rs.getString(1);
				 date = rs.getString(2);
				 charge = rs.getInt(3);
				 procFee = rs.getInt(4);
				 billingFee = rs.getInt(5);
				 barcode = rs.getString(6);
				 
				 finalAmt = charge+procFee+billingFee;
				 String amount = "$"+df.format(finalAmt);
				// String newDate =DateFormat.getInstance().format(date) ;
				 JSONObject obj = new JSONObject();
				 obj.put("invoiceNo",invoice);
				 obj.put("date",date);
				 log.info("##### DATE :"+date);
				// log.info("##### newDate :"+newDate);
				 obj.put("amount",amount);
				 obj.put("barcode",barcode);
				 transArray.add(obj);
				
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

		return transArray;

		
	}
	
	public JSONArray getInvoiceNotes(String patron)
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String patronNo = patronRecNo;
		String invoice;
		String date;
		String userid;
		String notes;
		String barcode;
		double finalAmt;
		DecimalFormat df = new DecimalFormat("0.00");		
	       	
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			 for(int i=0;i<invoiceNoArray.size();i++)
		        {
		        	 String inv = (String)invoiceNoArray.get(i);
		        
					rs = stmt.executeQuery(" SELECT T.INVOICENO,T.ADDEDDATE,T.USERID ,I.ADDITIONALNOTES" +
							"	FROM TRANSACTIONS T, INVOICENOTES I " +
							"WHERE T.INVOICENO = I.INVOICENO AND T.PATRONNO ="+ "'"+inv+"'");
					while (rs.next()) {
						 invoice = rs.getString(1);
						 date = rs.getString(2);
						 userid = rs.getString(3);
						 notes = rs.getString(4);
									
						 //String newDate =DateFormat.getInstance().format(date) ;
						 JSONObject obj = new JSONObject();
						 obj.put("invoiceNo",invoice);
						 obj.put("date",date);
						 obj.put("userid",userid);
						 obj.put("notes",notes);
						 invoiceNoteArray.add(obj);
						
					}
		  }//end of for
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
	
	public String getUserID(String username)
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String userid = null;
		
	       	
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			rs = stmt
			.executeQuery("	SELECT USERID FROM USERS " +
					"WHERE USERNAME = "+ "'"+username+"'");
			while (rs.next()) {
				userid = rs.getString(1);
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
		return userid;
	}
	
	public JSONArray getExplanationIDList()
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		JSONArray explanationIDArray = new JSONArray();	
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			rs = stmt
			.executeQuery("	select EXPLANATIONID,EXPLANATION FROM EXPLANATIONS WHERE EXPLANATIONID != '10'");
			while (rs.next()) {
				int id = rs.getInt(1);
				String explanation = rs.getString(2);
				 JSONObject obj = new JSONObject();
				 obj.put("id",id);
				 obj.put("explanation",explanation);
				 explanationIDArray.add(obj);				
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
		return explanationIDArray;
		}
	
	public JSONArray getResponseIDList()
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		JSONArray responseIDArray = new JSONArray();	
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			rs = stmt
			.executeQuery("	select RESPONSEID,RESPONSE FROM RESPONSES WHERE RESPONSEID != '10'");
			while (rs.next()) {
				int id = rs.getInt(1);
				String response = rs.getString(2);
				 JSONObject obj = new JSONObject();
				 obj.put("id",id);
				 obj.put("response",response);
				 responseIDArray.add(obj);				
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
		return responseIDArray;
		}
	public JSONArray getCommunicationIDList()
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		JSONArray communacationIDArray = new JSONArray();	
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			rs = stmt
			.executeQuery("	select COMMUNICATIONID, COMMUNICATION FROM  COMMUNICATIONS WHERE COMMUNICATIONID != '10' ");
			while (rs.next()) {
				int id = rs.getInt(1);
				String communication = rs.getString(2);
				 JSONObject obj = new JSONObject();
				 obj.put("id",id);
				 obj.put("communication",communication);
				 communacationIDArray.add(obj);				
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
		return communacationIDArray;
		}
}