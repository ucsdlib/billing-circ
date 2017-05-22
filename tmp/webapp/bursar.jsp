<html>
	<head>
        <title>BURSAR</title>
		<!--STYLESHEETS -->
        <link rel="stylesheet" href="css/reset.css" media="all"></link>
        <link rel="stylesheet" href="css/superfish.css" media="screen"></link>
        <link rel="stylesheet" href="css/ui.datepicker.css" media="screen"></link>
        <link rel="stylesheet" href="css/bursar.css" media="screen"></link>
        <link rel="stylesheet" href="css/bursar.print.css" media="print"></link>				
		<!--JAVASCRIPT -->
		<script type="text/javascript" src="js/shared/jquery-1.2.6.pack.js"></script>
		<script type="text/javascript" src="js/shared/jquery.blockUI.js"></script>
		<script type="text/javascript" src="js/shared/jquery-ui-1.5.3.packed.js"></script>
		<script type="text/javascript" src="js/shared/jquery-ui-effects.packed.js"></script>
		<script type="text/javascript" src="js/shared/hoverIntent.js"></script>
		<script type="text/javascript" src="js/shared/superfish.js"></script>
		<script type="text/javascript" src="js/shared/json2.js"></script>
		<script type="text/javascript" src="js/shared/loadMenu.js"></script>
		<script type="text/javascript" src="js/bursar.js"></script>
	</head>
	<body>
		<div class="container">
			<div class="header">
					<h1>BURSAR</h1>
					<h2>UCSD Libraries Billing Applications</h2>
			</div>
			<div class="data_container">
				<div id='grid'>
					<table class='dataGrid'>
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
				<div id='actions'>
					<fieldset>
						<legend>Actions</legend>
						<p>
							<input type="submit" id="getRecords" value="Get Paid Records"/>
							<input type="submit" id="processFile" value="Process Payment File"/>
							<input type="submit" id="logout" value="Logout"/>
						</p>
					</fieldset>
				</div>
			</div>
			<div class="footer">UCSD Libraries Digital Billing Applications</div>
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
