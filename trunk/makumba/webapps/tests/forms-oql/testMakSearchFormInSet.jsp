<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head>
<title>Test Search Form, ptrs/intEnum in SETs</title>
<link rel="StyleSheet" href="../style/style.css" type="text/css" media="all"/>
</head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<mak:response/>

<mak:searchForm in="test.Person" name="searchPerson" action="#results">
  gender <mak:criterion fields="gender"> <mak:searchField forceInputStyle="multiple" size="5" /> </mak:criterion> <br/>
  brother <mak:criterion fields="brother"> <mak:searchField forceInputStyle="multiple" size="5" /> </mak:criterion> <br/>
  <input type="submit" value="search">
</mak:searchForm>

<c:if test="${searchPersonDone}">
  <hr/>
  <b>Search form attributes:</b><br/>
  searchPersonDone: ${searchPersonDone}<br/>
  searchPersonFrom: ${searchPersonFrom}<br/>
  searchPersonVariableFrom: ${searchPersonVariableFrom}<br/>
  searchPersonWhere: ${searchPersonWhere}<br/>
  searchPersonQueryString: ${searchPersonQueryString}<br/>
  sortby: ${param.sortby}<br/>
  <hr/>
  <table id="results">
    <tr>
      <th><a href="testMakSearchFormInSet.jsp?${searchPersonQueryString}&sortby=o.TS_create" title="Sort by creation">#</a></th>
      <th><a href="testMakSearchFormInSet.jsp?${searchPersonQueryString}&sortby=o.indiv.name" title="Sort by name">Name</a></th>
      <th><a href="testMakSearchFormInSet.jsp?${searchPersonQueryString}&sortby=o.birthdate" title="Sort by birthdate">birthdate</a></th>
    </tr>
    <mak:resultList resultsFrom="searchPerson" orderBy="#{param.sortby}">
      <tr>
        <td>${mak:count()}</td>
        <td><mak:value expr="o.indiv.name" /> <mak:value expr="o.indiv.surname" /></td>
        <td><mak:value expr="o.birthdate" /></td>
        <td><mak:list from="o.speaks speaks2"><mak:value expr="speaks2.name"/> </mak:list></td>
      </tr>
    </mak:resultList>
  </table>
</c:if>

</body>
</html>
