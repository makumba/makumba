<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak"%>
<html>
<head><title>Test OQL Functions 2</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

p.indiv.fullName()<br>
<mak:list from="test.Person p" >
  ${mak:count()} - <mak:value expr="p.indiv.fullName()"/><br>
</mak:list>
