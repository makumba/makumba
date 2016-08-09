<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/list" prefix="mak" %>


<mak:object from="test.Person p, p.indiv i" where="i.name='john'">
  testName!<mak:value expr="p.indiv.name" />!endName<br>
  testSurnameDefault!<mak:value expr="p.indiv.surname" default="N/A" />!endSurnameDefault<br>
  testUniqCharUrlEncode!<mak:value expr="p.uniqChar" urlEncode="true" />!endUniqCharUrlEncode<br>
  testUniqCharAuto!<mak:value expr="p.uniqChar" />!endUniqCharAuto<br>
  testUniqCharAutoAddTitleTrue!<mak:value expr="p.uniqChar" addTitle="true"/>!endUniqCharAutoAddTitleTrue<br>
  testUniqCharHtml!<mak:value expr="p.uniqChar" html="true" />!endUniqCharHtml<br>
  testUniqCharNoHtml!<mak:value expr="p.uniqChar" html="false" />!endUniqCharNoHtml<br>
  testUniqCharMaxLength!<mak:value expr="p.uniqChar" maxLength="8" />!endUniqCharMaxLength<br>
  testUniqCharMaxLengthEllipsis!<mak:value expr="p.uniqChar" maxLength="8" ellipsis="---" />!endUniqCharMaxLengthEllipsis<br>
  testUniqCharMaxLengthEllipsisAddTitleAuto!<mak:value expr="p.uniqChar" maxLength="8" ellipsis="---" addTitle="auto" />!endUniqCharMaxLengthEllipsisAddTitleAuto<br>
  testExtraDataSomething!<mak:value expr="p.extraData.something" />!endExtraDataSomething<br>
  testExtraDataSomethingEmpty!<mak:value expr="p.extraData.something" empty="N/A" />!endExtraDataSomethingEmpty<br>
</mak:object>

</body>
</html>