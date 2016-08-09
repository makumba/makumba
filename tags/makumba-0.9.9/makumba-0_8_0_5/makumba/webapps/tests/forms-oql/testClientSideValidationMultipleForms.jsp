<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Multiple forms</title></head>
<body>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<mak:form action="testMakEditForm.jsp" method="post" >
  <mak:list from="test.Person p">
    <mak:editForm object="p" clientSideValidation="live">
      name: <mak:input name="indiv.name" /><br>
        surname: <mak:input name="indiv.surname" type="password" maxlength="5" /><br>
        gender: <mak:input name="gender" type="tickbox" /><br>
        birthdate: <mak:input name="birthdate" format="yyyy-MM-dd" calendarEditor="false" /><br>
        comment: <mak:input name="comment" /><br>
        weight: <mak:input name="weight" />
    </mak:editForm>
    <hr>
  </mak:list>
</mak:form>

</body>
</html>