var jsonData; 
$(function(){
	/** ACTIONS **/
	var jsonData;
	var sessionData;
	//var pendingData;
	var tokensPending;
	var tokensProblem;
	var pid;
	var invoiceNo;
	var win2;
    var arrayInvoice;
    var myString;
    var invoiceNoteArray;
    var invoiceNoteArrSize;
    var flag;
	var size;
	var username;
	var password;
	initActions();
	
	/** METHODS **/
	function initActions(){
		
		$('#buttonsPending').hide();
		$('#buttonsProblem').hide();
		$('#buttonsNewPatron').hide();
		$('#filediv').show();
		//$('#fileDiv').show();
		
		if($("#results tr").length > 1){
			$("#results").empty();
		}
		$("#emptyTable").remove();
		//load menu
		loadBillingMenu();
		$('#searchDiv').hide();
		$('#sessionDiv').hide();
		$('#bursarDiv').hide();
		
		$('#fileToUpload').bind('change', function() {

			  //this.files[0].size gets the size of file.
			  if(this.files[0].size <= 92){
				 $.unblockUI();
			     $.blockUI({ message: 'The file that you are attempting to load does not contain any transaction data, please investigate.' });
				 setTimeout($.unblockUI, 3000);
			  }
			});
	
		//file submission
		$("#processFile").bind("click",function(){
			
			$.blockUI({ message: '<img src="images/busy.gif" /> Processing Billing File...' });
			$("#results").empty(); //if the display message exists
			
			$('.container').hide(); //hide everything before new search
			$.ajaxFileUpload({
				url:"/billing/servlets/UploadBillingFile", 
				secureuri:true,
				fileElementId:'fileToUpload',
				fileArk: '',
				dataType: 'json',
				success: displayBillingQueues,
				error: function (data, status, e){
					$.unblockUI();
					//add error message
					$.blockUI({ message: 'There was an error or timeout processing the Billing File' });
					 setTimeout($.unblockUI, 2000);
				}
			});
		});
		
		
		
		//logout
		$("#logout").bind("click",function(){
			window.location = "/billing/logout.jsp";
		});
		
		$("#homeTab").bind("click",function(){
			//sendDataToServer();
			//alert("you clicked me!!!");
			$("#emptyTable").remove();
			$('#indexdiv').hide();
			$('#searchDiv').hide();
			$('#sessionDiv').hide();
			
			$('#bursarDiv').hide();
			
			$('#filediv').show();
			$('#actions').show();
			
			return false;
		});
		
		//============SESSION========================
		$("#SessionTab").bind("click",function(){
			//$('#dateCombo').hide();
			//$('#fileDiv').hide();
			$('#filediv').hide();
			$('#bursarDiv').hide();
			$("#emptyTable").empty(); 
			$.ajax({
				url: "/billing/servlets/GetSessionDates", 
				dataType: 'json',
				success: displaySessionDates,
				error:function (data, status, e){
					$.unblockUI();
					//add error message
					$.blockUI({ message: 'There was an error or timeout when caling GetSessionResults servlet' });
					 setTimeout($.unblockUI, 2000);
				}
			});	
			
		
			return false;
		});	
		
		
		$("#btnGetSessionData").bind("click",function(){
			var selectedVal = $('#dropdownSession').val();
			if($("table.dataGrid tr").length > 1){
				$("table.dataGrid tbody").empty();
			}
			$("#emptyTable").remove(); 
			//$('.data_container2').hide();
			//alert(selectedVal1);
			//alert(searchCriteria);
			$.ajax({
				url: "/billing/servlets/GetSessionResults", 
				dataType: 'json',
				data:{selectedVal:selectedVal},
				success: function(data){
					init(data);
				},
				error:function (data, status, e){
					$.unblockUI();
					//add error message
					$.blockUI({ message: 'There was an error or timeout when caling GetSessionResults servlet' });
					 setTimeout($.unblockUI, 2000);
				}
			});	
			
			return false;
		});	
		
		
		$("#btnresubmit").bind("click",function(){
			
			//var myString = $("input[id='chkSession']").getValue();
			 var tokensPending ;
			 var selector_checked = $("input[@id=chkSession]:checked").length; 
			 if(selector_checked == 0)
			 {
				 alert("Please select at least one record!");
			 }
			 else if(selector_checked > 0 )
			 {
				 //var valArray = $("input[id='chk']").fieldArray();
				 var myString = $("input[id='chkSession']").getValue();
				 var valArray = myString.tokenize(",", " ", true);
				 tokensPending = valArray;
				 
				 //==========================
				 $.ajax({
						url: "/billing/servlets/ResubmitData", 
						dataType: 'json',
						data: {invoiceArr:myString},
						success: displayAfterResubmit,
						error:function (data, status, e){
							$.unblockUI();
							//add error message
							$.blockUI({ message: 'There was an error or timeout when caling ResubmitData servlet' });
							 setTimeout($.unblockUI, 2000);
						}
					});		 
				
			
			 }
			
			return false;
		});	
		
		
			
		//deselect All button action for session
		$("#btnReset").bind("click",function(){		
			
			//var checked_status = this.checked;
			$("input[@id=chkSession]").each(function()
			{
				this.checked = false;
			});
			return false;
		});	
		//select All button action for session
		$("#btnSessionSelAll").bind("click",function(){		
			
			//var checked_status = this.checked;
			$("input[@id=chkSession]").each(function()
			{
				this.checked = true;
			});
			return false;
		});	
		
		//============SEARCH=====================================
		$("#SearchTab").bind("click",function(){
			$('#filediv').hide();
			$('#indexdiv').hide();
			$('#sessionDiv').hide();
			$('#bursarDiv').hide();
			//$('#action').hide();
			$('#searchDiv').show();
			return false;
		});	
		
		$("#btnSearch").bind("click",function(){
			var searchval= $("#txtSearch").val();
			if(searchval.length == 0)
			{
				alert("Please enter something to search!!!")
			}
			else
			{
			var searchCriteria = $('input[name=group1]:checked').val() ;
			$.blockUI({ message: '<img src="images/busy.gif" /> Getting Data...' });
			//alert(searchCriteria);
			$.ajax({
				url: "/billing/servlets/GetSearchResults", 
				dataType: 'json',
				data:{searchval:searchval,searchCriteria:searchCriteria},
				success: displaySearchResults,
				error:function (data, status, e){
					$.unblockUI();
					//add error message
					$.blockUI({ message: 'There was an error or timeout when caling GetSearchResults servlet' });
					 setTimeout($.unblockUI, 2000);
				}
			});	
			}
			return false;
		});
		
		$("#btnViewPatron").bind("click",function(){
			var selector_checked = $("input[@id=chk2]:checked").length; 
			 if(selector_checked == 0)
			 {
				 alert("Please select at least one record!");
			 }
			 else if(selector_checked == 1 )
			 {
				 var myString = $("input[id='chk2']").getValue();
				 var valArray = myString.tokenize(",", " ", true);
				 var tokensPending = valArray;
				 //alert(tokensPending);
				
				 $.ajax({
						url: "/billing/servlets/GetPatronHistory", 
						dataType: 'json',
						data: {patronNo:tokensPending},
						success: displayPatronHistoryWindow,
						error:function (data, status, e){
							$.unblockUI();
							//add error message
							$.blockUI({ message: 'There was an error or timeout when caling MODIFYQUEUES servlet' });
							 setTimeout($.unblockUI, 2000);
						}
					});	
			 }
			 else if(selector_checked > 1 )
			 {
				 alert("Please select one record at a time!");
			 }
		});	
		//==============END SEARCH=========================	
		
	
		$("#pending").bind("click",function(){
			$.unblockUI();
			//$('#file').hide();	
			//$('#actions').hide();
			//$('#actions').hide();
			$('#filediv').hide();
			$('#bursarDiv').hide();
			$('#searchDiv').hide();
			$('#sessionDiv').hide();
			$('#indexdiv').show();
			$("#results").empty(); //if the display message exists
			$('.container').hide(); //hide everything before new search
			if(jsonData.result === "success"){
				//$("#results").append("<b>Billing Text Results</b>");
				if(jsonData['pending'] !== undefined){
					$('#buttonsProblem').hide();
					$('#buttonsNewPatron').hide();
					$('#buttonsPending').show();
					//$('#actions').hide();
					var pp = jsonData['pendingTotal'];
					//alert("PENDING: total:"+pp);
					showQueue("Pending Queue",jsonData['pending'],jsonData['pendingTotal']);
					
				}
				else
				{
					$('#buttonsProblem').hide();
					$('#buttonsNewPatron').hide();
					$('#buttonsPending').hide();
					showQueue("Pending Queue",jsonData['pending'],0);
				}
				$('.container').fadeIn('fast'); //show the new data
			}else{
				//add error message
				$.blockUI({ message: 'No Data found!' });
				 setTimeout($.unblockUI, 2000);
			}
			return false;
		});
		//test newPatrons menu
		$("#newPatron").bind("click",function(){
			$.unblockUI();
			//$('#actions').hide();
			$('#filediv').hide();
			$('#bursarDiv').hide();
			$('#searchDiv').hide();
			$('#sessionDiv').hide();
			$('#indexdiv').show();
			$("#results").empty(); //if the display message exists
			$('.container').hide(); //hide everything before new search
			//================================================
			
			//===ajax call to get data related to that record==============
			 $.ajax({
					url: "/billing/servlets/GetSessionData", 
					dataType: 'json',
					data: {invoiceArr:myString},
					success: displayNewPQueue,						
					error:function (data, status, e){
						$.unblockUI();
						//add error message
						$.blockUI({ message: 'There was an error or timeout when caling GetSessionData servlet' });
						 setTimeout($.unblockUI, 2000);
					}
				});		
			
		
			return false;
		});
		
		//test problem menu
		$("#problem").bind("click",function(){
			$.unblockUI();
			//$('#actions').hide();
			$('#filediv').hide();
			$('#bursarDiv').hide();
			$('#searchDiv').hide();
			$('#sessionDiv').hide();
			$('#indexdiv').show();
			$("#results").empty(); //if the display message exists
			$('.container').hide(); //hide everything before new search
			
	//================================================
			
			//===ajax call to get data related to that record==============
			 $.ajax({
					url: "/billing/servlets/GetSessionData", 
					dataType: 'json',
					data: {invoiceArr:myString},
					success: displayProbQueue,						
					error:function (data, status, e){
						$.unblockUI();
						//add error message
						$.blockUI({ message: 'There was an error or timeout when caling GetSessionData servlet' });
						 setTimeout($.unblockUI, 2000);
					}
				});	
			
			return false;
		});
		
		//logout
		$("#logout").bind("click",function(){
			window.location = "/billing/logout.jsp";
		});
		
		$("#processOutputFile").bind("click",function(){
			processOutputFile();
			return false;
		});
		
		$("#sendData").bind("click",function(){
			//sendDataToServer();
			
			confirmSendData();
			return false;
		});
		
		
		 function confirmSendData()
		 {
		 var agree=confirm("Are you sure you want to send data to server?");
		 var temp;
		 if (agree)
		 	{
			// var username = prompt("Please enter your username for FTP server","");
			// var temp = prompt("Please enter your password for FTP server","");
				
			 //----------------------------------*/
			 var w = 480, h = 340;
			 if (document.all) {
				   /* the following is only available after onLoad */
				   w = document.body.clientWidth;
				   h = document.body.clientHeight;
				}
				else if (document.layers) {
				   w = window.innerWidth;
				   h = window.innerHeight;
				}
			 var popW = 300, popH = 200;
			 var leftPos = (w-popW)/2, topPos = (h-popH)/2;

			 wleft = (screen.width - w) / 2;
			  wtop = (screen.height - h) / 2;
			 var Text ='';
				Text+='<html> <head>';
				Text += '<script type="text/javascript" src="js/billing.js"></script>';	
				Text += '<script language="Javascript" type="text/javascript">';
			   	Text+= 'function CallParentWindowFunction(){ ';
			   	Text += ' var name=document.getElementById("txtUsername").value; ';
			   	Text += 'var pp=document.getElementById("txtpassword").value; ';
			 	Text += ' window.opener.getPassword(name,pp);window.close(); return true;';
			   	Text+= '}</script>';
				Text+= '</head> <body>';
				Text += ' <table><tr>Please enter FTP username and password</tr><tr><td>Username:</td> ';
				Text += '<td><input type="text" id="txtUsername" value="libzzz" READONLY></td></tr>';
				Text += ' <tr><td>Password:</td> ';
				Text += ' <td><input type="password" id="txtpassword" value="ron1946" READONLY></td></tr>';
				Text += '<tr><td><input type="submit" value= "Ok"  onClick="CallParentWindowFunction()"></td>';
				Text += '<td><input type="submit" value= "Cancel"  onClick="window.close()"></td></tr>';
				Text+='</table></body></html>';
				
					win3 = window.open("","newwin",'width=' + popW + ',height='+popH+',top='+wtop+',left='+wleft);
				//  win3 = window.open("","newwin","width=350,height=150"); 
				    var tmp = win3.document;
					tmp.write(Text);					
					tmp.close();
					//win3.moveTo(wleft, wtop);
					win3.focus();
					document.body.style.cursor = 'default'; 
					
					
					
			//==============================================
			 //if(username==null || username=="")
				// {return false;}
			// else if (password==null || password=="")
				  //{return false;}
		
		/*	 else
			  {
			 $.blockUI({ message: '<img src="images/busy.gif" /> Sending Data...' });
			 $.ajax({
					url: "/billing/servlets/SendOutputFiles", 
					dataType: 'json',
					data:{username:username,password:temp},
					success: displaySendFileLog,
					error:function (data, status, e){
						$.unblockUI();
						//add error message
						$.blockUI({ message: 'There was an error or timeout when caling SendOutputFiles servlet' });
						 setTimeout($.unblockUI, 2000);
					}
				});	
		 	  }
			 */
		 	}
		 else
		 {
			// displayPendingQueueConfirmNo(jsonData); 
			 return false ;
		 }
		
		 }
	
		
		//select All button action for pending queue
		$("#selectAllBtn").bind("click",function(){		
			
			//var checked_status = this.checked;
			$("input[@id=chk]").each(function()
			{
				this.checked = true;
			});
			return false;
		});	
		
		//deselect All button action for pending queue
		$("#deselectAllBtnPending").bind("click",function(){		
			
			//var checked_status = this.checked;
			$("input[@id=chk]").each(function()
			{
				this.checked = false;
			});
			return false;
		});	
		//select All button action for Problem queue
		$("#selectAllProbBtn").bind("click",function(){		
			
			//var checked_status = this.checked;
			$("input[@id=chk]").each(function()
			{
				this.checked = true;
			});
			return false;
		});	
		
		//deselect All button action for Problem queue
		$("#deselectAllBtnProblem").bind("click",function(){		
			
			//var checked_status = this.checked;
			$("input[@id=chk]").each(function()
			{
				this.checked = false;
			});
			return false;
		});	
          //Action event for delete button in the Pending Queue
		 $("#delBtnPen").bind("click",function(){
			 var selector_checked = $("input[@id=chk]:checked").length; 
			 if(selector_checked == 0)
			 {
				 alert("Please select at least one record!");
			 }
			 else if(selector_checked > 0 )
			 {
				 //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
				 //var valArray = $("input[id='chk']").fieldArray();
				 var myString = $("input[id='chk']").getValue();
				 var valArray = myString.tokenize(",", " ", true);
				 tokensPending = valArray;
				 var whichQueue = 'P';
				 confirmSubmitPending(myString);
				 
				
			 } 
			
			});	 
				 
		 
		 function confirmSubmitPending(myString)
		 {
		 var agree=confirm("Are you sure you wish to continue?");
		 if (agree)
		 	{
			 $.ajax({
					url: "/billing/servlets/ModifyQueues", 
					dataType: 'json',
					data: {invoiceArr:myString,whichQueue:'P'},
					success: displayPendingQueueAfterDelete,
					error:function (data, status, e){
						$.unblockUI();
						//add error message
						$.blockUI({ message: 'There was an error or timeout when caling MODIFYQUEUES servlet' });
						 setTimeout($.unblockUI, 2000);
					}
				});	
		 	}
		 else
		 {
			 displayPendingQueueConfirmNo(jsonData); 
			 return false ;
		 }
		 	
		 }

		 
		 function confirmSubmitProblem(myString)
		 {
		 var agree=confirm("Are you sure you wish to continue?");
		 if (agree)
		 	{
			 $.ajax({
					url: "/billing/servlets/ModifyQueues", 
					dataType: 'json',
					data: {invoiceArr:myString,whichQueue:'Q'},
					success: displayProblemQueueAfterDelete,
					error:function (data, status, e){
						$.unblockUI();
						//add error message
						$.blockUI({ message: 'There was an error or timeout when caling MODIFYQUEUES servlet' });
						 setTimeout($.unblockUI, 2000);
					}
				});		 
		 	}
		 else
		 {
			 displayPendingQueueConfirmNoProblem(jsonData); 
			 return false ;
		 }
		 	
		 }
		 
		 function confirmSubmitNewPatron(myString)
		 {
		 var agree=confirm("Are you sure you wish to continue?");
		 if (agree)
		 	{
			 $.ajax({
					url: "/billing/servlets/ModifyQueues", 
					dataType: 'json',
					data: {invoiceArr:myString,whichQueue:'N'},
					success: displayNewPatronQueueAfterDelete,
					error:function (data, status, e){
						$.unblockUI();
						//add error message
						$.blockUI({ message: 'There was an error or timeout when caling MODIFYQUEUES servlet' });
						 setTimeout($.unblockUI, 2000);
					}
				});		 
			 
		 	}
		 else
		 {
			 displayPendingQueueConfirmNoNewPatron(jsonData); 
			 return false ;
		 }
		 	
		 }
				 /*--------------------------------------*/	 
				 
				 
				 
				 
				 
				 
		 //Action event for delete button in the Problem Queue
		 $("#delBtnProb").bind("click",function(){
			
			 var selector_checked = $("input[@id=chk]:checked").length; 
			 if(selector_checked == 0)
			 {
				 alert("Please select at least one record!");
			 }
			 else if(selector_checked > 0 )
			 {
				 //var valArray = $("input[id='chk']").fieldArray();
				 var myString = $("input[id='chk']").getValue();
				 var valArray = myString.tokenize(",", " ", true);
				 tokensPending = valArray;
				 
				 //==========================
				 confirmSubmitProblem(myString); 
				
			
			 } 
			});	
		 //Action event for delete button in the Problem Queue
		 $("#delBtnNewP").bind("click",function(){
			
			 var selector_checked = $("input[@id=chk]:checked").length; 
			 if(selector_checked == 0)
			 {
				 alert("Please select at least one record!");
			 }
			 else if(selector_checked > 0 )
			 {
				 //var valArray = $("input[id='chk']").fieldArray();
				 var myString = $("input[id='chk']").getValue();
				 var valArray = myString.tokenize(",", " ", true);
				 tokensPending = valArray;
				 confirmSubmitNewPatron(myString);
				 //==========================
						
			 } 
			});	
		 //Action event for "move to Problem Queue" button in the Pending Queue
		 $("#moveToProbQBtn").bind("click",function(){
			 var selector_checked = $("input[@id=chk]:checked").length; 
			 if(selector_checked == 0)
			 {
				 alert("Please select at least one record!");
			 }
			 else if(selector_checked > 0 )
			 {
				 //var valArray = $("input[id='chk']").fieldArray();
				 var myString = $("input[id='chk']").getValue();
				 var valArray = myString.tokenize(",", " ", true);
				 tokensPending = valArray;
				 
				 //==========================
				 $.ajax({
						url: "/billing/servlets/MoveToOtherQueues", 
						dataType: 'json',
						data: {invoiceArr:myString,whichQueue:'P'},
						success: displayPendingQueueAfterDelete,
						error:function (data, status, e){
							$.unblockUI();
							//add error message
							$.blockUI({ message: 'There was an error or timeout when caling MODIFYQUEUES servlet' });
							 setTimeout($.unblockUI, 2000);
						}
					});		 
				
			
			 } 
			});
		//Action event for "move to Pending Queue" button in the Problem Queue
		 $("#moveToPendQBtn").bind("click",function(){
			
			 var selector_checked = $("input[@id=chk]:checked").length; 
			 if(selector_checked == 0)
			 {
				 alert("Please select at least one record!");
			 }
			 else if(selector_checked > 0 )
			 {
				 //var valArray = $("input[id='chk']").fieldArray();
				 var myString = $("input[id='chk']").getValue();
				 var valArray = myString.tokenize(",", " ", true);
				 tokensPending = valArray;
				 
				 //==========================
				 $.ajax({
						url: "/billing/servlets/MoveToOtherQueues", 
						dataType: 'json',
						data: {invoiceArr:myString,whichQueue:'Q'},
						success: displayProblemQueueAfterDelete,
						error:function (data, status, e){
							$.unblockUI();
							//add error message
							$.blockUI({ message: 'There was an error or timeout when caling MODIFYQUEUES servlet' });
							 setTimeout($.unblockUI, 2000);
						}
					});		 
				
			
			 } 
			});
	 
		//edit button for new patron queue
		 $("#editBtnNewP").bind("click",function(){
				
			 var selector_checked = $("input[@id=chk]:checked").length; 
			 if(selector_checked == 0)
			 {
				 alert("Please select at least one record!");
			 }
			 else if(selector_checked == 1 )
			 {
				// var myString = $("input[id='chk']").getValue();
				// var valArray = myString.tokenize(",", " ", true);
				 var myString = $("input[id='chk']").getValue();
				
				 var valArray = $("input[id='chk']").fieldArray();
				 displayEditWindow(valArray);
				//===ajax call to get data related to that record==============
				 $.ajax({
						url: "/billing/servlets/GetNewPatronData", 
						dataType: 'json',
						data: {invoiceArr:myString},
						success: displayEditWindow,
						error:function (data, status, e){
							$.unblockUI();
							//add error message
							$.blockUI({ message: 'There was an error or timeout when caling MODIFYQUEUES servlet' });
							 setTimeout($.unblockUI, 2000);
						}
					});		
			
			 }
			 else if(selector_checked > 1 )
			 {
				 alert("Please select one record at a time to edit!"); 
			 }
			});
		 
		//edit button for new patron queue
		 $("#moveToPendQBtnNewP").bind("click",function(){
			 var selector_checked = $("input[@id=chk]:checked").length; 
			 if(selector_checked == 0)
			 {
				 alert("Please select at least one record!");
			 }
			 else if(selector_checked > 0 )
			 {
				 //var valArray = $("input[id='chk']").fieldArray();
				 var myString = $("input[id='chk']").getValue();
				 var valArray = myString.tokenize(",", " ", true);
				 tokensPending = valArray;
				 
				 //==========================
				 $.ajax({
						url: "/billing/servlets/MoveToOtherQueues", 
						dataType: 'json',
						data: {invoiceArr:myString,whichQueue:'N'},
						success: displayNewPatronQueueAfterDelete,
						error:function (data, status, e){
							$.unblockUI();
							//add error message
							$.blockUI({ message: 'There was an error or timeout when caling MovetoOtherQueues servlet' });
							 setTimeout($.unblockUI, 2000);
						}
					});		 
				
			
			 } 
		 });
		 
		//edit button for Problem queue
		 $("#editBtnProb").bind("click",function(){
				
			 var selector_checked = $("input[@id=chk]:checked").length; 
			 if(selector_checked == 0)
			 {
				 alert("Please select at least one record!");
			 }
			 else if(selector_checked == 1 )
			 {
				 var myString = $("input[id='chk']").getValue();			
						
				//===ajax call to get data related to that record==============
				 $.ajax({
						url: "/billing/servlets/GetProblemQueueData", 
						dataType: 'json',
						data: {invoiceArr:myString},
						success: displayEditWindowForProblemQueue,						
						error:function (data, status, e){
							$.unblockUI();
							//add error message
							$.blockUI({ message: 'There was an error or timeout when caling GetProblemQueueData servlet' });
							 setTimeout($.unblockUI, 2000);
						}
					});		
			
			 }
			 else if(selector_checked > 1 )
			 {
				 alert("Please select one record at a time to edit!"); 
			 }
			});
		 
		 
		 
		//============Bursar========================
			$("#bursarTab").bind("click",function(){
				$('#filediv').hide();
				$('#indexdiv').hide();
				$('#sessionDiv').hide();
				$('#searchDiv').hide();
				getBursarData();
				$('#bursarDiv').show();
						
			
				return false;
			});	
			
			$("#startDate,#endDate").datepicker({
				beforeShow: customRange,
				dateFormat: 'yy-mm-dd',
				showOn: "button",
				buttonImage: "images/calendar.gif",
				buttonImageOnly: true,
				buttonText: 'Choose Date From Calendar',
				hideIfNoPrevNext : true
			});
			$("#getRecords").bind("click",function(){
				if(validDateFormats()){
					getBursarData();
				}else{
					showDateFormatError();
				}
				return false;
			});
			$("#processBursarFile").bind("click",function(){
				processBursarFile();
				return false;
			});
			
		}
		/** MAKE SURE DATEFORMAT IS CORRECT TO SEND TO SERVER **/
		function validDateFormats(){
			var startDate = $("#startDate").val();
			var endDate = $("#endDate").val();
			try{
				$.datepicker.parseDate("yy-mm-dd",startDate);
				$.datepicker.parseDate("yy-mm-dd",endDate);
			}
			catch(err){ return false; }
			return true;
		}
		function showDateFormatError(){
			var span = $("<span class='dateFormat'></span>").html("*** FORMAT: YYYY-MM-DD ***");
			$('#dateRange p').append(span);
			$('span.dateFormat').show("pulsate",{ times: 3 }, 1000, function(){
				$('span.dateFormat').remove();
			});
		}
		//current range req: max date is the current date
		function customRange(input) {
			return {
			    minDate: (input.id == 'endDate' ? $("#startDate").datepicker("getDate") : null),
			    maxDate: (input.id == 'startDate' ? $("#endDate").datepicker("getDate") : new Date())
			};
		}
		/** GET JSON DATA W/AJAX CALL **/
		function getBursarData(){
			//empty table if necessary
			if($("table.bursarDataGrid tr").length > 1){
				$("table.bursarDataGrid tbody").empty();
			}
			$("#emptyTable").remove(); //if the display message exists
			$('.bursar_data_container').hide(); //hide everything before new search
			$.blockUI({ message: '<img src="images/busy.gif" /> Loading Bursar Data...' });
			var startDate = $("#startDate").val();
			var endDate = $("#endDate").val();
			var ajaxParams = "";
			if(startDate !== undefined && startDate !== ""){
				ajaxParams = ajaxParams + "startDate="+startDate;
			}
			if(endDate !== undefined && endDate !== ""){
				var sep = "";
				if(ajaxParams !== undefined){
					sep = "&";
				}
				ajaxParams = ajaxParams + sep + "endDate="+endDate;
			}
			$.ajax({
				url: "/billing/servlets/GetBursarData",
				//url: "json.txt",
				dataType: "json",
				data: ajaxParams,
				error: function (xhr, desc, exceptionobj) {
					$.unblockUI();
					//add error message
					$.blockUI({ message: 'There was an error or timeout requesting the Bursar Data' });
					 setTimeout($.unblockUI, 2000);
				},
				success: function(data){
					initBursar(data);
				}
			});
		 
		 
		 
		 
		 
		 
		 
		 
		 
		 
		 
		 
		 
	}// END OF INITACTIONS
	
	//+++++++++++++++++++++  BURSAR ++++++++++++++++++++++++++
	
	function initBursar(data){
		if(data.total === 0){
			displayBursarEmptyData();
		}else{
			loadBursarGrid(data, initBursarHelpers);
		}
	}
	/** Show message when no data is returned from ajax call **/
	function displayBursarEmptyData(){
		$("#bursarGrid").append($("<div id='emptyTable'></div>").html("No data was returned from the date range specified"));
		$.unblockUI(); //unblock the ui
		$('.bursar_data_container').fadeIn('fast'); //show the new data
	}
	/** POPULATE HTML GRID **/
	function loadBursarGrid(data, callback){
		//go through each row, add to table
		$.each(data.rows, function(){
			appendBursarRow(this);
		});
		//CALL GRID HELPERS
		if ($.isFunction(callback)) {
			callback.apply(this,[data]);
		}
	}
	/** ADD EACH NEW ROW TO THE TABLE */
	function appendBursarRow(row){
		var tr = $("<tr></tr>")
			.append($("<td></td>").html($.trim(row.name)))
			.append($("<td></td>").html(row.session_activity_dt))
			.append($("<td></td>").html(row.pid))
			.append($("<td></td>").html(row.apply_amt))
			.append($("<td></td>").html($.trim(row.cashier_id)))
			.append($("<td></td>").html($.trim(row.document_nbr)))
			.append($("<td></td>").html(row.charge_detail_cd))
			.append($("<td></td>").html(row.charge_category_cd))
			.append($("<td></td>").html($.trim(row.payment_detail_cd)))
			.append($("<td></td>").html($.trim(row.payment_category_cd)));
		$("table.bursarDataGrid").append(tr);
	}
	/** INITIALIZE HELPER TABLE GRID FUNCTIONS **/
	function initBursarHelpers(data){
		updateDates(data);
		initBursarGrid();
		$.unblockUI(); //unblock the ui
		$('.bursar_data_container').fadeIn('fast'); //show the new data
	}
	/** SET THE CURRENT DATE VALES FROM JSON **/
	function updateDates(data){
		//HTML FIELDS
		$("#startDate").val(data.startDate);
		$("#endDate").val(data.endDate);
	}
	/** INITIALIZE GRID CSS **/
	function initBursarGrid(){
		$("table.bursarDataGrid tr:even").addClass("even");
		$("table.bursarDataGrid tr").hover(function () {
	        $(this).addClass("over");
	      }, 
	      function () {
	        $(this).removeClass("over");
	      }
		);	
	}
	function processBursarFile(){
		window.open("/billing/servlets/ProcessBursarData");
	}
	
	
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
//^^^^^^^^^^^^^^^SEARCH^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

	function displaySearchResults(data,status){
		$.unblockUI();
		$("#results2").empty(); 
		if(data['searchResultArraySize'] == 0){
			var table = $("<table border='1'></table>");
			var tableBody = $("<tbody></tbody>");
			tableBody.append($("<div id='emptyTable'></div>").html("No data"));
			}
		else
		{
			var table = $("<table border='1'></table>");
			var tableBody = $("<tbody></tbody>");
			$.each(data['searchResultArray'],function(){
				var row =  $("<tr></tr>");
				var col = $("<td></td>");
				var chkbox = $('<input type="checkbox" />').attr('id', 'chk2').attr('value', this.patronNo);
				col.append(chkbox);
				var col1 ;
				if(data.searchCriteria === 'Name')
				col1 =  $("<td></td>").html(this.patronName);
				else if(data.searchCriteria === 'PID')
					col1 =  $("<td></td>").html(this.PID);
				else if(data.searchCriteria === 'PatronRecNo')
					col1 =  $("<td></td>").html(this.patronNo);
				else if(data.searchCriteria === 'InvoiceNo')
					col1 =  $("<td></td>").html(this.invoiceNo);
				
				
				row.append(col).append(col1);
				tableBody.append(row);
			});
			$("#results2").css('height', '100px');
			$("#results2").css('overflow', 'auto');
			$("#results2").css('width', '50%');
			$("#results2").css('margin', '0');
			$("#results2").css('position', 'relative');
			$("#results2").css('padding-bottom', '10px');
		}
		table.append(tableBody);
		var p = $("<p></p>").html(table);
		$("#results2").append(p);
		$("#results2 tr:even").addClass("even");
		$("#results2 tr").hover(function () {
	        $(this).addClass("over");
	      }, 
	      function () {
	        $(this).removeClass("over");
	      }
		);
		
		return false;	
	}
	
	function displayPatronHistoryWindow(data,status){
		
		//var pname = null;
		var PID;
		var patron;
		var invoiceNo;
	    var userid;
	    var notes;
	    var pname;
	    var newPname;
	    var username;
		pname = data['basicData'].name;
		PID = data['basicData'].pid;
		patron = data['basicData'].patronRecordNo;
		notes = data['basicData'].notes;
		username = data.username;
		userid = data.userid;
		//alert(username);
		var html='';
		if(pname !== undefined)
		  newPname =pname.replace(" ", "") ;
		else
			newPname=pname;
	    
		html+='<html><head><link rel="stylesheet" type="text/css" href="css/popup.css" /></script>';
		html += '<script type="text/javascript" src="js/shared/jquery-1.2.6.pack.js"></script>';
		html += '<script type="text/javascript" src="js/shared/jquery.blockUI.js"></script>';
		html += '<script type="text/javascript" src="js/shared/jquery-ui-1.5.3.packed.js"></script>';
		html += '<script type="text/javascript" src="js/shared/jquery-ui-effects.packed.js"></script>';
		html += '<script type="text/javascript" src="js/shared/hoverIntent.js"></script>';
		html += '<script type="text/javascript" src="js/shared/jquery.field.js"></script>';
		html += '<script type="text/javascript" src="js/shared/Tokenizer.js"></script>';
		html += '<script type="text/javascript" src="js/search.js"></script>';	
		html += '<script type="text/javascript" src="js/shared/loadMenu.js"></script>';
		html += '<script language="Javascript" type="text/javascript">';
		//html += 'var inv ; ';
		//html += 'var size;';
		//html += 'var invoiceArr;';
		html+= 'function CallParentWindowFunction(){ ';
		html += 'var ppid = "'+PID+'";';
		html += 'var ppat = "'+patron+'";';
		html += 'var strpid = document.getElementById("txtpid").value;';
		html += 'var strpatron = document.getElementById("txtpat").value;';
		html += 'var strnote = document.getElementById("txtnote").value;';
		//html += 'var strnoteMod = strnote.replace(/^\s*/, "").replace(/\s*$/, "");';
		html += 'var strnoteMod = strnote.replace(/^\s+/g,"").replace(/\s+$/g,"");';
		html += 'if(strnoteMod.length >400) {alert("Note cannot be > 400 characters!");return false;}';

		html += 'var strname = document.getElementById("txtname").value;';
		html += 'var flagNote = false;if(strnoteMod.length > 0  ){flagNote = true;};';
		html += 'var flagPatron = true;if(ppat === strpatron){flagPatron = false;};';
		html +=  'var flagPID = true;if (ppid === strpid){flagPID = false;}window.opener.saveDataFromViewPatronHistory(strpid,strpatron,strnote,flagPID,flagNote,flagPatron,ppat);window.close();return true;}';
		
		//var size = '+size+'; 
		
		html += '</script>';
		
		html += '<script language="Javascript" type="text/javascript">';
		html+= 'function getNote(){ var selector_checked = $("input[@id=chkboxinvNote2]:checked").length; if(selector_checked == 0){ alert("Please select a record first!"); return false;} ';

	   html+= 'else if(selector_checked == 1){ var invNo =$("input[@id=chkboxinvNote2]:checked").val();';
	   //html += 'window.opener.getInvoiceNotes(invNo); ';
	   html += '$.blockUI({ message:"Getting invoice notes..." });';
	   //html += '$().ajaxStart($.blockUI).ajaxStop($.unblockUI);';
	  
	   //9/14
	   //var ajaxcall = '$.ajax({url: "/billing/servlets/GetInvoiceNotes", dataType: "json",data:{invNo:invNo},success:function(data){ $.unblockUI();if(data.sizeInvoiceNoteArray === 0) {$("#addNote").empty();var tableBody =$("<tbody></tbody>").html("No data");var p = $("<p></p>").html(tableBody);$("#addNote").append(p);return true;} else {var tableBody =$("<tbody></tbody>");$("#addNote").empty(); $.each(data["invoiceNoteArray"],function(){ var row =  $("<tr></tr>");var col1 =  $("<td></td>").html(this.date);col1.css("padding", "6px 11px"); var col2 =  $("<td></td>").html(this.username);col2.css("padding", "6px 11px");var col3 =  $("<td></td>").html(this.notes);row.append(col1).append(col2).append(col3);tableBody.append(row);}); var p = $("<p></p>").html(tableBody); $("#addNote").append(p);return true;}},error:function (data, status, e){$.unblockUI();$.blockUI({ message: "There was an error or timeout when calling GetInvoiceNotes servlet" }); setTimeout($.unblockUI, 2000);}});';
	  
	   
	   var ajaxcall = '$.ajax({url: "/billing/servlets/GetInvoiceNotes", dataType: "json",data:{invNo:invNo},success:function(data){ $.unblockUI();if(data.sizeInvoiceNoteArray === 0) {$("#addNote").empty();var tableBody =$("<tbody></tbody>").html("No data");var p = $("<p></p>").html(tableBody);$("#addNote").append(p);return true;} else {var tableBody =$("<tbody></tbody>");$("#addNote").empty(); $.each(data["invoiceNoteArray"],function(){ var row =  $("<tr></tr>");var col1 =  $("<td></td>").html(this.date);col1.css("padding", "6px 11px"); var col2 =  $("<td></td>").html(this.username);col2.css("padding", "6px 11px");var col3 =  $("<td></td>").html(this.notes);col3.css("padding", "6px 11px");var col4 =  $("<td></td>").html(this.expDesc);col4.css("padding", "6px 11px");var col5 =  $("<td></td>").html(this.comDesc);col5.css("padding", "6px 11px");var col6 =  $("<td></td>").html(this.resDesc);col6.css("padding", "6px 11px"); row.append(col1).append(col2).append(col3).append(col4).append(col5).append(col6);tableBody.append(row);}); var p = $("<p></p>").html(tableBody); $("#addNote").append(p);$("#addNote").css("overflow", "auto");$("#addNote").css("position", "relative");$("#addNotetr:even").addClass("even");$("#addNotetr").hover(function(){$(this).addClass("over");}, function(){$(this).removeClass("over");});return true;}},error:function (data, status, e){$.unblockUI();$.blockUI({ message: "There was an error or timeout when calling GetInvoiceNotes servlet" }); setTimeout($.unblockUI, 2000);}});';
	   html += ajaxcall;
	   //html += 'testA();';
	   html+=  'return false;}else{alert("Please select one record at a time");}}//end of getNote()';
	   
	  html += '</script>';    
		    
	  
	   
	    html += '<script language="Javascript" type="text/javascript">';
	    html+= 'function AddNotes(){ ';
	    html += 'var selector_checked = $("input[@id=chkboxinvNote2]:checked").length; if(selector_checked == 0){ alert("Please select a record first!"); return false;}';
	    html += 'else if(selector_checked == 1){ ';
	    html += 'var inv =$("input[@id=chkboxinvNote2]:checked").val();';
	    html += 'document.getElementById("txtStaff").value='+"'"+username+"'"+';';
	    //html += 'document.getElementById("txtDate").value= '+today+';';
	    html += 'return true;} else{alert("Please select one record at a time");}}//end of function addNotes';
	    html += '</script>';
	    
	    html += '<script language="Javascript" type="text/javascript">';
	    html+= 'function AddNewNotes(){ ';
	    html += 'var inv = $("input[@id=chkboxinvNote2]:checked").val();';
		html += 'var strNote = document.getElementById("txtNewNote").value;';
		html += 'var strUserId = document.getElementById("txtStaff").value;';
		html += 'var strexp = document.getElementById("expdropdown").value;';
		html += 'var strres = document.getElementById("resdropdown").value;';
		html += 'var strcom = document.getElementById("comdropdown").value;';
	    html += 'document.getElementById("txtNewNote").value= " ";';
	    html += 'document.getElementById("txtStaff").value= " ";';
	    //html += 'document.getElementById("txtDate").value= " ";';
	    var ajaxStat = '$.ajax({url: "/billing/servlets/InsertInvoiceNotes",dataType: "json",data:{strUserId:strUserId,inv:inv,strNote:strNote,strexp:strexp,strres:strres,strcom:strcom},success: function(data){if (data.flag === true)alert("Successfully inserted!"); else alert("Error in Insertion!");},error:function (data, status, e){$.unblockUI();$.blockUI({ message: "There was an error or timeout when calling InsertInvoiceNotes servlet" }); setTimeout($.unblockUI, 2000);}});';
	   // html += 'window.opener.insertInvoinceNote(strUserId,inv,strNote,strexp,strres,strcom);';
	    html += ajaxStat;
	    html += 'return true;}//end of function AddNewNotes';
		html += '</script>';

		html += '<TITLE>Edit Problem Queue Record</TITLE></head><body><div>';
		
			
		html+= '<fieldset><legend>Patron </legend>';
		html+= '<table border="0"> <tr><td>Name(exactly like Innopac record)</td></tr> <tr><td><input type="text" id="txtname" size="55" value= '+newPname+' readonly="readonly"> </td></tr></table>';
		html+='<table><tr><td>PID</td><td>Patron No</td><td></td><td></td></tr>';
		html+='<tr><td><input type="text" id="txtpid" size="9" maxlength="9" value= '+PID+'></td> <td><input type="text" id="txtpat" size="8" value= '+patron+'></td>';
		html+='<td><input type="button" value="Ok" onclick="CallParentWindowFunction()"></td> ';
		html+='<td><input type="button" value="Cancel" onclick="window.close()"></td></tr> </table>';
		html+='<table><tr><td>Notes</td></tr>';
		html+='<tr><td><textarea cols="40" rows="3" id="txtnote">'+notes+'</textarea></td></tr></table></fieldset>';
		
		
		html+='<br></div>';
		html+= '<fieldset><legend>Transactions </legend>';
		html+='<div id = "transaction">';
		html+='<table class="dataGrid"><thead><tr><th width="1%"></th><th>Invoice #</th><th>Invoice Date</th><th>Total Charge</th><th>Item Barcode</th></tr></thead>';
		html+='<tbody>';
		$.each(data['transactionList'],function(){
			html += '<tr>';
			html+='<td><input type="checkbox" id="chkboxinvNote2" value='+this.invoiceNo+'></td> <td>'+this.invoiceNo+'</td><td>'+this.date+'</td><td>'+this.amount+'</td><td>'+this.barcode+'</td></tr>';
			});
		
		html+='<br></div></table></fieldset><table><tr><td><input type="button" value="GetNote" onclick="getNote()"></td>';
		html += '<td><input type="button" value="AddNotes" onclick="AddNotes()"></td></tr></tbody></table>';
		//----------------------------
		// div for adding a new note
		html += '<div id = "newnote">';
		html += '<fieldset><legend>Add New Note </legend>';
		//html += '<table border="0"><tr><td>Added</td><td><input type="text" id="txtDate" size="12"></td>';
		html += '<table border="0"><tr>';
		html += '<td>Explanation</td><td><select id="expdropdown">';
		$.each(data['explanationIDArray'],function(){
			html+='<option value='+this.id+'>'+this.explanation+'</option>';
			});
		html += '</select></td></tr>';
		html += '<tr><td>Staff</td><td><input type="text" id="txtStaff" size="20" ></td>';
		html += '<td>Response</td><td><select id="resdropdown">';
		$.each(data['responseIDArray'],function(){
			html+='<option value='+this.id+'>'+this.response+'</option>';
			});
		html += '</select></td></tr>';
		html += '<tr><td>Note</td><td><input type="text" id="txtNewNote" size="50"></td>';
		html += '<td>Communication</td><td><select id="comdropdown">';
		$.each(data['communicationIDArray'],function(){
			html+='<option value='+this.id+'>'+this.communication+'</option>';
			});
		html += '</select></td></tr>';
		//html += '<tr><td>input type="button" value="Ok" onclick=""></td>';
		html += '<tr><td><input type="button" value="OK" onclick="AddNewNotes()"></td></tr>';
		html += '</table></fieldset>';	
		html += '</div>';
		
		//-------------------------
		html+= '<fieldset><legend>Notes </legend>';
		html+='<div id = "notes">';
		//html+='<table class="dataGrid"><thead><tr><th width = 65%>Added</th><th width = 20%>Staff</th><th width = 35%>Note</th></tr></thead></table>';
		//html+='<tbody></tbody>';
		html+='<table class="dataGrid"><thead><tr><th width = 15%>Added</th><th width = 15%>Staff</th><th width = 20%>Note</th><th width = 20%>Explanation</th><th width = 20%>Communication</th><th width = 20%>Response</th></tr></thead></table>';

		html+='<div id = "addNote"></div> </div>';
		html += '</fieldset>';
		
	    html+='</body></html>';
		win2 = window.open("", "window2", "width=880,height=850,scrollbars=yes "); 
		var tmp = win2.document;
		tmp.write(html);
		tmp.close();
		win2.focus();
		document.body.style.cursor = 'default'; 
	}



	
	
//}); //big function





function saveDataFromViewPatronHistory(pid,strpatron,strnote,flagPID,flagNote,flagPatron,ppat)
{

	if(pid.length > 9)
		{
			alert("PID you entered exceed the limit of lenth 9!!!!")
		}
		if(strpatron.length >8)
		{
			alert("Patron no you entered exceed the limit of lenth 8!!!!")
		}
	
	$.ajax({
		url: "/billing/servlets/ModifyBasicDataPatHistory", 
		dataType: 'json',
		data: {pid:pid,note:strnote,patronNo:strpatron,flagPID:flagPID,flagNote:flagNote,flagPatron:flagPatron,oldPatronNo:ppat},
		success: displayPatronHistoryAfterEdit,
		error:function (data, status, e){
			$.unblockUI();
			$.blockUI({ message: 'There was an error or timeout when calling EditProblemQueueData servlet' });
			 setTimeout($.unblockUI, 2000);
		}
	});	
}

function displayPatronHistoryAfterEdit(data,status){
	$.unblockUI();
	//$("#results").empty();
	if(data.status === "fail"){
		alert("Database update failed!");
	}
	else if(data.status === "success"){
		alert("Database update success!");
	}
	else if(data.status ===  "nochange")
	{
		
		alert("NO changes made!");
	}
	/*var searchval= $("#txtSearch").val();
	var searchCriteria = $('input[name=group1]:checked').val() ;
	
	//alert(searchCriteria);
	$.ajax({
		url: "/billing/servlets/GetSearchResults", 
		dataType: 'json',
		data:{searchval:searchval,searchCriteria:searchCriteria},
		success: displaySearchResults,
		error:function (data, status, e){
			$.unblockUI();
			//add error message
			$.blockUI({ message: 'There was an error or timeout when caling GetSearchResults servlet' });
			 setTimeout($.unblockUI, 2000);
		}
	});	*/
			
}
	
	//^^^^^^^^^^^^^^^^^^END SEARCH^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

//&&&&&&&&&&&&&&&&&&& SESSION  &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
function displayAfterResubmit(data,status)
{
	if(data.flag === false){
		alert(data.errorMessage);
	}
	else
	{
		jsonData = data;
		init(data.sessionData);
		
		
		
	}
}
function displaySessionDates(data,status){
	$('#indexdiv').hide();
	$('#searchDiv').hide();
	$("#dateCombo").empty();
	//$("#dateCombo").hide();
	var dropSession =  $('<select />').attr('id', 'dropdownSession');  
	//var chkbox = $('<input type="checkbox" />').attr('id', 'chk').attr('value', this.invoiceNo);
	if(data['sessiondates'] !== undefined){
		sessionData = data['sessiondates'];
		$.each(data['sessiondates'],function(){
			//$('#selectDate').append('<option value='+"'"+this.date+"'"+'>'+"'"+this.date+"'"+'</option>');
			dropSession.append('<option value='+this.date+'>'+this.date+'</option>');
		});	
	}
	else{
		//add error message
		$.blockUI({ message: 'No Session Data available' });
		 setTimeout($.unblockUI, 2000);
	}
	//$('.container').fadeIn('fast'); 
	var p = $("<p></p>").html(dropSession);
	$("#dateCombo").append(p);
	$('#sessionDiv').show();
	return false;
}


/** INITIALIZE GRID LOADING **/
function init(data){
	if($("table.dataGrid tr").length > 1){
		$("table.dataGrid tbody").empty();
	}
	if(data.total === 0){
		displayEmptyData();
	}else{
		loadGrid(data, initHelpers);
	}
}
/** Show message when no data is returned from ajax call **/
function displayEmptyData(){
	$("#grid").append($("<div id='emptyTable'></div>").html("No data was returned for the selected date"));
	$.unblockUI(); //unblock the ui
	$('.data_container2').fadeIn('fast'); //show the new data
	
}
/** POPULATE HTML GRID **/
function loadGrid(data, callback){
	//go through each row, add to table
	$.each(data.rows, function(){
		appendRow(this);
	});
	//CALL GRID HELPERS
	if ($.isFunction(callback)) {
		callback.apply(this,[data]);
	}
}
/** ADD EACH NEW ROW TO THE TABLE */

function appendRow(row){
	/*var tr = $("<tr></tr>")
		.append($("<td></td>").html($('<input type="checkbox" />').attr('id', 'chkSession').attr('value', this.pendingID)))
		.append($("<td></td>").html($.trim(row.name)))
		.append($("<td></td>").html(row.pid))
		.append($("<td></td>").html(row.amount1))
		.append($("<td></td>").html(row.amount2))
		.append($("<td></td>").html(row.amount3))
		.append($("<td></td>").html(row.invoiceNo));
	$("table.dataGrid").append(tr);
	*/
	//================
	var rows =  $("<tr></tr>");
	var col = $("<td></td>");
	//alert(this.pendingID);
	//var chkbox = $('<input type="checkbox" />').attr('id', 'chkSession').attr('value', this.pendingID);
	var chkbox = $('<input type="checkbox" id ="chkSession" value = '+row.pendingID+' />')
	col.append(chkbox);
	var col1 =  $("<td></td>").html(row.name);
	var col2 =  $("<td></td>").html(row.pid);
	var col3 =  $("<td></td>").html(row.amount1);
	var col4 =  $("<td></td>").html(row.amount2);
	var col5 =  $("<td></td>").html(row.amount3);
	var col6 =  $("<td></td>").html(row.invoiceNo);
	//var col7 =  $("<td></td>").html(this.rule);
	rows.append(col).append(col1).append(col2).append(col3).append(col4).append(col5).append(col6);
	$("table.dataGrid").append(rows);
	//==============
}


/** INITIALIZE HELPER TABLE GRID FUNCTIONS **/
function initHelpers(data){
	 //updateDates(data);
	initGrid();
	$.unblockUI(); //unblock the ui
	$('.data_container2').fadeIn('fast'); //show the new data
}

/** INITIALIZE GRID CSS **/
function initGrid(){
	$("table.dataGrid tr:even").addClass("even");
	$("table.dataGrid tr").hover(function () {
        $(this).addClass("over");
      }, 
      function () {
        $(this).removeClass("over");
      }
	);	
}

//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
	
	//TEST
	function testUI(){
		$.blockUI({ message: '<img src="images/busy.gif" /> Processing Billing File...' });
		$("#results").empty(); //if the display message exists
		$('.container').hide(); //hide everything before new search
		$.ajax({
			url: "billingTest.txt",
			dataType: "json",
			error: function (xhr, desc, exceptionobj) {
				$.unblockUI();
				//add error message
				$.blockUI({ message: 'There was an error or timeout requesting the Bursar Data', timeout: 2000 });
				 //setTimeout($.unblockUI, 2000);
			},
			success: displayBillingQueues
		});
	}

	function displayBillingQueues(data,status){
		$.unblockUI();

	
		if(data.result === "success"){
			jsonData = data;
			//$("#results").append("<b>Billing Text Results</b>");
			if(data['pending'] !== undefined){
				
				$('#indexdiv').show();
				$('#buttonsPending').show();
				//$('#actions').hide();
				$('#filediv').hide();
                var p=data['pendingTotal'];
				//alert("DisplayBillingQueue PENDING: total:"+p);
				showQueue("Pending Queue",data['pending'],data['pendingTotal']);
				
				//showButtons("Pending Queue");
				
				//$("#buttons").visibility ="visible";
				//document.getElementById("results").style.visibility = "visible";
			}
			else
			{
				$('#filediv').hide();
				showQueue("Pending Queue",data['pending'],0);
			}
			/*if(data['problem'] !== undefined){
				showQueue("Problem Queue",data['problem']);
			}
			if(data['newPatron'] !== undefined){
				showQueue("New Patron Queue",data['newPatron']);
			}*/
			$('.container').fadeIn('fast'); //show the new data
		}else{
			//add error message
			$.blockUI({ message: 'There was no data returned from processing the Billing File' });
			 setTimeout($.unblockUI, 2000);
		}
	}
	
	function showQueue(type, data,total){
		//results div
		//create fieldset, legend, table - 2 rows -> record, rule
		//append to #results
		if($("#results tr").length > 1){
			$("#results").empty();
		}
		$("#emptyTable").remove();
		//+++++++++++++++++++++++++++++++++++++++++++
		if (type === 'Pending Queue'){
		var fieldSet = $("<fieldset></fieldset>");
		var legend = $("<legend></legend>").html("<b>"+type+"</b>");
		var table = $("<table border='1'><thead><tr><th width='1%'></th><th width='15%'>Paytron Name</th><th width='10%'>PID</th><th width='8%'>Charge</th><th width='10%'>Processing Fee</th>" +
				"<th width='10%'>Billing Fee</th>" +
				"<th width='10%'>Invoice #</th>" +
				"</tr></thead></table>");
		var tableBody = $("<tbody></tbody>");
		var tot = total;
		//alert("tot = ",tot);
		if ( total == 0)
		{
			//alert("total === 0");
			tableBody.append($("<div id='emptyTable'></div>").html("No data"));
			
			$("#results").css('height', '500px');
			//$("#results").css('overflow', 'auto');
			$("#results").css('width', '100%');
			$("#results").css('margin', '0');
			$("#results").css('position', 'relative');
			$("#results").css('padding-bottom', '10px');
			
			
			
			
			table.append(tableBody);		    
			var p = $("<p></p>").html(fieldSet.append(legend).append(table));
			$("#results").append(p);
			//$("#results").css('height', '110px');
			//$("#results").css('width', '90%');
			//$("#results").css('padding-bottom', '10px');
			//$("#results").css('margin', '0');
			//$("#results").css('position', 'relative');
			
			//$("#results").css('overflow', 'visible');
			//var row =  $("<tr></tr>").html("No data");
			//var col = $("<td></td>").html("No data");
			//row.append(col);
			//tableBody.append(row);
			//tableBody.html("No data");
		}
		else
		{
			
		$.each(data,function(){
			var row =  $("<tr></tr>");
			var col = $("<td></td>");
			var valCombined = this.invoiceNo+'|'+this.chargeType;
			var chkbox = $('<input type="checkbox" />').attr('id', 'chk').attr('value',valCombined );
			col.append(chkbox);
			var col1 =  $("<td></td>").html(this.name);
			var col2 =  $("<td></td>").html(this.pid);
			var col3 =  $("<td></td>").html(this.amount1);
			var col4 =  $("<td></td>").html(this.amount2);
			var col5 =  $("<td></td>").html(this.amount3);
			var col6 =  $("<td></td>").html(this.invoiceNo);
			//var col7 =  $("<td></td>").html(this.rule);
			row.append(col).append(col1).append(col2).append(col3).append(col4).append(col5).append(col6);
			tableBody.append(row);
		});
		$("#results").css('height', '500px');
		$("#results").css('overflow', 'auto');
		$("#results").css('width', '100%');
		$("#results").css('margin', '0');
		$("#results").css('position', 'relative');
		$("#results").css('padding-bottom', '10px');
		}
		table.append(tableBody);
	    
		var p = $("<p></p>").html(fieldSet.append(legend).append(table));
		$("#results").append(p);
		$("#results tr:even").addClass("even");
		$("#results tr").hover(function () {
	        $(this).addClass("over");
	      }, 
	      function () {
	        $(this).removeClass("over");
	      }
		);	
		}//end of if
		//+++++++++++++++++++++++++++++++++++++++++++
		else{
			
			var fieldSet = $("<fieldset></fieldset>");
			var legend = $("<legend></legend>").html("<b>"+type+"</b>");
			var table = $("<table border='1'><thead><tr><th width='1%'></th><th width='15%'>Paytron Name</th><th width='10%'>PID</th><th width='8%'>Charge</th><th width='10%'>Processing Fee</th>" +
					"<th width='10%'>Billing Fee</th>" +
					"<th width='10%'>Invoice #</th>" +
					"<th width='12%'>Rule</th></tr></thead></table>");
			var tableBody = $("<tbody></tbody>");
			var tot = total;
			//alert("tot = ",tot);
			if ( total == 0)
			{
				//alert("total === 0");
				tableBody.append($("<div id='emptyTable'></div>").html("No data"));
				//$("#results").css('height', '110px');
				//$("#results").css('width', '90%');
				//$("#results").css('padding-bottom', '10px');
				//$("#results").css('margin', '0');
				//$("#results").css('position', 'relative');
				
				//$("#results").css('overflow', 'visible');
				//var row =  $("<tr></tr>").html("No data");
				//var col = $("<td></td>").html("No data");
				//row.append(col);
				//tableBody.append(row);
				//tableBody.html("No data");
				
				$("#results").css('height', '500px');
				//$("#results").css('overflow', 'auto');
				$("#results").css('width', '100%');
				$("#results").css('margin', '0');
				$("#results").css('position', 'relative');
				$("#results").css('padding-bottom', '10px');
				table.append(tableBody);
			    
				var p = $("<p></p>").html(fieldSet.append(legend).append(table));
				$("#results").append(p);
			}
			else
			{
				
			$.each(data,function(){
				var row =  $("<tr></tr>");
				var col = $("<td></td>");
				var valCombined = this.invoiceNo+'|'+this.chargeType;
				var chkbox = $('<input type="checkbox" />').attr('id', 'chk').attr('value', valCombined);
				col.append(chkbox);
				var col1 =  $("<td></td>").html(this.name);
				var col2 =  $("<td></td>").html(this.pid);
				var col3 =  $("<td></td>").html(this.amount1);
				var col4 =  $("<td></td>").html(this.amount2);
				var col5 =  $("<td></td>").html(this.amount3);
				var col6 =  $("<td></td>").html(this.invoiceNo);
				var col7 =  $("<td></td>").html(this.rule);
				row.append(col).append(col1).append(col2).append(col3).append(col4).append(col5).append(col6).append(col7);
				tableBody.append(row);
			});
			$("#results").css('height', '500px');
			$("#results").css('overflow', 'auto');
			$("#results").css('width', '100%');
			$("#results").css('margin', '0');
			$("#results").css('position', 'relative');
			$("#results").css('padding-bottom', '10px');
			}
			table.append(tableBody);
		    
			var p = $("<p></p>").html(fieldSet.append(legend).append(table));
			$("#results").append(p);
			$("#results tr:even").addClass("even");
			$("#results tr").hover(function () {
		        $(this).addClass("over");
		      }, 
		      function () {
		        $(this).removeClass("over");
		      }
			);
			
		}//end 0f else
		
		
		
		
		//******************************************
	
		
	}
	
	//====================FINAL========================
	
	function displayProbQueue(data,status){
		$.unblockUI();

	
		if(data.result === "success"){
			jsonData = data;
			//$("#results").append("<b>Billing Text Results</b>");
			if(data['problem'] !== undefined){
				$('#buttonsPending').hide(); 
				$('#buttonsNewPatron').hide();
				$('#buttonsProblem').show();			
				var pp = jsonData['problemTotal'];
				showQueue("Problem Queue",data['problem'],data['problemTotal']);
			}
			else
			{
				$('#buttonsProblem').hide();
				$('#buttonsNewPatron').hide();
				$('#buttonsPending').hide();
				showQueue("Problem Queue",data['problem'],0);
			}
			
			$('.container').fadeIn('fast'); //show the new data
		}else{
			//add error message
			$.blockUI({ message: 'No Data found!' });
			 setTimeout($.unblockUI, 2000);
		}
	}

	function displayNewPQueue(data,status){
		$.unblockUI();
	
		if(data.result === "success"){
			jsonData = data;
			if(data['newPatron'] !== undefined){
				$('#buttonsPending').hide();
				$('#buttonsProblem').hide();
				$('#buttonsNewPatron').show();
				//$('#actions').hide();
				var pp = data['newPatronTotal'];
				//alert("new paytron: total:"+pp);
				showQueue("New Patron Queue",data['newPatron'],data['newPatronTotal']);
			}
			else
			{
				$('#buttonsProblem').hide();
				$('#buttonsNewPatron').hide();
				$('#buttonsPending').hide();
				showQueue("New Patron Queue",data['newPatron'],0);
			}
			$('.container').fadeIn('fast'); //show the new data
		}else{
			//add error message
			$.blockUI({ message: 'No Data found!' });
			 setTimeout($.unblockUI, 2000);
		}
	}

	
	//==============================================
	
	//$$$$$$$$$DIALOG BOX STUFF$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	//==============================
	function displayPendingQueueConfirmNo(jsonData){
		
		
		$("#results").empty();
		$('.container').hide(); 
		if(jsonData.result === "success"){
			if(jsonData['pending'] !== undefined){
				$('#buttonsPending').show();
				$('#buttonsProblem').hide();
				$('#buttonsNewPatron').hide();
				$('#actions').hide();
				showQueue("Pending Queue",jsonData['pending'],jsonData['pendingTotal']);
				$('.container').fadeIn('fast'); //show the new data
			}else{
				//add error message
				$.blockUI({ message: 'There was no data returned from processing the Billing File' });
				 setTimeout($.unblockUI, 2000);
			}
	}
	}


	function displayPendingQueueConfirmNoProblem(jsonData){
		
		
		$("#results").empty();
		$('.container').hide(); 
		if(jsonData.result === "success"){
			if(jsonData['problem'] !== undefined){
				$('#buttonsPending').hide();
				$('#buttonsNewPatron').hide();
				$('#buttonsProblem').show();
				$('#actions').hide();
				showQueue("Problem Queue",jsonData['problem'],jsonData['problemTotal']);
				$('.container').fadeIn('fast'); //show the new data
			}else{
				//add error message
				$.blockUI({ message: 'There was no data returned from processing the Billing File' });
				 setTimeout($.unblockUI, 2000);
			}
	}
	}
	
function displayPendingQueueConfirmNoNewPatron(jsonData){
		
		
		$("#results").empty();
		$('.container').hide(); 
		if(jsonData.result === "success"){
			if(jsonData['newPatron'] !== undefined){
				$('#buttonsPending').hide();
				$('#buttonsNewPatron').show();
				$('#buttonsProblem').hide();
				$('#actions').hide();
				showQueue("New Patron Queue",jsonData['newPatron'],jsonData['newPatronTotal']);
				$('.container').fadeIn('fast'); //show the new data
			}else{
				//add error message
				$.blockUI({ message: 'There was no data returned from processing the Billing File' });
				 setTimeout($.unblockUI, 2000);
			}
	}
	}

	//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
	/** INITIALIZE HELPER TABLE GRID FUNCTIONS **/
/*	function initHelpers(data){
		initGrid();
		$.unblockUI(); //unblock the ui
		$('.container').fadeIn('fast'); //show the new data
	}
	/** INITIALIZE GRID CSS **/
/*	function initGrid(){
		$("table.dataGrid tr:even").addClass("even");
		$("table.dataGrid tr").hover(function () {
	        $(this).addClass("over");
	      }, 
	      function () {
	        $(this).removeClass("over");
	      }
		);	
	}
	*/
	function processOutputFile(){
		window.open("/billing/servlets/ProcessOutputData");
	}
  function sendDataToServer(){
	  window.open("/billing/servlets/SendOutputFiles");
  }
	
function displayPendingQueueAfterDelete(data,status){
	
	$.unblockUI();
	//$.blockUI({ message: '<img src="images/busy.gif" /> Deleting records...' });
	$("#results").empty();
	$('.container').hide(); 
	if(data.result === "success"){
		jsonData = data;
		//$("#results").append("<b>Billing Text Results</b>");
		if(data['pending'] !== undefined){
			$('#buttonsPending').show();
			$('#buttonsProblem').hide();
			$('#buttonsNewPatron').hide();
			$('#actions').hide();
			showQueue("Pending Queue",data['pending'],data['pendingTotal']);
			$('.container').fadeIn('fast'); //show the new data
		}else{
			//add error message
			$.blockUI({ message: 'There was no data returned from processing the Billing File' });
			 setTimeout($.unblockUI, 2000);
		}
}
}

//show Problem Queue after delete or move
function displayProblemQueueAfterDelete(data,status){
	$.unblockUI();
	//$.blockUI({ message: '<img src="images/busy.gif" /> Deleting records...' });
	$("#results").empty();
	$('.container').hide(); 
	if(data.result === "success"){
		jsonData = data;
		//$("#results").append("<b>Billing Text Results</b>");
		if(data['problem'] !== undefined){
			$('#buttonsPending').hide();
			$('#buttonsNewPatron').hide();
			$('#buttonsProblem').show();
			$('#actions').hide();
			showQueue("Problem Queue",jsonData['problem'],jsonData['problemTotal']);
			$('.container').fadeIn('fast'); //show the new data
		}else{
			//add error message
			$.blockUI({ message: 'There was no data returned from processing the Billing File' });
			 setTimeout($.unblockUI, 2000);
		}
}
}

//show New patron Queue after delete or move
function displayNewPatronQueueAfterDelete(data,status){
	$.unblockUI();
	//$.blockUI({ message: '<img src="images/busy.gif" /> Deleting records...' });
	$("#results").empty();
	$('.container').hide(); 
	if(data.result === "success"){
		jsonData = data;
		//$("#results").append("<b>Billing Text Results</b>");
		if(data['newPatron'] !== undefined){
			$('#buttonsPending').hide();
			$('#buttonsNewPatron').show();
			$('#buttonsProblem').hide();
			$('#actions').hide();
			showQueue("New Patron Queue",jsonData['newPatron'],jsonData['newPatronTotal']);
			$('.container').fadeIn('fast'); //show the new data
		}else{
			//add error message
			$.blockUI({ message: 'There was no data returned from processing the Billing File' });
			 setTimeout($.unblockUI, 2000);
		}
}
}

// show the edit window to new patron queue
function displayEditWindow(data,status){
	
	var pname;
	var PID;
	var patronTemp;
	var patron;
	var invoiceNo;
	var chargeType;
	var combined;
	pname = data.name;
	PID = data.pid;
	patron = data.patronRecordNo;
	invoiceNo = data.invoiceNo;
	chargeType = data.chargeType;
	combined = invoiceNo + '|' + chargeType;

	if(pname == undefined)
		return;

    var html='';
    
	html += '<!DOCTYPE html><html><head><link rel="stylesheet" type="text/css" href="css/overlay.css" />';
	html += '<script type="text/javascript" src="js/shared/jquery-1.2.6.pack.js"></script>';
	html += '<script type="text/javascript" src="js/shared/jquery.blockUI.js"></script>';
	html += '<script type="text/javascript" src="js/shared/jquery-ui-1.5.3.packed.js"></script>';
	html += '<script type="text/javascript" src="js/shared/jquery-ui-effects.packed.js"></script>';
	html += '<script type="text/javascript" src="js/shared/hoverIntent.js"></script>';
	html += '<script type="text/javascript" src="js/shared/jquery.field.js"></script>';
	html += '<script type="text/javascript" src="js/shared/Tokenizer.js"></script>';
	html += '<script type="text/javascript" src="js/billing.js"></script>';	
	html += '<script type="text/javascript" src="js/shared/loadMenu.js"></script>';
	html += '<script type="text/javascript">';
	html += 'function CallParentWindowFunction(){var selector_checked = $("input[@id=chkboxVerify]:checked").length; ';
	html += ' if(selector_checked == 0){ alert("Please select Verified against ISIS!"); return false;}' ;
    html += ' else {var str = document.getElementById("txtpid").value;window.opener.pid = str;window.opener.invoiceNo = document.getElementById("chkboxVerify").value; window.opener.test(str);window.close();return true;}}';
	html += 'function CallTest(){var str = document.getElementById("txtpid").value; window.opener.test(str);}</script>';
 
	html += '<TITLE>Edit New Patron record</TITLE></head><body><div>';
	
	html +='Patron Name :' + pname;
	html +='<br>';
	
	html += '<fieldset><legend>Patron No:' + patron + '</legend>';
	html += '<table cellpadding="0" cellspacing="0" border="1"> <tr><td>PID: </td><td><input type="text" id="txtpid" size="9" value= ' + PID + '> </td></tr>';
	html += '<tr><td><input type="checkbox" id="chkboxVerify" value= ' + combined + '></td> <td>Verified against ISIS</td></tr>';
	html += '<tr><td><input type="button" value="Ok" onclick="CallParentWindowFunction()"></td> ';
	html += '<td><input type="button" value="Cancel" onclick="window.close()"></td></tr> </table></fieldset>';
	
	html += '<br>';
    html += '</div></body></html>';
	
    var w = 480, h = 340;
	 if (document.all) {
		   /* the following is only available after onLoad */
		   w = document.body.clientWidth;
		   h = document.body.clientHeight;
		}
		else if (document.layers) {
		   w = window.innerWidth;
		   h = window.innerHeight;
		}
	 var popW = 400, popH = 250;
	 var leftPos = (w-popW)/2, topPos = (h-popH)/2;

	 wleft = (screen.width - w) / 2;
	  wtop = (screen.height - h) / 2;
	  
	  //win3 = window.open("","window2",'width=' + popW + ',height='+popH+',top='+wtop+',left='+wleft);
		win2 = window.open("", "window2", "width=600,height=350,scrollbars=yes"); 
		var tmp = win2.document;
		tmp.write(html);
		tmp.close();
			//win2.moveTo(wleft, wtop);
			win2.focus();
			document.body.style.cursor = 'default'; 
	  
	  
	/*  
	  
	win2 = window.open("", "window2", "width=600,height=350,scrollbars=yes"); 
	var tmp = win2.document;
	tmp.write(html);
	tmp.close();
	win2.focus();
	document.body.style.cursor = 'default'; 
	*/
}


//show the edit window to new patron queue
function displayEditWindowForProblemQueue(data,status){
	
	var pname;
	var newPname;
	var PID;
	var patronTemp;
	var patron;
	var invoiceNo;
	var notes;
	var userid;
	var today;
	var array = null;
	var username;
	pname = data['basicData'].name;
	PID = data['basicData'].pid;
	patron = data['basicData'].patronRecordNo;
	invoiceNo = data['basicData'].invoiceNo;
	notes=data['basicData'].notes;
	//size = data.size;
	//array = data['invoiceNoteArray'];
	//arrayInvoice = data['invoiceNoteArray'];
	username=data.username;
	userid = data.userid;
	today = data.today;
	myString = invoiceNo;
	var html='';
	if(pname !== undefined)
		  newPname =pname.replace(" ", "") ;
		else
			newPname=pname;
    
	html += '<!DOCTYPE HTML><html><head><link rel="stylesheet" type="text/css" href="css/popup.css" /></script>';
	html += '<script type="text/javascript" src="js/shared/jquery-1.2.6.pack.js"></script>';
	html += '<script type="text/javascript" src="js/shared/jquery.blockUI.js"></script>';
	html += '<script type="text/javascript" src="js/shared/jquery-ui-1.5.3.packed.js"></script>';
	html += '<script type="text/javascript" src="js/shared/jquery-ui-effects.packed.js"></script>';
	html += '<script type="text/javascript" src="js/shared/hoverIntent.js"></script>';
	html += '<script type="text/javascript" src="js/shared/jquery.field.js"></script>';
	html += '<script type="text/javascript" src="js/shared/Tokenizer.js"></script>';
	html += '<script type="text/javascript" src="js/billing.js"></script>';	
	html += '<script type="text/javascript" src="js/shared/loadMenu.js"></script>';
	html += '<script language="Javascript" type="text/javascript">';
	//html += 'var inv ; ';
	//html += 'var size;';
	//html += 'var invoiceArr;';
	html+= 'function CallParentWindowFunction(){ var invoiceNum = '+invoiceNo+';';
	html += 'var ppid = "'+PID+'";';
	html += 'var ppat= "'+patron+'";';
	html += 'var strpid = document.getElementById("txtpid").value;';
	html += 'var strpatron = document.getElementById("txtpat").value;';
	html += 'var strnote = document.getElementById("txtnote").value;';
	html += 'var strname = document.getElementById("txtname").value;';
	//html += 'var strnoteMod = strnote.replace(/^\s*/, "").replace(/\s*$/, "");';
	//html += 'var strnoteMod = strnote.replace(/^\s+|\s+$/g, "");';  
	html += 'var strnoteMod = strnote.replace(/^\s+/g,"").replace(/\s+$/g,"");';
	//html += 'alert(strnote.length+","+strnoteMod.length);';
	html += 'if(strnoteMod.length >400) {alert("Note cannot be > 400 characters!");return false;}';
	html += 'var flagNote = false;if(strnoteMod.length > 0 ){flagNote = true;};';
	html += 'var flagPatron = true;if(strpatron == ppat ){flagPatron = false;};';
	html +=  'var flagPID = true;if (ppid === strpid){flagPID = false;}window.opener.saveDataFromProbQueue(strpid,strpatron,strnoteMod,invoiceNum,flagPID,flagNote,flagPatron,ppat);window.close();return true;}';
	
	//var size = '+size+'; 
	
	html += '</script>';
	
	html += '<script language="Javascript" type="text/javascript">';
	html+= 'function getNote(){ var selector_checked = $("input[@id=chkboxinvNote]:checked").length; if(selector_checked == 0){ alert("Please select a record first!"); return false;} ';

   html+= 'else if(selector_checked == 1){ var invNo =$("input[@id=chkboxinvNote]:checked").val();';
   //html += 'window.opener.getInvoiceNotes(invNo); ';
   html += '$.blockUI({ message:"Getting invoice notes..." });';
   //html += '$().ajaxStart($.blockUI).ajaxStop($.unblockUI);';
   var ajaxcall = '$.ajax({url: "/billing/servlets/GetInvoiceNotes", dataType: "json",data:{invNo:invNo},success:function(data){ $.unblockUI();if(data.sizeInvoiceNoteArray === 0) {$("#addNote").empty();var tableBody =$("<tbody></tbody>").html("No data");var p = $("<p></p>").html(tableBody);$("#addNote").append(p);return true;} else {var tableBody =$("<tbody></tbody>");$("#addNote").empty(); $.each(data["invoiceNoteArray"],function(){ var row =  $("<tr></tr>");var col1 =  $("<td></td>").html(this.date);col1.css("padding", "4px 8px"); var col2 =  $("<td></td>").html(this.username);col2.css("padding", "6px 11px");var col3 =  $("<td></td>").html(this.notes);col3.css("padding", "6px 11px");var col4 =  $("<td></td>").html(this.expDesc);col4.css("padding", "6px 11px");var col5 =  $("<td></td>").html(this.comDesc);col5.css("padding", "6px 11px");var col6 =  $("<td></td>").html(this.resDesc);col6.css("padding", "6px 11px"); row.append(col1).append(col2).append(col3).append(col4).append(col5).append(col6);tableBody.append(row);}); var p = $("<p></p>").html(tableBody); $("#addNote").append(p);$("#addNote").css("overflow", "auto");$("#addNote").css("position", "relative");$("#addNotetr:even").addClass("even");$("#addNotetr").hover(function(){$(this).addClass("over");}, function(){$(this).removeClass("over");});return true;}},error:function (data, status, e){$.unblockUI();$.blockUI({ message: "There was an error or timeout when calling GetInvoiceNotes servlet" }); setTimeout($.unblockUI, 2000);}});';
   html += ajaxcall;
   //html += 'testA();';
   html+=  'return false;}else{alert("Please select one record at a time");}}//end of getNote()';
   
  html += '</script>';    
	    
  
   
    html += '<script language="Javascript" type="text/javascript">';
    html+= 'function AddNotes(){ ';
    html += 'var selector_checked = $("input[@id=chkboxinvNote]:checked").length; if(selector_checked == 0){ alert("Please select a record first!"); return false;}';
    html += 'else if(selector_checked == 1){ ';
    html += 'var inv =$("input[@id=chkboxinvNote]:checked").val();';
    //html += 'var inv =document.getElementById("chkboxinvNote").value; ';
    html += 'document.getElementById("txtStaff").value='+"'"+username+"'"+';';
    
    //html += 'document.getElementById("txtStaff").value='+username+';';
    //html += 'document.getElementById("txtDate").value= '+today+';';
    html += 'return true;} else{alert("Please select one record at a time");}}//end of function addNotes';
    html += '</script>';
    
    html += '<script language="Javascript" type="text/javascript">';
    html+= 'function AddNewNotes(){ ';
    html += 'var inv = $("input[@id=chkboxinvNote]:checked").val();';
	html += 'var strNote = document.getElementById("txtNewNote").value;';
	html += 'var strUserId = document.getElementById("txtStaff").value;';
	html += 'var strexp = document.getElementById("expdropdown").value;';
	html += 'var strres = document.getElementById("resdropdown").value;';
	html += 'var strcom = document.getElementById("comdropdown").value;';
    html += 'document.getElementById("txtNewNote").value= " ";';
    html += 'document.getElementById("txtStaff").value= " ";';
    //html += 'document.getElementById("txtDate").value= " ";';
    var ajaxStat = '$.ajax({url: "/billing/servlets/InsertInvoiceNotes",dataType: "json",data:{strUserId:strUserId,inv:inv,strNote:strNote,strexp:strexp,strres:strres,strcom:strcom},success: function(data){if (data.flag === true)alert("Successfully inserted!"); else alert("Error in Insertion!");},error:function (data, status, e){$.unblockUI();$.blockUI({ message: "There was an error or timeout when calling InsertInvoiceNotes servlet" }); setTimeout($.unblockUI, 2000);}});';
   // html += 'window.opener.insertInvoinceNote(strUserId,inv,strNote,strexp,strres,strcom);';
    html += ajaxStat;
    html += 'return true;}//end of function AddNewNotes';
	html += '</script>';

	html += '<TITLE>Edit Problem Queue Record</TITLE></head><body><div>';
	
		
	html+= '<fieldset><legend>Patron </legend>';
	html+= '<table border="0"> <tr><td>Name(exactly like Innopac record)</td></tr> <tr><td><input type="text" id="txtname" size="55" value= '+newPname+' readonly="readonly"> </td></tr></table>';
	html+='<table><tr><td>PID</td><td>Patron No</td><td></td><td></td></tr>';
	html+='<tr><td><input type="text" id="txtpid" size="12" value= '+PID+'></td> <td><input type="text" id="txtpat" size="8" value= '+patron+' "></td>';
	html+='<td><input type="button" value="Ok" onclick="CallParentWindowFunction()"></td> ';
	html+='<td><input type="button" value="Cancel" onclick="window.close()"></td></tr> </table>';
	html+='<table><tr><td>Notes</td></tr>';
	html+='<tr><td><textarea cols="40" rows="3" id="txtnote">'+notes+'</textarea></td></tr></table></fieldset>';
	
	
	html+='<br></div>';
	html+= '<fieldset><legend>Transactions </legend>';
	html+='<div id = "transaction">';
	html+='<table class="dataGrid"><thead><tr><th width="1%"></th><th>Invoice #</th><th>Invoice Date</th><th>Total Charge</th><th>Item Barcode</th></tr></thead>';
	html+='<tbody>';
	$.each(data['transactionList'],function(){
		html += '<tr>';
		html+='<td><input type="checkbox" id="chkboxinvNote" value='+this.invoiceNo+'></td> <td>'+this.invoiceNo+'</td><td>'+this.date+'</td><td>'+this.amount+'</td><td>'+this.barcode+'</td></tr>';
		});
	
	html+='<br></div></table></fieldset><table><tr><td><input type="button" value="GetNote" onclick="getNote()"></td>';
	html += '<td><input type="button" value="AddNotes" onclick="AddNotes()"></td></tr></tbody></table>';
	//----------------------------
	// div for adding a new note
	html += '<div id = "newnote">';
	html += '<fieldset><legend>Add New Note </legend>';
	//html += '<table border="0"><tr><td>Added</td><td><input type="text" id="txtDate" size="12"></td>';
	html += '<table border="0"><tr>';
	html += '<td>Explanation</td><td><select id="expdropdown">';
	$.each(data['explanationIDArray'],function(){
		html+='<option value='+this.id+'>'+this.explanation+'</option>';
		});
	html += '</select></td></tr>';
	html += '<tr><td>Staff</td><td><input type="text" id="txtStaff" size="20" ></td>';
	html += '<td>Response</td><td><select id="resdropdown">';
	$.each(data['responseIDArray'],function(){
		html+='<option value='+this.id+'>'+this.response+'</option>';
		});
	html += '</select></td></tr>';
	html += '<tr><td>Note</td><td><input type="text" id="txtNewNote" size="50"></td>';
	html += '<td>Communication</td><td><select id="comdropdown">';
	$.each(data['communicationIDArray'],function(){
		html+='<option value='+this.id+'>'+this.communication+'</option>';
		});
	html += '</select></td></tr>';
	//html += '<tr><td>input type="button" value="Ok" onclick=""></td>';
	html += '<tr><td><input type="button" value="OK" onclick="AddNewNotes()"></td></tr>';
	html += '</table></fieldset>';	
	html += '</div>';
	
	//-------------------------
	html+= '<fieldset><legend>Notes </legend>';
	html+='<div id = "notes">';
	html+='<table class="dataGrid"><thead><tr><th width = 15%>Added</th><th width = 15%>Staff</th><th width = 20%>Note</th><th width = 20%>Explanation</th><th width = 20%>Communication</th><th width = 20%>Response</th></tr></thead></table>';
//	html+='<table class="dataGrid"><thead><tr width=100%><th width = 65%>Added</th><th width = 20%>Staff</th><th width = 35%>Note</th></tr></thead></table>';

	//html+='<tbody></tbody>';
	html+='<div id = "addNote"></div> </div>';
	html += '</fieldset>';
	
    html+='</body></html>';
	win2 = window.open("", "window2", "width=880,height=850,scrollbars=yes "); 
	var tmp = win2.document;
	tmp.write(html);
	tmp.close();
	win2.focus();
	document.body.style.cursor = 'default'; 
}


}); //BIG CLOSE


