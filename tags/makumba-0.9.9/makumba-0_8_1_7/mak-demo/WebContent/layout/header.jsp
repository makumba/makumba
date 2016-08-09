<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<c:set var="path" value="${pageContext.request.contextPath}" scope="request" />
<% request.setAttribute("username", request.getRemoteUser()); %>
<c:if test="${!empty username}">
  <mak:object from="user.User u" where="u.username = $username">
    <mak:value expr="u" printVar="loggedInUser"/>
  </mak:object>
  <c:set var="loggedInUser" value="${loggedInUser}" scope="session" />
</c:if>

<c:choose>
  <c:when test="${!empty param.pageTitle}"><c:set var="pageTitle" value="${param.pageTitle}" scope="request"/></c:when>
  <c:otherwise><c:set var="pageTitle" value="" scope="request"/></c:otherwise>
</c:choose>
<html>
<head>
	<link rel="stylesheet" href="${path}/layout/style.jsp" type="text/css" media="all" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>${pageTitle} &laquo; The Makumba Blog</title>
</head>
<body>
<div id='ubercontainer'>
<div id='container'>

<div id='logo_bg'>
	<a href="${path}">
	<h1>The Makumba Blog</h1>
	<h2>Changing the industry since 2003</h2>
	</a>
</div>

<div id='right_menu'>
	<h3>Menu</h3>
	<ul>
		<li><a href='${pageContext.request.contextPath}/welcome.jsp'>Homepage</a></li>
    <li><a href='${pageContext.request.contextPath}/forum/'>Forum</a></li>
		<li><a href='#'>Previous posts</a></li>
		<li><a href='#'>Sitemap</a></li>
	</ul>
	<h3>Site admin</h3>
	<% if(request.getRemoteUser() == null) { %>
	<ul>
		<li><a href='${pageContext.request.contextPath}/login.jsp'>Login</a></li>
		<li><a href='${pageContext.request.contextPath}/register.jsp'>Register</a></li>
	</ul>
	<% } else { %>
	<br/>Logged in as user: <strong><%=request.getRemoteUser()%></strong>
	<ul>

		<% if(request.isUserInRole("admin")) { %>
			<li><a href='${pageContext.request.contextPath}/admin/'>Admin</a></li>
		<%}%>

		<li><a href='${pageContext.request.contextPath}/logout.jsp'>Logout</a></li>
	</ul>
	<% } %>
</div>

<div id='content'>

<% if (request.getAttribute("makumba.response") != null && !request.getAttribute("makumba.response").equals("")) { %>
<div class="mak_response"><mak:response/></div>
<% } %>
