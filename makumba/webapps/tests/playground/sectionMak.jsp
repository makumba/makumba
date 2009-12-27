<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@page import="java.util.Enumeration"%>
<%@page import="java.util.Iterator"%>
<%
String event = request.getParameter("_mak_eavent_");
if(event == null) { %>

<html>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<head>

<title>Section</title>
<script src="http://localhost:8080/tests/mak-tools/makumbaResources/javaScript/prototype.js" type="text/javascript"></script>
<script src="http://localhost:8080/tests/mak-tools/makumbaResources/javaScript/makumba-sections.js" type="text/javascript" ></script>
</head>
<body>
<button onClick="makEvent('myEvent1')">event1</button>
<button onClick="makEvent('myEvent3')">event3</button>
<br/><br/>

Div1: <mak:section name="div1" reload="myEvent1">div1 content</mak:section><br/>
Div2: <mak:section name="div2" show="myEvent2">div2 content</mak:section><br/>
Div3: <mak:section name="div3" hide="myEvent3">div3 content</mak:section><br/>
Div4: <mak:section name="div4" reload="myEvent3"><mak:list from="test.Person p"> <mak:value expr="p.indiv.name"/></mak:list></mak:section><br/>





</body>
</html>

<% } else {Thread.sleep(1000); response.setContentType("application/json");%>{div1: '<strong>new content div 1</strong>', div3: '<strong>new content div 3</strong>'}<% } %>