<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Nested forms</title></head>
<body>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

testNewFormStart!<mak:newForm type="test.Person" name="myNewForm" action="testMakNewForm.jsp" method="post" clientSideValidation="false">!endNewFormStart
        testName!<mak:input name="indiv.name"/>!endName
        testAdd!<mak:addForm object="myNewForm" field="address">
          testAddEmail!<mak:input name="email"/>!endAddEmail
        </mak:addForm>!endAdd
        testSubmit!<input type="submit">!endSubmit
testNewFormEnd!</mak:newForm>!endNewFormEnd


</body>
</html>