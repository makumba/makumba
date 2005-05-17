<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


<mak:object from="test.Person p, p.indiv i, p.address a" where="i.name='john'">
  testAddressDescription!<mak:value expr="a.description" />!endAddressDescription<br>
  testAddressDescriptionEmpty!<mak:value expr="a.description" empty="N/A" />!endAddressDescriptionEmpty<br>
  testAddressEmailDefault!<mak:value expr="a.email" default="N/A" />!endAddressEmailDefault<br>
  testAddressUsagestart!<mak:value expr="a.usagestart" />!endAddressUsagestart
</mak:object>

</body>
</html>