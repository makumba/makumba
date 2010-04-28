<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test Search Form with static where condition</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<mak:searchForm in="test.Person" name="searchPerson" action="testMakSearchFormStaticWhere.jsp">
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

<mak:resultList resultsFrom="searchPerson" staticWhere="length(o.fullName())>14" mode="filter">
  name: <mak:value expr="o.fullName()"/>, length: <mak:value expr="length(o.fullName())"/><br>
</mak:resultList>

</body>
</html>
