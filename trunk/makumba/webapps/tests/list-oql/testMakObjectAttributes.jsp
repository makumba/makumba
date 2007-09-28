<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Person list</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

Person:
<br>
<mak:object from="test.Person p, p.indiv i" where="i.name=$name">
  name:<mak:value expr="p.indiv.name" /><br> 
  weight:<mak:value expr="p.weight" /><br>
</mak:object>

</body>
</html>