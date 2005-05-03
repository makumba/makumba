<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Language list</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<b>Languages:</b>
<br><br><br>
<mak:list from="test.Language l">
  name:<mak:value expr="l.name"/>
  <br>
  isoCode:<mak:value expr="l.isoCode"/>
  <br><br>
</mak:list>

</body>
</html>
