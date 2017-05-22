package edu.ucsd.library.billing;

import org.apache.log4j.Logger;


public class CheckDuplicateClass {
	private static Logger log = Logger.getLogger( CheckDuplicateClass.class );
	private String invoiceno;
	private String pid;
	private String chargeType;
 public CheckDuplicateClass (String invoiceno,String patronNo,String chargeType)
 {
	 this.invoiceno =invoiceno;
	 this.pid = patronNo;
	 this.chargeType = chargeType;
	 
 }
 public boolean equals(Object other)
 {
	 if (other ==  null)
		 return false;
	 if (! (other instanceof CheckDuplicateClass))
		 return false;
	 CheckDuplicateClass cother= (CheckDuplicateClass) other;
	 
	 if ((this.invoiceno.equals(cother.invoiceno))&& (this.pid.equalsIgnoreCase(cother.pid)) && (this.chargeType.equalsIgnoreCase(cother.chargeType)))
			 return true;
	 else return false;
	 
	 
 }
}