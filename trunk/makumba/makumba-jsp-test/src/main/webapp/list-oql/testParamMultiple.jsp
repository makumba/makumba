<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<%@page import="java.util.Vector"%>
<%@page import="org.makumba.Pointer"%>
<html>
<head><title>Testing multiple parameters</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<% Vector languages = new Vector(); %>

<b>All languages:</b><br/>
<mak:list from="test.Language l">
  <c:if test="${mak:count() <= mak:maxCount() }"> <mak:value expr="l" var="l" /> <% languages.add(((Pointer) l).toExternalForm()); %> </c:if>
  <mak:value expr="l" />: <mak:value expr="l.name" /> <mak:value expr="l.isoCode" /> <br/>
</mak:list>
<br>

<b>All languages</b>: <%=languages%>
<br/><br/>

<% pageContext.setAttribute("languages", languages); %>

<b>Test IN SET, with all languages in parameter $languages</b><br/>
<mak:list from="test.Language l" where="l IN SET ($languages)" id="1">
  <mak:value expr="l" />: <mak:value expr="l.name" /> <mak:value expr="l.isoCode" /> <br/>
</mak:list>

<% languages.remove(0); %>

<b>Test IN SET, with all languages in parameter $languages but one</b><br/>
<mak:list from="test.Language l" where="l IN SET ($languages)">
  <mak:value expr="l" />: <mak:value expr="l.name" /> <mak:value expr="l.isoCode" /> <br/>
</mak:list>


<br/>

</body>
</html>