function test(varr)
{	
	//alert(invoiceNo);
	if(varr.length > 9)
	{
		alert("PID you entered exceed the limit of lenth 9!!!!")
	}
	else{
	$.ajax({
		url: "/billing/servlets/EditNewPatronData", 
		dataType: 'json',
		data: {invoiceNo:invoiceNo,pid:pid},
		success: displayNewPatronQueueAfterEdit,
		error:function (data, status, e){
			$.unblockUI();
			$.blockUI({ message: 'There was an error or timeout when caling EditNewPatronData servlet' });
			 setTimeout($.unblockUI, 2000);
		}
	});	
	}
}



function displayNewPatronQueueAfterEdit(data,status){
	$.unblockUI();
	$("#results").empty();
	$('.container').hide(); 
	if(data.result === "success"){
		jsonData = data;
			if(data['newPatron'] !== undefined){
			$('#buttonsPending').hide();
			$('#buttonsNewPatron').show();
			$('#buttonsProblem').hide();
			$('#actions').hide();
			displayQueue("New Patron Queue",data['newPatron'],data['newPatronTotal']);
			$('.container').fadeIn('fast'); //show the new data
		}else{
			//add error message
			$.blockUI({ message: 'There was no data returned from Editing New Patron table' });
			 setTimeout($.unblockUI, 2000);
		}
	}
}





