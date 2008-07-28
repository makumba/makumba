<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Advanced mak:value tests</title></head>
<body>

<%@taglib uri="http://www.makumba.org/list-hql" prefix="mak" %>


<mak:object from="test.Person p join p.indiv i" where="i.name='john'">
  testWeightTimes3_0!<mak:value expr="p.weight * 3.0" />!endWeight<br>
  testWeightTimes3!<mak:value expr="p.weight * 3" />!endWeight<br>
</mak:object>

</body>
</html>