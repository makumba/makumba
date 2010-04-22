<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head>
<title>Test Search Form #3</title>
<link rel="StyleSheet" href="../style/style.css" type="text/css" media="all"/>
</head>
<body>

<% pageContext.setAttribute("startSearch", new java.util.Date(75, 0, 1)); %>
<% pageContext.setAttribute("endSearch", new java.util.Date(109, 11, 1)); %>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<mak:searchForm in="test.Person" name="searchPerson" action="testMakSearchForm3.jsp">
  name or surname <mak:criterion fields="indiv.name, indiv.surname"> <mak:matchMode matchModes="contains, equals, begins, ends"/> <mak:searchField /> </mak:criterion> <br/>
  birthdate <mak:criterion fields="birthdate" isRange="true"> <mak:matchMode /> <mak:searchField default="$startSearch" /> AND <mak:searchField role="rangeEnd" default="$endSearch"/> </mak:criterion> <br/>
  Modified between: <mak:criterion fields="TS_modify" isRange="true"> <mak:searchField default="$startSearch" /> and <mak:searchField role="rangeEnd"/> </mak:criterion> <br/>
  Creation between: <mak:criterion fields="TS_create" isRange="true"> <mak:searchField default="$startSearch" /> and <mak:searchField role="rangeEnd"/> </mak:criterion> <br/>
  <input type="submit" value="search">
</mak:searchForm>

<c:if test="${searchPersonDone}">
  <c:set var="sortby" value="o.indiv.name, o.indiv.surname" />
  <c:choose>
    <c:when test='${param.sortby=="creation"}'><c:set var="sortby" value="o.TS_create, o.indiv.name, o.indiv.surname" /></c:when>
    <c:when test='${param.sortby=="name"}'><c:set var="sortby" value="o.indiv.name, o.indiv.surname" /></c:when>
    <c:when test='${param.sortby=="birthdate"}'><c:set var="sortby" value="o.birthdate" /></c:when>
  </c:choose>
  <hr/>
  <b>Search form attributes:</b><br/>
  searchPersonDone: ${searchPersonDone}<br/>
  searchPersonFrom: ${searchPersonFrom}<br/>
  searchPersonWhere: ${searchPersonWhere}<br/>
  searchPersonQueryString: ${searchPersonQueryString}<br/>
  <hr/>
  <table>
    <tr>
      <th><a href="testMakSearchForm3.jsp?${searchPersonQueryString}&sortby=creation" title="Sort by creation">#</a></th>
      <th><a href="testMakSearchForm3.jsp?${searchPersonQueryString}&sortby=name" title="Sort by name">Name</a></th>
      <th><a href="testMakSearchForm3.jsp?${searchPersonQueryString}&sortby=birthdate" title="Sort by birthdate">birthdate</a></th>
    </tr>
    <mak:resultList resultsFrom="searchPerson" orderBy="#{sortby}">
      <tr> <td>${mak:count()}</td> <td><mak:value expr="o.indiv.name"/> <mak:value expr="o.indiv.surname"/></td> <td><mak:value expr="o.birthdate"/></td> </tr>
    </mak:resultList>
  </table>
</c:if>

</body>
</html>