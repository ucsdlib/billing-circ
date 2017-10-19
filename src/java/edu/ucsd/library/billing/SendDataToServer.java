package edu.ucsd.library.billing;

import java.math.BigDecimal;
import java.net.SocketException;
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
import java.text.DecimalFormat;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.SSLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edu.ucsd.library.util.sql.ConnectionManager;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

//import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import edu.ucsd.library.shared.Mail;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
//import javax.mail.internet.*;
//import javax.mail.*;

public class SendDataToServer  {
	private static Logger log = Logger.getLogger( SendDataToServer.class );
	
	static String commonFileName = null;
	  private String userEmail;
	  static int personRecordCount =0;
	  static int entityRecordCount =0;
	  static String username = null;
	
	  public static JSONObject sendOutputFiles(HttpServletRequest request,HttpServletResponse response, JSONArray results,String password,String username,String FTPusername){
		  log.info("=======SendDataToServer BEGIN===================");
		  System.out.println("=======SendDataToServer BEGIN===================");
		  boolean finalFlag = false;
		  boolean transferSuccess = false;
		  boolean insertSuccess= false;
		  boolean removeSuccess = false;
		  boolean sessionSuccess= false;
		  String errorMsg = " ";
		  String userName =null;
		 JSONObject removeObj = new JSONObject();
		  	userName = username;
		  	//if(results.size() == 0)
		  	//	return false;
		  	
		  
		  String userID=getUserID(userName);
		 JSONObject chargeJobj = generateChargeFileContent(results);
		 String chargeFileContent =(String) chargeJobj.get("chargeBuffer");
		 String cCount= (String)chargeJobj.get("chargeRecordCount");
		 int chargeRecordCount = Integer.parseInt(cCount);
		 log.info("$$$$$  chargeFileContent: "+chargeFileContent.length());
		 log.info("$$$$$  chargeRecordCount: "+chargeRecordCount);
		
		 String fullname = getFullname(userName);
		  log.info("Fullname is:"+fullname);
		 System.out.println("userName is " + userName);

		  String emailcontent =getEmailContent(userName,chargeRecordCount);
		  
		  // try to send the 3 output files to server
		  

		  transferSuccess = sendReportFilesToServer(results,password,FTPusername,chargeFileContent); //run transfer

		  log.info("$$$$ transferSuccess: "+transferSuccess);
		
		  
		 if (transferSuccess)
		 {
			 //insert into database
			 insertSuccess= insertDataToDatabase( results,userID);
			 log.info("$$$$ insertSuccess: "+insertSuccess);
			 System.out.println("$$$$ insertDataToDatabase:" + insertSuccess);
			
			 if (!insertSuccess)
			 {
				 log.info("$$$$ insertion failed and files are removed:"); 
				 System.out.println("$$$$ insertion failed and files are removed:");
				 //remove files from FTP server
				 removeObj =removeFiles(username,password);
				 String strRemoveSuccess =(String) removeObj.get("removeFlag");
				 removeSuccess = Boolean.parseBoolean(strRemoveSuccess);
				 errorMsg += "Insertion to DB failed! \n";
			 }
			 
				 
			 if(transferSuccess && insertSuccess)
			 { finalFlag = true;
			   
			 }
			
			 if(removeSuccess)
			 {
				 log.info("$$$$ removeSuccess: "+removeSuccess);
				 finalFlag = false;
				 String removeError =(String)removeObj.get("removeError");
				 errorMsg +=  removeError; 
			 }
			 
			 if(finalFlag)
			 {
				 sessionSuccess = insertSessionData(results);
				 log.info("$$$$ sessionSuccess: "+sessionSuccess);			     
			     String ACTemail="act-prodcontrol@ucsd.edu";			    
			     String [] ACTstrArr={ACTemail};
			     String contactemail1="hweng@ucsd.edu";
			     String contactemail2="gferguson@ucsd.edu";
			     String contactemail3="lchodur@ucsd.edu";
			     String [] contactstrArr1={contactemail1};
			     String [] contactstrArr2={contactemail2};
			     String [] contactstrArr3={contactemail3};
			     String email=getEmail(userName);
			     if(email == null){
			    	 email= userName+"@ucsd.edu";
			     }			     
			     String [] strArr = {email};
			     
			     
			     
		       try {
		           Mail.sendMail(ACTemail,ACTstrArr , "Billing output file Transfer",emailcontent, "smtp.ucsd.edu");
		           Mail.sendMail(contactemail1,contactstrArr1 , "Billing output file Transfer",emailcontent, "smtp.ucsd.edu");
		           Mail.sendMail(contactemail2,contactstrArr2 , "Billing output file Transfer",emailcontent, "smtp.ucsd.edu");
		           Mail.sendMail(contactemail3,contactstrArr3 , "Billing output file Transfer",emailcontent, "smtp.ucsd.edu");
		           Mail.sendMail(email,strArr , "Billing output file Transfer",emailcontent, "smtp.ucsd.edu");  
		          
		       } catch (Exception e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	           } 
			 }
			 
			 errorMsg +="Output File Transfer success!\n";
		 }//end of if(transferSuccess)
       
		 else{
			 //remove files from FTP server because transfer failed
			 removeObj =removeFiles(username,password);
			 String strRemoveSuccess =(String) removeObj.get("removeFlag");
			 removeSuccess = Boolean.parseBoolean(strRemoveSuccess);
			log.info("tranfer failure: removeSuccess="+removeSuccess);
			 errorMsg += "Output File Transfer Failed! \n";
			 finalFlag = false; 
		 }
		
		 JSONObject finalObj = new JSONObject();
		 finalObj.put("finalFlag",finalFlag);
		 finalObj.put("errorMsg",errorMsg);
		 log.info("$$$$ finalFlag: "+finalFlag);
		 log.info("$$$$ errorMsg: "+errorMsg);
         log.info("=======SendDataToServer END===================");
         return finalObj;
	  }//end of run()
	
	  public static boolean sendReportFilesToServer(JSONArray results,String password,String username,String chargeFileContent){
		    boolean flag = false;
			boolean flag1 = true;
			boolean flag2 = true;
			boolean flag3 = true;
			boolean retValue1 = false;
			boolean retValue1L = false;
			 boolean retValue2 = false;
			 boolean retValue3 = false;
			//FTPClient ftp;
	       // String serverName = "adcom.ucsd.edu";
	       //  String pathname = "/SISP/ARD2502/LIBCIR/CHARGE/";
	         // generating file name
	         Calendar cal = Calendar.getInstance();
			    int day = cal.get(Calendar.DATE);
		        int month = cal.get(Calendar.MONTH) + 1;
		        int year = cal.get(Calendar.YEAR);
		        log.info("YEAR:"+year);
		        log.info("month:"+month);
		        log.info("day:"+day);
            										 
             double date1 = toJulian(new int[]{year,month,day});
		        double date2 = toJulian(new int[]{year,1,1});
		        int dif = (int) (date1-date2+1);
		        log.info("dif: " + dif + " days.");
             
		        String strYear =""+year;
		        String strDiff =""+dif;
		        for(int i=strDiff.length(); i < 3 ;i++)
		         {
		         	strDiff = "0"+strDiff;
		         }
		         
		      String filename = "D"+strYear.substring(2)+strDiff;
              log.info("File name: " + filename);  
	         
             retValue1 = sendChargeFileToServer(results,password,username,chargeFileContent,filename);
             retValue1L=sendChargeFileToLocal(password,username,chargeFileContent,filename);
             
             retValue2 =sendPersonFileToServer(results,password,username, filename); 
             retValue3 = sendEntityFileToServer(results,password,username,filename);
           
             if (!retValue1){
				 flag1 = false;
				 log.info("Charge File uploaded FAILED");
			 }
				  
             else  
             {
            	 log.info("Charge File uploaded successful.");
            	 
             }
             
             if (!retValue1L){
				 
				 log.info("Charge File write to local server FAILED");
			 }
				  
             else  
             {
            	 log.info("Charge File write to local server successful.");
            	 
             }
             
			 if (!retValue2){
				 flag3 = false;
				 log.info("Person File uploaded FAILED");
			 }					  
             else {
            	 
            	 log.info("Person File uploaded successful.");
             }
            	  
			 if (!retValue3){
				 flag3 = false;
				 log.info("Entity File uploaded FAILED");
			 }
				  
             else{
            	 log.info("Entity File uploaded successful.");	            	
             }
			 
			 if(flag1 && flag2 && flag3)
			 {
				 flag = true;
			 }
			 else
				 flag = false;
			
			 log.info("FLAG1 := " +flag1);
			 log.info("FLAG2 := " +flag2);
			 log.info("FLAG3 := " +flag3);
			 log.info("FLAG := " +flag);
			 
			 log.info("$$$$ FLAG is :"+flag);
			 return flag;
			 
			 
			 
			 /*
	         
	         
	         
	         
	         
	         
	         
	  	    ftp = new FTPClient();
	  	    ftp.setRemoteVerificationEnabled(false); 
	        log.info("username and password:"+username+" "+password);
			try {
			   int reply;
	           ftp.connect(serverName);
	           log.info("Connected to " + serverName + ".");
	           ftp.login(username, password);
	           log.info("Logged in with the usernmae and password "+username+" "+password);
	           ftp.enterLocalPassiveMode(); 
	          // ftp.setDataTimeout(600000000);
	           reply = ftp.getReplyCode();
	           
	           if (!FTPReply.isPositiveCompletion(reply))
	           {
	               ftp.disconnect();
	               System.err.println("FTP server refused connection.");
	               System.exit(1);
	           }
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);        
	           ftp.setFileType(FTP.ASCII_FILE_TYPE);
			}
			 catch (IOException e)
		        {
				 log.info("$$$$$ IO Exception in connecting/ logging to the server");
		        } 
			   // JSONObject chargeJobj = generateChargeFileContent(results);
	           // String bufferChargeFile = generateChargeFileContent(results);
			    String bufferChargeFile =  chargeFileContent;
	            String bufferPersonFile = generatePersonFileContent();
	            String bufferEntityFile = generateEntityFileContent();
	            log.info("$$$$$ OUT content:"+bufferChargeFile);
	           try{ 
	            ByteArrayOutputStream htmlStream = new ByteArrayOutputStream();
	            log.info("$$$$$ 111111111111111111 $$$$$$$$$$$$$$$$$$$");
	            PrintWriter out = new PrintWriter(htmlStream);
	            log.info("$$$$$ 22222222222222222222 $$$$$$$$$$$$$$$$$$$");
	            out.write(bufferChargeFile);
	            log.info("$$$$$ 333333333333333333  $$$$$$$$$$$$$$$$$$$");
	            out.flush();
	            out.close();
				
	            
				 boolean retValue1 =  ftp.storeFile(filename, new ByteArrayInputStream(htmlStream.toByteArray()));
				 log.info("$$$$$ 4444444444444444444  $$$$$$$$$$$$$$$$$$$");
				 log.info("$$$$$$$$$ RETVALUE1:"+retValue1);
				 
	             
	             
	             boolean flagP = ftp.changeWorkingDirectory("/SISP/ARD2502/LIBCIR/PERSON/");
		           log.info("flagP is:"+flagP);        
		           ftp.setFileType(FTP.ASCII_FILE_TYPE);
		           
		          if(flagP)
		          {
		        	  ByteArrayOutputStream htmlStream1 = new ByteArrayOutputStream();
						 PrintWriter out1 = new PrintWriter(htmlStream1);
			             out1.write(bufferPersonFile);
			             out1.flush();
			             out1.close();
	             retValue2 =  ftp.storeFile(filename, new ByteArrayInputStream(htmlStream1.toByteArray()));
	             log.info("$$$$$$$$$ RETVALUE2:"+retValue2);
	            
	             
		          }
		          boolean flagE = ftp.changeWorkingDirectory("/SISP/ARD2502/LIBCIR/ENTITY/");
		           log.info("flagE is:"+flagE);        
		           ftp.setFileType(FTP.ASCII_FILE_TYPE);
		          
		           if(flagE)
			          {
		        	   ByteArrayOutputStream htmlStream2 = new ByteArrayOutputStream();
			             PrintWriter out2 = new PrintWriter(htmlStream2);
						 out2.write(bufferEntityFile);
			             out2.flush();
			             out2.close();
		           retValue3 =  ftp.storeFile(filename, new ByteArrayInputStream(htmlStream2.toByteArray()));
				  log.info("$$$$$$$$$ RETVALUE3:"+retValue3);
			          }
				 if (!retValue1){
					 flag1 = false;
					 log.info("Charge File uploaded FAILED");
				 }
					  
	             else  
	             {
	            	 log.info("Charge File uploaded successful.");
	            	 
	             }
				 if (!retValue2){
					 flag3 = false;
					 log.info("Person File uploaded FAILED");
				 }					  
	             else {
	            	 
	            	 log.info("Person File uploaded successful.");
	             }
	            	  
				 if (!retValue3){
					 flag3 = false;
					 log.info("Entity File uploaded FAILED");
				 }
					  
	             else{
	            	 log.info("Entity File uploaded successful.");	            	
	             }
				 
				 if(flag1 && flag2 && flag3)
				 {
					 flag = true;
				 }
				 else
					 flag = false;
				
				 log.info("FLAG1 := " +flag1);
				 log.info("FLAG2 := " +flag2);
				 log.info("FLAG3 := " +flag3);
				 log.info("FLAG := " +flag);
				 ftp.logout();
				 ftp.disconnect();
			}//end of try
			 catch (FTPConnectionClosedException e)
		        {
				 flag1 = false;
		            log.info("Server closed connection.");
		           
		        }
		        catch (IOException e)
		        {
		        	 flag1 = false;
		        	 log.info("IO Exception %%%%%%%%%%%");
		        }
		        finally
		        {
		            if (ftp.isConnected())
		            {
		                try
		                {
		                    ftp.disconnect();
		                }
		                catch (IOException f)
		                {
		                    // do nothing
		                }
		            }
		        }
			 log.info("$$$$ FLAG is :"+flag);
			 return flag;
			 */
			}//end of sendReportFilesToServer()

	
	/*=============================================
	 * Sends the content of the chargefile
	 */
	
