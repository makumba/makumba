<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test Search Form, filter mode</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<mak:searchForm in="test.Person" name="searchPerson" action="testMakSearchFormFilterMode.jsp">
  name or surname <mak:criterion fields="indiv.name, indiv.surname"> <mak:matchMode matchModes="contains, equals, begins, ends"/> <mak:searchField /> </mak:criterion>
  <input type="submit" value="search">
</mak:searchForm>

<hr/>
<b>Search form attributes:</b><br/>
searchPersonDone: ${searchPersonDone}<br/>
searchPersonFrom: ${searchPersonFrom}<br/>
searchPersonVariableFrom: ${searchPersonVariableFrom}<br/>
searchPersonWhere: ${searchPersonWhere}<br/>
searchPersonQueryString: ${searchPersonQueryString}<br/>
<hr/>

<mak:resultList resultsFrom="searchPerson" mode="filter">
  <mak:value expr="o.indiv.name"/> <mak:value expr="o.indiv.surname"/><br>
</mak:resultList>

</body>
</html>