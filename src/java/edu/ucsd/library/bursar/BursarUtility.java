package edu.ucsd.library.bursar;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Calendar;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.ucsd.library.util.sql.ConnectionManager;

/**
 * 
 * @author mcritchlow
 *
 */
public class BursarUtility {
	private static Logger log = Logger.getLogger( BursarUtility.class );
	private static final SimpleDateFormat DB2_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat REPORT_DATE_FORMAT = new SimpleDateFormat("yyMMdd");
	private static final SimpleDateFormat REPORT_NAME_FORMAT = new SimpleDateFormat("MMddyy");
	private static final String lineSeparator =	System.getProperty("line.separator");
	//load db2 parameters
	/**
	 * Based on startDate and endDate, this function queries the campus DB2 datasource.
	 * @param startDate
	 * @param endDate
	 * @return A json data string to be streamed back from the servlet.
	 */
	public static JSONObject getBursarData(String startDate, String endDate){
		log.info("=======GETTING BURSAR DATA BEGIN===================");
		//if there is no start date, get the last stored report date
		if(startDate == null){
			startDate = ReportUtility.getLastDate();
		}
		//if there is no end date, use current date
		if(endDate == null){
			endDate = DB2_DATE_FORMAT.format(new java.util.Date());
		}
		log.info("Getting BURSAR data with START DATE: "+startDate+" and END DATE: "+endDate);
		//get result set
		RowSetDynaClass rs = runBursarQuery(startDate,endDate);
		if(rs != null){
			List<DynaBean> resultSet = rs.getRows();
			log.info("Data was returned from DB2 QUERY with: "+resultSet.size()+" results");
			Iterator<DynaBean> iter = resultSet.iterator();
			JSONObject results = new JSONObject();
			results.put("total", resultSet.size());
			//make query to DB2
			//JSON rows object
			JSONArray rows = new JSONArray();
			int i = 1;
			while(iter.hasNext()){
				DynaBean bean = iter.next();
				JSONObject cellResult = new JSONObject();
				cellResult.put("id", new Integer(i) );
				cellResult.put("name", bean.get("name") );
				cellResult.put("session_activity_dt", bean.get("session_activity_dt") );
				cellResult.put("pid", bean.get("pid") );
				cellResult.put("apply_amt", bean.get("apply_amt") );
				cellResult.put("cashier_id", bean.get("cashier_id") );
				cellResult.put("document_nbr", bean.get("document_nbr") );
				cellResult.put("charge_detail_cd", bean.get("charge_detail_cd") );
				cellResult.put("charge_category_cd", bean.get("charge_category_cd") );
				cellResult.put("payment_detail_cd", bean.get("payment_detail_cd") );
				cellResult.put("payment_category_cd", bean.get("payment_category_cd") );
				rows.add(cellResult);
				i++;
			}
			//clear variables
			iter = null;
			resultSet = null;
			
			results.put("startDate", startDate);
			results.put("endDate", endDate);
			results.put("rows", rows);
			return results;
		}
		 log.info("=======GETTING BURSAR DATA END===================");
		return null;
	}
	
