<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Person list</title></head>
<body>

<%@taglib uri="http://www.makumba.org/list-hql" prefix="mak" %>

Person:
<br>
<mak:object from="test.Person p join p.indiv i" where="i.name='john'">
  <mak:if test="p.indiv.name = 'john'"> It's johnny! </mak:if><br>
</mak:object>

</body>
</html>