<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Nested forms</title></head>
<body>
<%@taglib uri="http://www.makumba.org/forms" prefix="form" %>

testNewFormStart!<form:new type="test.Person" name="myNewForm" action="testMakNewForm.jsp" method="post" clientSideValidation="false">!endNewFormStart
        testName!<form:input name="indiv.name"/>!endName
        testAdd!<form:add object="myNewForm" field="address">
          testAddEmail!<form:input name="email"/>!endAddEmail
        </form:add>!endAdd
        testSubmit!<input type="submit">!endSubmit
testNewFormEnd!</form:new>!endNewFormEnd


</body>
</html>