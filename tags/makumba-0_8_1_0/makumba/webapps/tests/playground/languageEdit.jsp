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

<mak:object from="test.Language language" where="language=$language">
  <h1>Edit Language <i><mak:value expr="language.name" /></i></h1>
  <mak:editForm object="language" action="languageView.jsp" method="post">
    <table>
    <%-- Makumba Generator - START OF NORMAL FIELDS --%>
      <tr>
        <th><label for="Language"><span class="accessKey">U</span>nique index</label></th>
        <td><mak:input field="Language" styleId="Language" accessKey="u" /></td>
      </tr>
      <tr>
        <th><label for="TS_modify"><span class="accessKey">L</span>ast modification date</label></th>
        <td><mak:input field="TS_modify" styleId="TS_modify" accessKey="l" /></td>
      </tr>
      <tr>
        <th><label for="TS_create">Cr<span class="accessKey">e</span>ation date</label></th>
        <td><mak:input field="TS_create" styleId="TS_create" accessKey="e" /></td>
      </tr>
      <tr>
        <th><label for="name"><span class="accessKey">n</span>ame</label></th>
        <td><mak:input field="name" styleId="name" accessKey="n" /></td>
      </tr>
      <tr>
        <th><label for="isoCode"><span class="accessKey">i</span>soCode</label></th>
        <td><mak:input field="isoCode" styleId="isoCode" accessKey="i" /></td>
      </tr>
      <tr>
        <td>    <input type="submit" value="Save changes" accessKey="S">    <input type="reset" accessKey="R">    <input type="reset" value="Cancel" accessKey="C" onClick="javascript:back();">    </td>
      </tr>
    </table>
  </mak:editForm>
  <%-- Makumba Generator - END OF NORMAL FIELDS --%>

  <%-- Makumba Generator - START OF SETS --%>

  <%-- Makumba Generator - END OF SETS --%>

</table>
</mak:object>

<%-- Makumba Generator - END OF *** EDIT ***  PAGE FOR OBJECT test.Language --%>