//%%%%%%%%%%%%%%%%%%%%%%%%%ME%%%%%%%%%%%%%%%%%%%%%%%%%%
function saveDataFromViewPatronHistory(pid,strpatron,strnote,flagPID,flagNote,flagPatron,ppat)
{

	if(pid.length > 9)
		{
			alert("PID you entered exceed the limit of lenth 9!!!!")
		}
		if(strpatron.length >8)
		{
			alert("Patron no you entered exceed the limit of lenth 8!!!!")
		}
	
	$.ajax({
		url: "/billing/servlets/ModifyBasicDataPatHistory", 
		dataType: 'json',
		data: {pid:pid,note:strnote,patronNo:strpatron,flagPID:flagPID,flagNote:flagNote,flagPatron:flagPatron,oldPatronNo:ppat},
		success: displayPatronHistoryAfterEdit,
		error:function (data, status, e){
			$.unblockUI();
			$.blockUI({ message: 'There was an error or timeout when calling ModifyBasicDataPatHistory servlet' });
			 setTimeout($.unblockUI, 2000);
		}
	});	
}

 function displayPatronHistoryAfterEdit(data,status){
 	$.unblockUI();
 	//$("#results").empty();
 	if(data.status === "fail"){
 		alert("Database update failed!");
 	}
 	else if(data.status === "success"){
 		alert("Database update success! Please search again to see the update.");
 		
		 
		 //idiot
 	}
 	else if(data.status ===  "nochange")
 	{
 		
 		alert("NO changes made!");
 	}
 	
 			
 }
 
 
 //%%%%%%%%%%%%%%%%%%%%%%ME end%%%%%%%%%%%%%%%%%%%%%%

 /*
 function test(varr)
{	
	$.ajax({
		url: "/billing/servlets/EditNewPatronData", 
		dataType: 'json',
		data: {invoiceNo:invoiceNo,pid:pid},
		success: displayNewPatronQueueAfterEdit,
		error:function (data, status, e){
			$.unblockUI();
			$.blockUI({ message: 'There was an error or timeout when caling EditNewPatronData servlet' });
			 setTimeout($.unblockUI, 2000);
		}
	});	
	
}



function displayNewPatronQueueAfterEdit(data,status){
	$.unblockUI();
	$("#results").empty();
	$('.container').hide(); 
	if(data.result === "success"){
		jsonData = data;
			if(data['newPatron'] !== undefined){
			$('#buttonsPending').hide();
			$('#buttonsNewPatron').show();
			$('#buttonsProblem').hide();
			$('#actions').hide();
			displayQueue("New Patron Queue",data['newPatron'],data['newPatronTotal']);
			$('.container').fadeIn('fast'); //show the new data
		}else{
			//add error message
			$.blockUI({ message: 'There was no data returned from Editing New Patron table' });
			 setTimeout($.unblockUI, 2000);
		}
	}
}

*/

