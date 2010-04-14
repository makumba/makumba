<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@page import="java.util.Enumeration"%>
<%@page import="java.util.Iterator"%>
<html>
<head>
<title>Section - list</title>
</head>

<body>

<button onClick="mak.event('myEvent1')">event1</button>
<br/><br/>
<mak:list from="test.Person p">
<mak:section name="divList" reloadOn="myEvent1"><mak:value expr="p.indiv.name" /></mak:section>
</mak:list>


</body>
</html>