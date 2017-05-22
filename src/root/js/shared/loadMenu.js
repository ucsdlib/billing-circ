function loadBillingMenu(){
	var menuDiv = jQuery('<div></div').css("display","inline-block");
	var ul = jQuery('<ul class="sf-menu"></ul>');
	//home link
	//var home = jQuery('<li></li>').html(jQuery('<a  href="index.jsp"></a>').html("Home"));
	var home = jQuery('<li></li>').html(jQuery('<a id = "homeTab" href=""></a>').html("Home"));
	//bursar link
	//var bursar = jQuery('<li></li>').html(jQuery('<a href="bursar.jsp"></a>').html("Bursar"));
	var bursar = jQuery('<li></li>').html(jQuery('<a id = "bursarTab" href=""></a>').html("Bursar"));
	//var bursar = jQuery('<li></li>').html(jQuery('<a></a>').html("Bursar"));
	//3 transaction queues: pending, new, problem
	var queues = jQuery('<li></li>').append(jQuery('<a></a>').html("Transactions"));
	var transactionList = jQuery('<ul></ul>');
	var pending = jQuery('<li></li>').html(jQuery('<a id = "pending" href=""></a>').html("Pending"));
	var newPatrons = jQuery('<li></li>').html(jQuery('<a id= "newPatron" href=""></a>').html("New Patrons"));
	var problems = jQuery('<li></li>').html(jQuery('<a id ="problem" href=""></a>').html("Problem"));
	var outputFile = jQuery('<li></li>').html(jQuery('<a id ="processOutputFile" href=""></a>').html("Get Output Files"));
	var sendData = jQuery('<li></li>').html(jQuery('<a id ="sendData" href=""></a>').html("Send Data"));
	jQuery(transactionList).append(pending).append(newPatrons).append(problems).append(sendData).append(outputFile);
	jQuery(queues).append(transactionList);
	//administration
	var admin = jQuery('<li></li>').append(jQuery('<a id ="SearchTab" href=""></a>').html("Search"));
	var adminList = jQuery('<ul></ul>');
	var addUser = jQuery('<li></li>').html(jQuery('<a href="index.jsp"></a>').html("Add User"));
	var findUser = jQuery('<li></li>').html(jQuery('<a href="index.jsp"></a>').html("Search"));
	var problems = jQuery('<li></li>').html(jQuery('<a href="index.jsp"></a>').html("Problem"));
	jQuery(adminList).append(addUser).append(findUser).append(problems);
	//jQuery(admin).append(adminList);
	var session = jQuery('<li></li>').append(jQuery('<a id ="SessionTab" href=""></a>').html("Session"));
	//help link
	var help = jQuery('<li></li>').html(jQuery('<a href="help.jsp"></a>').html("Help"));
	//add everything to the top list
	//jQuery(ul).append(home).append(bursar).append(queues).append(admin).append(help);
	jQuery(ul).append(home).append(queues).append(admin).append(session).append(bursar);
	jQuery(menuDiv).append(ul);
	jQuery('.header').append(menuDiv);
	jQuery("ul.sf-menu").superfish({delay:400,animation:{opacity:'show',height:'show'},speed:'slow'});
	jQuery("ul.sf-menu a:first").css("border-left","0"); //remove the first border-left(css workaround)
}