function displayQueue(type, data,total){
	
	if($("#results tr").length > 1){
		$("#results").empty();
	}
	$("#emptyTable").remove();
	
	var fieldSet = $("<fieldset></fieldset>");
	var legend = $("<legend></legend>").html("<b>"+type+"</b>");
	var table = $("<table border='1'><thead><tr><th width='1%'></th><th width='15%'>Paytron Name</th><th width='10%'>PID</th><th width='8%'>Charge</th><th width='10%'>Processing Fee</th>" +
			"<th width='10%'>Billing Fee</th>" +
			"<th width='10%'>Invoice #</th>" +
			"<th width='12%'>Rule</th></tr></thead></table>");
	var tableBody = $("<tbody></tbody>");
	
	if ( total == 0)
	{
		//alert("total === 0");
		tableBody.append($("<div id='emptyTable'></div>").html("No data"));
		//$("#results").css('height', '110px');
		//$("#results").css('width', '90%');
		//$("#results").css('padding-bottom', '10px');
		//$("#results").css('margin', '0');
		//$("#results").css('position', 'relative');
		
		//$("#results").css('overflow', 'visible');
		//var row =  $("<tr></tr>").html("No data");
		//var col = $("<td></td>").html("No data");
		//row.append(col);
		//tableBody.append(row);
		//tableBody.html("No data");
	}
	else{
		
	$.each(data,function(){
		var row =  $("<tr></tr>");
		var col = $("<td></td>");
		var valCombined = this.invoiceNo+'|'+this.chargeType;
		var chkbox = $('<input type="checkbox" />').attr('id', 'chk').attr('value', valCombined);
		col.append(chkbox);
		var col1 =  $("<td></td>").html(this.name);
		var col2 =  $("<td></td>").html(this.pid);
		var col3 =  $("<td></td>").html(this.amount1);
		var col4 =  $("<td></td>").html(this.amount2);
		var col5 =  $("<td></td>").html(this.amount3);
		var col6 =  $("<td></td>").html(this.invoiceNo);
		var col7 =  $("<td></td>").html(this.rule);
		row.append(col).append(col1).append(col2).append(col3).append(col4).append(col5).append(col6).append(col7);
		tableBody.append(row);
	});
	$("#results").css('height', '500px');
	$("#results").css('overflow', 'auto');
	$("#results").css('width', '100%');
	$("#results").css('margin', '0');
	$("#results").css('position', 'relative');
	$("#results").css('padding-bottom', '10px');
	}//end of else
	table.append(tableBody);
    
	var p = $("<p></p>").html(fieldSet.append(legend).append(table));
	$("#results").append(p);
	$("#results tr:even").addClass("even");
	$("#results tr").hover(function () {
        $(this).addClass("over");
      }, 
      function () {
        $(this).removeClass("over");
      }
	);	

	
}

