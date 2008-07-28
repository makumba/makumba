<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Language list</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<b>Languages:</b>
<br><br><br>
<mak:list from="test.Language l">
  countLanguage:${mak:count()}<br>
  maxCountLanguage:${mak:maxCount()}<br>
  <mak:list from="test.Person p">
    nestedCountPerson:${mak:count()}<br>
    nestedMaxCountPerson:${mak:maxCount()}<br>
  </mak:list>
  lastCountPerson:${mak:lastCount()}<br>
  countLanguage2:${mak:count()}<br>
  maxCountLanguage2:${mak:maxCount()}<br>
  lastCountPerson2:${mak:lastCount()}<br>
</mak:list>
lastCountLanguage:${mak:lastCount()}<br>
</body>
</html>