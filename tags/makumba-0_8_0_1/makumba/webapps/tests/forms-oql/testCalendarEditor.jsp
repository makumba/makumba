<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Multiple forms</title></head>
<body>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:list from="test.Person p">
  <mak:editForm object="p" action="testCalendarEditor.jsp">
    name: <mak:input name="indiv.name" />
    surname: <mak:input name="indiv.surname" type="password" maxlength="5" />
    birthdate: <mak:input name="birthdate" format="yyyy-MM-dd" calendarEditor="true" /><br>
  </mak:editForm>
</mak:list>

</body>
</html>