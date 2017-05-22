package edu.ucsd.library.billing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import javax.naming.NamingException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import edu.ucsd.library.util.sql.ConnectionManager;



public class ProcessFile {
	private static Logger log = Logger.getLogger( ProcessFile.class );
	private InputStream stream = null;
	private Vector v = new Vector();
	private Vector vv = new Vector();
	static int vectorcount;
	private JSONArray probQueue = new JSONArray();
	private JSONArray newPatronQueue = new JSONArray();
	private JSONArray pendingQueue = new JSONArray();
	private JSONArray rawData = new JSONArray();
	private JSONArray dupRecords = new JSONArray();
	private JSONArray restRecords = new JSONArray();
	private JSONArray ja = new JSONArray();
	private JSONArray ja1 = new JSONArray();
	private String rule = "";


	public ProcessFile(InputStream stream) {
		this.stream = stream;

	}

	public void processBillingFile() throws IOException {

		String sarr[] = new String[3];
		String invoiceid = null;
		String date = null;
		String loc = null;
		String patronRecordNo = null;
		String pid = null;
		String name = null;
		String address = null;
		String pcode1 = null;
		String pcode2 = null;
		String patronAffliation = null;
		String patronType = null;
		String itemBarcode = null;
		String title = null;
		String callNo = null;
		String chargeType = null;
		String amount1 = null;
		String amount2 = null;
		String amount3 = null;
		String other = null;


		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String text = "";
		int i = 0;
		while ((text = reader.readLine()) != null) {
			log.info("Current billing record: "+text);
			rule = ""; //reset rule string
			if (i == 0) {
				i++;
				continue;
			} else {
				i++;
				//StringTokenizer tokenizer = new StringTokenizer(text, "|");
				//int tokencount = tokenizer.countTokens();
				String [] temp = text.split("\\|");
				int tokencount= temp.length;
				log.info("$$$$ token count is $$$ :"+tokencount);
				boolean newUser = false; //if there is an underscore in the PID entry
				boolean invalidPatron =false; // if invalid patron Rule #1
				for (int j = 0; j < tokencount; j++) {
					String ss = temp[j];
					log.info("Current record token: "+ss);
					// store Invoice ID
					if (j == 0){
						sarr[0] = ss;
						invoiceid = ss;
					}
					if (j == 1)
					{
						date =ss;
					}
					// store loc
					if (j == 2){
						loc = ss;
					}
					// store patron record ID
					if (j == 3){
						patronRecordNo = ss;
					}

					// store pid
					if (j == 4){
						//check PID, if an underscore, put into New Patron queue and continue
						if(ss.equals("_")){
							newUser = true;
							pid = ss; //break;
						}
						else {
							pid = ss.substring(1);
							sarr[1] = pid;
						}

					}
					if (j == 5)
					{
						name = ss;
					}
					if (j == 6)
					{
						address =ss;
					}
					if (j == 7)
					{
						pcode1 = ss;
					}
					if (j == 8){
						pcode2 = ss;
					}

					// get Affliation code
					if (j == 9){
						patronAffliation = ss;
					}
					// store patron type
					if (j == 10) {
						patronType = ss;
					//check Patron type if its 0,12,13,14,15,18,20,21,36 we do not import.
						boolean patronInvalid = checkInvalidPatronType(Integer.parseInt(patronType));
						if(patronInvalid)
						{invalidPatron = true;//break;}
						continue;
						}
					}
					if (j == 11){
						itemBarcode = ss;
					}
					if (j == 12){
						title = ss;
					}
					if (j == 13){
						callNo = ss;
					}
					if (j == 14) {
						// store charge type
						chargeType = ss;
						sarr[2] = chargeType;

					}
					if (j == 15){
						amount1 = ss;
					}
					if (j == 16){
						amount2 = ss;
					}
					if (j == 17){
						amount3 = ss;
					}
					log.info("j is:"+ j);
				}// end for
				log.info("111111111111111111111111111111");
				/*=================================
				 * Add all the transaction lines to the rawData
				 * Json array and processing of the rules will be
				 * done later
				 * ==================================
				 */
				String newAmtt1=  formatAmount(amount1);
				String newAmtt2=  formatAmount(amount2);
				String newAmtt3=  formatAmount(amount3);
				log.info("newAmtt1:"+newAmtt1);
				log.info("newAmtt2:"+newAmtt2);
				log.info("newAmtt3:"+newAmtt3);
			
	            if((newUser) && (!(invalidPatron ))){
                    JSONObject js1 = new JSONObject();
                    //js1.put("record", text);
                    js1.put("invoiceNo", invoiceid);
                    js1.put("date", date);
                    js1.put("loc", loc);
                    js1.put("patronRecordNo", patronRecordNo);
                    js1.put("pid", pid);
                    js1.put("name", name);
                    js1.put("address", address);
                    js1.put("pcode1", pcode1);
                    js1.put("pcode2", pcode2);
                    js1.put("patronAffliation", patronAffliation);
                    js1.put("patronType", patronType);
                    js1.put("itemBarcode", itemBarcode);
                    js1.put("title", title);
                    js1.put("callNo", callNo);
                    js1.put("chargeType", chargeType);
                    js1.put("amount1", newAmtt1);
                    js1.put("amount2",newAmtt2);
                    js1.put("amount3", newAmtt3);
                    js1.put("rule", "PID was underscore");
                    newPatronQueue.add(js1);
                    js1 = null;

                      }


                if(!(invalidPatron ) && (!newUser)){

				JSONObject obj = new JSONObject();
				obj.put("invoiceNo", invoiceid);
				obj.put("date", date);
				obj.put("loc", loc);
				obj.put("patronRecordNo", patronRecordNo);
				obj.put("pid", pid);
				obj.put("name", name);
				obj.put("address", address);
				obj.put("pcode1", pcode1);
				obj.put("pcode2", pcode2);
				obj.put("patronAffliation", patronAffliation);
				obj.put("patronType", patronType);
				obj.put("itemBarcode", itemBarcode);
				obj.put("title", title);
				obj.put("callNo", callNo);
				obj.put("chargeType", chargeType);
				obj.put("amount1", amount1);
				obj.put("amount2", amount2);
				obj.put("amount3", amount3);
				rawData.add(obj);
				obj =null;
                }// eend  if(!(invalidPatron ))
			}// end else
		}// end of while

		/*  ===============================
		 *  Executing all the rules
		 *  First get rid of the duplicates
		 *  ===============================
		 */
		// First delete any transaction which has  0 amounts
		log.info("rawdata size IS:"+rawData.size());
		for(int c=0; c < rawData.size(); c++)
		{

			JSONObject obj1 = (JSONObject)rawData.get(c);
			String s =(String)obj1.get("invoiceNo");
			log.info("&&&&&&&&&&&&RAW DATA:"+ s);
			int sumDup1 = 0;
			try
			{
				sumDup1 = Integer.parseInt((String)obj1.get("amount1")) +
					Integer.parseInt((String)obj1.get("amount2"))  +
					Integer.parseInt((String)obj1.get("amount3"));
				log.debug("&&&&&&&&&&&&sumDup1:"+ sumDup1);
			}
			catch ( Exception ex )
			{
				log.warn( "Error summing ammounts", ex );
				sumDup1 = 0; // skip to prevent further errors
				JSONObject js1 = new JSONObject();
				js1.put("invoiceNo", obj1.get("invoiceNo"));
				js1.put("date", obj1.get("date"));
				js1.put("loc", obj1.get("loc"));
				js1.put("patronRecordNo", obj1.get("patronRecordNo"));
				js1.put("pid", obj1.get("pid"));
				js1.put("name", obj1.get("name"));
				js1.put("address",obj1.get("address"));
				js1.put("pcode1", obj1.get("pcode1"));
				js1.put("pcode2", obj1.get("pcode2"));
				js1.put("patronAffliation", obj1.get("patronAffliation"));
				js1.put("patronType", obj1.get("patronType"));
				js1.put("itemBarcode", obj1.get("itemBarcode"));
				js1.put("title", obj1.get("title"));
				js1.put("callNo",obj1.get("callNo"));
				js1.put("chargeType", obj1.get("chargeType"));
				js1.put("amount1", obj1.get("amount1"));
				js1.put("amount2",obj1.get("amount2"));
				js1.put("amount3",obj1.get("amount3"));
				js1.put("rule", "Amount Parsing Error");
				probQueue.add(js1);
			}

			 log.info("&&&&&&&&&&&&sumDup1:"+ sumDup1);
			 if (sumDup1 == 0 )
			 continue;
			 else
			 {
				 if(s.length() > 1)
				 ja.add(obj1);
			 }

		}
		log.info("ja size IS:"+ja.size());
		/*rawData.clear();
		for(int p=0; p < ja.size(); p++)
		{
			JSONObject obj1 = (JSONObject)ja.get(p);
			rawData.add(obj1);

		}
		ja= null;*/

		for(int k =0; k < ja.size();k++)
		{log.info("ITERATION K IS:"+k);
			JSONObject obj1 = (JSONObject)ja.get(k);
			
			 String amount11 = ((String)obj1.get("amount1")).trim(); 
			 String amount21= ((String)obj1.get("amount2")).trim(); 
			 String amount31 = ((String)obj1.get("amount3")).trim(); 
			 String modAmount1 = formatAmount(amount11);
			 String modAmount2 = formatAmount(amount21);
			 String modAmount3 = formatAmount(amount31);
			 
			 String amount12 = null;
			 String amount22= null;
			 String amount32 = null;
			 String modAmount12 = null;
			 String modAmount22 = null;
			 String modAmount32 = null;
			 
			 
		  boolean isDupTrue = false;
		  int sumDup1 = Integer.parseInt((String)obj1.get("amount1")) +
						Integer.parseInt((String)obj1.get("amount2"))  +
						 Integer.parseInt((String)obj1.get("amount3"));
		  CheckDuplicateClass dup1 = new CheckDuplicateClass((String)obj1.get("invoiceNo"),(String)obj1.get("pid"),(String)obj1.get("chargeType"));
		  if (!checkValidIndex(obj1))
		  {log.info("Found already Processed record..." +(String)obj1.get("invoiceNo"));
		   log.info(" $$$ ITERATION K IS:"+k);
			 continue;}
		  for (int j = k+1 ; j< ja.size(); j++)
			{
			  log.info("ITERATION J IS:"+j);
				JSONObject obj2 = (JSONObject)ja.get(j);
				 amount12 = ((String)obj2.get("amount1")).trim(); 
				  amount22= ((String)obj2.get("amount2")).trim(); 
				  amount32 = ((String)obj2.get("amount3")).trim(); 
		          modAmount12 = formatAmount(amount12);
	              modAmount22 = formatAmount(amount22);
	              modAmount32 = formatAmount(amount32);
				if(!checkValidIndex(obj2))
				{continue;}
				else {
				int sumDup2 = Integer.parseInt((String)obj2.get("amount1")) +
				Integer.parseInt((String)obj2.get("amount2"))  +
				Integer.parseInt((String)obj2.get("amount3"));
				CheckDuplicateClass dup2 = new CheckDuplicateClass((String)obj2.get("invoiceNo"),(String)obj2.get("pid"),(String)obj2.get("chargeType"));
				if(dup1.equals(dup2)){
					isDupTrue = true;
					//if(checkValidIndex(obj2))
					//{
										
					if ((sumDup1 > 0 && sumDup2 > 0) || (sumDup1 < 0 && sumDup2 < 0))
					{   log.info("Inside duplicate part 1");
					    log.info("K Object invoive is:"+(String)obj1.get("invoiceNo"));
					    log.info("J Object invoive is:"+(String)obj2.get("invoiceNo"));
					
						// Should add the transaction lines to problem queue
                        JSONObject js1 = new JSONObject();
                        js1.put("invoiceNo", obj1.get("invoiceNo"));
                        js1.put("date", obj1.get("date"));
                        js1.put("loc", obj1.get("loc"));
                        js1.put("patronRecordNo", obj1.get("patronRecordNo"));
                        js1.put("pid", obj1.get("pid"));
                        js1.put("name", obj1.get("name"));
                        js1.put("address",obj1.get("address"));
                        js1.put("pcode1", obj1.get("pcode1"));
                        js1.put("pcode2", obj1.get("pcode2"));
                        js1.put("patronAffliation", obj1.get("patronAffliation"));
                        js1.put("patronType", obj1.get("patronType"));
                        js1.put("itemBarcode", obj1.get("itemBarcode"));
                        js1.put("title", obj1.get("title"));
                        js1.put("callNo",obj1.get("callNo"));
                        js1.put("chargeType", obj1.get("chargeType"));
                        js1.put("amount1", modAmount1);
                        js1.put("amount2",modAmount2);
                        js1.put("amount3",modAmount3);
                        js1.put("rule", "Duplicates ++/--");
                        probQueue.add(js1);
                        js1 = null;
                       // rawData.remove(k);

                        //Adding the second duplicated record to problem queue
                        JSONObject js2 = new JSONObject();
                       
                        js2.put("invoiceNo", obj2.get("invoiceNo"));
                        js2.put("date", obj2.get("date"));
                        js2.put("loc", obj2.get("loc"));
                        js2.put("patronRecordNo", obj2.get("patronRecordNo"));
                        js2.put("pid", obj2.get("pid"));
                        js2.put("name", obj2.get("name"));
                        js2.put("address",obj2.get("address"));
                        js2.put("pcode1", obj2.get("pcode1"));
                        js2.put("pcode2", obj2.get("pcode2"));
                        js2.put("patronAffliation", obj2.get("patronAffliation"));
                        js2.put("patronType", obj2.get("patronType"));
                        js2.put("itemBarcode", obj2.get("itemBarcode"));
                        js2.put("title", obj2.get("title"));
                        js2.put("callNo",obj2.get("callNo"));
                        js2.put("chargeType", obj2.get("chargeType"));
                        js2.put("amount1", modAmount12);
                        js2.put("amount2",modAmount22);
                        js2.put("amount3",modAmount32);
                        js2.put("rule", "Duplicates ++/--");
                        probQueue.add(js2);
                        js2 = null;
                        vv.add(obj1);
						vv.add(obj2);

					}//end of if
					//----------------------------------------------------------
					if ((sumDup1 > 0 && sumDup2 < 0) || (sumDup1 < 0 && sumDup2 > 0))
					{
						log.info("Inside duplicate part 2");
						//should add the 2 transactions to pending queue
						 JSONObject js1 = new JSONObject();
	                        js1.put("invoiceNo", obj1.get("invoiceNo"));
	                        js1.put("date", obj1.get("date"));
	                        js1.put("loc", obj1.get("loc"));
	                        js1.put("patronRecordNo", obj1.get("patronRecordNo"));
	                        js1.put("pid", obj1.get("pid"));
	                        js1.put("name", obj1.get("name"));
	                        js1.put("address",obj1.get("address"));
	                        js1.put("pcode1", obj1.get("pcode1"));
	                        js1.put("pcode2", obj1.get("pcode2"));
	                        js1.put("patronAffliation", obj1.get("patronAffliation"));
	                        js1.put("patronType", obj1.get("patronType"));
	                        js1.put("itemBarcode", obj1.get("itemBarcode"));
	                        js1.put("title", obj1.get("title"));
	                        js1.put("callNo",obj1.get("callNo"));
	                        js1.put("chargeType", obj1.get("chargeType"));
	                        js1.put("amount1", modAmount1);
	                        js1.put("amount2",modAmount2);
	                        js1.put("amount3",modAmount3);
	                        js1.put("rule", "Duplicates +-/-+");
	                        pendingQueue.add(js1);
	                        js1 = null;
	                        //rawData.remove(k);

	                        //Adding the second duplicated record to pending queue
	                        JSONObject js2 = new JSONObject();
	                        js2.put("invoiceNo", obj2.get("invoiceNo"));
	                        js2.put("date", obj2.get("date"));
	                        js2.put("loc", obj2.get("loc"));
	                        js2.put("patronRecordNo", obj2.get("patronRecordNo"));
	                        js2.put("pid", obj2.get("pid"));
	                        js2.put("name", obj2.get("name"));
	                        js2.put("address",obj2.get("address"));
	                        js2.put("pcode1", obj2.get("pcode1"));
	                        js2.put("pcode2", obj2.get("pcode2"));
	                        js2.put("patronAffliation", obj2.get("patronAffliation"));
	                        js2.put("patronType", obj2.get("patronType"));
	                        js2.put("itemBarcode", obj2.get("itemBarcode"));
	                        js2.put("title", obj2.get("title"));
	                        js2.put("callNo",obj2.get("callNo"));
	                        js2.put("chargeType", obj2.get("chargeType"));
	                        js2.put("amount1", modAmount12);
	                        js2.put("amount2",modAmount22);
	                        js2.put("amount3",modAmount32);
	                        js2.put("rule", "Duplicates +-/-+");
	                        pendingQueue.add(js2);
	                        js2 = null;
	                        vv.add(obj1);
							vv.add(obj2);

					}// end if ((sumDup1 > 0  && sumDup2 < 0) || (su
					}//if if(checkValidIndex(k))
					else {
						log.info("Found already Processed record in inner loop...");
						continue;
					}
					//---------------------------------------------------------------
				}//end of if(dup1.equals(dup2))

			}//end of inner for
			/*if( ! (isDupTrue))
			{
				ja1.add(obj1);
			}*/
		}//end of outer for
		log.info("Size of ja Array"+ ja.size());
		for(int p=0; p < ja.size();p++)
		{
			JSONObject obj1 = (JSONObject)ja.get(p);
			if (checkValidIndex (obj1))
			{
				ja1.add(obj1);
				
			}
		

		}
		log.info("Size of ja1 Array"+ ja1.size());
		for (int jj=0 ;jj< vv.size(); jj++)
		{
			JSONObject iObj= (JSONObject) vv.get(jj);
			String s =(String)iObj.get("invoiceNo");
			log.info("Printing VV"+ s);

		}
		for (int jj=0 ;jj< ja1.size(); jj++)
		{
			JSONObject iObj= (JSONObject) ja1.get(jj);
			String s =(String)iObj.get("invoiceNo");
			log.info("%%%%%%%%%%INVOICE"+ s);

		}
		/*======================
		 * Check the other Rules
		 * =========================
		 */

		for (int a=0; a < ja1.size();a++)
		{
			//if (!checkValidIndex (a))
				//continue;

			JSONObject obj1 = (JSONObject)ja1.get(a);
			log.info("^^^^^^^^invoice:"+(String)obj1.get("invoiceNo"));
			/*int s1 = Integer.parseInt((String)obj1.get("amount1"));
			int s2 = Integer.parseInt((String)obj1.get("amount2"));
			int s3 = Integer.parseInt((String)obj1.get("amount3"));
			if (s1 == 0 && s2 == 0 && s3 == 0)
			{
				continue;
			}*/
			// CheckDuplicateClass dup1 = new CheckDuplicateClass((String)obj1.get("invoiceNo"),(String)obj1.get("pid"),(String)obj1.get("chargeType"));
			//boolean isDupFile = isDuplicate(dup1);
			//v.add(dup1);
			boolean isDup = checkDuplicateDb((String)obj1.get("invoiceNo"), (String)obj1.get("pid"),(String)obj1.get("chargeType"),(String)obj1.get("amount1"),(String)obj1.get("amount2"),(String)obj1.get("amount3"));
            boolean validLOC = checkLoc((String)obj1.get("loc"));
            boolean validPatronType = checkPatronType((String)obj1.get("patronType"));
            boolean validChargeType = checkChargeType((String)obj1.get("chargeType"));
            /*
             * ======================================================
             * Checking rule duplicate records in file
             * =======================================================
             */
            /*if (isDupFile)
            {rule = "Duplicate record in file";
            JSONObject js1 = new JSONObject();
            js1.put("invoiceNo", obj1.get("invoiceNo"));
            js1.put("date", obj1.get("date"));
            js1.put("loc", obj1.get("loc"));
            js1.put("patronRecordNo", obj1.get("patronRecordNo"));
            js1.put("pid", obj1.get("pid"));
            js1.put("name", obj1.get("name"));
            js1.put("address",obj1.get("address"));
            js1.put("pcode1", obj1.get("pcode1"));
            js1.put("pcode2", obj1.get("pcode2"));
            js1.put("patronAffliation", obj1.get("patronAffliation"));
            js1.put("patronType", obj1.get("patronType"));
            js1.put("itemBarcode", obj1.get("itemBarcode"));
            js1.put("title", obj1.get("title"));
            js1.put("callNo",obj1.get("callNo"));
            js1.put("chargeType", obj1.get("chargeType"));
            js1.put("amount1", obj1.get("amount1"));
            js1.put("amount2",obj1.get("amount2"));
            js1.put("amount3",obj1.get("amount3"));
            js1.put("rule", rule);
            log.info("RULE &&&& :="+ rule);
            log.info("validChargeType &&&& :="+ validChargeType);
            log.debug("DEBUG: RULE &&&& :="+ rule);
            log.debug("DEBUG: validChargeType &&&& :="+ validChargeType);
            probQueue.add(js1);
            js1 = null;
            continue;

            }
            */
            String amt1 = ((String)obj1.get("amount1")).trim(); 
            String amt2= ((String)obj1.get("amount2")).trim(); 
            String amt3 = ((String)obj1.get("amount3")).trim(); 
            String modAmt1 = formatAmount(amt1);
            String modAmt2 = formatAmount(amt2);
            String modAmt3 = formatAmount(amt3);
            
            
            /*
             * ======================================================
             * Checking rule duplicate records in DB
             * =======================================================
             */
            if (isDup)
            {rule = "Duplicate record in DB";
            JSONObject js1 = new JSONObject();
            js1.put("invoiceNo", obj1.get("invoiceNo"));
            js1.put("date", obj1.get("date"));
            js1.put("loc", obj1.get("loc"));
            js1.put("patronRecordNo", obj1.get("patronRecordNo"));
            js1.put("pid", obj1.get("pid"));
            js1.put("name", obj1.get("name"));
            js1.put("address",obj1.get("address"));
            js1.put("pcode1", obj1.get("pcode1"));
            js1.put("pcode2", obj1.get("pcode2"));
            js1.put("patronAffliation", obj1.get("patronAffliation"));
            js1.put("patronType", obj1.get("patronType"));
            js1.put("itemBarcode", obj1.get("itemBarcode"));
            js1.put("title", obj1.get("title"));
            js1.put("callNo",obj1.get("callNo"));
            js1.put("chargeType", obj1.get("chargeType"));
            js1.put("amount1",modAmt1);
            js1.put("amount2",modAmt2);
            js1.put("amount3",modAmt3);
            js1.put("rule", rule);
            log.info("RULE &&&& :="+ rule);
            log.info("validChargeType &&&& :="+ validChargeType);
            log.debug("DEBUG: RULE &&&& :="+ rule);
            log.debug("DEBUG: validChargeType &&&& :="+ validChargeType);
            probQueue.add(js1);
            js1 = null;
            continue;

            }
            System.out.println("DEBUG:ISDUP :="+ isDup);
            System.out.println("DEBUG:RULE &&&& :="+ rule);
            log.debug("DEBUG:ISDUP :="+ isDup);
            log.debug("DEBUG:RULE &&&& :="+ rule);
            /*
             * ======================================================
             * Checking rule 2.2 
             * =======================================================
             */
            if ((!(validLOC)))
            {
            	 JSONObject js1 = new JSONObject();
                 js1.put("invoiceNo", obj1.get("invoiceNo"));
                 js1.put("date", obj1.get("date"));
                 js1.put("loc", obj1.get("loc"));
                 js1.put("patronRecordNo", obj1.get("patronRecordNo"));
                 js1.put("pid", obj1.get("pid"));
                 js1.put("name", obj1.get("name"));
                 js1.put("address",obj1.get("address"));
                 js1.put("pcode1", obj1.get("pcode1"));
                 js1.put("pcode2", obj1.get("pcode2"));
                 js1.put("patronAffliation", obj1.get("patronAffliation"));
                 js1.put("patronType", obj1.get("patronType"));
                 js1.put("itemBarcode", obj1.get("itemBarcode"));
                 js1.put("title", obj1.get("title"));
                 js1.put("callNo",obj1.get("callNo"));
                 js1.put("chargeType", obj1.get("chargeType"));
                 js1.put("amount1",modAmt1);
                 js1.put("amount2",modAmt2);
                 js1.put("amount3",modAmt3);
                 js1.put("rule", rule);
                 log.info("RULE &&&& :="+ rule);
                 log.info("validChargeType &&&& :="+ validChargeType);
                 log.debug("DEBUG: RULE &&&& :="+ rule);
                 log.debug("DEBUG: validChargeType &&&& :="+ validChargeType);
                 probQueue.add(js1);
                 js1 = null;
                 continue;
            }
            /*
             * ======================================================
             * Checking rule 2.3 
             * =======================================================
             */
            if ((!(validPatronType)))
            {
            	 JSONObject js1 = new JSONObject();
                 js1.put("invoiceNo", obj1.get("invoiceNo"));
                 js1.put("date", obj1.get("date"));
                 js1.put("loc", obj1.get("loc"));
                 js1.put("patronRecordNo", obj1.get("patronRecordNo"));
                 js1.put("pid", obj1.get("pid"));
                 js1.put("name", obj1.get("name"));
                 js1.put("address",obj1.get("address"));
                 js1.put("pcode1", obj1.get("pcode1"));
                 js1.put("pcode2", obj1.get("pcode2"));
                 js1.put("patronAffliation", obj1.get("patronAffliation"));
                 js1.put("patronType", obj1.get("patronType"));
                 js1.put("itemBarcode", obj1.get("itemBarcode"));
                 js1.put("title", obj1.get("title"));
                 js1.put("callNo",obj1.get("callNo"));
                 js1.put("chargeType", obj1.get("chargeType"));
                 js1.put("amount1",modAmt1);
                 js1.put("amount2",modAmt2);
                 js1.put("amount3",modAmt3);
                 js1.put("rule", rule);
                 log.info("RULE &&&& :="+ rule);
                 log.info("validChargeType &&&& :="+ validChargeType);
                 log.debug("DEBUG: RULE &&&& :="+ rule);
                 log.debug("DEBUG: validChargeType &&&& :="+ validChargeType);
                 probQueue.add(js1);
                 js1 = null;
                 continue;
            }
            /*
             * ======================================================
             * Checking rule 2.4 
             * =======================================================
             */
            if((!(validChargeType)))
            {
            	JSONObject js1 = new JSONObject();
                js1.put("invoiceNo", obj1.get("invoiceNo"));
                js1.put("date", obj1.get("date"));
                js1.put("loc", obj1.get("loc"));
                js1.put("patronRecordNo", obj1.get("patronRecordNo"));
                js1.put("pid", obj1.get("pid"));
                js1.put("name", obj1.get("name"));
                js1.put("address",obj1.get("address"));
                js1.put("pcode1", obj1.get("pcode1"));
                js1.put("pcode2", obj1.get("pcode2"));
                js1.put("patronAffliation", obj1.get("patronAffliation"));
                js1.put("patronType", obj1.get("patronType"));
                js1.put("itemBarcode", obj1.get("itemBarcode"));
                js1.put("title", obj1.get("title"));
                js1.put("callNo",obj1.get("callNo"));
                js1.put("chargeType", obj1.get("chargeType"));
                js1.put("amount1",modAmt1);
                js1.put("amount2",modAmt2);
                js1.put("amount3",modAmt3);
                js1.put("rule", rule);
                log.info("RULE &&&& :="+ rule);
                log.info("validChargeType &&&& :="+ validChargeType);
                log.debug("DEBUG: RULE &&&& :="+ rule);
                log.debug("DEBUG: validChargeType &&&& :="+ validChargeType);
                probQueue.add(js1);
                js1 = null;
                continue;
            }
          /*  if ((!(validLOC)) || (!(validPatronType))
                                            || (!(validChargeType)))
            	{
            	 JSONObject js1 = new JSONObject();
                 js1.put("invoiceNo", obj1.get("invoiceNo"));
                 js1.put("date", obj1.get("date"));
                 js1.put("loc", obj1.get("loc"));
                 js1.put("patronRecordNo", obj1.get("patronRecordNo"));
                 js1.put("pid", obj1.get("pid"));
                 js1.put("name", obj1.get("name"));
                 js1.put("address",obj1.get("address"));
                 js1.put("pcode1", obj1.get("pcode1"));
                 js1.put("pcode2", obj1.get("pcode2"));
                 js1.put("patronAffliation", obj1.get("patronAffliation"));
                 js1.put("patronType", obj1.get("patronType"));
                 js1.put("itemBarcode", obj1.get("itemBarcode"));
                 js1.put("title", obj1.get("title"));
                 js1.put("callNo",obj1.get("callNo"));
                 js1.put("chargeType", obj1.get("chargeType"));
                 js1.put("amount1", obj1.get("amount1"));
                 js1.put("amount2",obj1.get("amount2"));
                 js1.put("amount3",obj1.get("amount3"));
                 js1.put("rule", rule);
                 log.info("RULE &&&& :="+ rule);
                 log.info("validChargeType &&&& :="+ validChargeType);
                 log.debug("DEBUG: RULE &&&& :="+ rule);
                 log.debug("DEBUG: validChargeType &&&& :="+ validChargeType);
                 probQueue.add(js1);
                 js1 = null;
                 continue;

            }*/
            /*
             * ======================================================
             * Checking rule 2.6
             * =======================================================
             */

            boolean patronTypeChk = false;
            if ((Integer.parseInt((String)obj1.get("patronType")) == 2)
                   || (Integer.parseInt((String)obj1.get("patronType")) == 3)
                   || (Integer.parseInt((String)obj1.get("patronType")) == 22))
                    patronTypeChk = true;

            if (patronTypeChk && checkAffiliationCode(Integer.parseInt((String)obj1.get("patronAffliation"))))
            {
                rule = "Rule 2.6";
                JSONObject js1 = new JSONObject();
                js1.put("invoiceNo", obj1.get("invoiceNo"));
                js1.put("date", obj1.get("date"));
                js1.put("loc", obj1.get("loc"));
                js1.put("patronRecordNo", obj1.get("patronRecordNo"));
                js1.put("pid", obj1.get("pid"));
                js1.put("name", obj1.get("name"));
                js1.put("address",obj1.get("address"));
                js1.put("pcode1", obj1.get("pcode1"));
                js1.put("pcode2", obj1.get("pcode2"));
                js1.put("patronAffliation", obj1.get("patronAffliation"));
                js1.put("patronType", obj1.get("patronType"));
                js1.put("itemBarcode", obj1.get("itemBarcode"));
                js1.put("title", obj1.get("title"));
                js1.put("callNo",obj1.get("callNo"));
                js1.put("chargeType", obj1.get("chargeType"));
                js1.put("amount1",modAmt1);
                js1.put("amount2",modAmt2);
                js1.put("amount3",modAmt3);
                js1.put("rule", rule);
                pendingQueue.add(js1);
                js1 = null;
                continue;
            }
            /*
             * =============================================================
             * Checking rule 2.7 check patron record # and PID both in
             * the internal database or not
             * ============================================================
             */
            if (checkPatronRecAndPid((String)obj1.get("patronRecordNo"),(String)obj1.get("pid")))
            {
            	 JSONObject js1 = new JSONObject();
                 js1.put("invoiceNo", obj1.get("invoiceNo"));
                 log.info("This is what I want: invoiveno in AND="+obj1.get("invoiceNo"));
                 js1.put("date", obj1.get("date"));
                 js1.put("loc", obj1.get("loc"));
                 js1.put("patronRecordNo", obj1.get("patronRecordNo"));
                 js1.put("pid", obj1.get("pid"));
                 js1.put("name", obj1.get("name"));
                 js1.put("address",obj1.get("address"));
                 js1.put("pcode1", obj1.get("pcode1"));
                 js1.put("pcode2", obj1.get("pcode2"));
                 js1.put("patronAffliation", obj1.get("patronAffliation"));
                 js1.put("patronType", obj1.get("patronType"));
                 js1.put("itemBarcode", obj1.get("itemBarcode"));
                 js1.put("title", obj1.get("title"));
                 js1.put("callNo",obj1.get("callNo"));
                 js1.put("chargeType", obj1.get("chargeType"));
                 js1.put("amount1",modAmt1);
                 js1.put("amount2",modAmt2);
                 js1.put("amount3",modAmt3);
                 rule= "Patron rec # AND PID together in DB";
                 js1.put("rule", rule);
                 pendingQueue.add(js1);
                 js1 = null;
                 continue;
            }

            boolean bb1= checkPatronRecOrPid((String)obj1.get("patronRecordNo"),(String)obj1.get("pid"));
            boolean bb2= checkPatronRecAndPid((String)obj1.get("patronRecordNo"),(String)obj1.get("pid"));
            if ( !(bb1) && !(bb2)){
            	JSONObject js1 = new JSONObject();
                js1.put("invoiceNo", obj1.get("invoiceNo"));
                log.info("This is what I want: invoiveno in not Both= "+obj1.get("invoiceNo"));
                js1.put("date", obj1.get("date"));
                js1.put("loc", obj1.get("loc"));
                js1.put("patronRecordNo", obj1.get("patronRecordNo"));
                js1.put("pid", obj1.get("pid"));
                js1.put("name", obj1.get("name"));
                js1.put("address",obj1.get("address"));
                js1.put("pcode1", obj1.get("pcode1"));
                js1.put("pcode2", obj1.get("pcode2"));
                js1.put("patronAffliation", obj1.get("patronAffliation"));
                js1.put("patronType", obj1.get("patronType"));
                js1.put("itemBarcode", obj1.get("itemBarcode"));
                js1.put("title", obj1.get("title"));
                js1.put("callNo",obj1.get("callNo"));
                js1.put("chargeType", obj1.get("chargeType"));
                js1.put("amount1",modAmt1);
                js1.put("amount2",modAmt2);
                js1.put("amount3",modAmt3);
                rule= "Patron rec # AND PID together NOT in DB";
                js1.put("rule", rule);
                newPatronQueue.add(js1);
                js1 = null;
                continue;
            }
            /*
             * =============================================================
             * Checking rule 2.8 check patron record # or PID in the
             * internal database
             * ============================================================
             */
            if (checkPatronRecOrPid((String)obj1.get("patronRecordNo"),(String)obj1.get("pid")))
            {
            	JSONObject js1 = new JSONObject();
                js1.put("invoiceNo", obj1.get("invoiceNo"));
                log.info("This is what I want: invoiveno in OR="+obj1.get("invoiceNo"));
                js1.put("date", obj1.get("date"));
                js1.put("loc", obj1.get("loc"));
                js1.put("patronRecordNo", obj1.get("patronRecordNo"));
                js1.put("pid", obj1.get("pid"));
                js1.put("name", obj1.get("name"));
                js1.put("address",obj1.get("address"));
                js1.put("pcode1", obj1.get("pcode1"));
                js1.put("pcode2", obj1.get("pcode2"));
                js1.put("patronAffliation", obj1.get("patronAffliation"));
                js1.put("patronType", obj1.get("patronType"));
                js1.put("itemBarcode", obj1.get("itemBarcode"));
                js1.put("title", obj1.get("title"));
                js1.put("callNo",obj1.get("callNo"));
                js1.put("chargeType", obj1.get("chargeType"));
                js1.put("amount1",modAmt1);
                js1.put("amount2",modAmt2);
                js1.put("amount3",modAmt3);
                rule= "Either Patron rec # OR PID match in DB";
                js1.put("rule", rule);
                probQueue.add(js1);
                js1 = null;
                continue;
            }
           obj1 = null;
		}//end for

	}// end processBillingFile
//================END processBillingFile method==============================================
 public boolean checkValidIndex (JSONObject obj)
 { boolean b = true;
	 if (vv.size()>0){
	 for (int kk=0 ;kk< vv.size(); kk++)
		{
		 JSONObject jObj= (JSONObject) vv.get(kk);
		 CheckDuplicateClass dup1 = new CheckDuplicateClass((String)jObj.get("invoiceNo"),(String)jObj.get("pid"),(String)jObj.get("chargeType"));
		 CheckDuplicateClass dup2 = new CheckDuplicateClass((String)obj.get("invoiceNo"),(String)obj.get("pid"),(String)obj.get("chargeType"));
			
			if(dup1.equals(dup2))
				b = false;


		}
 }
 else  b= true;
	 return b;
 }
	public JSONObject getProcessingResults() {
		JSONObject results = new JSONObject();
		boolean hasData = false; // make sure there is at least data in one
									// queue
		if (newPatronQueue.size() >= 1) {
			hasData = true;
			results.put("newPatron", newPatronQueue);
		}
		if (probQueue.size() >= 1) {
			hasData = true;
			results.put("problem", probQueue);
		}
		if (pendingQueue.size() >= 1) {
			hasData = true;
			results.put("pending", pendingQueue);
			//stroing the pending Queue into the session
			//session.setAttribute("pendingData1", pendingQueue);
			log.info("$$$$ PENDING IN PROCESSFILE:"+pendingQueue.size());
		}
		if (hasData) {
			results.put("result", "success");
		} else {
			results.put("result", "fail");
		}
		return results;
	}
	public JSONArray getPendingQueue() {
		log.info("$$$$ PENDING IN PROCESSFILE2:"+pendingQueue.size());
		return this.pendingQueue;
	}
	
