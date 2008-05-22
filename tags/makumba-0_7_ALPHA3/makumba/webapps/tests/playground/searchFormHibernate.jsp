<% /* $Id: /usr/local/cvsroot/karamba/public_html/welcome.jsp,v 2.39 2007/06/28 17:18:22 manu Exp $ */ %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ page buffer="16kb"%>
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
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<style>
<!--
.smallText {
  font-size: x-small;
}
-->
</style>

<table border="0">
  <mak:searchForm in="test.Person" name="searchArchive" method="get">
    <tr>
      <th>name</th> 
      <td>
        <mak:criterion fields="indiv.name, indiv.surname">
          <mak:matchMode matchModes="contains, equals, begins, ends" type="radio" /><br>
          <mak:searchField /> 
        </mak:criterion>
      </td>
    </tr>
    <tr>
      <th>uniq ptr (language)</th> 
      <td>
        <mak:criterion fields="uniqPtr"> <mak:searchField nullOption="any" /> </mak:criterion>
      </td>
    </tr>
    <tr>
      <th>uniq ptr (language) name</th> 
      <td>
        <mak:criterion fields="uniqPtr.name"> 
          <mak:matchMode matchModes="contains, equals, begins, ends" type="radio" /><br>
          <mak:searchField /> 
        </mak:criterion>
      </td>
    </tr>
    <tr>
      <th>speaks</th> 
      <td>
        <mak:criterion fields="speaks"> 
          <mak:searchField size="4"/> 
        </mak:criterion>
      </td>
    </tr>
    <tr>
      <th>gender</th> 
      <td>
        <mak:criterion fields="gender"> <mak:searchField nullOption="any" type="tickbox" /> </mak:criterion>
      </td>
    </tr>
    <tr>
      <th>designer</th> 
      <td>
        <mak:criterion fields="designer"> <mak:searchField nullOption="any" type="tickbox" /> </mak:criterion> 
      </td>
    </tr>
    <tr>
      <th>birthday</th> 
      <td>
        <mak:criterion fields="birthdate">
          <mak:matchMode matchModes="equals, before, after" /> 
          <mak:searchField /> 
        </mak:criterion>
      </td>
    </tr>
    <tr>
      <th>firstSex</th> 
      <td>
        <mak:criterion fields="firstSex" isRange="true">
          <mak:matchMode matchModes="between, betweenInclusive" /> 
          <mak:searchField role="rangeBegin" /> 
          <mak:searchField role="rangeEnd" /> 
        </mak:criterion>
      </td>
    </tr>
    <td colspan="2" align="center"><input type="submit"></td>
  </mak:searchForm>

</table>
<br/>
<%--results:<br>
done: ${searchArchiveDone}<br>
from: ${searchArchiveVariableFrom}<br>
--%>
where: ${searchArchiveWhere}<br>
<br>

<c:if test="${searchArchiveDone}">
  <h3>Results with mak:resultList</h3>
  <table width="100%">
    <%@include file="personListHeaderInclude.jsp" %>
    <mak:resultList resultsFrom="searchArchive" >
      <%@include file="personListDisplayInclude.jsp" %>
    </mak:resultList>
  </table>
  
  <h3>Results with standard mak:list</h3>
  <table width="100%">
    <%@include file="personListHeaderInclude.jsp" %>
    <mak:list from="test.Person o" variableFrom="#{searchArchiveVariableFrom}" where="#{searchArchiveWhere}" id="makList">
      <%@include file="personListDisplayInclude.jsp" %>
    </mak:list>
  </table>  
</c:if>

<hr/>

</body>
<html>