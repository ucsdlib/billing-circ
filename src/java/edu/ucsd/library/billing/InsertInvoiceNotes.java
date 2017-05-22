package edu.ucsd.library.billing;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.text.*;
import java.text.DateFormat;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import org.apache.log4j.Logger;

import java.sql.Connection;
//import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import javax.naming.NamingException;
import java.util.Date;
import edu.ucsd.library.util.sql.ConnectionManager;

public class InsertInvoiceNotes extends HttpServlet {
	private static Logger log = Logger.getLogger( InsertInvoiceNotes.class );
	private String strUserId = null;
	private String inv = null;
	private String strNote = null;
	private String strexp = null;
	private String strres = null;
	private String strcom = null;
	JSONObject results =new JSONObject() ;
	
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request,HttpServletResponse response){
		
		log.info("InsertInvoiceNotes: $$$$$$$$$ BEGIN $$$$$$$$$$$$$$");
		try{
			strUserId = request.getParameter("strUserId");
			log.info("$$$$ strUserId:"+strUserId);
			inv = request.getParameter("inv");
			log.info("$$$$ inv:"+inv);
			strNote = request.getParameter("strNote");
			log.info("$$$$ strNote:"+strNote);
			strexp = request.getParameter("strexp");
			log.info("$$$$ strexp:"+strexp);
			strres = request.getParameter("strres");
			log.info("$$$$ strres:"+strres);
			strcom = request.getParameter("strcom");
			log.info("$$$$ strcom:"+strcom);
			
		}
		catch(Exception e2)
		{
			log.error("NO data got from request in InsertInvoiceNotes servlet");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from InsertInvoiceNotes servlet", e);
				return;
			}
		}	
		try{
			String userID = getUserID(strUserId);
			log.info("$$$$ userID"+userID);
		boolean flag = InsertInvoiceNote(userID,inv,strNote,strexp,strres,strcom);
		results.put("flag",flag);
		log.info("$$ flag:"+flag);
		response.setContentType("text/plain;charset=UTF-8");
		response.addHeader("Pragma", "no-cache");
		response.setStatus(200);
		PrintWriter writer = new PrintWriter(response.getOutputStream());
		writer.write(results.toString());
		writer.close();
		log.info("InsertInvoiceNotes: $$$$$$$$$ END $$$$$$$$$$$$$$");
		}
	  catch (Exception e) {
		// TODO Auto-generated catch block
		log.info("Error sending back queue data from InsertInvoiceNotes", e);
	}
	
	}
	
	public boolean InsertInvoiceNote(String strUserId,String inv,String strNote,String strexp,String strres,String strcom)
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		boolean flag = true;
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			int maxNoteID =0;
			rs = stmt.executeQuery(" SELECT MAX(NOTEID) FROM INVOICENOTES");
			while (rs.next()) {
				maxNoteID = rs.getInt(1);
				 System.out.println(maxNoteID);
			}
			 DateFormat shortDf = DateFormat.getDateInstance(DateFormat.SHORT);
			  String todayStr = shortDf.format(new Date());
			  String [] temp = todayStr.split("/");
			  String newArray = "20"+temp[2]+"-"+temp[0]+"-"+temp[1];
			  log.info("newarray:"+newArray);
			  java.sql.Date when = java.sql.Date.valueOf( newArray);
			  log.info("when:"+when);
			  
			  
			 PreparedStatement pstmt = conn.prepareStatement(
					    "INSERT INTO INVOICENOTES ( NOTEID,INVOICENO, EXPLANATIONID,RESPONSEID,COMMUNICATIONID,DATEADDED,DATEUPDATED,USERID,ADDITIONALNOTES ) " +
					    " values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			 
			    pstmt.setLong( 1, (maxNoteID+1) );
			    pstmt.setString( 2,inv ); 
			    pstmt.setString( 3, strexp ); 
			    pstmt.setString( 4, strres);
			    pstmt.setString(5, strcom ); 
			    pstmt.setDate( 6, when);
			    pstmt.setDate( 7, when ); 
			    pstmt.setString( 8, strUserId);
			    pstmt.setString( 9, strNote ); 
			    
			    pstmt.execute();
			    
			    conn.commit();
			 
			//String query = "INSERT into INVOICENOTES values ("+"'"+(maxNoteID+1)+"',"+"'"+inv+"',"+"'"+strexp+"',"+"'"+strres+"',"+"'"+strcom+"',"+"sysdate,sysdate,"+strUserId+",'"+strNote+"'"+")";
			
			//stmt.executeUpdate(query);
			
		conn.close();
	} catch (NumberFormatException e) {
		// TODO Auto-generated catch block
		 flag = false;
		log.error("NumberFormatException", e);
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		 flag = false;
		log.error("SQLException", e);
	} catch (NamingException e) {
		 flag = false;
		log.error("JNDI Lookup failed for DB2 connection", e);
	}
	return flag;	
		
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
	
}