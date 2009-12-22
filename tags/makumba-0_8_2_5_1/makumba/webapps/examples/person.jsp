<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Person list</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

Persons:
<mak:list from="Person p">
  <mak:value expr="p.name" />, <mak:value expr="p.age" /> <br/>
</mak:list>

</body>
</html>