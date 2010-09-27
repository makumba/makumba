<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test Bug 958 - reserved keywords</title></head>
<body>

<h1><a href="http://bugs.best.eu.org/cgi-bin/bugzilla/show_bug.cgi?id=958">Bug 958 - reserved keywords</a></h1>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:list from="general.Person p">
  <mak:value expr="p.name" var="out" />
</mak:list>

</body>
