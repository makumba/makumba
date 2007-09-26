<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/list-hql" prefix="mak" %>


<mak:object from="test.Person p join p.indiv i join p.address a" where="i.name='john'">
  testAddressDescription!<mak:value expr="a.description" />!endAddressDescription<br>
  testAddressDescriptionEmpty!<mak:value expr="a.description" empty="N/A" />!endAddressDescriptionEmpty<br>
  testAddressPhoneDefault!<mak:value expr="a.phone" default="N/A" />!endAddressPhoneDefault<br>
  testAddressUsagestart!<mak:value expr="a.usagestart" />!endAddressUsagestart
</mak:object>

</body>
</html>