	  private static JSONObject generateChargeFileContent(JSONArray results){
		  String actioncode_pid = null;
			String detailCode = "LIB"; // Rule # 2.1
			String invoiceNumber = null;
			String standardText = "LIBRARY ITEM";
			String itemBarcode = null;
			boolean twoLines = false;
			boolean isPositiveAmt = false;
			String fifthChar = null;
			String finalAmount = null;
		    JSONArray rows = new JSONArray();
			rows = results;
			int noOfRecords = 0;
			double totalCharges = 0;
			double newAmt = 0;
			 DateFormat shortDf = DateFormat.getDateInstance(DateFormat.SHORT);
			String todayStr = shortDf.format(new Date());
			log.info("BILLING UTILITY todayStr:"+ todayStr);
			String [] temp = todayStr.split("/");
			String fp = temp[0];
			StringBuffer out= new StringBuffer();
			String chargeBuffer = null;
			int chargeRecordCount=0;
			if(fp.length()<2)
			{
				fp = "0"+fp;
			}
			String sp = temp[1];
			if(sp.length()<2)
			{
				sp = "0"+sp;
			}
			String tp = temp[2];
			if(tp.length()<2)
			{
				tp = "0"+tp;
			}
			
			String today = fp+sp+tp;
			log.info("BILLING UTILITY today:"+ today);
			log.info("BILLING UTILITY SIZE OF ARRAY:"+ rows.size());
			try {
				//header
				out.append("CHDR CLIBCIRC.CHARGE"+" "+today+" "+today+" "+"000001");
				out.append("\r\n");
				for(int i = 0; i < rows.size(); i++){
					JSONObject row = (JSONObject)rows.get(i);
					// Get the pid and append "A" at the begining to represent action code
					String pidd =((String)row.get("pid")).trim();
					actioncode_pid ="AA"+pidd.substring(1);
					log.info("BILLING UTILITY actioncode_pid:"+actioncode_pid);
					String chargeType = ((String)row.get("chargeType")).trim();
					//if (chargeType.equalsIgnoreCase("LOST") && chargeType.equalsIgnoreCase("REPLACEMENT"))
						//twoLines = true;
					
					/* ==========Rule 2.2============================= */
				  String loc = ((String)row.get("loc")).trim(); 
					String fourthChar = " ";
					log.info("BILLING UTILITY loc:"+loc);
					System.out.println("BILLING UTILITY loc:"+loc);

           if (loc.equals("alcd") || loc.equals("brask")|| loc.equals("glcd"))
           {
             fourthChar = "T";
           }else
           {
           	 char c = loc.charAt(0);
           	 fourthChar = getFourthCharacter(c);
           }
           
           System.out.println("BILLING UTILITY fourthChar:"+ fourthChar);
				//detailCode += fourthChar;
					
					/* ==========Rule 2.3========================== */
					
					if (chargeType.equalsIgnoreCase("LOST") || chargeType.equalsIgnoreCase("REPLACEMENT"))
					{
						twoLines = true;
						
					}
					else if (chargeType.equalsIgnoreCase("MANUAL"))
					{
						fifthChar = "R";
					}
					
					else if (chargeType.equalsIgnoreCase("OVERDUE"))
					{
						fifthChar = "F";
					}
					else if (chargeType.equalsIgnoreCase("OVERDUEX"))
					{
						fifthChar = "R";
					}
					
					//detailCode += fifthChar
					/*==============Rule 2.4============================ */
					
					String patronType = ((String)row.get("patronType")).trim(); 
					log.info("BILLING UTILITY patronType:"+patronType);
					int patron = Integer.parseInt(patronType);
					String sixthChar = getSixthCharacter(patron);
					//detailCode += sixthChar
					 /*=============================
					  * Caluclating the amout
					  * ============================
					  */
					 String amount1 = ((String)row.get("amount1")).trim(); 
					 String amount2= ((String)row.get("amount2")).trim(); 
					 String amount3 = ((String)row.get("amount3")).trim(); 
					 
					 //----------------------------------
					  String amount1Str = amount1.substring(1);
					  log.info("amount1Str is"+amount1Str);
					  int indexDot = amount1Str.indexOf(".");
					  log.info("indexDot is:"+indexDot);
					  String amount1Mod = amount1Str.substring(0,indexDot)+amount1Str.substring(indexDot+1);
					  log.info("amount1Mod is"+amount1Mod);
					 
					  String amount2Str = amount2.substring(1);
					  log.info("amount2Str is"+amount2Str);
					  int indexDot2 = amount2Str.indexOf(".");
					  log.info("indexDot2 is:"+indexDot2);
					  String amount2Mod = amount2Str.substring(0,indexDot2)+amount2Str.substring(indexDot2+1);
					  log.info("amount2Mod is"+amount2Mod);
					 
					  String amount3Str = amount3.substring(1);
					  log.info("amount3Str is"+amount3Str);
					  int indexDot3 = amount3Str.indexOf(".");
					  log.info("indexDot3 is:"+indexDot3);
					  String amount3Mod = amount3Str.substring(0,indexDot3)+amount3Str.substring(indexDot3+1);
					  log.info("amount3Mod is"+amount3Mod);
					 //----------------------------
					  double total = Integer.parseInt(amount1Mod)+ Integer.parseInt(amount2Mod)+ Integer.parseInt(amount3Mod);
					  String amt = ""+total;
					  log.info("BILLING UTILITY amt:"+amt);
					  int t= amt.indexOf(".");
					  String tempp= amt.substring(0,t);
					  log.info("BILLING UTILITY tempp:"+tempp);
					  //02/05/2010 modified
					  //String amtStr = tempp.substring(0,tempp.length()-2)+"."+tempp.substring(tempp.length()-1);
					  String amtStr = tempp.substring(0,tempp.length()-2)+"."+tempp.substring(tempp.length()-2);
					  log.info("BILLING UTILITY amtStr:"+amtStr);
					  newAmt = Double.parseDouble(amtStr);
					  totalCharges += newAmt;
					  log.info("BILLING UTILITY newAmt:"+newAmt);
					   if (newAmt < 0)
					   { // handle for negative numbers
						   newAmt = newAmt - (newAmt *2);
						  DecimalFormat twoDForm1 = new DecimalFormat("#.##");
						   double newAmt2= Double.valueOf(twoDForm1.format(newAmt));
						   String s = Double.toString(newAmt2);
						   log.info("s OLD:"+s);
							   int w= s.indexOf(".");
							   log.info("s:"+s);
							   log.info("w:"+w);
							   String sTemp2= s.substring(w+1);
							   log.info("sTemp2"+sTemp2);
							   if(sTemp2.length()< 2)
							   {
								   //that menas only one deciamal point
								   s = s + "0";
								   log.info("BILLING UTILITY inside <:"+s);
							   }
							   log.info("s New:"+s);
						 /*  int pp= s.indexOf(".");
						   String sTemp= s.substring(pp+1);
						   if(sTemp.length()< 2)
						   {
							   //that menas only one deciamal point
							   s = s + "0";
							   log.info("BILLING UTILITY inside <:"+s);
						   }
						  		*/		   
						   /*newAmt = newAmt - (newAmt *2);
						  	 String s = Double.toString(newAmt);*/
						  	 char lastChar = s.charAt(s.length()-1);
						  	  log.info("BILLING UTILITY lastChar:"+lastChar);
						  	 char newLastChar = getLastCharNegative(lastChar);
						  	  log.info("BILLING UTILITY newLastChar:"+newLastChar);
						    // remove last char
						  	String s1 = s.substring(0,s.length()-1);
						  	log.info("BILLING UTILITY s1:"+s1);
						  	//remove "." and add newLastChar at the end
						  	int p= s.indexOf(".");
							String s2= s1.substring(0,p)+ s1.substring(p+1) +newLastChar;
							log.info("BILLING UTILITY s2:"+s2);
							 int index = 11 - s2.length();
							 for (int i1=0; i1 < index ;i1++)
							 {
								 s2 = "0" + s2;
								 
							 }
							 finalAmount = s2;
					   }
					   else {
						   // handle for positive numbers
						   isPositiveAmt= true;
						   DecimalFormat twoDForm1 = new DecimalFormat("#.##");
						   double newAmt2= Double.valueOf(twoDForm1.format(newAmt));
						   String s = Double.toString(newAmt2);
						   log.info("s OLD:"+s);
							   int w= s.indexOf(".");
							   log.info("s:"+s);
							   log.info("w:"+w);
							   String sTemp2= s.substring(w+1);
							   log.info("sTemp2"+sTemp2);
							   if(sTemp2.length()< 2)
							   {
								   //that menas only one deciamal point
								   s = s + "0";
								   log.info("BILLING UTILITY inside <:"+s);
							   }
							   log.info("s New:"+s);
						  /* String s = Double.toString(newAmt);
						   int w= s.indexOf(".");
						   String sTemp2= s.substring(w+1);
						   if(sTemp2.length()< 2)
						   {
							   //that menas only one deciamal point
							   s = s + "0";
							   log.info("BILLING UTILITY inside <:"+s);
						   }
						   */
						   char lastChar = s.charAt(s.length()-1);
						   log.info("BILLING UTILITY lastChar:"+lastChar);
						   char newLastChar = getLastCharPositive(lastChar);
						   log.info("BILLING UTILITY newLastChar:"+newLastChar);
						    // remove last char
						  	String s1 = s.substring(0,s.length()-1);
						  	//remove "." and add newLastChar at the end
						  	int p= s.indexOf(".");
							String s2= s1.substring(0,p)+ s1.substring(p+1) +newLastChar;
							log.info("BILLING UTILITY s2:"+s2);
							 int index = 11 - s2.length();
							 for (int i1=0; i1 < index ;i1++)
							 {
								 s2 = "0" + s2;
								 
							 }
							 finalAmount = s2;
					   }
					  // detailCode += finalAmount ;
					   /* get the invoice number */
					   invoiceNumber = ((String)row.get("invoiceNo")).trim();
					   log.info("BILLING UTILITY invoiceNumber:"+invoiceNumber);
					   /* get item barcode */
					   String barcode  = ((String)row.get("itemBarcode"));
					   log.info("BILLING UTILITY barcode:"+barcode);
					   if(barcode != null)
					   {
					   if (barcode.length() > 1)
							   {
						   itemBarcode = barcode.substring(1);
						   log.info("BILLING UTILITY itemBarcode in if:"+itemBarcode);
						   if(itemBarcode.length()>14)
						   {
							   String mm =itemBarcode.substring(0,14);
							   itemBarcode = mm;
							   log.info("BILLING UTILITY itemBarcode in if lenght >14:"+itemBarcode);
						   }
							   }
					   else
					   {
						   itemBarcode = "      ";
						   log.info("BILLING UTILITY itemBarcode in else:"+itemBarcode);
					   }
					   }
					   else
					   {
						   itemBarcode = "      ";  
					   }
					   
					   /* =========================================
					    *  Wrting the file - first write for the record which has
					    *  charge type MANUAL,OVERDUE,OVERDUEX
					    *  ========================================
					    */
					  
					   if(! twoLines) {
						   log.info("BILLING UTILITY start of if");
						   out.append(actioncode_pid);
						   for(int space = 0; space < 35; space++){
								out.append(" "); //10 spaces
							}
						   detailCode += fourthChar + fifthChar + sixthChar + finalAmount;
						   log.info("DEBUG: fourthChar in !twoLines:"+fourthChar);
						   log.info("DEBUG: fifthChar in !twoLines:"+fifthChar);
						   log.info("DEBUG: sixthChar in !twoLines:"+sixthChar);
						   log.info("DEBUG: finalAmount in !twoLines:"+finalAmount);
						   
						   out.append(detailCode);
						   detailCode = "LIB";
						   for(int space = 0; space < 6; space++){
								out.append(" "); //5 spaces
							}     
						   out.append (invoiceNumber);
						   for(int space = 0; space < 8; space++){
								out.append(" "); //5 spaces
							} 
						   out.append (standardText);
						   log.info("BILLING UTILITY standardText:"+standardText);
						   out.append(" ");
						   out.append (itemBarcode);
						   log.info("BILLING UTILITY itemBarcode:"+itemBarcode);
						   out.append("\r\n");
						  // out.append("\r\n"); 
						  // out.append("\r\n"); 
						   log.info("BILLING UTILITY end of if");
						   noOfRecords++;
					   }
					   //************** 8 /28 /2009 *********************************************
					   else
					   {
						   int amount3Value =Integer.parseInt(amount3Mod);
						   log.info("^^^^^^ amount3Value: ^^^^"+amount3Value);
						   if(isPositiveAmt)
						   {
							   if(amount3Value == 0)
							   {
								   log.info("^^^^^^ Inside amount3value == 0 ^^^^^ ");
								   out.append(actioncode_pid);
								   log.info(" ^^^^^^ actioncode_pid:"+actioncode_pid);
								   for(int space = 0; space < 35; space++){
										out.append(" "); //10 spaces
									}
								   detailCode += fourthChar + "R" + sixthChar + finalAmount;
								   out.append(detailCode);
								   detailCode = "LIB";
								   log.info("^^^^^^ detailCode R:"+detailCode);
								   for(int space = 0; space < 6; space++){
										out.append(" "); //5 spaces
									}     
								   out.append (invoiceNumber);
								   for(int space = 0; space < 8; space++){
										out.append(" "); //5 spaces
									} 
								   out.append (standardText);
								   log.info("^^^^^^ standardText R:"+standardText);
								   out.append(" ");
								   out.append (itemBarcode);
								   log.info("^^^^^^ itemBarcode:"+itemBarcode);
								   
								   out.append("\r\n"); 
								   log.info("^^^^^^  Inside amount3value == 0  END");
								   noOfRecords++;
								   
							   }
							   
							   
							   else 
							   { //that means print 2 lines
							   log.info("BILLING UTILITY start of else");
							   out.append(actioncode_pid);
							   for(int space = 0; space < 35; space++){
									out.append(" "); //35 spaces
								}
							   detailCode += fourthChar + "F" + sixthChar + "0000000070{";
							   log.info("BILLING UTILITY detailCode ELSE:"+detailCode);
							   out.append(detailCode);
							   detailCode = "LIB";
							   for(int space = 0; space < 6; space++){
									out.append(" "); //5 spaces
								}     
							   out.append (invoiceNumber);
							   for(int space = 0; space < 8; space++){
									out.append(" "); //5 spaces
								} 
							   out.append (standardText);
							   log.info("BILLING UTILITY standardText:"+standardText);
							   out.append(" ");
							   out.append (itemBarcode);
							   log.info("BILLING UTILITY itemBarcode:"+itemBarcode);
							   out.append("\r\n"); 
							  // out.append("\r\n"); 
							   //out.append("\r\n"); 
							   
							   out.append(actioncode_pid);
							   log.info("BILLING UTILITY actioncode_pid:"+actioncode_pid);
							   for(int space = 0; space < 35; space++){
									out.append(" "); //10 spaces
								}
							   //-------------------
							   log.info("^^^^^^^^^^^^^^^2/9/2010 ^^^^^^^^^");
							   log.info("New amt:"+newAmt);
							   double remainingAmt = newAmt - 7;
							   log.info("remainingAmt:"+remainingAmt);
							   //================2/15=====Chandana=======
							  DecimalFormat twoDForm = new DecimalFormat("#.##");
							  double newRemainingAmt= Double.valueOf(twoDForm.format(remainingAmt));
							  String s = Double.toString(newRemainingAmt);
							  log.info("s OLD:"+s);
							   int w= s.indexOf(".");
							   log.info("s:"+s);
							   log.info("w:"+w);
							   String sTemp2= s.substring(w+1);
							   log.info("sTemp2"+sTemp2);
							   if(sTemp2.length()< 2)
							   {
								   //that menas only one deciamal point
								   s = s + "0";
								   log.info("BILLING UTILITY inside <:"+s);
							   }
							   log.info("s New:"+s);
							   char lastChar = s.charAt(s.length()-1);
							   log.info("BILLING UTILITY lastChar in +:"+lastChar);
							   char newLastChar = getLastCharPositive(lastChar);
							   log.info("BILLING UTILITY newLastChar  in +:"+newLastChar);
							    // remove last char
							  	String s1 = s.substring(0,s.length()-1);
							  	//remove "." and add newLastChar at the end
							  	int p= s.indexOf(".");
								String s2= s1.substring(0,p)+ s1.substring(p+1) +newLastChar;
								log.info("BILLING UTILITY s2  in +:"+s2);
								 int index = 11 - s2.length();
								 for (int i1=0; i1 < index ;i1++)
								 {
									 s2 = "0" + s2;
									 
								 }
								 finalAmount = s2;
							   //-------------------
							   detailCode += fourthChar + "R" + sixthChar + finalAmount;
							   out.append(detailCode);
							   detailCode = "LIB";
							   log.info("BILLING UTILITY detailCode R:"+detailCode);
							   for(int space = 0; space < 6; space++){
									out.append(" "); //5 spaces
								}     
							   out.append (invoiceNumber);
							   for(int space = 0; space < 8; space++){
									out.append(" "); //5 spaces
								} 
							   out.append (standardText);
							   log.info("BILLING UTILITY standardText R:"+standardText);
							   out.append(" ");
							   out.append (itemBarcode);
							   log.info("BILLING UTILITY itemBarcode:"+itemBarcode);
							   
							   out.append("\r\n"); 
							  // out.append("\r\n"); 
							  // out.append("\r\n"); 
							   log.info("BILLING UTILITY end of else");
							   noOfRecords = noOfRecords + 2;
							   
							   }//end of else for if(amount3Value == 0)   
							   
						   }//end of if (is positive)
						   
						   
						   
						   else
						   { 
						   out.append(actioncode_pid);
						   log.info("BILLING UTILITY actioncode_pid:"+actioncode_pid);
						   for(int space = 0; space < 35; space++){
								out.append(" "); //10 spaces
							}
						   detailCode += fourthChar + "R" + sixthChar + finalAmount;
						   out.append(detailCode);
						   detailCode = "LIB";
						   log.info("BILLING UTILITY detailCode R:"+detailCode);
						   for(int space = 0; space < 6; space++){
								out.append(" "); //5 spaces
							}     
						   out.append (invoiceNumber);
						   for(int space = 0; space < 8; space++){
								out.append(" "); //5 spaces
							} 
						   out.append (standardText);
						   log.info("BILLING UTILITY standardText R:"+standardText);
						   out.append(" ");
						   out.append (itemBarcode);
						   log.info("BILLING UTILITY itemBarcode:"+itemBarcode);
						   
						   out.append("\r\n"); 
						   log.info("BILLING UTILITY end of else");
						   noOfRecords++;
							   
						   }
						   
					   }
					 twoLines = false;
					 isPositiveAmt = false;
				}//end of for loop
				log.info("UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU");
				log.info("totalCharges :"+totalCharges);
				DecimalFormat twoDForm = new DecimalFormat("#.##");
				totalCharges = Double.valueOf(twoDForm.format(totalCharges));
				out.append("CTRL "+getRecordCount(noOfRecords+2)+" "+getTotalCharges(totalCharges));
				chargeBuffer =out.toString();
				
			}//end of try
			catch (Exception e) {
				log.error("Unable to generate report file", e);
				log.info("SendDataToServer ERROR in generateChargeFileContent");
				return null;
			}
			chargeRecordCount =noOfRecords+2;
			log.info("$$$$$$$$ chargeRecordCount: "+chargeRecordCount);
			log.info("SendDataToServer RETURNIN TRUE ");
			JSONObject obj =new JSONObject();
			String chargeStrCount = ""+chargeRecordCount;
			obj.put("chargeBuffer",chargeBuffer);
			obj.put("chargeRecordCount",chargeStrCount);
			return obj;
	  }
	
