<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<%@page import="java.util.Vector"%>
<%@page import="org.makumba.Pointer"%>
<html>
<head><title>Testing IN and NOT IN</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<% Vector otherPeople = new Vector(); %>

<b>All persons:</b><br/>
<mak:list from="test.Person p">
  <c:if test="${mak:count() < mak:maxCount() }"> <mak:value expr="p" var="p" /> <% otherPeople.add(((Pointer) p).toExternalForm()); %> </c:if>
  <mak:value expr="p" />: <mak:value expr="p.indiv.name" /> <mak:value expr="p.indiv.surname" /> <br/>
</mak:list>
<br>

<b>Everyone but the last person</b>: <%=otherPeople%>
<br/><br/>

<% pageContext.setAttribute("otherPeople", otherPeople); %>

<b>Test NOT IN, skipping last person</b><br/>
<mak:list from="test.Person p" where="1=1 AND (p NOT IN SET ($otherPeople)) AND 1=1">
  <mak:value expr="p" />: <mak:value expr="p.indiv.name" /> <mak:value expr="p.indiv.surname" /> <br/>
</mak:list>
<br/>

<b>Test NOT IN, version #2</b><br/>
<mak:list from="test.Person p" where="1=1 AND NOT (p IN SET ($otherPeople)) AND 1=1">
  <mak:value expr="p" />: <mak:value expr="p.indiv.name" /> <mak:value expr="p.indiv.surname" /> <br/>
</mak:list>
<br>

<b>Testing the opposite</b><br/>
<mak:list from="test.Person p" where="1=1 AND (p IN SET ($otherPeople)) AND 1=1">
  <mak:value expr="p" />: <mak:value expr="p.indiv.name" /> <mak:value expr="p.indiv.surname" /> <br/>
</mak:list>


</body>
</html>