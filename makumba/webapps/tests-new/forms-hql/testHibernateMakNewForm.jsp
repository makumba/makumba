<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>
<%@taglib uri="http://www.makumba.org/list-hql" prefix="mak" %>
<%@taglib uri="http://www.makumba.org/forms" prefix="form" %>

testNewFormStart!<form:new type="test.Person" action="testHibernateMakNewForm.jsp" method="post" clientSideValidation="false">!endNewFormStart
        testName!<form:input name="indiv.name"/>!endName
        testSubmit!<input type="submit">!endSubmit
testNewFormEnd!</form:new>!endNewFormEnd

</body>
</html>