function saveDataFromProbQueue(pid,strpatron,strnote,invoiceNo,flagPID,flagNote,flagPatron,ppat)
{
	/*alert(pid);
	alert(strpatron);
	alert(strnote);
	alert(invoiceNo);
	alert(flagPID);
	alert(flagNote);*/
	if(pid.length > 9)
	{
		alert("PID you entered exceed the limit of lenth 9!!!!")
	}
	if(strpatron.length >8)
	{
		alert("Patron no you entered exceed the limit of lenth 8!!!!")
	}
	else{
	$.ajax({
		url: "/billing/servlets/EditProblemQueueData", 
		dataType: 'json',
		data: {invoiceNo:invoiceNo,pid:pid,note:strnote,patronNo:strpatron,flagPID:flagPID,flagNote:flagNote,flagPatron:flagPatron,oldPatronNo:ppat},
		success: displayProblemQueueAfterEdit,
		error:function (data, status, e){
			$.unblockUI();
			$.blockUI({ message: 'There was an error or timeout when calling EditProblemQueueData servlet' });
			 setTimeout($.unblockUI, 2000);
		}
	});	
	}
}

function displayProblemQueueAfterEdit(data,status){
	$.unblockUI();
	$("#results").empty();
	$('.container').hide(); 
	if(data.status === "fail"){
		alert("Database update failed!");
	}
	else{
		jsonData = data;
		if(data['problem'] !== undefined){
			$('#buttonsPending').hide();
			$('#buttonsNewPatron').hide();
			$('#buttonsProblem').show();
			$('#actions').hide();
			displayQueue("Problem Queue",data['problem'],data['problemTotal']);
			$('.container').fadeIn('fast'); //show the new data
		}else{
			//add error message
			$.blockUI({ message: 'There was no data returned from Editing New Patron table' });
			 setTimeout($.unblockUI, 2000);
		}
	}
			
}

