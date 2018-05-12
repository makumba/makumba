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

<%-- Makumba Generator - START OF  *** NEW ***  PAGE FOR OBJECT test.Language --%>
<h1>New Language</h1>
<mak:newForm type="test.Language" action="languageList.jsp" name="language" >
  <table>
  <%-- Makumba Generator - START OF NORMAL FIELDS --%>
    <tr>
      <th><label for="name"><span class="accessKey">n</span>ame</label></th>
      <td><mak:input field="name" styleId="name" accessKey="n" /></td>
    </tr>
    <tr>
      <th><label for="isoCode"><span class="accessKey">i</span>soCode</label></th>
      <td><mak:input field="isoCode" styleId="isoCode" accessKey="i" /></td>
    </tr>
  <%-- Makumba Generator - END OF NORMAL FIELDS --%>
    <tr>
      <td>  <input type="submit" value="Add" accessKey="A">  <input type="reset" accessKey="R">  <input type="reset" value="Cancel" accessKey="C" onClick="javascript:back();">  </td>
    </tr>
  </table>
</mak:newForm>

<%-- Makumba Generator - END OF *** NEW ***  PAGE FOR OBJECT test.Language --%>
