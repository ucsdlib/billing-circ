package edu.ucsd.library.bursar;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;

import org.apache.log4j.Logger;



/**
 * 
 * @author mcritchlow
 *
 */
public class GetBursarData extends HttpServlet {
	private static Logger log = Logger.getLogger( GetBursarData.class );
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request,HttpServletResponse response){
		//get SOLR query and callback
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		
		try {
			JSONObject results = BursarUtility.getBursarData(startDate, endDate);
			if(results == null){
				log.error("There is no json data to return from servlet GetBursarData servlet");
				try {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no results");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("There was an error sending error message back in response from GetBursarData servlet", e);
					return;
				}
			}
			//set json object in session variable
			HttpSession session = request.getSession();
			session.setAttribute("bursarData", results);
			//send response
			response.setContentType("application/json");
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
