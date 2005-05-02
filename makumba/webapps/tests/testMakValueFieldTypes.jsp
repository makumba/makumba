<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


<mak:object from="test.Person p, p.indiv i, p.address a" where="i.name='john'">
  testName!<mak:value expr="p.indiv.name" />!endName<br>
  testSurname!<mak:value expr="p.indiv.surname" />!endSurname<br>
  testUniqCharAuto!<mak:value expr="p.uniqChar" />!endUniqCharAuto<br>
  testUniqCharAutoAddTitleTrue!<mak:value expr="p.uniqChar" addTitle="true"/>!endUniqCharAutoAddTitleTrue<br>
  testUniqCharHtml!<mak:value expr="p.uniqChar" html="true" />!endUniqCharHtml<br>
  testUniqCharNoHtml!<mak:value expr="p.uniqChar" html="false" />!endUniqCharNoHtml<br>
  testUniqCharMaxLength!<mak:value expr="p.uniqChar" maxLength="8" />!endUniqCharMaxLength<br>
  testUniqCharMaxLengthEllipsis!<mak:value expr="p.uniqChar" maxLength="8" ellipsis="---" />!endUniqCharMaxLengthEllipsis<br>
  testUniqCharMaxLengthEllipsisAddTitleAuto!<mak:value expr="p.uniqChar" maxLength="8" ellipsis="---" addTitle="auto" />!endUniqCharMaxLengthEllipsisAddTitleAuto<br>
  testBirthdate!<mak:value expr="p.birthdate" />!endBirthdate<br>
  testBirthdateFormat!<mak:value expr="p.birthdate" format="dd-mm-yy" />!endBirthdateFormat<br>
  testGender!<mak:value expr="p.gender" />!endGender<br>
  testUniqInt!<mak:value expr="p.uniqInt" />!endUniqInt<br>
  testWeight!<mak:value expr="p.weight" />!endWeight<br>
  testComment!<mak:value expr="p.comment" />!endComment<br>
  testCommentLineSeparator!<mak:value expr="p.comment" lineSeparator="<abc>" />!endCommentLineSeparator<br>
  testCommentLongLineLength!<mak:value expr="p.comment" longLineLength="500" />!endCommentLongLineLength<br>
  testAddressDescription!<mak:value expr="a.description" />!endAddressDescription<br>
  testAddressDescriptionEmpty!<mak:value expr="a.description" empty="N/A" />!endAddressDescriptionEmpty<br>
  testAddressEmailDefault!<mak:value expr="a.email" default="N/A" />!endAddressEmailDefault<br>
  testAddressUsagestart!<mak:value expr="a.usagestart" />!endAddressUsagestart
  
  testTS_create!<mak:value expr="p.TS_create" />!endTS_create<br>
  testTS_modify!<mak:value expr="p.TS_create" />!endTS_modify<br>  
</mak:object>

</body>
</html>