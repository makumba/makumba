<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


<mak:object from="test.Person p, p.indiv i" where="i.name='john'">
  testName!<mak:value expr="p.indiv.name" />!endName<br>
  testSurname!<mak:value expr="p.indiv.surname" />!endSurname<br>
  testUniqCharAuto!<mak:value expr="p.uniqChar" />!endUniqCharAuto<br>
  testUniqCharHtml!<mak:value expr="p.uniqChar" html="true" />!endUniqCharHtml<br>
  testUniqCharNoHtml!<mak:value expr="p.uniqChar" html="false" />!endUniqCharNoHtml<br>
  testUniqCharMaxLength!<mak:value expr="p.uniqChar" maxLength="8" />!endUniqCharMaxLength<br>
  testUniqCharMaxLengthEllipsis!<mak:value expr="p.uniqChar" maxLength="8" ellipsis="---" />!endUniqCharMaxLengthEllipsis<br>
  testBirthdate!<mak:value expr="p.birthdate" />!endBirthdate<br>
  testBirthdateFormat!<mak:value expr="p.birthdate" format="dd-mm-yy" />!endBirthdateFormat<br>
  
</mak:object>

</body>
</html>