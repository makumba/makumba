<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@page import="java.util.Enumeration"%>
<%@page import="java.util.Iterator"%>
<html>
<head>
<title>Section - list with iteration expression</title>
</head>

<body>

<button onClick="mak.event('myEvent1')">event1</button>
<br/><br/>
<mak:list from="test.Person p">
<button onClick="mak.event('myEvent1', '<mak:value expr="p.indiv.name" />')">event5 iteration <mak:value expr="p.indiv.name" /></button>
<mak:section name="divListId" reloadOn="myEvent1" iterationExpr = "p.indiv.name"><mak:value expr="p.indiv.name" /></mak:section>
</mak:list>

</body>
</html>