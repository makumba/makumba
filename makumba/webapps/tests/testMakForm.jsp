<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:object from="test.Person p, p.indiv i" where="i.name='john'">
    testMakFormStart!<mak:form handler="doSomething" action="testMakAddForm.jsp" method="post">!endMakFormStart
        testInput!<mak:input name="xxx" value="i.name"/>!endInput
        <input type="submit">
    testMakFormEnd!</mak:form>!endMakFormEnd
</mak:object>

</body>
</html>