  /*
   * Returns the content of the Person File
   */
	  public static String generatePersonFileContent(){
		  StringBuffer out= new StringBuffer();
		  DateFormat shortDf = DateFormat.getDateInstance(DateFormat.SHORT);
		  String todayStr = shortDf.format(new Date());
			log.info("SEND DATATO SERVER person todayStr:"+ todayStr);
			String [] temp = todayStr.split("/");
			String fp = temp[0];
			String chargeBuffer = null;
			if(fp.length()<2)
			{
				fp = "0"+fp;
			}
			String sp = temp[1];
			if(sp.length()<2)
			{
				sp = "0"+sp;
			}
			String tp = temp[2];
			if(tp.length()<2)
			{
				tp = "0"+tp;
			}
			
			String today = fp+sp+tp;
			log.info("BILLING UTILITY today:"+ today);
		  try {
				//header
				out.append("PHDR CLIBCIRC.PERSON"+" "+today+" "+today+" "+"000001");
				out.append("\r\n");
				out.append("PTRL 000002");
		  }
		  catch (Exception e) {
				log.error("Unable to generate report file Person", e);
				log.info("SendDataToServer ERROR in generatePersonFileContent");
				return null;
			}
			log.info("generatePersonFileContent RETURNIN TRUE ");
			chargeBuffer = out.toString();
		  
		return chargeBuffer;  
	  }
  
  /*
   * Returns the content of the Entity file
   */
	  public static String generateEntityFileContent(){
		  StringBuffer out= new StringBuffer();
		  DateFormat shortDf = DateFormat.getDateInstance(DateFormat.SHORT);
		  String todayStr = shortDf.format(new Date());
			log.info("SEND DATATO SERVER Entity todayStr:"+ todayStr);
			String [] temp = todayStr.split("/");
			String fp = temp[0];
			String chargeBuffer = null;
			if(fp.length()<2)
			{
				fp = "0"+fp;
			}
			String sp = temp[1];
			if(sp.length()<2)
			{
				sp = "0"+sp;
			}
			String tp = temp[2];
			if(tp.length()<2)
			{
				tp = "0"+tp;
			}
			
			//String today = tp+fp+sp;
			String today = fp+sp+tp;
			log.info("BILLING UTILITY today:"+ today);
		  try {
				//header
				out.append("EHDR CLIBCIRC.ENTITY"+" "+today+" "+today+" "+"000001");
				out.append("\r\n");
				out.append("ETRL 000002");
		  }
		  catch (Exception e) {
				log.error("Unable to generate report file Entity", e);
				log.info("SendDataToServer ERROR in generateEntityFileContent");
				return null;
			}
			log.info("generateEntityFileContent RETURNIN TRUE ");
			chargeBuffer = out.toString();
		  
		return chargeBuffer; 
  }
	  
