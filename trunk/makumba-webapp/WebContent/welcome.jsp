<jsp:include page="header.jsp" />

<% if(request.getParameter("page").equals("home")){ %>
	<jsp:include page="home.jsp" />
<% }%>

<% if(request.getParameter("page").equals("companies")){ %>
	<jsp:include page="listCompanies.jsp" />
<% }%>

<% if(request.getParameter("page").equals("departments")){ %>
	<jsp:include page="listDepartments.jsp" />
<% }%>

<% if(request.getParameter("page").equals("myTasks")){ %>
	<jsp:include page="myTasks.jsp" />
<% }%>

