<html>
	<head>
        <title>BILLING</title>
		<!--STYLESHEETS -->
		 <link rel="stylesheet" href="css/billing.css" media="screen"></link>
        <link rel="stylesheet" href="css/reset.css" media="all"></link>
        <link rel="stylesheet" href="css/superfish.css" media="screen"></link>
        <link rel="stylesheet" href="css/ui.datepicker.css" media="screen"></link>
       <!-- <link rel="stylesheet" href="css/bursar.css" media="screen"></link>  --> 
        <link rel="stylesheet" href="css/bursar.print.css" media="print"></link>	
        	
		<!--JAVASCRIPT -->
		<script type="text/javascript" src="js/shared/jquery-1.2.6.pack.js"></script>
		<script type="text/javascript" src="js/shared/jquery.blockUI.js"></script>
		<script type="text/javascript" src="js/shared/jquery-ui-1.5.3.packed.js"></script>
		<script type="text/javascript" src="js/shared/jquery-ui-effects.packed.js"></script>
		<script type="text/javascript" src="js/shared/hoverIntent.js"></script>
		<script type="text/javascript" src="js/shared/superfish.js"></script>
		<script type="text/javascript" src="js/shared/ajaxfileupload.js"></script>
		<script type="text/javascript" src="js/shared/json2.js"></script>
		<script type="text/javascript" src="js/shared/loadMenu.js"></script>
		<script type="text/javascript" src="js/shared/jquery.field.js"></script>
		<script type="text/javascript" src="js/shared/Tokenizer.js"></script>
		<script type="text/javascript" src="js/billing.js"></script>
	
		<script type="text/javascript" src="js/processQueues.js"></script>
		<script type="text/javascript" src="js/shared/jquery-ui-1.7.2.custom.min.js"></script>
	</head>
	<body>
		<div class="container">
			<div class="header">
					<h1>BILLING</h1>
					<h2>UCSD Libraries Billing Applications</h2>
			</div>
			<div id ="filediv">
			<div id='actions'>
					<fieldset>
						<legend>Actions</legend>
						<p>
							<input type="file" id="fileToUpload" name="fileToUpload"/>
							<input type="submit" id="processFile" value="Process Billing File"/>
							 <!-- <input type="submit" id="processOutputFile" value="Get Output Files"/> -->
							<input type="submit" id="logout" value="Logout"/>
						</p>
					</fieldset>
				</div>
			</div>
			<div class="data_container">
			<div id ="indexdiv">
				 <div id='results'> </div>
				
				<!--<div id='results'>
					<table class='dataGrid'>
						<thead>
							<tr>
							    <th> </th>
								<th>Patron Name</th>
								<th>PID</th>
								<th>Charge</th>
								<th>Processing Fee</th>
								<th>Billing Fee</th>
								<th>Invoice #</th>
								<th>Rule</th>
							</tr>
						</thead>
						<tbody></tbody>
					</table>
				</div>
				-->
				<div id ="buttonsPending">
				<input type="submit" id="delBtnPen" value="Delete"/>
				<input type="submit" id="moveToProbQBtn" value="Move to Problem Queue"/>
				<input type="submit" id="selectAllBtn" value="Select All"/>
				<input type="submit" id="deselectAllBtnPending" value="Deselect All"/>
				</div>
				<div id ="buttonsProblem">
				<input type="submit" id="editBtnProb" value="Edit"/>
				<input type="submit" id="delBtnProb" value="Delete"/>
				<input type="submit" id="moveToPendQBtn" value="Move to Pending Queue"/>
				<input type="submit" id="selectAllProbBtn" value="Select All"/>
				<input type="submit" id="deselectAllBtnProblem" value="Deselect All"/>
				</div>
				<div id ="buttonsNewPatron">
				<input type="submit" id="editBtnNewP" value="Edit"/>
				<input type="submit" id="moveToPendQBtnNewP" value="Move to Pending Queue"/>
				<input type="submit" id="delBtnNewP" value="Delete"/>
				</div>
				
				<!--  
				<div id='actions'>
					<fieldset>
						<legend>Actions</legend>
						<p>
							<input type="file" id="fileToUpload" name="fileToUpload"/>
							<input type="submit" id="processFile" value="Process Billing File"/>
							 <!-- <input type="submit" id="processOutputFile" value="Get Output Files"/> -->
							<!-- <input type="submit" id="logout" value="Logout"/>
						</p>
					</fieldset>
				</div>
				-->
				
				</div> <!-- index div end -->
				
				<!-- ================================================================== -->
				<!--SEARCH div -->
		 	<div id = "searchDiv" >
			<div id="search">
			<fieldset>
						<legend>Search</legend>
						<input type="text" id="txtSearch" size="25"/>
						<input type="submit" id="btnSearch" value="Search"/>
			</fieldset>			
			</div>
			
			<div id="searchby">
			<fieldset>
						<legend>Search By</legend>
						<div id = "radio">
						<input type="radio" name="group1" value="Name" checked> Name(Last Name first)&nbsp;&nbsp;
						<input type="radio" name="group1" value="PID">PID&nbsp;&nbsp;
						<input type="radio" name="group1" value="PatronRecNo">Patron Record #&nbsp;&nbsp;
						<input type="radio" name="group1" value="InvoiceNo">Invoice # &nbsp;
						</div>
			</fieldset>			
			</div>
			<div class="data_container2">
			<fieldset>
						<legend>Results</legend>
				 <div id='results2'> </div>
				<input type="submit" id="btnViewPatron" value="View Patron"/>
			</fieldset>	
		    </div>
			</div>
			<!-- ================================================================== -->
		<div id = "sessionDiv" >
				<div id="getData">
						<table> 
						<tr> <td>Session Activity Data</td>		
						<td><div id = "dateCombo"> </div></td>
						<td><input type="submit" id="btnGetSessionData" value="Get Data"/></td>
						<td>  </td>
						
						</tr>
						</table>
				<div class="data_container2">		
					<div id='grid'>
					<table class='dataGrid'>
						<thead>
							<tr>
								<th>Resubmit</th>
								<th>Patron Name</th>
								<th>PID</th>
								<th>Charge</th>
								<th>Processing Fee</th>
								<th>Billing Fee</th>
								<th>Invoice #</th>
							</tr>
						</thead>
						<tbody></tbody>
					</table>
				</div>
				<div id = "sessionBtns">
				<input type="submit" id="btnresubmit" value="Resumbit Items"/>
				<input type="submit" id="btnSessionSelAll" value="Select All"/>
				<input type="submit" id="btnReset" value="Reset"/>
				<INPUT TYPE="button" value="Print" onClick="window.print()">
				<!--<input type="submit" id="btnSessionOk" value="Ok"/>  -->
				</div>
			   </div>
	   </div>
	   	</div>
				<!-- =============================bursar================================ -->
				
				<div id="bursarDiv">
				<div class="bursar_data_container">
				<div id='bursarGrid'>
					<table class='bursarDataGrid'>
						<thead>
							<tr>
								<th>Name</th>
								<th>Session_activity_dt</th>
								<th>Pid</th>
								<th>Apply_amt</th>
								<th>Cashier_id</th>
								<th>Document_nbr</th>
								<th>Charge_detail_cd</th>
								<th>Charge_category_cd</th>
								<th>Payment_detail_cd</th>
								<th>Payment_category_cd</th>
							</tr>
						</thead>
						<tbody></tbody>
					</table>
				</div>
				<div id='dateRange'>
					<fieldset>
						<legend>Date Range</legend>
						<p>
							<label for='startDate'>Start Date</label><input type="text" size="10" value="" id="startDate"/>
							<label for='endDate'>End Date</label><input type="text" size="10" value="" id="endDate"/>
						</p>
					</fieldset>
				</div>
				<div id='bursarActions'>
					<fieldset>
						<legend>Actions</legend>
						<p>
							<input type="submit" id="getRecords" value="Get Paid Records"/>
							<input type="submit" id="processBursarFile" value="Process Payment File"/>
							
						</p>
					</fieldset>
				</div>
			</div>
								
				
				</div>  <!-- end of bursar div -->
				
				
				
				<!-- ========================================================= -->
			</div> <!--  data container div end -->
			
			<div class="footer">UCSD Libraries Digital Billing Applications RELEASE_0.06</div>
		</div>
		
		
		
		
		
		<script type="text/javascript">
			var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
			document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
		</script>
		<script type="text/javascript">
			try {
			var pageTracker = _gat._getTracker("UA-6900359-1");
			pageTracker._trackPageview();
			} catch(err) {}
		</script>
	</body>
</html>