	  public static String getEmailContent(String userName,int chargeRecordCount)
	  {
			log.info("$$$$$$$$ INSIDE GETEMAIL  chargeRecordCount: "+chargeRecordCount);
		/*  DateFormat shortDf = DateFormat.getDateInstance(DateFormat.SHORT);
		  String todayStr = shortDf.format(new Date());
		  StringBuffer out= new StringBuffer();
		  String [] temp = todayStr.split("/");
			String fp = temp[0];
			if(fp.length()<2)
			{
				fp = "0"+fp;
			}
			String sp = temp[1];
			if(sp.length()<2)
			{
				sp = "0"+sp;
			}
			String tp = temp[2];
			if(tp.length()<2)
			{
				tp = "0"+tp;
			}	
			String today = tp+fp+sp;	
  */ // 1/28
			DateFormat shortDf = DateFormat.getDateInstance(DateFormat.SHORT);
			String todayStr = shortDf.format(new Date());
			 StringBuffer out= new StringBuffer();
		    Calendar cal = Calendar.getInstance();
		    int day = cal.get(Calendar.DATE);
	        int month = cal.get(Calendar.MONTH) + 1;
	        int year = cal.get(Calendar.YEAR);
	        log.info("YEAR:"+year);
	        log.info("month:"+month);
	        log.info("day:"+day);
      										 
	        double date1 = toJulian(new int[]{year,month,day});
	        double date2 = toJulian(new int[]{year,1,1});
	        int dif = (int) (date1-date2+1);
	        log.info("dif: " + dif + " days.");
       
	        String strYear =""+year;
	        String strDiff =""+dif;
	        for(int i=strDiff.length(); i < 3 ;i++)
	         {
	         	strDiff = "0"+strDiff;
	         }
		         
		      String today = strYear.substring(2)+strDiff;
			
	  out.append("User Information");
	  out.append("\r\n");
	  out.append("=================");
	  out.append("\r\n");
	  out.append("Date:"+todayStr);
	  out.append("\r\n");
	  out.append("\r\n");
	  out.append("Department: Library");
	  out.append("\r\n");
	  out.append("Contact: Department of Business and Finance");
	  out.append("\n");
	  out.append("\r\n");
	  out.append("Email Address: Department_of_Business_and_Finance_Q&A@AD.UCSD.EDU");
	  out.append("\r\n");
	  out.append("Phone: 858 534 5621");
	  out.append("\r\n");
	  out.append("Mailcode: 0175M");
	  out.append("\r\n");
	  out.append("\r\n");
	  out.append("\r\n");
	  out.append("File Description: Accounts receivable batch input files");
	  out.append("\r\n");
	  out.append("From: "+userName);
	  out.append("\r\n");
	  out.append("Record length: 320 characters");
	  out.append("\r\n");out.append("\r\n");
	  out.append("FILE         DATASET NAME (yymmdd=Julian date)           RECORD COUNT");
	  out.append("\r\n");
	  out.append("---------    ---------------------------------------     ------------\n");
	  out.append("AR CHARGE:   SISP.ARD2502.LIBCIR.CHARGE.D"+today+" =         "+chargeRecordCount);
	  out.append("\r\n");
	  out.append("\r\n");
	  out.append("AR ENTITY:   SISP.ARD2502.LIBCIR.ENTITY.D"+today+" =         2");
	  out.append("\r\n");
	  out.append("\r\n");
	  out.append("AR PERSON:   SISP.ARD2502.LIBCIR.PERSON.D"+today+" =         2");
	  out.append("\r\n");
	  out.append("\r\n");
	  out.append("Date input file is to be processed:"+todayStr);
	  out.append("\r\n");
	  out.append("\r\n");
	  out.append("__________________________________________________________________________\r\n");
	  return out.toString();
	  
		  
	  }
	  
	  
	   public static String getFullname(String username)
	   {
		   String fullname = null;
		   Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			log.info("$$$$ username: ####"+username);
			try {
				conn = ConnectionManager.getConnection("billing");
				stmt = conn.createStatement();
				rs = stmt.executeQuery(" SELECT FULLNAME FROM USERS WHERE USERNAME ="+"'"+username+"'");
				while (rs.next()) {
					fullname = rs.getString(1);
					log.info("$$$$$$fullname$$$ "+fullname);
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
		return fullname;
		   
	   }
	   
	   public static String getEmail(String username)
	   {
		   String fullname = null;
		   Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			log.info("$$$$ username: ####"+username);
			try {
				conn = ConnectionManager.getConnection("billing");
				stmt = conn.createStatement();
				rs = stmt.executeQuery(" SELECT EMAIL FROM USERS WHERE USERNAME ="+"'"+username+"'");
				while (rs.next()) {
					fullname = rs.getString(1);
					log.info("email: "+fullname);
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
		return fullname;
		   
	   }
	   //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	   public static boolean insertDataToDatabase(JSONArray pending,String userID)
	   {
			  Connection conn = null;
				Statement stmt = null;
				ResultSet rs = null;
				String userid = null;
				
				String itemNum = null;
				int maxTransID = 0;
				int chargeNO = 0;
				String barcode = null;
				 
				 
				boolean flag = true;
				try {
					conn = ConnectionManager.getConnection("billing");
					stmt = conn.createStatement();
					
				}  catch (SQLException e) {
					// TODO Auto-generated catch block
					flag = false;
					log.error("SQLException in creating the connection", e);
					System.out.println("SQLException in creating the connection" + e);
				 } catch (NamingException e) {
					log.error("JNDI Lookup failed for DB2 connection", e);
				}
			  
			  for(int i = 0; i < pending.size(); i++){
					JSONObject row = (JSONObject)pending.get(i);
					String  invNumber =((String)row.get("invoiceNo")).trim();
					String pid =((String)row.get("pid")).trim();
					String  invoiceDate =((String)row.get("date")).trim();
					String  chargeLoc =((String)row.get("loc")).trim();
					String  chargeTypee =((String)row.get("chargeType")).trim();
					String  chargeFee =((String)row.get("amount1")).trim();
					String  processingFee =((String)row.get("amount2")).trim();
					String  billingFees =((String)row.get("amount3")).trim();
					String  patronNumber =((String)row.get("patronRecordNo")).trim();
					String  barcodeTemp =((String)row.get("itemBarcode")).trim();
					String title =((String)row.get("title")).trim();
					String callNo =((String)row.get("callNo")).trim();
					String pName =((String)row.get("name")).trim();
					String aff =((String)row.get("patronAffliation")).trim();
					String pType =((String)row.get("patronType")).trim();
					
					
					log.info("invNumber: "+invNumber);
					log.info("invoiceDate "+invoiceDate);
					log.info("chargeLoc: "+chargeLoc);
					log.info("chargeTypee: "+chargeTypee);
					log.info("processingFee: "+processingFee);
					log.info("billingFees: "+billingFees);
					log.info("patronNumber: "+patronNumber);
					log.info("barcodeTemp: "+barcodeTemp);
					log.info("title: "+title);
					log.info("callNo: "+callNo);
					log.info("name: "+pName);
					log.info("aff: "+aff);
					log.info("pType: "+pType);
					
					 //===================================
					 double chargeTotal = Double.parseDouble(chargeFee.substring(1));	
					 log.info("chargeTotal"+chargeTotal);
					 double finalAmtCharge = 0;
					
					 finalAmtCharge =  chargeTotal;
					 log.info("finalAmtCharge"+finalAmtCharge);
					  //===================================
					 double finalAmtprocessingFee = 0;
					 double processingFeeTotal = Double.parseDouble(processingFee.substring(1));	
					 log.info("processingFeeTotal"+processingFeeTotal);
					
					 finalAmtprocessingFee =  processingFeeTotal;
					 log.info("finalAmtprocessingFee"+finalAmtprocessingFee);
					  //========================================
					 
					  double finalAmtBillingFees = 0;
						double billingFeesTotal = Double.parseDouble(billingFees.substring(1));	
						log.info("billingFeesTotal"+billingFeesTotal);
						
						finalAmtBillingFees =  billingFeesTotal;
						log.info("finalAmtBillingFees"+finalAmtBillingFees);
						  //========================================
					 
					
					//String  invoiceDate =((String)row.get("date")).trim();
						// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					  if (barcodeTemp.length() > 2)
					  {
							int t= barcodeTemp.indexOf("b");
							if(t == 0)
							{
								barcode = barcodeTemp.substring(1); 
									
							}
							else
							{
								 barcode = barcodeTemp; 
							}
					
					    log.info("barcode: "+barcode);
					    try {
						  rs = stmt.executeQuery(" SELECT ITEMNO FROM ITEMS WHERE BARCODE="+ "'"+barcode+"'");
						 
							  while (rs.next()) 
							  {
								   itemNum = rs.getString(1);
							     log.info("ITEMNO: "+itemNum);
							   }
						
					    } catch (NumberFormatException e) {
						  // TODO Auto-generated catch block
						  flag = false;
						  log.error("NumberFormatException", e);
					    } catch (SQLException e) {
						  // TODO Auto-generated catch block
						  flag = false;
						  log.error("SQLException", e);
					    } 
					
					  }//end  if (barcodeTemp.length() > 2)
					  else{
						 //if no barcode insert a record to ITEMS table and then use that new itemno
						  try{
							  rs = stmt.executeQuery("SELECT MAX(itemNo) FROM ITEMS");
								long maxItemNo =0;
								while (rs.next()) {
									maxItemNo = rs.getLong(1);
								
								}
								
								 PreparedStatement pstmt = conn.prepareStatement(
										    "INSERT INTO ITEMS ( ITEMNO,BARCODE, TITLE,CALLNUMBER ) " +
										    " values (?, ?, ?, ?)");
		                        
								 
								    pstmt.setLong( 1, (maxItemNo+1) );
								    pstmt.setString( 2,barcodeTemp ); 
								    pstmt.setString( 3, title ); 
								    pstmt.setString( 4, callNo);
								    log.info("+++++++ INSERTING INTO ITEMS +++++++++++++");
								    log.info("$$$$$ (maxItemNo+1): "+(maxItemNo+1));
								    log.info("$$$$$ barcodeTemp: "+(barcodeTemp+1));
								    log.info("$$$$$ title: "+title);
								    log.info("$$$$$ callNo: "+callNo);
								    
								    pstmt.execute();
								    
								  
								    long newItemlong = maxItemNo+1;
								    itemNum = ""+newItemlong;
								   log.info("$$$$$ new iTem no was: "+newItemlong);	
								   log.info("$$$$$$$$   Inserted the record to ITEMS table...");	
								   System.out.println("$$$$$$$$   Inserted the record to ITEMS table...");
								   
						 }
						 catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								flag = false;
								log.error("NumberFormatException", e);
								System.out.println("INSERT INTO ITEMS NumberFormatException:" + e);
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								flag = false;
								log.error("SQLException", e);
								System.out.println("INSERT INTO ITEMS SQLException:" + e);
							} 
							
					 }//end of else part for if(barcode
					 
					 
					//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					 try
					 {
					    rs = stmt.executeQuery(" SELECT MAX(TRANSACTIONNO) FROM TRANSACTIONS");
					
					    while (rs.next()) {
						   maxTransID = rs.getInt(1);
						   log.info("max TRANSACTIONNO: "+maxTransID);
					    }

					
					    rs = stmt.executeQuery("SELECT CHARGETYPE FROM CHARGETYPES WHERE upper(DESCRIPTION) LIKE"+ "'" + chargeTypee + "%'");
						
						  while (rs.next()) {
							chargeNO = rs.getInt(1);
							log.info("chargeNO: "+chargeNO);
						  }
					
					  } catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							flag = false;
							log.error("NumberFormatException", e);
					  } catch (SQLException e) {
							// TODO Auto-generated catch block
							flag = false;
							log.error("SQLException", e);
					  } 
					  
					//======= 01/28 insert new patrons====================
					try
					{
						String patronNoN = patronNumber.substring(1);
					
					  rs = stmt.executeQuery(" SELECT count(*)FROM PATRONS WHERE PATRONNO ="+"'" +patronNoN+"'");
				    int count1 = 0;
						while (rs.next()) {
							 count1 = rs.getInt(1);				
							 log.info("count1="+count1);
						}
						int maxPatronId =0;
						if(count1 == 0)
						{
							// get maximum patron id and insert the patron to Patron table
							rs = stmt.executeQuery(" SELECT MAX(PATRONID) FROM PATRONS");
							
							while (rs.next()) {
								maxPatronId = rs.getInt(1);
								 log.info("maxPatronId: "+maxPatronId);
							}
						
						  //get patron no substrin 1
						  PreparedStatement pstmt = conn.prepareStatement(
							    "INSERT INTO PATRONS ( PATRONNO,PID,PATRONNAME,PATRONTYPE,AFFILIATION,PATRONID,NOTES) " +
							    " values (?, ?, ?, ?, ? ,?,?)");
						
						  pstmt.setString( 1,patronNumber.substring(1));
					    pstmt.setString( 2,pid ); 
					    pstmt.setString( 3, pName ); 
					    pstmt.setInt( 4, Integer.parseInt(pType));
					    pstmt.setInt( 5, Integer.parseInt(aff));
					    pstmt.setInt( 6, (maxPatronId+1));
					    pstmt.setString( 7, "" ); 
					    log.info("+++++++ INSERTING INTO PATRONS +++++++++++++");
					    log.info("$$$$$ patronNumber: "+patronNumber.substring(1));
					    log.info("$$$$$ pid: "+pid);
					    log.info("$$$$$ pName: "+pName);
					    log.info("$$$$$ pType: "+ Integer.parseInt(pType));
					    log.info("$$$$$ Aff: "+Integer.parseInt(aff));
					    log.info("$$$$$ (maxPatronId+1): "+(maxPatronId+1));
					    
					    pstmt.execute();					    
					    log.info("executed  good 1...");
					    System.out.println("INSERT INTO PATRONS executed  good...");

					    
					  }
					 } catch (SQLException e) {
						// TODO Auto-generated catch block
						 log.info("SQLException  when inserting patrons...");
						 System.out.println("INSERT INTO PATRONS SQLException..." + e);
						 log.error("SQLException", e);
						flag = false;
				 	}
					//==========insert part=================
				if(flag) {
					try {

						  String patronNumberNew = patronNumber.substring(1);
							PreparedStatement pstmt = conn.prepareStatement(
								    "INSERT INTO TRANSACTIONS ( TRANSACTIONNO,INVOICENO, INVOICEDATE,CHARGELOCATION,CHARGETYPE,CHARGE,PROCESSINGFEE,BILLINGFEE,PATRONNO,ITEMNO,ADDEDDATE,USERID ) " +
								    " values (?, ?, ?, ?, ? ,?,?,?,?,?,?,?)");
						 
						  String strInvDate ="20"+invoiceDate.substring(0,2)+"-"+invoiceDate.substring(2,4)+"-"+invoiceDate.substring(4);
						  log.info("strInvDate"+strInvDate);
						  java.sql.Date jsqlD = java.sql.Date.valueOf( strInvDate );
						  DateFormat shortDf = DateFormat.getDateInstance(DateFormat.SHORT);
						  String todayStr = shortDf.format(new Date());
						  String [] temp = todayStr.split("/");
						  String newArray = "20"+temp[2]+"-"+temp[0]+"-"+temp[1];						  
						  java.sql.Date when = java.sql.Date.valueOf( newArray);
						  log.info("when:"+when);
						  if (itemNum == null)
						  {
							  itemNum = "000000";
						  }

						  

						    pstmt.setLong( 1, (maxTransID+1) );
						    pstmt.setString( 2,invNumber ); 
						    pstmt.setDate( 3, jsqlD ); 
						    pstmt.setString( 4, chargeLoc);
						    pstmt.setInt( 5, chargeNO);
						    pstmt.setDouble( 6, finalAmtCharge);
						    pstmt.setDouble( 7, finalAmtprocessingFee ); 
						    pstmt.setDouble(8, finalAmtBillingFees ); 
						    pstmt.setString(9, patronNumberNew ); 
						    pstmt.setString(10, itemNum);  
						    pstmt.setDate( 11,when ); 
						    pstmt.setString( 12, userID);
						   					    
						   // conn.commit();
						    log.info("+++++++ INSERTING INTO TRANSACTIONS +++++++++++++");
						    log.info("$$$$$  (maxTransID+1) : "+ (maxTransID+1) );
						    log.info("$$$$$ invNumber: "+invNumber);
						    log.info("$$$$$ jsqlD: "+jsqlD);
						    log.info("$$$$$ chargeLoc: "+chargeLoc);
						    log.info("$$$$$ finalAmtCharge: "+finalAmtCharge);
						    log.info("$$$$$ finalAmtprocessingFee: "+finalAmtprocessingFee);
						    log.info("$$$$$ finalAmtBillingFees: "+finalAmtBillingFees);
						    log.info("$$$$$ patronNumberNew: "+patronNumberNew);
						    log.info("$$$$$ itemNum: "+itemNum);
						    log.info("$$$$$ when: "+when);
						    log.info("$$$$$ userid: "+userID);

						    System.out.println("+++++++ INSERTING INTO TRANSACTIONS +++++++++++++");
						    System.out.println("$$$$$1  (maxTransID+1): "+ (maxTransID+1) );
						    System.out.println("$$$$$2 invNumber: "+invNumber);
						    System.out.println("$$$$$3 jsqlD: "+jsqlD);
						    System.out.println("$$$$$4 chargeLoc: "+chargeLoc);
						    System.out.println("$$$$$5 chargeNO: "+chargeNO);
						    System.out.println("$$$$$6 finalAmtCharge: "+finalAmtCharge);
						    System.out.println("$$$$$7 finalAmtprocessingFee: "+finalAmtprocessingFee);
						    System.out.println("$$$$$8 finalAmtBillingFees: "+finalAmtBillingFees);
						    System.out.println("$$$$$9 patronNumberNew: "+patronNumberNew);
						    System.out.println("$$$$$10 itemNum: "+itemNum);
						    System.out.println("$$$$$11 when: "+when);
						    System.out.println("$$$$$12 userid: "+userID);
						    
						   pstmt.execute();

						  log.info("executed  good...");
						  System.out.println("executed  good...");
						  
					
				   } catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						flag = false;
						log.error("NumberFormatException", e);
						System.out.println("NumberFormatException" + e);
					 } catch (SQLException e) {
							// TODO Auto-generated catch block
							 log.info("SQLException  333333...");
							 log.error("SQLException", e);
							 
							 System.out.println("SQLException  555555..." + e);

							 flag = false;
							 try{ if (conn != null) {
							        conn.rollback();
							        
							      }
							 }
						   catch (SQLException eg) { 
						   	log.info("Connection rollback MAIN...");
						    log.error("SQLException", eg);
						   }
				   } 
          }
			   }//end of for
			   
			  try{
				   rs.close();
				   stmt.close();
				   //conn.commit();
				   conn.close();
				   
			   } catch (SQLException e) {
					// TODO Auto-generated catch block
						flag = false;
						try{ if (conn != null) 
							   {
							    rs.close();
							    stmt.close();
						       conn.rollback();
						       conn.close();
						      }
						 }
						 catch (SQLException eg) { 
						 	 System.out.println("Connection rollback MAIN...");
						   log.error("SQLException", eg);
						 }
			  	} 
		     log.info("flag from insert:"+flag);
		     System.out.println("flag from insert:"+flag);
			   return flag;
	   }
	   
	   
	   public static String getUserID(String username)
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
	//============================================================
   // &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
  //================================================================
	  private static String getFourthCharacter(char c)
	  {
	  	String code = null;
	  	switch (c) {
	  	case '8': code = "L";
	  			  break;
	  	case '9': code = "L";
	  			  break;
	  	case 'a': code = "C";
	  			  break;
	  	case 'b': code = "B";
	  	  		  break;
	  	case 'c': code = "C";
	  	  		  break;
	  	case 'e': code = "E";
	  	  		  break;
	  	case 'f': code = "C";
	  	  		  break;
	  	case 'g': code = "C";
	  			  break;
	  	case 'i': code = "R";
	  			  break;
	  	case 'l': code = "L";
	  	  		  break;
	  	case 'm': code = "M";
	  	  		  break;
	  	case 's': code = "I";
	  	  		  break;
	  	case 'o': code = "L";
	  	  		  break;
	  	case 'O': code = "L";
	  			  break;
	  	}
	  	return code;
	  }

	  private static String getSixthCharacter(int c)
	  {
	  	String s = null;
	  	if (c == 1 || c == 23 || c == 24 || c == 25 || c == 26 || c == 27 || c == 32 || c == 33 || c == 34 || c == 35 || c == 36 || c == 37 || c == 38 || c == 39 || c == 40 || c == 41 || c == 44)
	  		s= "X";
	  	else if (c == 2 || c == 42)
	  		s = "G";
	  	else if (c == 3 || c == 22)
	  		s = "Z";
	  	else if (c == 4 || c == 5 || c == 6 || c == 7 || c == 8 || c == 9 || c == 11 || c == 19 || c == 43)
	  		s = "O";
	  	else if (c == 16 || c == 17)
	  		s = "S";
	  	
	  	return s;
	  }

	  private static char getLastCharPositive(char c){
	  	char newC = ' ';
	  	switch (c){
	  	case '0' : newC = '{';break;
	  	case '1' : newC = 'A';break;
	  	case '2' : newC = 'B';break;
	  	case '3' : newC = 'C';break;
	  	case '4' : newC = 'D';break;
	  	case '5' : newC = 'E';break;
	  	case '6' : newC = 'F';break;
	  	case '7' : newC = 'G';break;
	  	case '8' : newC = 'H';break;
	  	case '9' : newC = 'I';break;
	  	
	  	}
	  	return newC;
	  }

	  private static char getLastCharNegative(char c){
	  	char newC = ' ';
	  	switch (c){
	  	case '0' : newC = '}';break;
	  	case '1' : newC = 'J';break;
	  	case '2' : newC = 'K';break;
	  	case '3' : newC = 'L';break;
	  	case '4' : newC = 'M';break;
	  	case '5' : newC = 'N';break;
	  	case '6' : newC = 'O';break;
	  	case '7' : newC = 'P';break;
	  	case '8' : newC = 'Q';break;
	  	case '9' : newC = 'R';break;
	  	
	  	}
	  	return newC;
	  }
	  private static String getRecordCount(int noOfRecords){
	  	
	  	String str = ""+ noOfRecords;
	  	int len = str.length();
	  	String finalString = str;
	  	for(int i =0 ;i <6-len; i++ )
	  	{
	  		finalString = "0"+finalString; 
	  		
	  	}
	  	
	  	return finalString;
	  	
	  }
	  private static String getTotalCharges(double totalCharges){
	  	String finalString = null;
	  	if (totalCharges < 0)
	  	{
	  		totalCharges = totalCharges - (totalCharges *2);
	  		log.info("totalCharges :"+totalCharges);
	
	  		DecimalFormat twoDForm = new DecimalFormat("#.##");
			totalCharges = Double.valueOf(twoDForm.format(totalCharges));
			log.info("totalCharges New :"+totalCharges);
			 String str = ""+ totalCharges;
			 log.info("TOTAL: ="+str);
		  	   int w= str.indexOf(".");
			   log.info("str:"+str);
			   log.info("w:"+w);
			   String sTemp2=str.substring(w+1);
			   log.info("sTemp2"+sTemp2);
			   if(sTemp2.length()< 2)
			   {
				   //that menas only one deciamal point
				   str = str + "0";
				   log.info("BILLING UTILITY inside <:"+str);
			   }
			   log.info("TOTAL new: ="+str);
	  		
	  		int p= str.indexOf(".");
	  		String s2= str.substring(0,p)+ str.substring(p+1);
	  		finalString = s2;
	  		//	int len = s2.length();
	  		
	  	//=========2/18======chandana====
	  		
	  		 char lastChar = finalString.charAt(finalString.length()-1);
		  	 log.info("if(amount<0): lastChar:"+lastChar);
		  	 char newLastChar = getLastCharNegative(lastChar);
		  	  log.info("BILLING UTILITY newLastChar:"+newLastChar);
		    // remove last char
		  	String s1 = finalString.substring(0,finalString.length()-1);
		  	log.info("BILLING UTILITY s1:"+s1);
		  	finalString = s1+newLastChar;
		  	log.info("FINAL finalString:"+finalString);		
		  	int len = finalString.length();
	  		//================================= 		
		  	//for(int i =0 ;i <10 -len; i++ )
	  		for(int i =0 ;i <11 -len; i++ )
	  		{
	  			finalString = "0"+finalString; 
	  			
	  		}
	  		//finalString += "}";
	  	}
	  	
	  	else
	  	{
	  		log.info("totalCharges :"+totalCharges);
			DecimalFormat twoDForm = new DecimalFormat("#.##");
			totalCharges = Double.valueOf(twoDForm.format(totalCharges));
			log.info("totalCharges New :"+totalCharges);
	  		String str = ""+ totalCharges;
	  		log.info("TOTAL: ="+str);
	  		int w= str.indexOf(".");
	  	   log.info("str:"+str);
	  	   log.info("w:"+w);
	  	   String sTemp2=str.substring(w+1);
	  	   log.info("sTemp2"+sTemp2);
	  	   if(sTemp2.length()< 2)
	  	   {
	  		   //that menas only one deciamal point
	  		   str = str + "0";
	  		   log.info("BILLING UTILITY inside <:"+str);
	  	   }
	  		int p= str.indexOf(".");
	  		String s2= str.substring(0,p)+ str.substring(p+1);
	  		finalString = s2;
	 	    //=========2/18======chandana====
	  		
	 		 char lastChar = finalString.charAt(finalString.length()-1);
		  	 log.info("if(amount<0): lastChar:"+lastChar);
		  	 char newLastChar = getLastCharPositive(lastChar);
		  	  log.info("BILLING UTILITY newLastChar:"+newLastChar);
		    // remove last char
		  	String s1 = finalString.substring(0,finalString.length()-1);
		  	log.info("BILLING UTILITY s1:"+s1);
		  	finalString = s1+newLastChar;
		  	log.info("FINAL finalString:"+finalString);		
		  	int len = finalString.length();
	 		//================================= 
			
	  		//for(int i =0 ;i <10 -len; i++ )	
	  		for(int i =0 ;i <11 -len; i++ )  		
	  	  		{
	  			finalString = "0"+finalString; 
	  			
	  		}
	  		//finalString += "{";
	  	}
	  	
	  	
	  	return finalString;
	  	
	  }
	
	  public static JSONObject removeFiles(String username,String password)
      {
		    FTPSClient ftp = null;
	        boolean flag1 = true;
            JSONObject obj =new JSONObject();
            int reply;
			String removeError = null;
			boolean  deleteFlagCharge=true;
		    boolean  deleteFlagPerson=false;
			boolean  deleteFlagEntity=false;
			  log.info("============Inside removeFiles================");
	         
	  	    //ftp = new FTPSClient();
	  	   // ftp.setRemoteVerificationEnabled(false); 
	  	 
	  	/*try {
			   try{
	        	 ftp = new FTPSClient(protocol,false);
	        	 ftp.setRemoteVerificationEnabled(false); 
	         }
		  	   catch(NoSuchAlgorithmException ne)
		  	   {
		  		 log.info("$$$$$ NoSuchAlgorithmException"); 
		  	   }
	  	  log.info("username and password:"+username+" "+password);
	  	  ftp.connect(serverName);
	           log.info("Connected to " + serverName + ".");
	           ftp.login(username, password);
	           log.info("Logged in with the usernmae and password "+username+" "+password);
	           ftp.enterLocalPassiveMode(); 
	          // ftp.setDataTimeout(600000000);
	           reply = ftp.getReplyCode();
	           
	           if (!FTPReply.isPositiveCompletion(reply))
	           {
	               ftp.disconnect();
	               System.err.println("FTP server refused connection.");
	               System.exit(1);
	           }
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);        
	           ftp.setFileType(FTP.ASCII_FILE_TYPE);
			}
			 catch (IOException e)
		        {
				 log.info("$$$$$ IO Exception in connecting/ logging to the server");
		        } 
			*/ 
		
				//delete the file
				//deleteFlagCharge = ftp.deleteFile( "CHARGES.txt") ;
				//deleteFlagEntity = ftp.deleteFile( "PERSON.txt") ;
				//deleteFlagEntity = ftp.deleteFile( "ENTITY.txt") ;
				deleteFlagCharge=deleteChargeFileFromServer(password,username);
				deleteFlagPerson=deletePersonFileFromServer(password,username);
				deleteFlagCharge=deleteEntityFileFromServer(password,username);
				
				if(!deleteFlagCharge)
				{
					removeError="CHARGES.txt file could not be removed from the server!\n";
					obj.put("removeError",removeError);
				}
				if(!deleteFlagPerson)
				{
					removeError +="PERSON.txt file could not be removed from the server!\n";
					obj.put("removeError",removeError);
				}
				if(!deleteFlagCharge)
				{
					removeError += "ENTITY.txt file could not be removed from the server!\n";
					obj.put("removeError",removeError);
				}
				
				 //ftp.logout();
				// ftp.disconnect();
				
		    
	            
			boolean removeFlag = false;
			if ( deleteFlagCharge && deleteFlagPerson && deleteFlagEntity )
			{
				removeFlag = true;
			}
			
			obj.put("removeFlag",removeFlag);
			return obj;
      
  }
	  
	  public static  java.sql.Date getCurrentJavaSqlDate() {
		    java.util.Date today = new java.util.Date();
		    return new java.sql.Date(today.getTime());
		  }

	  
	  public static boolean insertSessionData(JSONArray results)
	  {
			log.info ("insertSessionData   BEGIN");
			log.info ("results  SIZE: "+results.size());
		  boolean flag = true;
		  JSONArray rows = results;
		  Connection conn = null;
			Statement stmt = null;
			ResultSet rs = null;
			String userid = null;
			int maxPendingID = 0;
						 
			 
			try {
				conn = ConnectionManager.getConnection("billing");
				stmt = conn.createStatement();
				
			}  catch (SQLException e) {
				// TODO Auto-generated catch block
				flag = false;
				log.error("SQLException in creating the connection", e);
			 } catch (NamingException e) {
				log.error("JNDI Lookup failed for DB2 connection", e);
			}

			try
			 {
			    rs = stmt.executeQuery(" SELECT MAX(PENDINGID) FROM PENDING_HISTORY");
			
			    while (rs.next()) {
				   maxPendingID = rs.getInt(1);
				   log.info("max PendingID: "+maxPendingID);
			    }
			
			  } catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					flag = false;
					log.error("NumberFormatException", e);
			  } catch (SQLException e) {
					// TODO Auto-generated catch block
					flag = false;
					log.error("SQLException", e);
			  } 
		  
		  
		  for(int i = 0; i < rows.size(); i++){
				JSONObject row = (JSONObject)rows.get(i);
				maxPendingID = maxPendingID + 1;
				
				  try{
				String invoiceNumber = ((String)row.get("invoiceNo")).trim();
				String invoiceDate =((String)row.get("date")).trim();
				String chargeLoc = ((String)row.get("loc")).trim();
				String chargeTypee =((String)row.get("chargeType")).trim();
				String patronNo = ((String)row.get("patronRecordNo")).trim();
				String patronType =((String)row.get("patronType")).trim();
				String pid = ((String)row.get("pid")).trim();
				String pName =((String)row.get("name")).trim();
				String address = ((String)row.get("address")).trim();
				String pcode1=((String)row.get("pcode1")).trim();
				String pcode2 =((String)row.get("pcode2")).trim();
				String aff = ((String)row.get("patronAffliation")).trim();
				String barcodee = ((String)row.get("itemBarcode")).trim();
				String bookTitle = ((String)row.get("title")).trim();
				String bookCallNo =((String)row.get("callNo")).trim();
				String chargeFee = ((String)row.get("amount1")).trim();
				String processingFee = ((String)row.get("amount2")).trim();
				String billingFee = ((String)row.get("amount3")).trim();
				
				log.info ("invoiceNumber: "+invoiceNumber);
				log.info ("invoiceDate: "+invoiceDate);
				log.info ("chargeLoc: "+chargeLoc);
				log.info ("chargeTypee: "+chargeTypee);
				log.info ("patronNo: "+patronNo);
				log.info ("pid: "+pid);
				log.info ("pName: "+pName);
				log.info ("address: "+address);
				log.info ("pcode1: "+pcode1);
				log.info ("pcode2: "+pcode2);
				log.info ("aff: "+aff);
				log.info ("barcodee: "+barcodee);
				log.info ("bookTitle: "+bookTitle);
				log.info ("bookCallNo: "+bookCallNo);
				log.info ("chargeFee: "+chargeFee);
				log.info ("processingFee: "+processingFee);
				log.info ("billingFee: "+billingFee);
		
				
				
				 String s2 = "20"+invoiceDate.substring(0,2)+"-"+invoiceDate.substring(2,4)+"-"+invoiceDate.substring(4);
				log.info("s2 is:"+s2);
				java.sql.Date jsqlD1 = java.sql.Date.valueOf(s2);
				 java.sql.Date today = getCurrentJavaSqlDate();
				//insertion
				// String patronNum = patronNo.substring(1);
				 String pidd = null;
				 String barCode = null;
				
				 PreparedStatement pstmt1 = conn.prepareStatement(
						    "INSERT INTO PENDING_HISTORY ( PENDINGID,TRANSACTIONDATE,INVOICENO,CHARGELOCATION,CHARGETYPE,PATRONNO,PATRONTYPE,PID,NAME,ADDRESS,PCODE1,PCODE2,AFFILIATION,BARCODE,TITLE,CALLNO,CHARGEFEE,PROCESSINGFEE,BILLINGFEE, INVOICEDATE) " +
						    " values (?, ?, ?, ?, ? ,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		      
				  pstmt1.setLong( 1, maxPendingID);
				  pstmt1.setDate( 2,today);
				  pstmt1.setString( 3, invoiceNumber ); 
				  pstmt1.setString( 4, chargeLoc);
				  pstmt1.setString( 5, chargeTypee);
				  pstmt1.setString( 6, patronNo);
				  pstmt1.setString( 7, patronType ); 
				  pstmt1.setString(8, pid ); 
				  pstmt1.setString(9, pName ); 
				  pstmt1.setString(10, address ); 				    
				  pstmt1.setString(11, pcode1 ); 				 
				  pstmt1.setString( 12,pcode2 ); 
				  pstmt1.setString(13, aff );
				  pstmt1.setString( 14, barcodee );
				  pstmt1.setString( 15, bookTitle );
				  pstmt1.setString( 16, bookCallNo );
				  pstmt1.setString( 17, chargeFee );
				  pstmt1.setString( 18, processingFee );
				  pstmt1.setString( 19, billingFee );
				  pstmt1.setDate( 20, jsqlD1);
				    
				  pstmt1.execute();
				  System.out.println("pending_history executed  good...");
				  //conn.close();
		  } catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				flag = false;
				log.error("NumberFormatException", e);
				System.out.println("NumberFormatException" + e);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				 log.info("SQLException  333333...");
				 log.error("SQLException", e);
				 System.out.println("SQLException" + e);
				flag = false;
				try{ if (conn != null) {
				        conn.rollback();
				        
				      }
				
				}
				 catch (SQLException eg) { log.info("Connection rollback MAIN...");
				 log.error("SQLException", eg);
				 }
			} 
				
				
				
				
		  }
		  return flag;
	  }

