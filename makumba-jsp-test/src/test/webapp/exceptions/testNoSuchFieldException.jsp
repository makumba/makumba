<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<html>
<head><title>Test Data Definition Not Found</title></head>
<body>

<%-- should display a data definition not found exception  --%>
<mak:list from="test.Person p">
  <mak:value expr="p.someNotExistingField"/>
</mak:list>

</body>
