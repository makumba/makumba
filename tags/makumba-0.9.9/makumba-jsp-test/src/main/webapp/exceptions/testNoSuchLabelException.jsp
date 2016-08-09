<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<html>
<head><title>Test Label Not Found</title></head>
<body>

<%-- should display an error complaining about an unknown label  --%>
<mak:list from="test.Person p">
  <mak:value expr="p2.indiv.name"/>
</mak:list>

</body>