	public JSONArray getProblemQueue() {
		log.info("$$$$ probQueue IN PROCESSFILE2:"+probQueue.size());
		return this.probQueue;
	}
	

	public JSONArray getNewPatronQueue() {
		log.info("$$$$ newPatronQueue IN PROCESSFILE2:"+newPatronQueue.size());
		return this.newPatronQueue;
	}
	/**
	 *
	 * @param patronRecordNo
	 * @param pid
	 * @return
	 */

	private boolean checkPatronRecOrPid(String patronRecordNo, String pid) {
        String substrPatron = patronRecordNo.substring(1);
        String substrPid = pid.substring(1);
        String pidMod = "A"+substrPid;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int count = 0;
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			rs = stmt
					.executeQuery("SELECT COUNT(*) FROM PATRONS WHERE  (PATRONNO = "
							+ "'"
							+ substrPatron
							+ "'"
							+ " OR  PID ="
							+ "'"
							+ pidMod
							+ "')"
							+ "AND NOT EXISTS"
							+ "(SELECT * FROM PATRONS WHERE PATRONNO = "
							+ "'"
							+ substrPatron
							+ "'"
							+ " AND  PID ="
							+ "'"
							+ pidMod + "')");
			while (rs.next()) {

				count = Integer.parseInt(rs.getString(1));
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

		if (count > 0)
			{
			rule = "Rule 2.8";
			return true;
			}
		else {
			//rule = "Rule 2.8";
			return false;
		}

	}

	/*
	 * This method checks the internal database that both patronRecordNo and pid
	 * is avaialble
	 */
	private boolean checkPatronRecAndPid(String patronRecordNo, String pid) {
        String substrPatron = patronRecordNo.substring(1);
        String substrPid = pid.substring(1);
        String pidMod = "A"+substrPid;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int count = 0;
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			rs = stmt
					.executeQuery("SELECT count(*) FROM PATRONS WHERE (PATRONNO) = "
							+ "'"
							+ substrPatron
							+ "'"
							+ "AND PID ="
							+ "'"
							+ pidMod + "'");
			while (rs.next()) {

				count = Integer.parseInt(rs.getString(1));
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

		if (count > 0)
			{
			rule = "Rule 2.7";
			return true;
			}

		else {
			rule = "Rule 2.7";
			return false;
		}

	}

	private boolean checkAffiliationCode(int code) {
		boolean b;
		if (code == 63)
            b= true;
        else if (code == 68)
            b = true;
        else if (code == 69)
            b= true;
        else if (code == 70)
            b = true;
        else if (code == 71)
            b= true;
        else if (code == 72)
            b = true;
        else if (code == 73)
            b= true;
        else if (code == 74)
            b = true;
        else if (code == 139)
            b= true;
        else if (code == 151)
            b = true;
        else if (code == 167)
            b= true;
        else if (code == 177)
            b = true;
        else if (code == 178)
            b = true;
        else if (code == 187)
            b= true;
        else if (code == 192)
            b = true;
        else if (code == 193)
            b= true;
        else if (code == 194)
            b = true;
        else if (code == 195)
            b= true;
        else if (code >= 76 && code <= 134)
            b = true;
        else
            b = false;

		return b;

	}

	private boolean checkPID(String pid) {
		boolean b = false;
		  if (pid.equals('_')) {
	            rule = "Rule 2.5";
	            b = true;
	        }
		else
			b = false;
		return b;
	}

	private boolean checkChargeType(String chargeType) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		int count = 0;
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT count(*) FROM CHARGETYPES WHERE upper(DESCRIPTION) LIKE"
							+ "'" + chargeType + "%'");
			while (rs.next()) {

				count = Integer.parseInt(rs.getString(1));
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

		if (count > 0)
			return true;
		else {
			rule = "ChargeType not defined";
			return false;
		}

	}

	private boolean checkPatronType(String patronType) {

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		int count = 0;

		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT count(*) FROM PATRONS WHERE PATRONTYPE ="
							+ patronType);
			while (rs.next()) {

				count = Integer.parseInt(rs.getString(1));
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

		if (count > 0)
			return true;
		else {
			rule = "PatronType not defined";
			return false;
		}

	}

	private boolean checkLoc(String ss) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		char c = ss.charAt(0);

		int count = 0;
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			rs = stmt
					.executeQuery("SELECT count(*) FROM LOCATIONCODE WHERE (LOCATIONCODE) = "
							+ "'" + c + "'");
			while (rs.next()) {

				count = Integer.parseInt(rs.getString(1));
			}
			conn.close();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			log.error("NumberFormatException", e);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			log.error("SQLException", e);
		} catch (NamingException e) {
			log.error("JNDI Lookup failed for DB22 connection", e);
		}

