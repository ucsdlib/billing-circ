$(function(){
	/** ACTIONS **/
	initActions();
	/** LOAD DEFAULT BURSAR DATA FROM SERVLET **/
	getBursarData();
	
	/** METHODS **/
	function initActions(){
		//load menu
		loadBillingMenu();
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
		$("#processFile").bind("click",function(){
			processFile();
			return false;
		});
		$("#logout").bind("click",function(){
			window.location = "/billing-circ/logout.jsp";
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
		if($("table.dataGrid tr").length > 1){
			$("table.dataGrid tbody").empty();
		}
		$("#emptyTable").remove(); //if the display message exists
		$('.container').hide(); //hide everything before new search
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
			url: "/billing-circ/servlets/GetBursarData",
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
				init(data);
			}
		});
	}
	/** INITIALIZE GRID LOADING **/
	function init(data){
		if(data.total === 0){
			displayEmptyData();
		}else{
			loadGrid(data, initHelpers);
		}
	}
	/** Show message when no data is returned from ajax call **/
	function displayEmptyData(){
		$("#grid").append($("<div id='emptyTable'></div>").html("No data was returned from the date range specified"));
		$.unblockUI(); //unblock the ui
		$('.container').fadeIn('fast'); //show the new data
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
		$("table.dataGrid").append(tr);
	}
	/** INITIALIZE HELPER TABLE GRID FUNCTIONS **/
	function initHelpers(data){
		updateDates(data);
		initGrid();
		$.unblockUI(); //unblock the ui
		$('.container').fadeIn('fast'); //show the new data
	}
	/** SET THE CURRENT DATE VALES FROM JSON **/
	function updateDates(data){
		//HTML FIELDS
		$("#startDate").val(data.startDate);
		$("#endDate").val(data.endDate);
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
	function processFile(){
		window.open("/billing-circ/servlets/ProcessBursarData");
	}
});