function insertInvoinceNote(strUserId,inv,strNote,strexp,strres,strcom) {
	 $.ajax({
			url: "/billing/servlets/InsertInvoiceNotes", 
			dataType: 'json',
			data:{strUserId:strUserId,inv:inv,strNote:strNote,strexp:strexp,strres:strres,strcom:strcom},
			success: function(data){if (data.flag === 'true')alert("Successfully inserted!"); else alert("Error in Insertion!");},
			error:function (data, status, e){
				$.unblockUI();
				$.blockUI({ message: 'There was an error or timeout when calling InsertInvoiceNotes servlet' });
				 setTimeout($.unblockUI, 2000);
			}
		});	
	
}

function getInvoiceNotes(invNo) {
//alert(invNo);	
	flag=false;
	//invoiceNoteArrSize = -1;
	$.blockUI();
	    $.ajax({
			url: "/billing/servlets/GetInvoiceNotes", 
			dataType: 'json',
			data:{invNo:invNo},
			success: setinvoiceNotesArray,
			error:function (data, status, e){
				$.unblockUI();
				$.blockUI({ message: 'There was an error or timeout when calling GetInvoiceNotes servlet' });
				 setTimeout($.unblockUI, 2000);
			}
		});	
}
function setinvoiceNotesArray(data,status) {
	     $.unblockUI();
	    invoiceNoteArray = data['invoiceNoteArray'];
	    invoiceNoteArrSize = data.size;
	    flag = true;
	   // win2.document.getElementById("size").value = invoiceNoteArrSize;
	  //  win2.document.getElementById ("invoiceArr").value = invoiceNoteArray;
	   // win2.testA();
	   // alert("size:"+invoiceNoteArrSize);
}


