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

public class GetSessionData extends HttpServlet {
	private static Logger log = Logger.getLogger( GetSessionData.class );
	boolean foundSessionData = true;
	boolean foundRequestData = true;
	JSONObject results =new JSONObject() ;
	JSONArray dummy = null;
	JSONArray pending = new JSONArray();
	JSONArray problem = new JSONArray();
	JSONArray newPatron =new JSONArray();
	JSONArray newPending = new JSONArray();
	HttpSession session = null;
	String invoiceNumString = null;
	String  invoiceNoList= null;
	String whichQueue = null;
	
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	
	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		//get SOLR query and callback
		log.info("GetSessionData: $$$$$$$$$ BEFORE $$$$$$$$$$$$$$");
					session = request.getSession();
					 pending = (session.getAttribute("pendingData") != null ) ? (JSONArray)session.getAttribute("pendingData") : null;
					 problem = (session.getAttribute("problemData") != null ) ? (JSONArray)session.getAttribute("problemData") : null;
					 newPatron = (session.getAttribute("newPatronData") != null ) ? (JSONArray)session.getAttribute("newPatronData") : null;
					log.info("GetSessionData: Found data from session");
					
					try{
						results.put("problem", problem);
						results.put("pending", pending);
						results.put("newPatron", newPatron);
				    results.put("pendingTotal",pending.size());
					results.put("problemTotal",problem.size());
					results.put("newPatronTotal",newPatron.size());
					results.put("result", "success");
					log.info("$$$$ size of the pending queue in sesssion:"+pending.size());
					log.info("$$$$ size of the problem queue in sesssion:"+problem.size());
					log.info("$$$$ size of the newPatron queue in sesssion:"+newPatron.size());
					//send response
					response.setContentType("text/html");
					response.addHeader("Pragma", "no-cache");
					response.setStatus(200);
					PrintWriter writer = new PrintWriter(response.getOutputStream());
					writer.write(results.toString());
					writer.close();
					log.info("GetSessionData: $$$$$$$$$ END $$$$$$$$$$$$$$");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("Error sending back Bursar data", e);
				}
					
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}