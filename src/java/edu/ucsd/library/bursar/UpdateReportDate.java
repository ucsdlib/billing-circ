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
public class UpdateReportDate extends HttpServlet {
	private static Logger log = Logger.getLogger( UpdateReportDate.class );
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request,HttpServletResponse response){
		String reportDate = request.getParameter("reportDate");		
		try {
			boolean results = ReportUtility.writeLastDate(reportDate);
			if(!results){
				log.error("Unable to set new Report Date from servlet UpdateReportDate servlet");
				try {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no results");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("There was an error sending error message back in response from UpdateReportDate servlet", e);
					return;
				}
			}
			//send response
			response.setContentType("text/plain");
			response.addHeader("Pragma", "no-cache");
			response.setStatus(200);
			PrintWriter writer = new PrintWriter(response.getOutputStream());
			writer.write("success");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Error sending back Bursar data", e);
		}
	}
}
