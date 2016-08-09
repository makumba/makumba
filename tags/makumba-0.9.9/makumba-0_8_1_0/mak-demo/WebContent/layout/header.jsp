<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<c:set var="path" value="${pageContext.request.contextPath}" scope="request" />
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
		<li><a href='#'>Homepage</a></li>
		<li><a href='#'>Previous posts</a></li>
		<li><a href='#'>Oujee</a></li>
		<li><a href='#'>Sitemap</a></li>
		<li><a href='#'>Oujee</a></li>
	</ul>
	<h3>Site admin</h3>
	<% if(request.getRemoteUser() == null) { %>
	<ul>
		<li><a href='login.jsp'>Login</a></li>
		<li><a href='register.jsp'>Register</a></li>
	</ul>
	<% } else { %>
	<br/>Logged in as user: <strong><%=request.getRemoteUser()%></strong>
	<ul>

		<% if(request.isUserInRole("admin")) { %>
			<li><a href='admin/'>Admin</a></li>
		<%}%>

		<li><a href='logout.jsp'>Logout</a></li>
	</ul>
	<% } %>
</div>

<div id='content'>
