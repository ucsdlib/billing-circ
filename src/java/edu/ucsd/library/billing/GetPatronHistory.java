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

public class GetPatronHistory extends HttpServlet {
	private static Logger log = Logger.getLogger( GetPatronHistory.class );
	JSONObject results =new JSONObject() ;	
	JSONArray transArray = new JSONArray();
	JSONArray basicDataArray = new JSONArray();
	HttpSession session = null;
	String username = null;
	String patronNo = null;
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}
	
	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		log.info("$$$$$ GetPatronHistory BEGIN $$$$$$$$$$");
		//session = request.getSession();
		//username = (session.getAttribute("username") != null ) ? (String)session.getAttribute("username") : null;
		//log.info("GetPatronHistory: username:"+username);
		java.security.Principal pObj = request.getUserPrincipal();
		username = pObj.getName();
		log.info("USER NAME:"+ username);
		try{
			patronNo = request.getParameter("patronNo");
			log.info("$$$$ patronNo:"+patronNo);
			
		}
		catch(Exception e2)
		{
			//undRequestData = false;
			log.error("NO data got from request in GetProblemQueueData servlet: invoiceNOARRAY is expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from GetProblemQueueData servlet", e);
				return;
			}
			
		}
		try{
			JSONObject basicData= getBasicData(patronNo);
			JSONArray transactionList = getTransactionList(patronNo);
			String userid =getUserID(username);
			log.info("$$ size of the transactionList:"+transactionList.size());
			//log.info("$$ size of the basicDataArray:"+basicDataArray.size());
			results.put("userid",userid);
			results.put("username",username.trim());
			log.info("^%%%%%%% PATHISTORY  username"+username);
			log.info("^%%%%%%% PATHISTORY  username length"+username.length());
			log.info("$$ USER ID IS: "+userid);
			results.put("transactionList",transactionList);
			results.put("basicData", basicData);
			JSONArray explanationIDArray =getExplanationIDList();
			JSONArray communicationIDArray =getCommunicationIDList();
			JSONArray responseIDArray = getResponseIDList();
			log.info("$$ size of the explanationIDArray:"+explanationIDArray.size());
			log.info("$$ size of the communicationIDArray:"+communicationIDArray.size());
			log.info("$$ size of the responseIDArray:"+responseIDArray.size());
			results.put("explanationIDArray",explanationIDArray);
			results.put("communicationIDArray",communicationIDArray);
			results.put("responseIDArray",responseIDArray);
			response.setContentType("text/plain;charset=UTF-8");
			response.addHeader("Pragma", "no-cache");
			response.setStatus(200);
			PrintWriter writer = new PrintWriter(response.getOutputStream());
			writer.write(results.toString());
			writer.close();
		}
		catch(Exception e)
		{
			log.info("Error sending back queue data from GetPatronHistory", e);
		}
		log.info("$$$$$ GetPatronHistory END $$$$$$$$$$");
	}
	
	public JSONObject getBasicData(String patronNo)
	{ JSONObject basicData= new JSONObject();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(" SELECT PATRONNO,PID,PATRONNAME,NOTES FROM PATRONS WHERE PATRONNO ="+"'" +patronNo+"'");
			while (rs.next()) {
				String patronno = rs.getString(1);
				String pidd = rs.getString(2);
				String pName = rs.getString(3);
				String notes = rs.getString(4);
				basicData.put("patronRecordNo",patronno);
				basicData.put("pid", pidd);
				basicData.put("name", pName);
				if(notes != null)
				{
				basicData.put("notes", notes.trim());
				}
				else
				{
					basicData.put("notes", " ");
				}
				log.info("$$$$$$basic data$$$ "+patronno+" "+pidd+" "+pName+" "+notes);
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

		
		return basicData;
	}
	
	public JSONArray getTransactionList(String patronNo)
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;		 
		String invoice;
		String date;		
		String charge;  
		String procFee;
		String billingFee;		
		double chargeAmount;
		double procAmount;
		double billingAmount;
		String barcode;
		double finalAmt;
		DecimalFormat df = new DecimalFormat("0.00");
		JSONArray transArray = new JSONArray();
		
		patronNo = "p"+patronNo;
		
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			/*rs = stmt.executeQuery(" SELECT T.INVOICENO, T.INVOICEDATE, T.CHARGE,T.PROCESSINGFEE,T.BILLINGFEE ,I.BARCODE" +
					"	FROM TRANSACTIONS T, ITEMS I " +
					"WHERE T.PATRONNO = "+ "'"+patronNo+"'"+" AND I.ITEMNO = T.ITEMNO ORDER BY (T.INVOICEDATE) DESC"); */
			
			rs = stmt.executeQuery(" SELECT INVOICENO,INVOICEDATE,CHARGEFEE,PROCESSINGFEE,BILLINGFEE,BARCODE" +
					"	FROM PENDING_HISTORY WHERE PATRONNO = "+ "'"+patronNo+"' ORDER BY INVOICEDATE DESC");
			
			
			
			while (rs.next()) {
				 invoice = rs.getString(1);
				 date = rs.getString(2);
				 charge = rs.getString(3);
				 procFee = rs.getString(4);
				 billingFee = rs.getString(5);
				 barcode = rs.getString(6);
				 
				 log.info("#####charge$ :"+charge);
				 chargeAmount = Double.valueOf(charge.substring(1)).doubleValue();
				 procAmount = Double.valueOf(procFee.substring(1)).doubleValue();
				 billingAmount = Double.valueOf(billingFee.substring(1)).doubleValue();
				 
				 finalAmt = chargeAmount+procAmount+billingAmount;
				 String amount = "$"+df.format(finalAmt);
				// String newDate =DateFormat.getInstance().format(date) ;
				 JSONObject obj = new JSONObject();
				 obj.put("invoiceNo",invoice);
				 log.info("##### invoiceNo :"+invoice);
				 obj.put("date",date);
				 log.info("##### DATE :"+date);
				// log.info("##### newDate :"+newDate);
				 obj.put("amount",amount);
				 log.info("##### amount :"+amount);
				 obj.put("barcode",barcode);
				 log.info("##### barcode :"+barcode);
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