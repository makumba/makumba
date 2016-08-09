<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<mak:searchForm in="test.Person" name="searchPerson" action="testMakSearchForm2.jsp">
  name or surname <mak:criterion fields="indiv.name, indiv.surname" matchMode="begins"> <mak:searchField /> </mak:criterion>
  <input type="submit" value="search">
</mak:searchForm>

<c:if test="${searchPersonDone}">
  <hr>
  searchPersonWhere: ${searchPersonWhere}<br>
  searchPersonFrom: ${searchPersonFrom}<br>  
  <mak:resultList resultsFrom="searchPerson">
    <mak:value expr="o.indiv.name"/> <mak:value expr="o.indiv.surname"/><br>
  </mak:resultList>
</c:if>

</body>
</html>