<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

testNewFormStart!<mak:newForm type="test.Person" action="testMakNewForm.jsp" method="post" clientSideValidation="false">!endNewFormStart
        testFile!<mak:input name="picture" type="file" />!endFile
        testSubmit!<input type="submit">!endSubmit
testNewFormEnd!</mak:newForm>!endNewFormEnd

</body>
</html>