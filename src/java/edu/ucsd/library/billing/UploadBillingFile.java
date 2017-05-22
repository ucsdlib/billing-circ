package edu.ucsd.library.billing;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import org.apache.log4j.Logger;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.ParamPart;
import com.oreilly.servlet.multipart.Part;

import edu.ucsd.library.util.sql.EmployeeInfo;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


public class UploadBillingFile extends HttpServlet {
	private static Logger log = Logger.getLogger( UploadBillingFile.class );
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request,HttpServletResponse response){
		//get SOLR query and callback
		boolean foundInputFile = true;
		JSONObject results = null;
		JSONArray pending = null;
		JSONArray problem = null;
		JSONArray newPatron = null;
		JSONArray pendingSession = null;
		String username = null;
		try {
			//username = getUserName(request);
			//log.info("USER NAME"+ username);
			java.security.Principal pObj = request.getUserPrincipal();
			username = pObj.getName();
			log.info("USER NAME:"+ username);
			MultipartParser mp = new MultipartParser(request,10000 * 1024 * 1024); // 10MB
			Part part = null;
			ParamPart paramPart = null;
			FilePart filePart = null;
			while ((part = mp.readNextPart()) != null) {
				String name = part.getName();
				if (part.isParam()) {
					
				}else if (part.isFile()) {
					foundInputFile = true;
					log.info("Found attachment file for Billing application");
					filePart = (FilePart) part;
					ProcessFile processor = new ProcessFile(filePart.getInputStream());
					processor.processBillingFile(); //perform processing
					results = processor.getProcessingResults();
					pending = processor.getPendingQueue();
					problem =processor.getProblemQueue();
					newPatron=processor.getNewPatronQueue();
				}
			}
		} catch (IOException e1) {
			foundInputFile = false;
			log.error("There was an error retrieving the attached file in UploadBillingFile servlet");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no file attached");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from UploadBillingFile servlet", e);
				return;
			}
		}
		if(foundInputFile){
			try {
				if(results.get("result") == "fail"){
					log.error("There is no json data to return from servlet UploadBillingFile servlet");
					try {
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no results");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						log.error("There was an error sending error message back in response from UploadBillingFile servlet", e);
						return;
					}
				}
				//set json object in session variable
				HttpSession session = request.getSession();
				 pendingSession = (session.getAttribute("pendingData") != null ) ? (JSONArray)session.getAttribute("pendingData") : null;
				// pendingSession = (session.getAttribute("problemData") != null ) ? (JSONArray)session.getAttribute("problemData") : null;
				// newPatronSession = (session.getAttribute("newPatronData") != null ) ? (JSONArray)session.getAttribute("newPatronData") : null;
				 if(pendingSession != null )
				 {
					 for(int j=0;j<pendingSession.size();j++)
					 {
						 JSONObject obj = (JSONObject)pendingSession.get(j);
						 pending.add(obj);  
					 }
					 
				 }
				 
				session.setAttribute("username", username);
				session.setAttribute("billingData", results);
				session.setAttribute("pendingData", pending);
				session.setAttribute("problemData", problem);
				session.setAttribute("newPatronData", newPatron);
				results.put("pendingTotal",pending.size());
				results.put("problemTotal",problem.size());
				results.put("newPatronTotal",newPatron.size());
				results.put("result", "success");
				log.info("$$$$ size of the pending queue in sesssion:"+pending.size());
				//send response
				response.setContentType("text/html");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(200);
				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write(results.toString());
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("Error sending back Bursar data", e);
			}
		}
	}
	
	
	/*private String getUserName(HttpServletRequest request){
        DataSource dsSourceAuth;
        EmployeeInfo emp;
        String username = "";
        try {
              Context initCtx = new InitialContext();
              dsSourceAuth = (DataSource)initCtx.lookup("java:comp/env/jdbc/authzt");
              emp = EmployeeInfo.lookup((javax.sql.DataSource) dsSourceAuth, request.getRemoteUser() );
              username = emp.getEmail();
              String nn = emp.getUsername();
              log.info("&&&&&&&&&&&&&&&&USER NAME:"+username);
              log.info("&&&&&&&&&&&&&&&&Email:"+nn);
        } catch (NamingException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
        }
        return username;
  }
  */

}

