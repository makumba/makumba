<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<%@page import="java.util.Vector"%>
<%@page import="org.makumba.Pointer"%>
<html>
<head><title>Testing parameters with different types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%
   pageContext.setAttribute("indiv", "john");
%>

<mak:list from="test.Person p" where="p.indiv.name = $indiv OR p.indiv = $indiv">
  <mak:value expr="p.indiv.name" /> <mak:value expr="p.indiv.surname" /> <mak:value expr="p.indiv" var="indivPtr" />
<%
   pageContext.setAttribute("indiv", indivPtr);
%>
</mak:list>

<br/>


<mak:list from="test.Person p" where="p.indiv.name = $indiv OR p.indiv = $indiv" id="1">
  <mak:value expr="p.indiv.name" /> <mak:value expr="p.indiv.surname" /> <mak:value expr="p.indiv" var="indivPtr" />
</mak:list>

<br/>

<!--  shitty karamba behaviour -->
<mak:list from="test.Person p" where="p = $indiv OR p.indiv = $indiv" id="2">
  <mak:value expr="p.indiv.name" /> <mak:value expr="p.indiv.surname" /> <mak:value expr="p.indiv" var="indivPtr" />
</mak:list>


</body>
</html>