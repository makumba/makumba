<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak"%>
<html>
<head><title>Test MDD Functions 2</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:list from="test.Person p" >
  <mak:value expr="p.fullName()"/>, <mak:value expr="p.indiv"/>: <mak:value expr="p.functionWithPointerIsThatPerson(p.indiv)"/> <br/>
</mak:list>
