<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/view-hql" prefix="mak" %>


<mak:object from="test.Person p join p.indiv i" where="i.name='john'">
  testGender!<mak:value expr="p.gender" />!endGender<br>
  testUniqInt!<mak:value expr="p.uniqInt" />!endUniqInt<br>
</mak:object>

</body>
</html>