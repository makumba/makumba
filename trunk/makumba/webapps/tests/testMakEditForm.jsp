<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:object from="test.Person p, p.indiv i" where="i.name='john'">
    testMakEditStart!<mak:editForm object="p" action="testMakEditForm.jsp" method="post">!endMakEditStart
		<mak:input name="indiv.name" />
    testMakEditEnd!</mak:editForm>!endMakEditEnd
</mak:object>

</body>
</html>