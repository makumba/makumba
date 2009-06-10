<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test bug 588</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<b>Languages:</b>
<mak:list from="test.Language l">
  <mak:if test="$attr=1">
    $attr=1 !
  </mak:if>
</mak:list>
</body>
</html>