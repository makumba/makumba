<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test distinct</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<b>Total languages spoken:</b>
<mak:list from="test.Person p, p.speaks l">
  <mak:value expr="count(*)" />
</mak:list>

<br>
<b>Unique languages spoken:</b>
<mak:list from="test.Person p, p.speaks l" id="distinct">
  <mak:value expr="count(distinct l)" />
</mak:list>

</body>
</html>