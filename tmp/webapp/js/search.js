$(function(){
	initActions();
	
	function initActions(){
		
		//loadBillingMenu();
		//$('#indexdiv').hide();
		//$('#searchDiv').show();
		
		//var p = $("<p></p>").html("No data");
		//$("#results").append(p);
		
		$("#btnSearch").bind("click",function(){
			var searchval= $("#txtSearch").val();
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
			});	
			return false;
		});
		
		$("#btnViewPatron").bind("click",function(){
			var selector_checked = $("input[@id=chk]:checked").length; 
			 if(selector_checked == 0)
			 {
				 alert("Please select at least one record!");
			 }
			 else if(selector_checked == 1 )
			 {
				 var myString = $("input[id='chk']").getValue();
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
	}
	
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
				var chkbox = $('<input type="checkbox" />').attr('id', 'chk').attr('value', this.patronNo);
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
		//notes = data['basicData'].notes;
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
		html += 'var strpid = document.getElementById("txtpid").value;';
		html += 'var strpatron = document.getElementById("txtpat").value;';
		html += 'var strnote = document.getElementById("txtnote").value;';
		html += 'var strname = document.getElementById("txtname").value;';
		html += 'var flagNote = false;if(strnote.length > 2 ){flagNote = true;};';
		html +=  'var flagPID = true;if (ppid === strpid){flagPID = false;}window.opener.saveDataFromViewPatronHistory(strpid,strpatron,strnote,flagPID,flagNote);window.close();return true;}';
		
		//var size = '+size+'; 
		
		html += '</script>';
		
		html += '<script language="Javascript" type="text/javascript">';
		html+= 'function getNote(){ var selector_checked = $("input[@id=chkboxinvNote]:checked").length; if(selector_checked == 0){ alert("Please select a record first!"); return false;} ';

	   html+= 'else if(selector_checked == 1){ var invNo =$("input[@id=chkboxinvNote]:checked").val();';
	   //html += 'window.opener.getInvoiceNotes(invNo); ';
	   html += '$.blockUI({ message:"Getting invoice notes..." });';
	   //html += '$().ajaxStart($.blockUI).ajaxStop($.unblockUI);';
	   var ajaxcall = '$.ajax({url: "/billing/servlets/GetInvoiceNotes", dataType: "json",data:{invNo:invNo},success:function(data){ $.unblockUI();if(data.sizeInvoiceNoteArray === 0) {$("#addNote").empty();var tableBody =$("<tbody></tbody>").html("No data");var p = $("<p></p>").html(tableBody);$("#addNote").append(p);return true;} else {var tableBody =$("<tbody></tbody>");$("#addNote").empty(); $.each(data["invoiceNoteArray"],function(){ var row =  $("<tr></tr>");var col1 =  $("<td></td>").html(this.date);col1.css("padding", "6px 11px"); var col2 =  $("<td></td>").html(this.username);col2.css("padding", "6px 11px");var col3 =  $("<td></td>").html(this.notes);row.append(col1).append(col2).append(col3);tableBody.append(row);}); var p = $("<p></p>").html(tableBody); $("#addNote").append(p);return true;}},error:function (data, status, e){$.unblockUI();$.blockUI({ message: "There was an error or timeout when calling GetInvoiceNotes servlet" }); setTimeout($.unblockUI, 2000);}});';
	   html += ajaxcall;
	   //html += 'testA();';
	   html+=  'return false;}else{alert("Please select one record at a time");}}//end of getNote()';
	   
	  html += '</script>';    
		    
	  
	   
	    html += '<script language="Javascript" type="text/javascript">';
	    html+= 'function AddNotes(){ ';
	    html += 'var selector_checked = $("input[@id=chkboxinvNote]:checked").length; if(selector_checked == 0){ alert("Please select a record first!"); return false;}';
	    html += 'else if(selector_checked == 1){ ';
	    html += 'var inv =$("input[@id=chkboxinvNote]:checked").val();';
	    html += 'document.getElementById("txtStaff").value='+"'"+username+"'"+';';
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
		html+='<tr><td><input type="text" id="txtpid" size="9" maxlength="9" value= '+PID+'></td> <td><input type="text" id="txtpat" size="12" value= '+patron+' readonly="readonly"></td>';
		html+='<td><input type="button" value="Ok" onclick="CallParentWindowFunction()"></td> ';
		html+='<td><input type="button" value="Cancel" onclick="window.close()"></td></tr> </table>';
		html+='<table><tr><td>Notes</td></tr>';
		html+='<tr><td><textarea cols="40" rows="3" id="txtnote"> </textarea></td></tr></table></fieldset>';
		
		
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
		html+='<table class="dataGrid"><thead><tr><th>Added</th><th>Staff</th><th>Note</th></tr></thead></table>';
		//html+='<tbody></tbody>';
		html+='<div id = "addNote"></div> </div>';
		html += '</fieldset>';
		
	    html+='</body></html>';
		win2 = window.open("", "window2", "width=860,height=650,scrollbars=yes "); 
		var tmp = win2.document;
		tmp.write(html);
		tmp.close();
		win2.focus();
		document.body.style.cursor = 'default'; 
	}



	
	
}); //big function





function saveDataFromViewPatronHistory(pid,strpatron,strnote,flagPID,flagNote)
{
	
	$.ajax({
		url: "/billing/servlets/ModifyBasicDataPatHistory", 
		dataType: 'json',
		data: {pid:pid,note:strnote,patronNo:strpatron,flagPID:flagPID,flagNote:flagNote},
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