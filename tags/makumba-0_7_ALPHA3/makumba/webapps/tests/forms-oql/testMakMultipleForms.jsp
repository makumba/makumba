<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Multiple forms</title></head>
<body>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<mak:list from="test.Person p">
    testMakEditFormStart!<mak:editForm object="p" action="testMakEditForm.jsp" method="post" clientSideValidation="false">!endMakEditFormStart
      testName!<mak:input name="indiv.name" />!endName
        testSurname!<mak:input name="indiv.surname" type="password" maxlength="5" />!endSurname
        testGender!<mak:input name="gender" type="tickbox" />!endGender
        testBirthdate!<mak:input name="birthdate" format="yyyy-MM-dd" calendarEditor="false" />!endBirthdate
        testComment!<mak:input name="comment" />!endComment
        testWeight!<mak:input name="weight" />!endWeight
    testMakEditFormEnd!</mak:editForm>!endMakEditFormEnd
</mak:list>

</body>
</html>