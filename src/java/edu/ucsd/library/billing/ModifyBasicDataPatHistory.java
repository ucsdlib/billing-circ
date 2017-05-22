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

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import javax.naming.NamingException;

import edu.ucsd.library.util.sql.ConnectionManager;

public class ModifyBasicDataPatHistory extends HttpServlet {
	private static Logger log = Logger.getLogger( ModifyBasicDataPatHistory.class );
	String pid = null;
	String note = null;
	String patronNo =  null;
	String flagPID = null;
	String flagNote = null;
	JSONObject results =new JSONObject() ;
	JSONArray problem = null;
	boolean foundRequestData = true;
   boolean flag = false;
	String oldPatronNo = null;
	String flagPatron = null;
	
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		
		log.info("ModifyBasicDataPatHistory: $$$$$$$$$ BEGIN $$$$$$$$$$$$$$");
		try{
			
			pid = request.getParameter("pid");
			log.info("$$$$ PID:"+pid);
			note= request.getParameter("note");
			log.info("$$$$ note:"+note);
			patronNo= request.getParameter("patronNo");
			log.info("$$$$ patronNo:"+patronNo);
			flagPID= request.getParameter("flagPID");
			log.info("$$$$ flagPID:"+flagPID);
			flagNote= request.getParameter("flagNote");
			log.info("$$$$ flagNote:"+flagNote);
			flagPatron= request.getParameter("flagPatron");
			log.info("$$$$ flagPatron:"+flagPatron);
			oldPatronNo= request.getParameter("oldPatronNo");
			log.info("$$$$ oldPatronNo:"+oldPatronNo);
			
		}
		catch(Exception e2)
		{
			foundRequestData = false;
			log.error("NO data got from request in EditProblemQueueData servlet: invoiceNOARRAY is expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from EditProblemQueueData servlet", e);
				return;
			}
			
		}
		
		
		if(foundRequestData){
			try{
				if(flagPatron.equalsIgnoreCase("false") &&  flagPID.equalsIgnoreCase("false") && flagNote.equalsIgnoreCase("false"))
			{
				//no need to update the database :just send the data in session
					 results.put("status","nochange");
			}
			else{
				 //flag = updateDatabase(patronNo,pid,note,flagPID,flagNote);
				flag = updateDatabase(patronNo,pid,note,flagPID,flagNote,flagPatron,oldPatronNo);
				 if(flag){
					 //update the problem queue in session
					// JSONArray data = getData(patronNo,problem,invoiceNo,pid);
					// log.info("$$ data:"+data.equals(null));
					 results.put("status","success");
				 }
				 else{
				 // no need to update problem queue since database update failed.
					 results.put("status","fail");
				
				 }
			}
			
			
			
			response.setContentType("text/plain;charset=UTF-8");
			response.addHeader("Pragma", "no-cache");
			response.setStatus(200);
			PrintWriter writer = new PrintWriter(response.getOutputStream());
			writer.write(results.toString());
			writer.close();
			log.info("EditProblemQueueData: END");
			results = new JSONObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info("Error sending back queue data from EditProblemQueueData", e);
		}
			
		}
		
		
		
		
		
		log.info("ModifyBasicDataPatHistory: $$$$$$$$$ END $$$$$$$$$$$$$$");
		}
	
	public boolean updateDatabase(String patronNo,String pid, String noteMod,String  flagPID,String flagNote,String flagPatron,String oldPatronNo)
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		boolean result = true;
		String query = null;
		PreparedStatement pstmt = null;
		String note=noteMod.trim();
		
		try {
			conn = ConnectionManager.getConnection("billing");
			//stmt = conn.createStatement();				        
			//stmt.executeUpdate(query);
			//conn.close();
		
	    if (flagPatron.equalsIgnoreCase("false") && flagPID.equalsIgnoreCase("true")&& flagNote.equalsIgnoreCase("true"))
		{
			//query = "UPDATE PATRONS SET PID = "+"'"+pid+"'"+", NOTES = "+"'"+note+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET PID = ?, NOTES = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,pid );
			 pstmt.setString( 2,note ); 
			 pstmt.setString( 3,patronNo ); 
		}
		else if (flagPatron.equalsIgnoreCase("false") && flagPID.equalsIgnoreCase("true")&& flagNote.equalsIgnoreCase("false"))
		{
			//query = "UPDATE PATRONS SET PID = "+"'"+pid+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET PID = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,pid );
			 pstmt.setString( 2,patronNo ); 
		}
		
		else if (flagPatron.equalsIgnoreCase("false") && flagPID.equalsIgnoreCase("false")&& flagNote.equalsIgnoreCase("true"))
		{
			//query = "UPDATE PATRONS SET NOTES = "+"'"+note+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET NOTES = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,note );
			 pstmt.setString( 2,patronNo ); 
		}
		else if (flagPatron.equalsIgnoreCase("true") && flagPID.equalsIgnoreCase("true")&& flagNote.equalsIgnoreCase("true"))
		{
			//query = "UPDATE PATRONS SET NOTES = "+"'"+note+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET PATRONNO=? ,PID = ?, NOTES = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,patronNo );
			 pstmt.setString( 2,pid ); 
			 pstmt.setString( 3,note ); 
			 pstmt.setString( 4,oldPatronNo ); 
		}
		else if (flagPatron.equalsIgnoreCase("true") && flagPID.equalsIgnoreCase("true")&& flagNote.equalsIgnoreCase("false"))
		{
			//query = "UPDATE PATRONS SET PID = "+"'"+pid+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET PATRONNO=? , PID = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,patronNo );
			 pstmt.setString( 2,pid ); 
			 pstmt.setString( 3,oldPatronNo ); 
		}
		
		else if (flagPatron.equalsIgnoreCase("true") && flagPID.equalsIgnoreCase("false")&& flagNote.equalsIgnoreCase("true"))
		{
			//query = "UPDATE PATRONS SET NOTES = "+"'"+note+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET PATRONNO=? , NOTES = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,patronNo );
			 pstmt.setString( 2,note ); 
			 pstmt.setString( 3,oldPatronNo ); 
		}
	    
		else if (flagPatron.equalsIgnoreCase("true") && flagPID.equalsIgnoreCase("false")&& flagNote.equalsIgnoreCase("false"))
		{
			//query = "UPDATE PATRONS SET NOTES = "+"'"+note+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET PATRONNO=? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,patronNo );
			 pstmt.setString( 2,oldPatronNo ); 
			
		}
		//try {
			//conn = ConnectionManager.getConnection("billing");
			//stmt = conn.createStatement();				        
			//stmt.executeUpdate(query);
			 pstmt.execute();			    
			 conn.commit();
			conn.close();
		} catch (NumberFormatException e) {
			result = false;
			// TODO Auto-generated catch block
			log.error("NumberFormatException", e);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			result = false;
			log.error("SQLException", e);
		} catch (NamingException e) {
			result = false;
			log.error("JNDI Lookup failed for DB2 connection", e);
		}
		return result;
	}

	/*public boolean updateDatabase(String patronNo,String pid, String note,String  flagPID,String flagNote)
	{
		log.info("^^^^^^^ PID in update is : ^^^^:"+pid);
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		boolean result = true;
		String query = null;
		PreparedStatement pstmt = null;
		
		try {
			conn = ConnectionManager.getConnection("billing");
			//stmt = conn.createStatement();				        
			//stmt.executeUpdate(query);
			//conn.close();
		  	    
	    
		if (flagPID.equalsIgnoreCase("true")&& flagNote.equalsIgnoreCase("true"))
		{
			//query = "UPDATE PATRONS SET PID = "+"'"+pid+"'"+", NOTES = "+"'"+note+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET PID = ?, NOTES = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,pid );
			 pstmt.setString( 2,note ); 
			 pstmt.setString( 3,patronNo ); 
		}
		else if (flagPID.equalsIgnoreCase("true")&& flagNote.equalsIgnoreCase("false"))
		{
			//query = "UPDATE PATRONS SET PID = "+"'"+pid+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET PID = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,pid );
			 pstmt.setString( 2,patronNo ); 
		}
		
		else if (flagPID.equalsIgnoreCase("false")&& flagNote.equalsIgnoreCase("true"))
		{
			//query = "UPDATE PATRONS SET NOTES = "+"'"+note+"'"+" WHERE PATRONNO ="+ "'"+patronNo+"'";
			pstmt= conn.prepareStatement("UPDATE PATRONS SET NOTES = ? WHERE PATRONNO = ?");
			
			 pstmt.setString( 1,note );
			 pstmt.setString( 2,patronNo ); 
		}
		
		//log.info("^^^^^^^ query in update is : ^^^^:"+query);
		//try {
			//conn = ConnectionManager.getConnection("billing");
		//	stmt = conn.createStatement();				        
			//stmt.executeUpdate(query);
			 pstmt.execute();			    
			 conn.commit();
			conn.close();
			
		} catch (NumberFormatException e) {
			result = false;
			// TODO Auto-generated catch block
			log.error("NumberFormatException", e);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			result = false;
			log.error("SQLException", e);
		} catch (NamingException e) {
			result = false;
			log.error("JNDI Lookup failed for DB2 connection", e);
		}
		return result;
	}
	*/

}
