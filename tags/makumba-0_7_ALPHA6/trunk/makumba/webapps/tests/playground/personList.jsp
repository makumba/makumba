<% /* $Id: /usr/local/cvsroot/karamba/public_html/welcome.jsp,v 2.39 2007/06/28 17:18:22 manu Exp $ */ %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<html>
<head>
  <title>Playground</title>
  <link rel="StyleSheet" href="../style/style.css" type="text/css" media="all"/>
</head>

<body>
Person: <a href="personNew.jsp">New</a> | <a href="personList.jsp">List</a> ||| 
Languages:  <a href="languageNew.jsp">New</a> | <a href="languageList.jsp">List</a>
<br><br>

response:<mak:response/><br><br><br>

<style type="text/css">
td {
  font-size: x-small;
}
th {
  font-size: small;
}
</style>

<h1>List Persons</h1>

<table>
  <%@include file="personListHeaderInclude.jsp" %>
  <mak:list from="test.Person o">
    <%@include file="personListDisplayInclude.jsp" %>
  </mak:list>
</table>
</body>
</html>
