<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


<mak:object from="test.Person p, p.indiv i" where="i.name='john'">
  testName!<mak:value expr="p.indiv.name" />!endName<br>
  testSurname!<mak:value expr="p.indiv.surname" />!endSurname<br>
  testAllAuto!<mak:value expr="p.uniqChar" />!endAllAuto<br>
  testAllHtml!<mak:value expr="p.uniqChar" html="true" />!endAllHtml<br>
  testAllNoHtml!<mak:value expr="p.uniqChar" html="false" />!endAllNoHtml<br>
  testBirthdate!<mak:value expr="p.birthdate" />!endBirthdate<br/>
  testBirthdateFormat!<mak:value expr="p.birthdate" format="dd-mm-yy" />!testBirthdateFormat<br/>
  
</mak:object>

</body>
</html>