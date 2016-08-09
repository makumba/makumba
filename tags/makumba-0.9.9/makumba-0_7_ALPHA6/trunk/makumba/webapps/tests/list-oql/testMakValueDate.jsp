<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


<mak:object from="test.Person p, p.indiv i" where="i.name='john'">
  testBirthdate!<mak:value expr="p.birthdate" />!endBirthdate<br>
  testBirthdateFormat!<mak:value expr="p.birthdate" format="dd-mm-yy" />!endBirthdateFormat<br>
</mak:object>

</body>
</html>