		if (count > 0)
			return true;
		else {
			rule = "LOC is not defined";
			return false;

		}
	}

	/**
	 * This method will check duplicate records in the database
	 */
	private boolean checkDuplicateDb(String invoiceno, String pid,
			String chargeType,String amount1,String amount2,String amount3) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		log.info("Current PID: "+pid);
		//String substrPid = pid.substring(1);
       // String newpid = pid;
        //= "A"+substrPid;
		
		int count = 0;
		
		 String chageFee = null;
		 String processFee = null;
		 String billingFees = null;
		 if(amount1.length()> 1)
		 {
			 chageFee = amount1.substring(0, amount1.length()-2); 
		 }
		 else
		 {
			 chageFee=amount1;
		 }
		 
		 if(amount2.length()> 1)
		 {
			 processFee = amount2.substring(0, amount2.length()-2); 
		 }
		 else
		 {
			 processFee=amount2;
		 }
		 if(amount3.length()> 1)
		 {
			 billingFees = amount3.substring(0, amount3.length()-2); 
		 }
		 else
		 {
			 billingFees=amount3;
		 }
		try {
			conn = ConnectionManager.getConnection("billing");
			stmt = conn.createStatement();
			/*rs = stmt
					.executeQuery("SELECT  count(*) FROM TRANSACTIONS T INNER JOIN PAYMENTS P ON P.INVOICE = T.INVOICENO WHERE P.PID= "
							+ "'"
							+ newpid
							+ "'"
							+ "AND T.INVOICENO ="
							+ "'"
							+ invoiceno
							+ "'"
							+ "AND T.CHARGETYPE IN (SELECT CHARGETYPE FROM CHARGETYPES WHERE upper(DESCRIPTION) LIKE"
							+ "'" + chargeType + "%')");*/
			/*rs = stmt
			.executeQuery("SELECT  count(*) FROM TRANSACTIONS T WHERE  T.INVOICENO ="
					+ "'"
					+ invoiceno
					+ "'"
					+ "AND T.CHARGETYPE IN (SELECT CHARGETYPE FROM CHARGETYPES WHERE upper(DESCRIPTION) LIKE"
					+ "'" + chargeType + "%')");
					*/
			
			 
			 rs = stmt
				.executeQuery("SELECT  count(*) FROM TRANSACTIONS T WHERE  T.INVOICENO ="
						+ "'"
						+ invoiceno
						+ "'"
						+ "AND T.CHARGE ="
						+ "'"
						+ chageFee
						+ "'"
						+ "AND T.PROCESSINGFEE ="
						+ "'"
						+ processFee
						+ "'"
						+ "AND T.BILLINGFEE ="
						+ "'"
						+ billingFees
						+ "'"
						+ "AND T.CHARGETYPE IN (SELECT CHARGETYPE FROM CHARGETYPES WHERE upper(DESCRIPTION) LIKE"
						+ "'" + chargeType + "%')");
			while (rs.next()) {

				count = Integer.parseInt(rs.getString(1));
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

		if (count > 0) {
			rule = "Rule 2.1";
			return true;
		}

		else {

			return false;
		}
	}

	/**
	 * This method will check for duplicate records in the Billing.txt file
	 *
	 * @param sarr
	 * @return
	 */
	private boolean isDuplicate(CheckDuplicateClass sarr) {

		boolean b = false;
		int count = v.size();
		vectorcount = count;
		if (sarr == null)
			vectorcount = -1;
		if (count == 0)
			b = false;
		else if (count >= 1 && sarr != null) {
			ListIterator iter = v.listIterator();
			while (iter.hasNext()) {
				CheckDuplicateClass temp = (CheckDuplicateClass) iter.next();
				if (temp.equals(sarr)) {
					b = true;
					break;
				}

			}

		}
		return b;
	}

	/*
	 * This method checks the rule #1 to identify the internal transactions
	 */
	private boolean checkInvalidPatronType(int a) {
		boolean b;
		switch (a) {
		case 0:
		case 12:
		case 13:
		case 14:
		case 15:
		case 18:
		case 20:
		case 21:
		case 36:
			b = true;
			break;
		default:
			b = false;
			break;

		}
		return b;

	}
	public String formatAmount(String str1) {
		 int len = str1.length();
		 String newStr = null;
		 if(len >2)
		 {
		  newStr = "$"+str1.substring(0,len-2)+"."+str1.substring(len-2);
		  log.info("Inside formatAmount:"+newStr);
		 }
		 else 
		 {
			 newStr = "$"+str1+"."+"00";
		 }
		 return newStr;
		
	}

}
