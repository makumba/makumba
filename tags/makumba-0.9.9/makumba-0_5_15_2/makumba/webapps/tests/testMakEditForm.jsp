<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:object from="test.Person p, p.indiv i" where="i.name='john'">
    testMakEditFormStart!<mak:editForm object="p" action="testMakEditForm.jsp" method="post">!endMakEditFormStart
    	testName!<mak:input name="indiv.name" />!endName
        testSurname!<mak:input name="indiv.surname" type="password" maxlength="5" />!endSurname
        testGender!<mak:input name="gender" type="tickbox" />!endGender
        testBirthdate!<mak:input name="birthdate" format="yyyy-MM-dd" />!endBirthdate
        testComment!<mak:input name="comment" />!endComment
        testWeight!<mak:input name="weight" />!endWeight
    testMakEditFormEnd!</mak:editForm>!endMakEditFormEnd
</mak:object>

</body>
</html>