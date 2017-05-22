$(function(){
	/** ACTIONS **/
	initActions();
	
	/** METHODS **/
	function initActions(){
		loadBillingMenu();
		$("#reportDate").datepicker({
			dateFormat: 'yy-mm-dd',
			showOn: "button",
			buttonImage: "images/calendar.gif",
			buttonImageOnly: true,
			buttonText: 'Choose Date From Calendar',
			hideIfNoPrevNext : true
		});
		$("#updateReportDate").bind("click",function(){
			if(validDateFormat()){
				updateReportDate();
			}else{
				showDateFormatError();
			}
			return false;
		});
		$("#logout").bind("click",function(){
			window.location = "/billing/logout.jsp";
		});
		$("#goToBursar").bind("click",function(){
			window.location = "/billing/bursar.jsp";
		});
	}
	/** MAKE SURE DATEFORMAT IS CORRECT TO SEND TO SERVER **/
	function validDateFormat(){
		var reportDate = $("#reportDate").val();
		try{
			$.datepicker.parseDate("yy-mm-dd",reportDate);
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
	/** GET JSON DATA W/AJAX CALL **/
	function updateReportDate(){
		//empty table if necessary
		if($("table.dataGrid tr").length > 1){
			$("table.dataGrid tbody").empty();
		}
		$('.container').hide();
		$.blockUI({ message: '<img src="images/busy.gif" /> Saving New Report Date...' });
		var reportDate = $("#reportDate").val();
		var ajaxParams = "reportDate="+reportDate;
		$.ajax({
			url: "/billing/servlets/UpdateReportDate",
			dataType: "text",
			data: ajaxParams,
			error: function (xhr, desc, exceptionobj) {
				$.unblockUI();
				//add error message
				$.blockUI({ message: 'There was an error saving the report date. Please try again.' });
				 setTimeout($.unblockUI, 2000);
			},
			success: function(data){
				$.unblockUI();
				//add error message
				$.blockUI({ message: 'The report date was successfully saved.' });
				 setTimeout($.unblockUI, 2000);
			}
		});
	}
	
});