function displaySendFileLog(data,status){
	$.unblockUI();
	
	if(data.success === "true")
	{
		jsonData = data;
		//jsonData['problem'] == null;
		alert("successfully transmitted!");
		//jsonData['pending'] = data['pendingQueue'];
		//jsonData['pendingTotal'] = data.pendingTotal;
		$('#buttonsPending').show();
		$('#buttonsNewPatron').hide();
		$('#buttonsProblem').hide();
		$('#searchDiv').hide();
		$('#sessionDiv').hide();
		//$('#indexdiv').show();
		 
		$('#actions').hide();
		displayQueue("Pending Queue",data['pending'],data.pendingTotal);
		$('.container').fadeIn('fast'); //show the new data
	}
	else{
		alert(data.errorMsg);
	
}
	
		
}

function getPassword(username,password) {
	

    
   // alert(username);
	// alert(password);	
   
	if(username==null || username=="")
		 {alert("You need to enter username!");}
	 else if (password==null || password=="")
		 {alert("You need to enter password!");}

	 else
	  {
		 $("#results").empty();
		
	 $.blockUI({ message: '<img src="images/busy.gif" /> Sending Data...' });
	 $.ajax({
	 	  type: "POST",
			url: "/billing/servlets/SendOutputFiles", 
			dataType: 'json',
			data:{username:username,password:password},
			success: displaySendFileLog,
			error:function (data, status, e){
				$.unblockUI();
				//add error message
				$.blockUI({ message: 'There was an error or timeout when caling SendOutputFiles servlet' });
				 setTimeout($.unblockUI, 2000);
			}
		});	
	  }
  
		   
		}


