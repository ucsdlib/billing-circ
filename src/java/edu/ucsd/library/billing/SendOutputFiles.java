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

public class SendOutputFiles extends HttpServlet {
	private static Logger log = Logger.getLogger( SendOutputFiles.class );
	String password=null;
	String FTPusername = null;
	JSONArray problem = null;
	JSONArray newPatron = null;
	
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		log.info("$$$$$$$$$ SendOutputFiles BEGIN $$$$$$$$$$$$$$$$ ");
		HttpSession session = request.getSession();
		JSONArray results = (session.getAttribute("pendingData") != null ) ? (JSONArray)session.getAttribute("pendingData") : null;
		String username = (session.getAttribute("username") != null ) ? (String)session.getAttribute("username") : null;
		 problem = (session.getAttribute("problemData") != null ) ? (JSONArray)session.getAttribute("problemData") : null;
		 newPatron = (session.getAttribute("newPatronData") != null ) ? (JSONArray)session.getAttribute("newPatronData") : null;
	
		log.info("$$$$ username $$$$:"+username);
		
		if(results.size() == 0){
			log.error("There is no json data in the session");
			/*try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "json parameter needed");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from SendOutputFiles servlet", e);
				return;
			}
			*/
			try{
				JSONObject obj = new JSONObject();
				obj.put("success","false");
				obj.put("errorMsg","Pending Queue is empty or Billing file has already being transmitted!!");
				
				response.setContentType("text/html");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(200);
				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write(obj.toString());
				writer.close();
				//log.info("$$$$$$$$$ SendOutputFiles END $$$$$$$$$$$$$$$$ ");
				}
				catch(IOException e){
					log.error("Error sending back Bursar data", e);
				}
		}
		else
		{
			log.info("SendOutputFiles size of results:"+results.size());
		
		
		try{
			password = request.getParameter("password");
			log.info("$$$$ password:"+password);
			FTPusername= request.getParameter("username");
			log.info("$$$$ FTPusername $$$$:"+FTPusername);
		}
		catch(Exception e2)
		{
			//undRequestData = false;
			log.error("NO data got from request in SendOutputFiles servlet: password is expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from SendOutputFiles servlet", e);
				return;
			}
			
		}
		
		
		//try and generate output text file
		log.info("PROCESSOUTPUTDATA SIZE OF ARRAY:"+ results.size());
		JSONObject Cobj = SendDataToServer.sendOutputFiles(request, response, results,password,username,FTPusername);
		 String errorMsg =(String) Cobj.get("errorMsg");
		 //String str= (String)Cobj.get("finalFlag");
		 
		 boolean success = false;
		Boolean successBool = (Boolean)Cobj.get("finalFlag");
		success= successBool.booleanValue();
		/* try{
		 success = Boolean.parseBoolean(str);
		 }
		 catch(ClassCastException e)
		 {
			 log.info("Class Case exception!!!");
		 }
		 */
		 log.info("SendOutputFiles errorMsg:"+errorMsg);
		log.info("SendOutputFiles boolean success:"+success);
		if(success)
		{
		JSONArray newArr = new JSONArray();
			session.setAttribute("pendingData", newArr);
			
		}
		/*if(!success){
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "unable to generate report file");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from SendOutputFiles servlet", e);
				return;
			}
		}*/
		
		 String bStr = successBool.toString();
		 String pendingTot = "0";
		 JSONArray pendingQueue = new JSONArray();
		try{
		JSONObject obj = new JSONObject();
		obj.put("success",bStr);
		obj.put("errorMsg",errorMsg);
		obj.put("pending",pendingQueue);
		obj.put("pendingTotal",pendingQueue.size());
		obj.put("newPatron", newPatron);
		obj.put("problem", problem);
		obj.put("problemTotal",problem.size());
		obj.put("newPatronTotal",newPatron.size());
		obj.put("result", "success");
		response.setContentType("text/html");
		response.addHeader("Pragma", "no-cache");
		response.setStatus(200);
		PrintWriter writer = new PrintWriter(response.getOutputStream());
		writer.write(obj.toString());
		writer.close();
		log.info("$$$$$$$$$ SendOutputFiles END $$$$$$$$$$$$$$$$ ");
		}
		catch(IOException e){
			log.error("Error sending back Bursar data", e);
		}
		
		
		}//end of else
		
	}
	
	
	
	
}