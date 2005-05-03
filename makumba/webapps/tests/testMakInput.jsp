<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:object from="test.Person p, p.indiv i" where="i.name='john'">
    <mak:editForm object="p" action="testMakInput.jsp" method="post">
        editName!<mak:input name="indiv.name" />!endName
        editSurname!<mak:input name="indiv.surname" type="password" maxlength="5" />!endSurname
        editGender!<mak:input name="gender" type="tickbox" />!endGender
        editBirthdate!<mak:input name="birthdate" format="yyyy-MM-dd" />!endBirthdate
        editComment!<mak:input name="comment" />!endComment
    </mak:editForm>
</mak:object>

</body>
</html>