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


public class ModifyQueues extends HttpServlet {
	private static Logger log = Logger.getLogger( UploadBillingFile.class );
	boolean foundSessionData = true;
	boolean foundRequestData = true;
	JSONObject results =new JSONObject() ;
	JSONArray dummy = null;
	JSONArray pending = null;
	JSONArray problem = null;
	JSONArray newPatron = null;
	JSONArray newPending = new JSONArray();
	HttpSession session = null;
	String invoiceNumString = null;
	String  invoiceNoList= null;
	String whichQueue = null;
	
	public void doGet(HttpServletRequest request, 
			HttpServletResponse response) {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request,HttpServletResponse response){
		//get SOLR query and callback
		log.info("MODIFYQUEUES: $$$$$$$$$ BEFORE $$$$$$$$$$$$$$");
					session = request.getSession();
					 pending = (session.getAttribute("pendingData") != null ) ? (JSONArray)session.getAttribute("pendingData") : null;
					 problem = (session.getAttribute("problemData") != null ) ? (JSONArray)session.getAttribute("problemData") : null;
					 newPatron = (session.getAttribute("newPatronData") != null ) ? (JSONArray)session.getAttribute("newPatronData") : null;
					log.info("MODIFYQUEUES: Found data from session");
					
					if((pending == null )|| (problem == null)){
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
			whichQueue = request.getParameter("whichQueue");
			log.info("$$$$ invoiceNoList:"+invoiceNoList);
			log.info("$$$$ whichQueue:"+whichQueue);
		}
		catch(Exception e2)
		{
			foundRequestData = false;
			log.error("NO data got from request in ModifyQueues servlet: invoiceNOARRAY is expected");
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "no data got from request");
			} catch (IOException e) {
				log.error("There was an error sending error message back in response from ModifyQueues servlet", e);
				return;
			}
			
		}
		// if we got data from both session and request 
		// we are going to modify queues
		if(foundSessionData && foundRequestData){
			try {
				
				log.info("$$$$ size of the pending queue in sesssion:"+pending.size());
				log.info("$$$$ size of the newPatron queue in sesssion:"+newPatron.size());
				log.info("$$$$ size of the problem queue in sesssion:"+problem.size());
				String [] temp = invoiceNoList.split(",");
				
				
				if(whichQueue.equals("P"))
				{
					newPending = getNewArray(pending,temp);
					results.put("pending", newPending);
					results.put("problem", problem);
					results.put("newPatron", newPatron);
					results.put("pendingTotal",newPending.size());
					results.put("problemTotal",problem.size());
					results.put("newPatronTotal",newPatron.size());
					session.setAttribute("problemData", problem);
					session.setAttribute("pendingData", newPending);
					session.setAttribute("newPatronData", newPatron);
				}
				else if (whichQueue.equals("Q"))
				{
					newPending = getNewArray(problem,temp);
					results.put("problem", newPending);
					results.put("pending", pending);
					results.put("newPatron", newPatron);
					results.put("pendingTotal",pending.size());
					results.put("problemTotal",newPending.size());
					results.put("newPatronTotal",newPatron.size());
					session.setAttribute("problemData", newPending);
					session.setAttribute("pendingData", pending);
					session.setAttribute("newPatronData", newPatron);
				}
				
				else if (whichQueue.equals("N"))
				{
					newPending = getNewArray(newPatron,temp);
					results.put("problem", problem);
					results.put("pending", pending);
					results.put("newPatron", newPending);
					results.put("pendingTotal",pending.size());
					results.put("problemTotal",problem.size());
					results.put("newPatronTotal",newPending.size());
					session.setAttribute("problemData", problem);
					session.setAttribute("pendingData", pending);
					session.setAttribute("newPatronData", newPending);
				}
				log.info("$$$$ size of the newPending queue in sesssion:"+newPending.size());
				//session.setAttribute("newPatronData", newPatron);
							
				//results.put("newPatron", newPatron);
				results.put("result", "success");
				results.put("whichQueue", whichQueue);
				//results.put("newPatronTotal",newPatron.size());
				
				//send response
				response.setContentType("text/plain;charset=UTF-8");
				response.addHeader("Pragma", "no-cache");
				response.setStatus(200);
				PrintWriter writer = new PrintWriter(response.getOutputStream());
				writer.write(results.toString());
				writer.close();
				newPending = new JSONArray();
				temp = null;
				log.info("MODIFYQUEUE: END");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.info("Error sending back queue data from MODIFYQUEUES", e);
			}
		}
	}
	
	public JSONArray getNewArray(JSONArray dummy, String[] temp)
	{
		boolean flag = false;
		JSONArray newPending = new JSONArray();
		int tokencount= temp.length;
		for(int i=0; i < dummy.size();i++)
		  {
			 flag = false;
			  JSONObject obj = (JSONObject)dummy.get(i);
			  for(int j=0; j<tokencount;j++)
			  {
				 //=================3/2============
				  String [] arr = temp[j].split("\\|");
				  String invNo = arr[0];
				  String chargeType = arr[1];
				  log.info("$$$$ UUUUUUUUUU getNewArray UUUUUUUUUUUUUUUU");
				  log.info("$$$$ INVno: "+invNo);
				  log.info("$$$$ ChargeType:"+chargeType);
				  //===============================
				  if(((obj.get("invoiceNo")).equals(invNo)) && ((obj.get("chargeType")).equals(chargeType)))
				  {
					  flag = true;
					  break;
				  }
			  }
			  
			  if(!flag)
			  {
				  newPending.add(obj);  
			  }
			  
		  }
		return newPending;
	}
	
}

