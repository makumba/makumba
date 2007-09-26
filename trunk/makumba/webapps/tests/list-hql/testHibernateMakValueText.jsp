<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/list-hql" prefix="mak" %>


<mak:object from="test.Person p join p.indiv i" where="i.name='john'">
  testComment!<mak:value expr="p.comment" />!endComment<br>
  testCommentLineSeparator!<mak:value expr="p.comment" lineSeparator="<abc>" />!endCommentLineSeparator<br>
  testCommentLongLineLength!<mak:value expr="p.comment" longLineLength="500" />!endCommentLongLineLength<br>
</mak:object>

</body>
</html>