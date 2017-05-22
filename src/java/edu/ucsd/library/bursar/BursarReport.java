package edu.ucsd.library.bursar;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
/**
 * Generates a text file attachment that is returned in the response of the servlet
 * @author mcritchlow
 *
 */
public class BursarReport extends HttpServlet {
	private static Logger log = Logger.getLogger( BursarReport.class );
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request,HttpServletResponse response){
		//get json object from session variable
		HttpSession session = request.getSession();
		JSONObject results = (session.getAttribute("bursarData") != null ) ? (JSONObject)session.getAttribute("bursarData") : null;
		if(results == null){
			log.error("There is no json data in the session");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "json parameter needed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("There was an error sending error message back in response from BursarReport servlet", e);
				return;
			}
		}
		BursarUtility.createBursarReport(request, response, results);
	}
}
