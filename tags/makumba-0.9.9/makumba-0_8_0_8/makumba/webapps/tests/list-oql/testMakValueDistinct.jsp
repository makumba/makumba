<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test distinct</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


<mak:list from="test.Person p, p.indiv i">
  <mak:value expr="count(distinct(p))" />
</mak:list>

</body>
</html>