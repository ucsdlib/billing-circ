package edu.ucsd.library.billing;

import java.io.IOException;
import java.text.NumberFormat;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.text.DecimalFormat;

public class BillingUtility {
	private static Logger log = Logger.getLogger( BillingUtility .class );
	private static final String lineSeparator =	System.getProperty("line.separator");

	
public static boolean processBillingData(HttpServletRequest request,HttpServletResponse response, JSONArray results){
		String fileName = "CHARGES.txt";
		String actioncode_pid = null;
		String detailCode = "LIB"; // Rule # 2.1
		String invoiceNumber = null;
		String standardText = "LIBRARY ITEM";
		String itemBarcode = null;
		boolean twoLines = false;
		boolean isPositiveAmt = false;
		String fifthChar = null;
		String finalAmount = null;
	    DateFormat shortDf = DateFormat.getDateInstance(DateFormat.SHORT);
		response.setContentType("text/plain");
		response.setHeader("Content-Disposition", "attachment;filename="+fileName);
		response.setHeader("Cache-Control", "no-store,no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		JSONArray rows = new JSONArray();
		rows = results;
		int noOfRecords = 0;
		double totalCharges = 0;
		double newAmt = 0;
		String todayStr = shortDf.format(new Date());
		log.info("BILLING UTILITY todayStr:"+ todayStr);
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
		//String today = "0"+todayStr.substring(0,1)+todayStr.substring(2,4)+todayStr.substring(5);
		String today = fp+sp+tp;
		log.info("BILLING UTILITY today:"+ today);
		log.info("BILLING UTILITY SIZE OF ARRAY:"+ rows.size());
		try {
			//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			//header
			PrintWriter out = new PrintWriter(response.getOutputStream());
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
           	 char c2 = loc.charAt(1);

           	 if (c == 'c' && c2 == 'c'){
		          c = 'z';
	           }

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
					   int pp= s.indexOf(".");
					   String sTemp= s.substring(pp+1);
					   if(sTemp.length()< 2)
					   {
						   //that menas only one deciamal point
						   s = s + "0";
						   log.info("BILLING UTILITY inside <:"+s);
					   }
					  				   
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
						   DecimalFormat twoDForm = new DecimalFormat("#.##");
						   double newRemainingAmt= Double.valueOf(twoDForm.format(remainingAmt));
						   String s = Double.toString(newRemainingAmt);
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
			log.info("totalCharges New :"+totalCharges);
			out.append("CTRL "+getRecordCount(noOfRecords+2)+" "+getTotalCharges(totalCharges));
			
			
			//chargeBuffer =out.toString();
			out.flush();
			
			
			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			/*
			PrintWriter out = new PrintWriter(response.getOutputStream());
			//header
			out.write("CHDR CLIBCIRC.CHARGE"+" "+today+" "+today+" "+"000001");
			out.write("\r\n");
			for(int i = 0; i < rows.size(); i++){
				JSONObject row = (JSONObject)rows.get(i);
				// Get the pid and append "A" at the begining to represent action code
				String pidd =((String)row.get("pid")).trim();
				actioncode_pid ="AA"+pidd.substring(1);
				log.info("BILLING UTILITY actioncode_pid:"+actioncode_pid);
				String chargeType = ((String)row.get("chargeType")).trim();
				//if (chargeType.equalsIgnoreCase("LOST") && chargeType.equalsIgnoreCase("REPLACEMENT"))
					//twoLines = true;
				
			// ==========Rule 2.2=============================
				String loc = ((String)row.get("loc")).trim(); 
				log.info("BILLING UTILITY loc:"+loc);
				char c = loc.charAt(0);
				String fourthChar = getFourthCharacter(c);
				//detailCode += fourthChar;
			// ==========Rule 2.3========================== 
				
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
				//==============Rule 2.4============================ 
				
				String patronType = ((String)row.get("patronType")).trim(); 
				log.info("BILLING UTILITY patronType:"+patronType);
				int patron = Integer.parseInt(patronType);
				String sixthChar = getSixthCharacter(patron);
				//detailCode += sixthChar
				 //============================
				 // * Caluclating the amout
				 //============================
				  
				 String amount1 = ((String)row.get("amount1")).trim(); 
				 String amount2= ((String)row.get("amount2")).trim(); 
				 String amount3 = ((String)row.get("amount3")).trim(); 
				  double total = Integer.parseInt(amount1)+ Integer.parseInt(amount2)+ Integer.parseInt(amount3);
				  String amt = ""+total;
				  log.info("BILLING UTILITY amt:"+amt);
				  int t= amt.indexOf(".");
				  String tempp= amt.substring(0,t);
				  log.info("BILLING UTILITY tempp:"+tempp);
				  String amtStr = tempp.substring(0,tempp.length()-2)+"."+tempp.substring(tempp.length()-1);
				  log.info("BILLING UTILITY amtStr:"+amtStr);
				  newAmt = Double.parseDouble(amtStr);
				  totalCharges += newAmt;
				  log.info("BILLING UTILITY newAmt:"+newAmt);
				   if (newAmt < 0)
				   { // handle for negative numbers
					   newAmt = newAmt - (newAmt *2);
					   String s = Double.toString(newAmt);
					   int pp= s.indexOf(".");
					   String sTemp= s.substring(pp+1);
					   if(sTemp.length()< 2)
					   {
						   //that menas only one deciamal point
						   s = s + "0";
						   log.info("BILLING UTILITY inside <:"+s);
					   }
					  				   
					  //newAmt = newAmt - (newAmt *2);
					  	//String s = Double.toString(newAmt);
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
					   String s = Double.toString(newAmt);
					   int w= s.indexOf(".");
					   String sTemp2= s.substring(w+1);
					   if(sTemp2.length()< 2)
					   {
						   //that menas only one deciamal point
						   s = s + "0";
						   log.info("BILLING UTILITY inside <:"+s);
					   }
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
				  // get the invoice number 
				   invoiceNumber = ((String)row.get("invoiceNo")).trim();
				   log.info("BILLING UTILITY invoiceNumber:"+invoiceNumber);
				  //get item barcode 
				   String barcode  = ((String)row.get("itemBarcode")).trim();
				   log.info("BILLING UTILITY barcode:"+barcode);
				   if (barcode.length() > 1)
						   {
					   itemBarcode = barcode.substring(1);
					   log.info("BILLING UTILITY itemBarcode in if:"+itemBarcode);
						   }
				   else
				   {
					   itemBarcode = "      ";
					   log.info("BILLING UTILITY itemBarcode in else:"+itemBarcode);
				   }
				   
				  // =========================================
				   //*  Wrting the file - first write for the record which has
				   //  charge type MANUAL,OVERDUE,OVERDUEX
				    //  ========================================
				   
				  
				   if(! twoLines) {
					   log.info("BILLING UTILITY start of if");
					   out.write(actioncode_pid);
					   for(int space = 0; space < 35; space++){
							out.write(" "); //10 spaces
						}
					   detailCode += fourthChar + fifthChar + sixthChar + finalAmount;
					   log.info("DEBUG: fourthChar in !twoLines:"+fourthChar);
					   log.info("DEBUG: fifthChar in !twoLines:"+fifthChar);
					   log.info("DEBUG: sixthChar in !twoLines:"+sixthChar);
					   log.info("DEBUG: finalAmount in !twoLines:"+finalAmount);
					   
					   out.write(detailCode);
					   detailCode = "LIB";
					   for(int space = 0; space < 6; space++){
							out.write(" "); //5 spaces
						}     
					   out.write (invoiceNumber);
					   for(int space = 0; space < 8; space++){
							out.write(" "); //5 spaces
						} 
					   out.write (standardText);
					   log.info("BILLING UTILITY standardText:"+standardText);
					   out.write(" ");
					   out.write (itemBarcode);
					   log.info("BILLING UTILITY itemBarcode:"+itemBarcode);
					   out.write("\r\n");
					  // out.write("\r\n"); 
					  // out.write("\r\n"); 
					   log.info("BILLING UTILITY end of if");
					   noOfRecords++;
				   }
				   else
				   {
					   if(isPositiveAmt)
					   {
						   log.info("BILLING UTILITY start of else");
						   out.write(actioncode_pid);
						   for(int space = 0; space < 35; space++){
								out.write(" "); //35 spaces
							}
						   detailCode += fourthChar + "F" + sixthChar + "0000000070{";
						   log.info("BILLING UTILITY detailCode ELSE:"+detailCode);
						   out.write(detailCode);
						   detailCode = "LIB";
						   for(int space = 0; space < 6; space++){
								out.write(" "); //5 spaces
							}     
						   out.write (invoiceNumber);
						   for(int space = 0; space < 8; space++){
								out.write(" "); //5 spaces
							} 
						   out.write (standardText);
						   log.info("BILLING UTILITY standardText:"+standardText);
						   out.write(" ");
						   out.write (itemBarcode);
						   log.info("BILLING UTILITY itemBarcode:"+itemBarcode);
						   out.write("\r\n"); 
						  // out.write("\r\n"); 
						   //out.write("\r\n"); 
						   
						   out.write(actioncode_pid);
						   log.info("BILLING UTILITY actioncode_pid:"+actioncode_pid);
						   for(int space = 0; space < 35; space++){
								out.write(" "); //10 spaces
							}
						   //-------------------
						   double remainingAmt = newAmt - 7;
						   String s = Double.toString(remainingAmt);
						   int w= s.indexOf(".");
						   String sTemp2= s.substring(w+1);
						   if(sTemp2.length()< 2)
						   {
							   //that menas only one deciamal point
							   s = s + "0";
							   log.info("BILLING UTILITY inside <:"+s);
						   }
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
						   out.write(detailCode);
						   detailCode = "LIB";
						   log.info("BILLING UTILITY detailCode R:"+detailCode);
						   for(int space = 0; space < 6; space++){
								out.write(" "); //5 spaces
							}     
						   out.write (invoiceNumber);
						   for(int space = 0; space < 8; space++){
								out.write(" "); //5 spaces
							} 
						   out.write (standardText);
						   log.info("BILLING UTILITY standardText R:"+standardText);
						   out.write(" ");
						   out.write (itemBarcode);
						   log.info("BILLING UTILITY itemBarcode:"+itemBarcode);
						   
						   out.write("\r\n"); 
						  // out.write("\r\n"); 
						  // out.write("\r\n"); 
						   log.info("BILLING UTILITY end of else");
						   noOfRecords = noOfRecords + 2;
					   }
					   else
					   { 
					   out.write(actioncode_pid);
					   log.info("BILLING UTILITY actioncode_pid:"+actioncode_pid);
					   for(int space = 0; space < 35; space++){
							out.write(" "); //10 spaces
						}
					   detailCode += fourthChar + "R" + sixthChar + finalAmount;
					   out.write(detailCode);
					   detailCode = "LIB";
					   log.info("BILLING UTILITY detailCode R:"+detailCode);
					   for(int space = 0; space < 6; space++){
							out.write(" "); //5 spaces
						}     
					   out.write (invoiceNumber);
					   for(int space = 0; space < 8; space++){
							out.write(" "); //5 spaces
						} 
					   out.write (standardText);
					   log.info("BILLING UTILITY standardText R:"+standardText);
					   out.write(" ");
					   out.write (itemBarcode);
					   log.info("BILLING UTILITY itemBarcode:"+itemBarcode);
					   
					   out.write("\r\n"); 
					  // out.write("\r\n"); 
					 //  out.write("\r\n"); 
					   log.info("BILLING UTILITY end of else");
					   noOfRecords++;
						   
					   }
					   
				   }
				 twoLines = false;
				 isPositiveAmt = false;
			}//end of for loop
			out.write("CTRL "+getRecordCount(noOfRecords+2)+" "+getTotalCharges(totalCharges));
			out.flush();
			
			
			*/
		} catch (IOException e) {
			log.error("Unable to generate report file", e);
			log.info("BILLING UTILITY ERROR");
			return false;
		}
		log.info("BILLING UTILITY RETURNIN TRUE ");
		return true;
	}

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
	case 'z': code = "T";
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
/*  		totalCharges = totalCharges - (totalCharges *2);
  		log.info("totalCharges :"+totalCharges);
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		totalCharges = Double.valueOf(twoDForm.format(totalCharges));
		log.info("totalCharges New :"+totalCharges);
  		String str = ""+ totalCharges;
  		log.info("TOTAL: ="+str);
  		int p= str.indexOf(".");
  		String s2= str.substring(0,p)+ str.substring(p+1);
  		int len = s2.length();
  		finalString = s2;
  		for(int i =0 ;i <10 -len; i++ )
  		{
  			finalString = "0"+finalString; 
  			
  		}
  		finalString += "}";
  	}
  	
  	else
  	{
  		log.info("totalCharges :"+totalCharges);
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		totalCharges = Double.valueOf(twoDForm.format(totalCharges));
		log.info("totalCharges New :"+totalCharges);
  		String str = ""+ totalCharges;
  		log.info("TOTAL: ="+str);
  		int p= str.indexOf(".");
  		String s2= str.substring(0,p)+ str.substring(p+1);
  		int len = s2.length();
  		finalString = s2;
  		for(int i =0 ;i <10 -len; i++ )
  		{
  			finalString = "0"+finalString; 
  			
  		}
  		finalString += "{";
  	}*/
  	
  		totalCharges = totalCharges - (totalCharges *2);
  		log.info("totalCharges :"+totalCharges);
		
  		DecimalFormat twoDForm = new DecimalFormat("#.##");
		totalCharges = Double.valueOf(twoDForm.format(totalCharges));
		log.info("totalCharges New :"+totalCharges);
  	   String str = ""+ totalCharges;
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
  		log.info("TOTAL old: ="+str);
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
}