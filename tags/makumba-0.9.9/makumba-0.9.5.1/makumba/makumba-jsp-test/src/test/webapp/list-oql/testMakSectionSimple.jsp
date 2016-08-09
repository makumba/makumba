<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@page import="java.util.Enumeration"%>
<%@page import="java.util.Iterator"%>
<html>
<head>
<title>Section - simple</title>
</head>

<body>
<button onClick="mak.event('myEvent1')">event1</button>
<button onClick="mak.event('myEvent2')">event2</button>
<button onClick="mak.event('myEvent3')">event3</button>
<br/><br/>

Div1: <mak:section name="div1" reloadOn="myEvent1">div1 content</mak:section><br/>
Div2: <mak:section name="div2" showOn="myEvent2">div2 content</mak:section><br/>
Div3: <mak:section name="div3" hideOn="myEvent3">div3 content</mak:section><br/>
Div4: <mak:section name="div4" reloadOn="myEvent3"><mak:list from="test.Person p"> <mak:value expr="p.indiv.name"/></mak:list></mak:section><br/>

</body>
</html>