	  public static double toJulian(int[] ymd) {
			int JGREG= 15 + 31*(10+12*1582);
		 double HALFSECOND = 0.5;

			   int year=ymd[0];
			   int month=ymd[1]; // jan=1, feb=2,...
			   int day=ymd[2];    
			   int julianYear = year;
			   if (year < 0) julianYear++;
			   int julianMonth = month;
			   if (month > 2) {
			     julianMonth++;
			   }
			   else {
			     julianYear--;
			     julianMonth += 13;
			   }
			   
			   double julian = (java.lang.Math.floor(365.25 * julianYear)
			        + java.lang.Math.floor(30.6001*julianMonth) + day + 1720995.0);
			   if (day + 31 * (month + 12 * year) >= JGREG) {
			     // change over to Gregorian calendar
			     int ja = (int)(0.01 * julianYear);
			     julian += 2 - ja + (0.25 * ja);
			   }
			   return java.lang.Math.floor(julian);
			 }
	  
	  
	  
	  public static boolean sendChargeFileToLocal(String password,String username,String chargeFileContent,String filename){
		  String newFileName = "/pub/data1/billing/CHARGE.txt";
		  
		  BufferedWriter bufferedWriter = null;
		  
		  boolean retValue =  false;
	        
	        try {
	            
	            //Construct the BufferedWriter object
	            bufferedWriter = new BufferedWriter(new FileWriter(newFileName));
	            
	            //Start writing to the output stream
	            bufferedWriter.write(chargeFileContent);
	            
	            
	        } catch (FileNotFoundException ex) {
	            ex.printStackTrace();
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        } finally {
	            //Close the BufferedWriter
	            try {
	                if (bufferedWriter != null) {
	                    bufferedWriter.flush();
	                    bufferedWriter.close();
	                    retValue=true;
	                    
	                }
	            } catch (IOException ex) {
	                ex.printStackTrace();
	            }
	        }
	        
	        return retValue;
	  }
	  
	 
	  
	  
	  
	  
	  public static boolean sendChargeFileToServer(JSONArray results,String password,String username,String chargeFileContent,String filename){
		  FTPSClient ftp=null;
	     /* String serverName = "adcom.ucsd.edu";
	      String pathname = "/SISP/ARD2502/LIBCIR/CHARGE/";
	      String newFileName = "'SISP.ARD2502.LIBCIR.CHARGE."+filename+"'";
	      
		 //======= Test FTP===============  
		  String serverName =  "dail.ucsd.edu";
	         String pathname = "/pub/data2/ftp/";
	        
	         //======= END Test FTP=============== */
		  String newFileName = "'SISP.ARD2502.LIBCIR.CHARGE."+filename+"'";
		  String serverName = null;
		  String pathname = null;
		  try{
	      InitialContext context = new InitialContext();
	      serverName =
	          (String)context.lookup("java:comp/env/billingServer/hostname");
	      pathname = 
	         	 (String)context.lookup("java:comp/env/billingServer/path");
	      log.info("$$$ serverName:"+serverName);
	      log.info("$$$ pathname:"+pathname);
	      pathname = pathname+"CHARGE";
	      log.info("$$$ pathnameMod:"+pathname);
		  }
		  catch(NamingException nee)
		  {
			  log.info("$$$ NamingException:"+nee);
		  }
	        // String newFileName = "CHARGES.txt";
	         String protocol = "SSL";
	         boolean retValue1 =  false;
	         log.info("$$$$$$ BEGIN  Sending Charge file :)");
	     /*============================+++++++++++++++++++++++++++=======================
		        try{
		        	 log.info("$$$ 1111111111111111");
		        	 ftp = new FTPSClient("SSL",false);
		        	 ftp.setRemoteVerificationEnabled(false);
		        	 log.info("$$$222222222");
		         }
			  	   catch(NoSuchAlgorithmException ne)
			  	   {
			  		 log.info("$$$$$ NoSuchAlgorithmException"); 
			  	   }
				try {
				   int reply;
				   log.info("$$$ BEFORE CONNECTING TO SERVER");
		           ftp.connect(serverName);
		           log.info("$$$ 333333333333333333");
		           log.info("Connected to " + serverName + ".");
		           reply = ftp.getReplyCode();
		           log.info("$$$ reply:"+reply);
		           ftp.execPBSZ(0);
		           ftp.execPROT("P");
		           ftp.login(username, password);
		           log.info("$$$ LOGGED TO THE SERVER");
		          
		         //  log.info("Logged in with the usernmae and password "+username+" "+password);
		           ftp.enterLocalPassiveMode(); 
		          // ftp.setDataTimeout(600000000);
		          
		           
		           if (!FTPReply.isPositiveCompletion(reply))
		           {
		               ftp.disconnect();
		               System.err.println("FTP server refused connection.");
		               System.exit(1);
		           }
		           ftp.changeToParentDirectory();
		           boolean flagg = ftp.changeWorkingDirectory(pathname);
		           log.info("Flaggg is:"+flagg);    
		           //ftp.execPBSZ( 0 );
		           //ftp.execPROT( "P" );

		           ftp.setFileType(FTP.ASCII_FILE_TYPE);
				}
				 catch (IOException e)
			        {
					 log.info("$$$$$ IO Exception in connecting/ logging to the server");
			        } 
				//=============++++++++++++++++++++++++++++++++++++==============================
				  
				  */
	            try {
						 ftp = new FTPSClient("SSL",false);
						 ftp.setRemoteVerificationEnabled(false);
					 try {
						 log.info("$$$ BEFORE CONNECTING TO SERVER");
						 ftp.connect(serverName);
						
						} catch (SocketException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						log.info("Connected to " + serverName + ".");
				        int  reply = ftp.getReplyCode();
				        log.info("$$$ reply:"+reply);
				        try {
							ftp.execPBSZ(0);
						} catch (SSLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				         try {
							ftp.execPROT("P");
						} catch (SSLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				        try {
							ftp.login(username, password);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						log.info("$$$ LOGGED TO THE SERVER");
				       
				       
						try{
						ftp.enterLocalPassiveMode(); 
						 ftp.changeToParentDirectory();
				           boolean flagg = ftp.changeWorkingDirectory(pathname);
				           log.info("Flaggg is:"+flagg);    
				           ftp.setFileType(FTP.ASCII_FILE_TYPE);
						} catch (IOException e)
				        {
							 log.info("$$$$$ IO Exception ");
					        } 
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 //{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{
				    String bufferChargeFile =  chargeFileContent;
				    
				    log.info("$$$$$ OUT content:"+bufferChargeFile);
			           try{ 
			        	
			            ByteArrayOutputStream htmlStream = new ByteArrayOutputStream();
			            log.info("$$$$$ 111111111111111111 $$$$$$$$$$$$$$$$$$$");
			            PrintWriter out = new PrintWriter(htmlStream);
			            log.info("$$$$$ 22222222222222222222 $$$$$$$$$$$$$$$$$$$");
			            out.write(bufferChargeFile);
			            log.info("$$$$$ 333333333333333333  $$$$$$$$$$$$$$$$$$$");
			            out.flush();
			            out.close();
						
			            
						 retValue1 =  ftp.storeFile(newFileName, new ByteArrayInputStream(htmlStream.toByteArray()));
						 log.info("$$$$$ 4444444444444444444  $$$$$$$$$$$$$$$$$$$");
						 log.info("$$$$$$$$$ RETVALUE1:"+retValue1);
				 
						 ftp.logout();
						 ftp.disconnect();
					}//end of try
					 catch (FTPConnectionClosedException e)
				        {
						      log.info("Server closed connection.");
				           
				        }
				        catch (IOException e)
				        {
				        	
				        	 log.info("IO Exception %%%%%%%%%%%");
				        }
				        finally
				        {
				            if (ftp.isConnected())
				            {
				                try
				                {
				                    ftp.disconnect();
				                }
				                catch (IOException f)
				                {
				                    // do nothing
				                }
				            }
				        }
				    
				        log.info("$$$$$$ END  Sending Charge file :)"+retValue1);
				return   retValue1;	    
				    
				    
	  }
	  
	  
	  
	  
	  public static boolean sendPersonFileToServer(JSONArray results,String password,String username,String filename){
		  
		    
	       /* String serverName = "adcom.ucsd.edu";
	        String pathname = "/SISP/ARD2502/LIBCIR/PERSON/";
	        String newFileName = "'SISP.ARD2502.LIBCIR.PERSON."+filename+"'";
	      
	       
		  //======= Test FTP=============== 
		  String serverName =  "dail.ucsd.edu";
	         String pathname = "/pub/data2/ftp/";
	       
	         //======= Test FTP===============*/  
		  	FTPSClient ftp=null;
		    String newFileName = "'SISP.ARD2502.LIBCIR.PERSON."+filename+"'";
		    String serverName = null;
			  String pathname = null;
			  try{
		      InitialContext context = new InitialContext();
		      serverName =
		          (String)context.lookup("java:comp/env/billingServer/hostname");
		      pathname = 
		         	 (String)context.lookup("java:comp/env/billingServer/path");
		      log.info("$$$ serverName:"+serverName);
		      log.info("$$$ pathname:"+pathname);
		      pathname = pathname+"PERSON";
		      log.info("$$$ pathnameMod:"+pathname);
			  }
			  catch(NamingException nee)
			  {
				  log.info("$$$ NamingException:"+nee);
			  }
	        // String newFileName = "PERSON.txt";
	         String protocol = "SSL";
	         boolean retValue1 =  false;
		         log.info("$$$$$$ BEGIN  Sending Person file :)");
			  	//************* 1/21/2010-************************************    
			  	/*  try{
			        	 ftp = new FTPSClient(protocol,false);
			        	 ftp.setRemoteVerificationEnabled(false); 
			         }
				  	   catch(NoSuchAlgorithmException ne)
				  	   {
				  		 log.info("$$$$$ NoSuchAlgorithmException"); 
				  	   }
			       // log.info("username and password:"+username+" "+password);
					try {
					   int reply;
			           ftp.connect(serverName);
			           log.info("Connected to " + serverName + ".");
			           ftp.login(username, password);
			           log.info("$$$ LOGGED IN TO THE SERVER");
			           //log.info("Logged in with the usernmae and password "+username+" "+password);
			           ftp.enterLocalPassiveMode(); 
			          // ftp.setDataTimeout(600000000);
			           reply = ftp.getReplyCode();
			           
			           if (!FTPReply.isPositiveCompletion(reply))
			           {
			               ftp.disconnect();
			               System.err.println("FTP server refused connection.");
			               System.exit(1);
			           }
			          
			           ftp.changeToParentDirectory();
			           boolean flagg = ftp.changeWorkingDirectory(pathname);
			           log.info("Flaggg is:"+flagg);    
			           ftp.execPBSZ( 0 );
			           ftp.execPROT( "P" );
			           ftp.setFileType(FTP.ASCII_FILE_TYPE);
					}
					 catch (IOException e)
				        {
						 log.info("$$$$$ IO Exception in connecting/ logging to the server");
				        } 
					 */
					 //******************* 1/21/2010**************************
		         try {
					 ftp = new FTPSClient("SSL",false);
					 ftp.setRemoteVerificationEnabled(false);
				 try {
					 log.info("$$$ BEFORE CONNECTING TO SERVER");
					 ftp.connect(serverName);
					
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					log.info("Connected to " + serverName + ".");
			        int  reply = ftp.getReplyCode();
			        log.info("$$$ reply:"+reply);
			        try {
						ftp.execPBSZ(0);
					} catch (SSLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			         try {
						ftp.execPROT("P");
					} catch (SSLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        try {
						ftp.login(username, password);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					log.info("$$$ LOGGED TO THE SERVER");
			       
			       
					try{
					ftp.enterLocalPassiveMode(); 
					 ftp.changeToParentDirectory();
			           boolean flagg = ftp.changeWorkingDirectory(pathname);
			           log.info("Flaggg is:"+flagg);    
			           ftp.setFileType(FTP.ASCII_FILE_TYPE);
					} catch (IOException e)
			        {
						 log.info("$$$$$ IO Exception ");
				        } 
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		         
		         
		         		String bufferPersonFile = generatePersonFileContent();
					     log.info("$$$$$ OUT content:"+bufferPersonFile);
				           try{ 
				            ByteArrayOutputStream htmlStream = new ByteArrayOutputStream();
				            log.info("$$$$$ 111111111111111111 $$$$$$$$$$$$$$$$$$$");
				            PrintWriter out = new PrintWriter(htmlStream);
				            log.info("$$$$$ 22222222222222222222 $$$$$$$$$$$$$$$$$$$");
				            out.write(bufferPersonFile);
				            log.info("$$$$$ 333333333333333333  $$$$$$$$$$$$$$$$$$$");
				            out.flush();
				            out.close();
							
				            
							 retValue1 =  ftp.storeFile(newFileName, new ByteArrayInputStream(htmlStream.toByteArray()));
							 log.info("$$$$$ 4444444444444444444  $$$$$$$$$$$$$$$$$$$");
							 log.info("$$$$$$$$$ RETVALUE1:"+retValue1);
					    
							 ftp.logout();
							 ftp.disconnect();
						}//end of try
						 catch (FTPConnectionClosedException e)
					        {
							      log.info("Server closed connection.");
					           
					        }
					        catch (IOException e)
					        {
					        	
					        	 log.info("IO Exception %%%%%%%%%%%");
					        }
					        finally
					        {
					            if (ftp.isConnected())
					            {
					                try
					                {
					                    ftp.disconnect();
					                }
					                catch (IOException f)
					                {
					                    // do nothing
					                }
					            }
					        }
					    
					        log.info("$$$$$$ END  SendingPerson file :)"+retValue1);
					return   retValue1;	    
		  
	  }
	    
	  
	  
  public static boolean sendEntityFileToServer(JSONArray results,String password,String username,String filename){
	  
	 
       /* String serverName = "adcom.ucsd.edu";
        String pathname = "/SISP/ARD2502/LIBCIR/ENTITY/";
        String newFileName = "'SISP.ARD2502.LIBCIR.ENTITY."+filename+"'";
       
     
       //======= Test FTP===============
	  String serverName =  "dail.ucsd.edu";
       String pathname = "/pub/data2/ftp/";
       
	  //======= END Test FTP===============  */
	  FTPSClient ftp=null;
	  String newFileName = "'SISP.ARD2502.LIBCIR.ENTITY."+filename+"'";
	  String serverName = null;
	  String pathname = null;
	  try{
      InitialContext context = new InitialContext();
      serverName =
          (String)context.lookup("java:comp/env/billingServer/hostname");
      pathname = 
         	 (String)context.lookup("java:comp/env/billingServer/path");
      log.info("$$$ serverName:"+serverName);
      log.info("$$$ pathname:"+pathname);
      pathname = pathname+"ENTITY";
      log.info("$$$ pathnameMod:"+pathname);
	  }
	  catch(NamingException nee)
	  {
		  log.info("$$$ NamingException:"+nee);
	  }
        
      //  String newFileName = "ENTITY.txt";     
        String protocol = "SSL";
         boolean retValue1 =  false;
         log.info("$$$$$$----- BEGIN  Sending Entity file ----------------");
         
         //********** 1/21/2010 *********************************
         /*
         try{
        	 ftp = new FTPSClient(protocol,false);
        	 ftp.setRemoteVerificationEnabled(false); 
         }
	  	   catch(NoSuchAlgorithmException ne)
	  	   {
	  		 log.info("$$$$$ NoSuchAlgorithmException"); 
	  	   }
			try {
				  	   
			   int reply;
	           ftp.connect(serverName);
	           log.info("Connected to " + serverName + ".");
	           ftp.login(username, password);
	           log.info("$$$ LOGGED IN TO THE SERVER");
	           //log.info("Logged in with the usernmae and password "+username+" "+password);
	           log.info("$$$ LOGGED IN TO THE SERVER");
	           ftp.enterLocalPassiveMode(); 
	          // ftp.setDataTimeout(600000000);
	           reply = ftp.getReplyCode();
	           
	           if (!FTPReply.isPositiveCompletion(reply))
	           {
	               ftp.disconnect();
	               System.err.println("FTP server refused connection.");
	               System.exit(1);
	           }
	           ftp.changeToParentDirectory();
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);    
	           ftp.execPBSZ(0);
	           ftp.execPROT("P");       
	           ftp.setFileType(FTP.ASCII_FILE_TYPE);
			}
			 catch (IOException e)
		        {
				 log.info("$$$$$ IO Exception in connecting/ logging to the server");
		        } 
			 */
			 //************ 1/21/2010*********************
         try {
			 ftp = new FTPSClient("SSL",false);
			 ftp.setRemoteVerificationEnabled(false);
		 try {
			 log.info("$$$ BEFORE CONNECTING TO SERVER");
			 ftp.connect(serverName);
			
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("Connected to " + serverName + ".");
	        int  reply = ftp.getReplyCode();
	        log.info("$$$ reply:"+reply);
	        try {
				ftp.execPBSZ(0);
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	         try {
				ftp.execPROT("P");
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				ftp.login(username, password);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("$$$ LOGGED TO THE SERVER");
	       
	       
			try{
			ftp.enterLocalPassiveMode(); 
			 ftp.changeToParentDirectory();
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);    
	           ftp.setFileType(FTP.ASCII_FILE_TYPE);
			} catch (IOException e)
	        {
				 log.info("$$$$$ IO Exception ");
		        } 
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	            String bufferEntityFile = generateEntityFileContent();
	            
			    log.info("$$$$$ OUT content:"+bufferEntityFile);
		           try{ 
		            ByteArrayOutputStream htmlStream = new ByteArrayOutputStream();
		            log.info("$$$$$ 111111111111111111 $$$$$$$$$$$$$$$$$$$");
		            PrintWriter out = new PrintWriter(htmlStream);
		            log.info("$$$$$ 22222222222222222222 $$$$$$$$$$$$$$$$$$$");
		            out.write(bufferEntityFile);
		            log.info("$$$$$ 333333333333333333  $$$$$$$$$$$$$$$$$$$");
		            out.flush();
		            out.close();
					
		            
					 retValue1 =  ftp.storeFile(newFileName, new ByteArrayInputStream(htmlStream.toByteArray()));
					 log.info("$$$$$ 4444444444444444444  $$$$$$$$$$$$$$$$$$$");
					 log.info("$$$$$$$$$ RETVALUE1:"+retValue1);
			    
					 ftp.logout();
					 ftp.disconnect();
				}//end of try
				 catch (FTPConnectionClosedException e)
			        {
					      log.info("Server closed connection.");
			           
			        }
			        catch (IOException e)
			        {
			        	
			        	 log.info("IO Exception %%%%%%%%%%%");
			        }
			        finally
			        {
			            if (ftp.isConnected())
			            {
			                try
			                {
			                    ftp.disconnect();
			                }
			                catch (IOException f)
			                {
			                    // do nothing
			                }
			            }
			        }
			    
			        log.info("$$$$$$ END  Sending  Entity file "+retValue1);
			return   retValue1;	    
		  
	  }	  
	  
	  
	  
  public static boolean deleteChargeFileFromServer(String password,String username){
	  FTPSClient ftp=null;
     /*String serverName = "adcom.ucsd.edu";
      String pathname = "/SISP/ARD2502/LIBCIR/CHARGE/";
      */
	  Calendar cal = Calendar.getInstance();
	    int day = cal.get(Calendar.DATE);
      int month = cal.get(Calendar.MONTH) + 1;
      int year = cal.get(Calendar.YEAR);
      log.info("YEAR:"+year);
      log.info("month:"+month);
      log.info("day:"+day);
  										 
   double date1 = toJulian(new int[]{year,month,day});
      double date2 = toJulian(new int[]{year,1,1});
      int dif = (int) (date1-date2+1);
      log.info("dif: " + dif + " days.");
   
      String strYear =""+year;
      String strDiff =""+dif;
      for(int i=strDiff.length(); i < 3 ;i++)
       {
       	strDiff = "0"+strDiff;
       }
       
      String filename = "D"+strYear.substring(2)+strDiff;
      log.info("File name: " + filename);  
      String newFileName = "'SISP.ARD2502.LIBCIR.CHARGE."+filename+"'";
     /*
	  String serverName =  "dail.ucsd.edu";
      String pathname = "/pub/data2/ftp/"; */
      // String newFileName = "CHARGE.txt";
       String serverName = null;
 	  String pathname = null;
 	  try{
       InitialContext context = new InitialContext();
       serverName =
           (String)context.lookup("java:comp/env/billingServer/hostname");
           pathname = 
          	 (String)context.lookup("java:comp/env/billingServer/path");
       log.info("$$$ serverName:"+serverName);
       log.info("$$$ pathname:"+pathname);
       pathname = pathname+"CHARGE";
       log.info("$$$ pathnameMod:"+pathname);
       
 	  }
 	  catch(NamingException nee)
 	  {
 		  log.info("$$$ NamingException:"+nee);
 	  }
       
      String protocol = "SSL";
	  boolean retValue1 =  false;
         log.info("$$$$$$ BEGIN  Deleting Charge file :)");
	  	 /* 
	        try{
	        	 ftp = new FTPSClient(protocol,false);
	        	 ftp.setRemoteVerificationEnabled(false); 
	         }
		  	   catch(NoSuchAlgorithmException ne)
		  	   {
		  		 log.info("$$$$$ NoSuchAlgorithmException"); 
		  	   }
			try {
			   int reply;
	           ftp.connect(serverName);
	           log.info("Connected to " + serverName + ".");
	           ftp.login(username, password);
	           log.info("$$$ LOGGED IN TO THE SERVER");
	         //  log.info("Logged in with the usernmae and password "+username+" "+password);
	           ftp.enterLocalPassiveMode(); 
	          // ftp.setDataTimeout(600000000);
	           reply = ftp.getReplyCode();
	           
	           if (!FTPReply.isPositiveCompletion(reply))
	           {
	               ftp.disconnect();
	               System.err.println("FTP server refused connection.");
	               System.exit(1);
	           }
	           ftp.changeToParentDirectory();
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);    
	           ftp.execPBSZ( 0 );
	           ftp.execPROT( "P" );       
	           ftp.setFileType(FTP.ASCII_FILE_TYPE);
			}
			 catch (IOException e)
		        {
				 log.info("$$$$$ IO Exception in connecting/ logging to the server");
		        } 
			 */
         //==== new========
         try {
			 ftp = new FTPSClient("SSL",false);
			 ftp.setRemoteVerificationEnabled(false);
		 try {
			 log.info("$$$ BEFORE CONNECTING TO SERVER");
			 ftp.connect(serverName);
			
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("Connected to " + serverName + ".");
	        int  reply = ftp.getReplyCode();
	        log.info("$$$ reply:"+reply);
	        try {
				ftp.execPBSZ(0);
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	         try {
				ftp.execPROT("P");
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				ftp.login(username, password);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("$$$ LOGGED TO THE SERVER");
	       
	       
			try{
			ftp.enterLocalPassiveMode(); 
			 ftp.changeToParentDirectory();
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);    
	           ftp.setFileType(FTP.ASCII_FILE_TYPE);
			} catch (IOException e)
	        {
				 log.info("$$$$$ IO Exception ");
		        } 
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
         
         //======================
			  try{
				    retValue1 = ftp.deleteFile(newFileName) ;
					 ftp.logout();
					 ftp.disconnect();
				}//end of try
				 catch (FTPConnectionClosedException e)
			        {
					      log.info("Server closed connection.");
			           
			        }
			        catch (IOException e)
			        {
			        	
			        	 log.info("IO Exception %%%%%%%%%%%");
			        }
			        finally
			        {
			            if (ftp.isConnected())
			            {
			                try
			                {
			                    ftp.disconnect();
			                }
			                catch (IOException f)
			                {
			                    // do nothing
			                }
			            }
			        }
			    
			        log.info("$$$$$$ END  deleting Charge file :)"+retValue1);
			return   retValue1;	    
			    
			    
  }
  
  public static boolean deletePersonFileFromServer(String password,String username){
	  FTPSClient ftp=null;
	 
      /*String serverName = "adcom.ucsd.edu";
      String pathname = "/SISP/ARD2502/LIBCIR/PERSON/";
       */
	  Calendar cal = Calendar.getInstance();
	    int day = cal.get(Calendar.DATE);
      int month = cal.get(Calendar.MONTH) + 1;
      int year = cal.get(Calendar.YEAR);     			 
      double date1 = toJulian(new int[]{year,month,day});
      double date2 = toJulian(new int[]{year,1,1});
      int dif = (int) (date1-date2+1);
      log.info("dif: " + dif + " days.");   
      String strYear =""+year;
      String strDiff =""+dif;
      for(int i=strDiff.length(); i < 3 ;i++)
       {
       	strDiff = "0"+strDiff;
       }
       
      String filename = "D"+strYear.substring(2)+strDiff;
      log.info("File name: " + filename);  
      String newFileName = "'SISP.ARD2502.LIBCIR.PERSON."+filename+"'";
      /* 
      String serverName =  "dail.ucsd.edu";
      String pathname = "/pub/data2/ftp/";
      */
	  String serverName = null;
	  String pathname = null;
	  try{
      InitialContext context = new InitialContext();
      serverName =
          (String)context.lookup("java:comp/env/billingServer/hostname");
      pathname = 
         	 (String)context.lookup("java:comp/env/billingServer/path");
      log.info("$$$ serverName:"+serverName);
      log.info("$$$ pathname:"+pathname);
      pathname = pathname+"PERSON";
      log.info("$$$ pathnameMod:"+pathname);
	  }
	  catch(NamingException nee)
	  {
		  log.info("$$$ NamingException:"+nee);
	  }
       //String newFileName = "PERSON.txt";
      String protocol = "SSL";
	  boolean retValue1 =  false;
         log.info("$$$$$$ BEGIN  Deleting Person file :)");
	  /*	  
	        try{
	        	 ftp = new FTPSClient(protocol,false);
	        	 ftp.setRemoteVerificationEnabled(false); 
	         }
		  	   catch(NoSuchAlgorithmException ne)
		  	   {
		  		 log.info("$$$$$ NoSuchAlgorithmException"); 
		  	   }
			try {
			   int reply;
	           ftp.connect(serverName);
	           log.info("Connected to " + serverName + ".");
	           ftp.login(username, password);
	           log.info("$$$ LOGGED IN TO THE SERVER");
	         //  log.info("Logged in with the usernmae and password "+username+" "+password);
	           ftp.enterLocalPassiveMode(); 
	          // ftp.setDataTimeout(600000000);
	           reply = ftp.getReplyCode();
	           
	           if (!FTPReply.isPositiveCompletion(reply))
	           {
	               ftp.disconnect();
	               System.err.println("FTP server refused connection.");
	               System.exit(1);
	           }
	           ftp.changeToParentDirectory();
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);    
	           ftp.execPBSZ( 0 );
	           ftp.execPROT( "P" );        
	           ftp.setFileType(FTP.ASCII_FILE_TYPE);
			}
			 catch (IOException e)
		        {
				 log.info("$$$$$ IO Exception in connecting/ logging to the server");
		        } 
			*/
         try {
			 ftp = new FTPSClient("SSL",false);
			 ftp.setRemoteVerificationEnabled(false);
		 try {
			 log.info("$$$ BEFORE CONNECTING TO SERVER");
			 ftp.connect(serverName);
			
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("Connected to " + serverName + ".");
	        int  reply = ftp.getReplyCode();
	        log.info("$$$ reply:"+reply);
	        try {
				ftp.execPBSZ(0);
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	         try {
				ftp.execPROT("P");
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				ftp.login(username, password);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("$$$ LOGGED TO THE SERVER");
	       
	       
			try{
			ftp.enterLocalPassiveMode(); 
			 ftp.changeToParentDirectory();
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);    
	           ftp.setFileType(FTP.ASCII_FILE_TYPE);
			} catch (IOException e)
	        {
				 log.info("$$$$$ IO Exception ");
		        } 
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
			  try{
				    retValue1 = ftp.deleteFile(newFileName) ;
				    log.info("$$$$$ ret value from delete Person file: "+retValue1);
					 ftp.logout();
					 ftp.disconnect();
				}//end of try
				 catch (FTPConnectionClosedException e)
			        {
					      log.info("Server closed connection.");
			           
			        }
			        catch (IOException e)
			        {
			        	
			        	 log.info("IO Exception %%%%%%%%%%%");
			        }
			        finally
			        {
			            if (ftp.isConnected())
			            {
			                try
			                {
			                    ftp.disconnect();
			                }
			                catch (IOException f)
			                {
			                    // do nothing
			                }
			            }
			        }
			    
			        log.info("$$$$$$ END  deleting Charge file :)"+retValue1);
			return   retValue1;	    
			    
			    
  }
  
  public static boolean deleteEntityFileFromServer(String password,String username){
	  FTPSClient ftp=null;
     /* String serverName = "adcom.ucsd.edu";
      String pathname = "/SISP/ARD2502/LIBCIR/ENTITY/";
      */
	  Calendar cal = Calendar.getInstance();
	  int day = cal.get(Calendar.DATE);
      int month = cal.get(Calendar.MONTH) + 1;
      int year = cal.get(Calendar.YEAR); 		 
      double date1 = toJulian(new int[]{year,month,day});
      double date2 = toJulian(new int[]{year,1,1});
      int dif = (int) (date1-date2+1);
      log.info("dif: " + dif + " days.");   
      String strYear =""+year;
      String strDiff =""+dif;
      for(int i=strDiff.length(); i < 3 ;i++)
       {
       	strDiff = "0"+strDiff;
       }
       
      String filename = "D"+strYear.substring(2)+strDiff;
      log.info("File name: " + filename);  
      String newFileName = "'SISP.ARD2502.LIBCIR.ENTITY."+filename+"'";
     
     /* String serverName =  "dail.ucsd.edu";
      String pathname = "/pub/data2/ftp/"; */
      // String newFileName = "ENTITY.txt";
       
       String serverName = null;
 	  String pathname = null;
 	  try{
       InitialContext context = new InitialContext();
       serverName =
       (String)context.lookup("java:comp/env/billingServer/hostname");
       pathname = 
      	 (String)context.lookup("java:comp/env/billingServer/path");
       log.info("$$$ serverName:"+serverName);
       log.info("$$$ pathname:"+pathname);
       pathname = pathname+"ENTITY";
       log.info("$$$ pathnameMod:"+pathname);
 	  }
 	  catch(NamingException nee)
 	  {
 		  log.info("$$$ NamingException:"+nee);
 	  }
       
       
      String protocol = "SSL";
	  boolean retValue1 =  false;
         log.info("$$$$$$ BEGIN  Deleting Entity file :)");
	  /*	  
	        try{
	        	 ftp = new FTPSClient(protocol,false);
	        	 ftp.setRemoteVerificationEnabled(false); 
	         }
		  	   catch(NoSuchAlgorithmException ne)
		  	   {
		  		 log.info("$$$$$ NoSuchAlgorithmException"); 
		  	   }
			try {
			   int reply;
	           ftp.connect(serverName);
	           log.info("Connected to " + serverName + ".");
	           ftp.login(username, password);
	           log.info("$$$ LOGGED IN TO THE SERVER");
	         //  log.info("Logged in with the usernmae and password "+username+" "+password);
	           ftp.enterLocalPassiveMode(); 
	          // ftp.setDataTimeout(600000000);
	           reply = ftp.getReplyCode();
	           
	           if (!FTPReply.isPositiveCompletion(reply))
	           {
	               ftp.disconnect();
	               System.err.println("FTP server refused connection.");
	               System.exit(1);
	           }
	           ftp.changeToParentDirectory();
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);    
	           ftp.execPBSZ( 0 );
	           ftp.execPROT( "P" );    
	           ftp.setFileType(FTP.ASCII_FILE_TYPE);
			}
			 catch (IOException e)
		        {
				 log.info("$$$$$ IO Exception in connecting/ logging to the server");
		        } 
*/
         try {
			 ftp = new FTPSClient("SSL",false);
			 ftp.setRemoteVerificationEnabled(false);
		 try {
			 log.info("$$$ BEFORE CONNECTING TO SERVER");
			 ftp.connect(serverName);
			
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("Connected to " + serverName + ".");
	        int  reply = ftp.getReplyCode();
	        log.info("$$$ reply:"+reply);
	        try {
				ftp.execPBSZ(0);
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	         try {
				ftp.execPROT("P");
			} catch (SSLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				ftp.login(username, password);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("$$$ LOGGED TO THE SERVER");
	       
	       
			try{
			ftp.enterLocalPassiveMode(); 
			 ftp.changeToParentDirectory();
	           boolean flagg = ftp.changeWorkingDirectory(pathname);
	           log.info("Flaggg is:"+flagg);    
	           ftp.setFileType(FTP.ASCII_FILE_TYPE);
			} catch (IOException e)
	        {
				 log.info("$$$$$ IO Exception ");
		        } 
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
			  try{
				    retValue1 = ftp.deleteFile(newFileName) ;
				    log.info("$$$$$ ret val delealeting entity file: "+retValue1);
					 ftp.logout();
					 ftp.disconnect();
				}//end of try
				 catch (FTPConnectionClosedException e)
			        {
					      log.info("Server closed connection.");
			           
			        }
			        catch (IOException e)
			        {
			        	
			        	 log.info("IO Exception %%%%%%%%%%%");
			        }
			        finally
			        {
			            if (ftp.isConnected())
			            {
			                try
			                {
			                    ftp.disconnect();
			                }
			                catch (IOException f)
			                {
			                    // do nothing
			                }
			            }
			        }
			    
			        log.info("$$$$$$ END  deleting Entity file :)"+retValue1);
			return   retValue1;	    
			    
			    
  }
	  
	  
	  
	  
	
}//end of class