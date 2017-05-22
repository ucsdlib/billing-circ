<%
	session.invalidate();
	response.sendRedirect("/billing/index.jsp");
%>