	private static RowSetDynaClass runBursarQuery(String startDate, String endDate){
		Statement stmt = null;
		Connection db2Conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		RowSetDynaClass results = null;
		try {
			db2Conn = ConnectionManager.getConnection("db2");
			stmt = db2Conn.createStatement();
			
			String query = 
				"SELECT Ar_apply.name, Ar_cashier.session_activity_dt, Ar_apply.pid, Ar_apply.apply_amt, " +
					"Ar_apply.cashier_id,  Ar_detail.document_nbr, Ar_apply.charge_detail_cd,"+
					"Ar_apply.charge_category_cd, Ar_apply.payment_detail_cd, Ar_apply.payment_category_cd" +
			    " FROM isis_ar.ar_apply Ar_apply, isis_ar.ar_cashier Ar_cashier, isis_ar.ar_detail Ar_detail" +
			   " WHERE Ar_apply.cashier_id = Ar_cashier.cashier_id" +
			   " AND Ar_apply.session_nbr = Ar_cashier.session_nbr" +
			   " AND Ar_apply.pid = Ar_detail.pid" +
			   " AND Ar_apply.transaction_nbr = Ar_detail.transaction_nbr" +
			   " AND (Ar_apply.person_entity_ind = 'P'" +
			   " AND Ar_apply.reapplied_flag IN ('N','Y')" +
			   " AND Ar_apply.cashier_id NOT LIKE 'LIB%'" +
			   " AND Ar_apply.charge_detail_cd LIKE 'LIB%'" +
			   " AND Ar_apply.charge_category_cd LIKE 'L%'" +
			   " AND Ar_cashier.session_activity_dt >= '" +startDate+"'"+
			   " AND Ar_cashier.session_activity_dt < '" +endDate+"'"+
			   " AND Ar_apply.payment_category_cd IN ('CASH','SFS')" +
			   " AND Ar_apply.charge_detail_cd NOT IN ('LIBLPS'))" +
			   " ORDER BY Ar_apply.name, Ar_cashier.session_activity_dt, Ar_detail.document_nbr";
			pstmt = db2Conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			results = new RowSetDynaClass(rs);
			
		} catch (SQLException e) {
			log.error("Unable to initiate DB2 connection", e);
		} catch (NamingException e) {
			log.error("JNDI Lookup failed for DB2 connection", e);
		} catch (Exception e) {
			log.error("Other error occurred", e);
		}finally{
			ReportUtility.writeLastDate(endDate); //store the new "last report" date
			try {
				if (rs != null){ rs.close(); }	
				if (stmt != null){ stmt.close(); }		
				if (pstmt != null){ pstmt.close(); }
				if (db2Conn != null){ db2Conn.close(); }
			} catch (SQLException e) {
			}
		}
		return results;
	}
	/**
	 * The method creates a printer-friendly html version of the current web interface view.
	 * @param request
	 * @param response
	 * @param bursarData Data retrieved from GetBursarData servlet, returned via json parameter in ajax call to BursarReport servlet
	 * @return
	 */
	public static boolean createBursarReport(HttpServletRequest request,HttpServletResponse response, JSONObject bursarData){
		//TODO
		return true;
	}
	/**
	 * This method will handle processing the bursar data. 
	 * For now: send report back to user as text file
	 * @param request
	 * @param response
	 * @param bursarData
	 * @return
	 */
	public static boolean processBursarData(HttpServletRequest request,HttpServletResponse response, JSONObject results){
		log.info("=======PROCESSING BURSAR DATA BEGIN===================");
		String reportDate = REPORT_DATE_FORMAT.format(new java.util.Date());
		String fileName = "bursar"+REPORT_NAME_FORMAT.format(new java.util.Date())+".txt";
		response.setContentType("text/plain");
		response.setHeader("Content-Disposition", "attachment;filename="+fileName);
		response.setHeader("Cache-Control", "no-store,no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		JSONArray rows = (JSONArray)results.get("rows");
		try {
			PrintWriter out = new PrintWriter(response.getOutputStream());
			for(int i = 0; i < rows.size(); i++){
				JSONObject row = (JSONObject)rows.get(i);
				out.write(reportDate);//date YYMMDD
				out.write(((String)row.get("document_nbr")).trim());//invoice number
				for(int space = 0; space < 22; space++){
					out.write(" "); //22 spaces
				}
				out.write("u"+((String)row.get("pid")).trim());
				for(int space = 0; space < 10; space++){
					out.write(" "); //10 spaces
				}
				out.write(getAmount(((BigDecimal)row.get("apply_amt")).toString().trim())); //formatted dollar amount
				out.write(" "); //extra space after
				//out.write(lineSeparator);
				out.write("\r\n"); //using windows line-separator for now
			}
			out.flush();
		} catch (IOException e) {
			log.error("Unable to generate report file", e);
			return false;
		}
		log.info("=======PROCESSING BURSAR DATA END===================");
		return true;
	}
	/**
	 * Return the formatted date for the filename
	 * FORMAT: MMDDYY where YY is the first 2 digits of the year. Eg: 2008 -> 20
	 */
	private static String getFileDate(){
		String fileDate = DB2_DATE_FORMAT.format(new java.util.Date());
		String[] dateParts = fileDate.split("-");
		return dateParts[1]+dateParts[2]+dateParts[0].substring(0,2);
	}
	/**
	 * Converts the apply_amt value into a formatted 8 character string with leading 0's if no data exists
	 */
	public static String getAmount(String apply_amt){
		int decimalIndex = apply_amt.indexOf('.');
		if(decimalIndex == -1){
			apply_amt += "00";
		}else{
			if(apply_amt.substring(decimalIndex+1).length() == 1){ //if only 1 character after decimal
				apply_amt += "0";
			}
			apply_amt = apply_amt.substring(0,decimalIndex) + apply_amt.substring(decimalIndex+1);
		}
		while(apply_amt.length() < 8){
			apply_amt = "0" + apply_amt;
		}
		return apply_amt;
	}
	
	public static void main(String[] args){
		//System.out.print(BursarUtility.getBursarData("11/01/2007", "11/31/2007"));
		System.out.print(BursarUtility.getBursarData("2008-12-02", null));
	}
}
