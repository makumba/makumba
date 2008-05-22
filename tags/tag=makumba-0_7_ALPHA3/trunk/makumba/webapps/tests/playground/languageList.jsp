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

<%-- Makumba Generator - START OF  *** LIST ***  PAGE FOR OBJECT test.Language --%>
<h1>List Languages</h1>

<table>
  <tr>
    <th><a href="languageList.jsp?sortBy=created">#</a></th>
    <th><a href="languageList.jsp?sortBy=name">name</a></th>
    <th><a href="languageList.jsp?sortBy=isoCode">isoCode</a></th>
    <th><a href="languageList.jsp?sortBy=created">Created</a></th>
    <th><a href="languageList.jsp?sortBy=modified">Modified</a></th>
    <th>Actions</th>
  </tr>
  <mak:list from="test.Language language" orderBy="language.name">
    <tr>
      <td>${mak:count()}</td>
      <td><mak:value expr="language.name" /></td>
      <td><mak:value expr="language.isoCode" /></td>
      <td><mak:value expr="language.TS_create" format="yyyy-MM-dd hh:mm:ss" /></td>
      <td><mak:value expr="language.TS_modify" format="yyyy-MM-dd hh:mm:ss" /></td>
      <td><a href="languageEdit.jsp?language=<mak:value expr="language" />">[Edit]</a> <a href="languageDelete.jsp?language=<mak:value expr="language" />">[Delete]</a> </td>    
    </tr>
  </mak:list>
</table>
<a href="languageNew.jsp">[New]</a>

<%-- Makumba Generator - END OF *** LIST ***  PAGE FOR OBJECT test.Language --%>
