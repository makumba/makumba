<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Language list</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<b>Languages:</b>
<br><br><br>
<mak:list from="test.Language l">
  count:${mak:count()}<br>
  maxCount:${mak:maxCount()}<br>
</mak:list>
lastCount:${mak:lastCount()}<br>
</body>
</html>