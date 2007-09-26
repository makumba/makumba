<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>
<%@taglib uri="http://www.makumba.org/list-hql" prefix="mak" %>
<%@taglib uri="http://www.makumba.org/forms" prefix="form" %>

<mak:object from="test.Person p join p.indiv i" where="i.name='john'">
    testMakEditFormStart!<form:edit object="p" action="testMakEditForm.jsp" method="post" clientSideValidation="false">!endMakEditFormStart
    	testName!<form:input name="indiv.name" />!endName
        testSurname!<form:input name="indiv.surname" type="password" maxlength="5" />!endSurname
        testGender!<form:input name="gender" type="tickbox" />!endGender
        testBirthdate!<form:input name="birthdate" format="yyyy-MM-dd" calendarEditor="false" />!endBirthdate
        testComment!<form:input name="comment" />!endComment
        testWeight!<form:input name="weight" />!endWeight
    testMakEditFormEnd!</form:edit>!endMakEditFormEnd
</mak:object>

</body>
</html>