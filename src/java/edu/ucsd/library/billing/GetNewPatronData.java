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


//test
public class GetNewPatronData extends HttpServlet {
	private static Logger log = Logger.getLogger( GetNewPatronData.class );
	boolean foundSessionData = true;
	boolean foundRequestData = true;
	JSONObject results =new JSONObject() ;
	JSONArray newPatron = null;
	
	HttpSession session = null;
	String invoiceNumString = null;
	String  invoiceNoList= null;
	
	
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
	
		log.info("GetNewPatronData: $$$$$$$$$ BEGIN $$$$$$$$$$$$$$");
		session = request.getSession();
		newPatron = (session.getAttribute("newPatronData") != null ) ? (JSONArray)session.getAttribute("newPatronData") : null;
		log.info("MoveToOtherQueues: Found data from session");
		
		if(newPatron == null ){
			log.error("There is no json data in the session");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "json parameter needed");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from ModifyQueues servlet", e);
				return;
			}
		}
		
		
		try{
			invoiceNoList = request.getParameter("invoiceArr");
			log.info("$$$$ invoiceNoList:"+invoiceNoList);
			
		}
		catch(Exception e2)
		{
			foundRequestData = false;
			log.error("NO data got from request in ModifyQueues servlet: invoiceNOARRAY is expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from MoveToOtherQueues servlet", e);
				return;
			}
			
		}
		
		
		
		if(foundSessionData && foundRequestData){
			try {
				log.info("$$$$ size of the newPatron queue in sesssion:"+newPatron.size());
				String [] temp = invoiceNoList.split(",");
				JSONObject data = getData(newPatron,temp);
				log.info("$$ data:"+data.equals(null));
				results = data;
				response.setContentType("text/plain;charset=UTF-8");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(200);
				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write(results.toString());
				writer.close();
				log.info("GetNewPatronData: END");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.info("Error sending back queue data from GetNewPatronData", e);
			}
			
			}//end of if(foundSe
		
		
	}//end of post
	
	
	public JSONObject getData(JSONArray from,String[] temp)
	{
		boolean flag = false;
		JSONObject newjObj = new JSONObject();
		int tokencount= temp.length;
		for(int i=0; i < from.size();i++)
		  {
			 flag = false;
			  JSONObject obj2 = (JSONObject)from.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				//=================3/2============
				  String [] arr = temp[j].split("\\|");
				  String invNo = arr[0];
				  String chargeType = arr[1];
				  log.info("$$$$ UUUUUUUUUU getData UUUUUUUUUUUUUUUU");
				  log.info("$$$$ INVno: "+invNo);
				  log.info("$$$$ ChargeType:"+chargeType);
				  //===============================
				  if(((obj2.get("invoiceNo")).equals(invNo)) && ((obj2.get("chargeType")).equals(chargeType)))
				  {
				 // if((obj2.get("invoiceNo")).equals(temp[j]))
				 // {
					  flag = true;
					  newjObj.put("invoiceNo", obj2.get("invoiceNo"));
					  newjObj.put("name", obj2.get("name"));
					  String pat = (String)obj2.get("patronRecordNo");
					  newjObj.put("patronRecordNo", pat.substring(1));
					  newjObj.put("pid", obj2.get("pid"));
					  //===chandana 3/4================
					  newjObj.put("chargeType", obj2.get("chargeType"));
					  log.info("$$$$$ chargeType:"+ obj2.get("chargeType"));
					  //=================================
					  log.info("$$$$$ name:"+ obj2.get("name"));
					  log.info("$$$$$ patronRecordNo:"+ obj2.get("patronRecordNo"));
					  log.info("$$$$$ pid:"+ obj2.get("pid"));
					  
					  
					  break;
				  }
			  }
			  
			 if(flag)
			 break;
			  
		  }
		return newjObj;
	}
	
}