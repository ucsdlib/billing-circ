<%
	session.invalidate();
	response.sendRedirect("/billing-circ/index.jsp");
%>