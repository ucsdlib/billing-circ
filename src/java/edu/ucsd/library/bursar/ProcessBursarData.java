package edu.ucsd.library.bursar;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
/**
 * 
 * @author mcritchlow
 *
 */
public class ProcessBursarData extends HttpServlet {
	private static Logger log = Logger.getLogger( ProcessBursarData.class );
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request,HttpServletResponse response){
		HttpSession session = request.getSession();
		JSONObject results = (session.getAttribute("bursarData") != null ) ? (JSONObject)session.getAttribute("bursarData") : null;
		if(results == null){
			log.error("There is no json data in the session");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "json parameter needed");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from ProcessBursarData servlet", e);
				return;
			}
		}
		
		log.info("Generate output text file");
		boolean success = BursarUtility.processBursarData(request, response, results);
		if(!success){
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "unable to generate report file");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from ProcessBursarData servlet", e);
				return;
			}
		}
		
	}
}
