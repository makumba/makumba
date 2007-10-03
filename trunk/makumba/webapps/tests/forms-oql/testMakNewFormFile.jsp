<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/forms" prefix="form" %>

testNewFormStart!<form:new type="test.Person" action="testMakNewForm.jsp" method="post" clientSideValidation="false">!endNewFormStart
        testFile!<form:input name="picture" type="file" />!endFile
        testSubmit!<input type="submit">!endSubmit
testNewFormEnd!</form:new>!endNewFormEnd

</body>
</html>