<html>
	<head>
        <title>BILLING Circulation</title>
		<!--STYLESHEETS -->
		 <link rel="stylesheet" href="css/search.css" media="screen"></link>
        <link rel="stylesheet" href="css/reset.css" media="all"></link>
        <link rel="stylesheet" href="css/superfish.css" media="screen"></link>
        <link rel="stylesheet" href="css/ui.datepicker.css" media="screen"></link>
           
        	
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
		<script type="text/javascript" src="js/shared/jquery-ui-1.7.2.custom.min.js"></script>
		<script type="text/javascript" src="js/search.js"></script>
	</head>
	<body>
	
	<div class="container">
			<div class="header">
					<h1>BILLING Circulation</h1>
					<h2>UCSD Libraries BILLING Circulation Applications</h2>
			</div>
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
			<div class="data_container">
			<fieldset>
						<legend>Results</legend>
				 <div id='results'> </div>
				<input type="submit" id="btnViewPatron" value="View Patron"/>
			</fieldset>	
		    </div>
	
	</div>
	<div class="footer">UCSD Libraries Digital BILLING Circulation Applications</div>
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
	