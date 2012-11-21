<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<%@page import="java.util.Vector"%>
<%@page import="org.makumba.Pointer"%>
<html>
<head><title>Testing assignement of repeated parameter</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%
   pageContext.setAttribute("indiv", "john");
   pageContext.setAttribute("gender", "Male");
%>

<mak:list from="test.Person p" where="$indiv = 'john' OR $indiv = p.indiv.name AND p.gender = $gender">
  <mak:value expr="p.indiv.name" /> <mak:value expr="p.indiv.surname" /> <mak:value expr="p.gender" />
</mak:list>

<%
   pageContext.setAttribute("indiv", "bart");
%>

<mak:list from="test.Person p" where="$indiv = 'john' OR $indiv = p.indiv.name AND p.gender = $gender" id="1">
  <mak:value expr="p.indiv.name" /> <mak:value expr="p.indiv.surname" />
</mak:list>


</body>
</html>