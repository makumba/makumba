<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<mak:object from="test.Person p" where="p.indiv.name='addToNewPerson'">
	<mak:list from="p.address a">
        testDescription!<mak:value expr="a.description" />!endDescription
        testEmail!<mak:value expr="a.email" />!endEmail
	</mak:list>
</mak:object>

</body>
</html>