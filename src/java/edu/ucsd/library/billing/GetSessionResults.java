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

public class GetSessionResults extends HttpServlet {
	private static Logger log = Logger.getLogger( GetSessionResults .class );
	boolean foundRequestData = true;
	String searchval = null;
	String searchCriteria = null;
	HttpSession session = null;
	JSONObject results =new JSONObject() ;
	String date =null;
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}
	
	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		log.info("$$$$$$$$$ GetSessionResults BEGIN $$$$$$$$$$$$$$$$ ");
		session = request.getSession();
		
		try{
			date = request.getParameter("selectedVal");
			log.info("$$$$ date:"+date);
			
		}
		catch(Exception e2)
		{
			foundRequestData = false;
			log.error("NO data got from request in GetSessionResults servlet: Date is expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from GetSessionResults servlet", e);
				return;
			}
			
		}
		
		
		if(foundRequestData){
		
		try{
			JSONArray rows =getResults(date);
			log.info("$$$$$$$$$ rows size"+rows.size());
			results.put("rows",rows);
			results.put("total",rows.size());
			session.setAttribute("sessionData", rows);
			//log.info("$$ size of the invoiceNoteArray:"+invoiceNoteArray.size());
			response.setContentType("text/plain;charset=UTF-8");
			response.addHeader("Pragma", "no-cache");
			response.setStatus(200);
			PrintWriter writer = new PrintWriter(response.getOutputStream());
			writer.write(results.toString());
			writer.close();
		
		log.info("$$$$$$$$$ GetSessionResults END $$$$$$$$$$$$$$$$ ");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		log.info("Error sending back queue data from GetSessionResults", e);
	}
		}//if found	
	}
	
	private JSONArray getResults(String date)
	{
		log.info("inside getResults() : date is:"+date);
		JSONArray rows = new JSONArray();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
	//	String date = null;
		
		 String[] tempArr= date.split("-");
		 log.info("TempArr"+tempArr[0]+" "+tempArr[1]+" "+tempArr[2]);
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			 rs = stmt.executeQuery("SELECT * FROM BILLING.PENDING_HISTORY WHERE to_char(TRANSACTIONDATE,'DD') = "+ "'" + tempArr[2] + "'"+" AND to_char(TRANSACTIONDATE,'MM') = "+ "'" + tempArr[1] + "'" +"AND to_char(TRANSACTIONDATE,'YYYY') =" + "'" + tempArr[0] + "'"+"ORDER BY NAME,INVOICENO");
				
				while (rs.next()) {
					
					String pendingID = rs.getString(1);
					log.info("pendingID:"+pendingID);
					String tranDate = rs.getString(2);
					log.info("tranDate:"+tranDate);
					String invNo = rs.getString(3);
					log.info("invNo:"+invNo);
					String chargeLoc = rs.getString(4);
					log.info("chargeLoc:"+chargeLoc);
					String chargeType = rs.getString(5);
					log.info("chargeType:"+chargeType);
					String patronNo = rs.getString(6);
					log.info("patronNo:"+patronNo);
					String patronType = rs.getString(7);
					log.info("patronType:"+patronType);
					String pid = rs.getString(8);
					log.info("pid:"+pid);
					String name = rs.getString(9);
					log.info("name:"+name);
					String address = rs.getString(10);
					log.info("address:"+address);
					String pcode1 = rs.getString(11);
					log.info("pcode1:"+pcode1);
					String pcode2 = rs.getString(12);
					log.info("pcode2:"+pcode2);
					String aff = rs.getString(13);
					log.info("aff:"+aff);
					String barcode = rs.getString(14);
					log.info("barcode:"+barcode);
					String title = rs.getString(15);
					log.info("title:"+title);
					String callNo = rs.getString(16);
					log.info("callNo:"+callNo);
					String chageFee = rs.getString(17);
					log.info("chageFee:"+chageFee);
					String processFee = rs.getString(18);
					log.info("processFee:"+processFee);
					String billingFee = rs.getString(19);
					log.info("billingFee:"+billingFee);
					String invDate = rs.getString(20);
					log.info("invDate:"+invDate);


						
					
					
					JSONObject obj = new JSONObject();
					
					obj.put("pendingID", pendingID);
					obj.put("tranDate", tranDate);
					obj.put("invoiceNo", invNo);
					obj.put("date", invDate);
					obj.put("loc", chargeLoc);
					obj.put("patronRecordNo", patronNo);
					obj.put("pid", pid);
					obj.put("name", name);
					obj.put("address", address);
					obj.put("pcode1", pcode1);
					obj.put("pcode2", pcode2);
					obj.put("patronAffliation", aff);
					obj.put("patronType", patronType);
					obj.put("itemBarcode", barcode);
					obj.put("title", title);
					obj.put("callNo", callNo);
					obj.put("chargeType", chargeType);
					obj.put("amount1", chageFee);
					obj.put("amount2", processFee);
					obj.put("amount3", billingFee);
					rows.add(obj);
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
		
		return rows;
	}
	
	
	
	
}