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


public class EditNewPatronData extends HttpServlet {
	private static Logger log = Logger.getLogger( EditNewPatronData.class );
	boolean foundSessionData = true;
	boolean foundRequestData = true;
	JSONObject results =new JSONObject() ;
	JSONArray newPatron = null;
	JSONArray pending = null;
	JSONArray problem = null;
	HttpSession session = null;
	String invoiceNumString = null;
	String  invoiceNo= null;
	String pid = null;
	
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
	
		log.info("EditNewPatronData: $$$$$$$$$ BEGIN $$$$$$$$$$$$$$");
		session = request.getSession();
		newPatron = (session.getAttribute("newPatronData") != null ) ? (JSONArray)session.getAttribute("newPatronData") : null;
		 pending = (session.getAttribute("pendingData") != null ) ? (JSONArray)session.getAttribute("pendingData") : null;
		 problem = (session.getAttribute("problemData") != null ) ? (JSONArray)session.getAttribute("problemData") : null;
		
		
		log.info("EditNewPatronData: Found data from session");
		
		if(newPatron == null ){
			log.error("There is no json data in the session");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "json parameter needed");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from EditNewPatronData servlet", e);
				return;
			}
		}
		
		
		try{
			invoiceNo = request.getParameter("invoiceNo");
			log.info("$$$$ invoiceNo:"+invoiceNo);
			pid = request.getParameter("pid");
			log.info("$$$$ PID:"+pid);
			
		}
		catch(Exception e2)
		{
			foundRequestData = false;
			log.error("NO data got from request in EditNewPatronData servlet: invoiceNOARRAY is expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from EditNewPatronData servlet", e);
				return;
			}
			
		}
		
		
		
		if(foundSessionData && foundRequestData){
			try {
				log.info("$$$$ size of the newPatron queue in sesssion:"+newPatron.size());
				//String [] temp = invoiceNoList.split(",");
				JSONArray data = getData(newPatron,invoiceNo,pid);
				log.info("$$ data:"+data.equals(null));
				session.setAttribute("newPatronData", data);
				
				//results.put("newPatron", data);
				//results.put("newPatronTotal",data.size());
				
				results.put("pending", pending);
				results.put("problem", problem);
				results.put("newPatron", data);
				results.put("pendingTotal",pending.size());
				results.put("problemTotal",problem.size());
				results.put("newPatronTotal",data.size());
				session.setAttribute("problemData", problem);
				session.setAttribute("pendingData", pending);
				session.setAttribute("newPatronData", data);
				results.put("result", "success");
				
				
				response.setContentType("text/plain;charset=UTF-8");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(200);
				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write(results.toString());
				writer.close();
				log.info("EditNewPatronData: END");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.info("Error sending back queue data from EditNewPatronData", e);
			}
			
			}//end of if(foundSe
		
		
	}//end of post
	
	
	public JSONArray getData(JSONArray from,String invoiceNo,String pid)
	{
		log.info("{{{{{{{{{{{{{{{{{{:from.size()="+from.size());
		boolean flag = false;
		JSONObject newjObj = new JSONObject();
		JSONArray newArr = new JSONArray();
		String patronNo =null;
		//===========chandana 3/4==============
		  String [] arr = invoiceNo.split("\\|");
		  String invNo = arr[0];
		  String chargeType = arr[1];
		  log.info("$$$$ UUUUUUUUUU getData EditNewPatronDataUUUUUUUUUUUUUUUU");
		  log.info("$$$$ INVno: "+invNo);
		  log.info("$$$$ ChargeType:"+chargeType);
		  //===================================
		for(int i=0; i < from.size();i++)
		  {
			  JSONObject obj2 = (JSONObject)from.get(i);
			
			  if(((obj2.get("invoiceNo")).equals(invNo)) && ((obj2.get("chargeType")).equals(chargeType)))
			       {
					  patronNo=(String)obj2.get("patronRecordNo");
					  log.info("$$$$ patronNo:"+patronNo);
					  break;
				  }
				  
		  }
		
		
		for(int i=0; i < from.size();i++)
		  {
			 flag = false;
			  JSONObject obj2 = (JSONObject)from.get(i);
			
				 
				  if((obj2.get("patronRecordNo")).equals(patronNo))
				  {
			//  if(((obj2.get("patronRecordNo")).equals(patronNo)) && ((obj2.get("invoiceNo")).equals(invNo)) && ((obj2.get("chargeType")).equals(chargeType)))
			 // {
					  flag = true;
					  log.info("$$$$$ name:"+ obj2.get("name"));
					  log.info("$$$$$ patronRecordNo:"+ obj2.get("patronRecordNo"));
					  log.info("$$$$$ pid:"+ obj2.get("pid"));			  
					  
					  newjObj.put("invoiceNo", obj2.get("invoiceNo"));
					  log.info("$$$$$ invoiceNo:"+ obj2.get("invoiceNo"));	
					  newjObj.put("date", obj2.get("date"));
					  newjObj.put("loc", obj2.get("loc"));
					  newjObj.put("patronRecordNo", obj2.get("patronRecordNo"));
					  newjObj.put("pid", pid);
					  newjObj.put("name", obj2.get("name"));
					  newjObj.put("address",obj2.get("address"));
					  newjObj.put("pcode1", obj2.get("pcode1"));
					  newjObj.put("pcode2", obj2.get("pcode2"));
					  newjObj.put("patronAffliation", obj2.get("patronAffliation"));
					  newjObj.put("patronType", obj2.get("patronType"));
					  newjObj.put("itemBarcode", obj2.get("itemBarcode"));
					  newjObj.put("title", obj2.get("title"));
					  newjObj.put("callNo",obj2.get("callNo"));
                      newjObj.put("chargeType", obj2.get("chargeType"));
                      newjObj.put("amount1", obj2.get("amount1"));
                      newjObj.put("amount2",obj2.get("amount2"));
                      newjObj.put("amount3",obj2.get("amount3"));
                      newjObj.put("rule",obj2.get("rule"));
                      newArr.add(newjObj);
                      newjObj = new JSONObject();
				  }
				  else
				  {
					  newArr.add(obj2);
				  }
			  
			  
			
			  
		  }
		log.info("{{{{{{{{{{{{{{{{{{:newArr.size()="+newArr.size());
		return newArr;
	}
	
}