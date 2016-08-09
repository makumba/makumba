<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>
<%@taglib uri="http://www.makumba.org/list" prefix="mak" %>
<%@taglib uri="http://www.makumba.org/forms" prefix="form" %>

<mak:object from="test.Person p" where="p.indiv.name='john'">
	testEmail!<mak:list from="p.address a">
	   	<mak:value expr="a.email" />
	</mak:list>!endEmail
</mak:object>

</body>
</html>