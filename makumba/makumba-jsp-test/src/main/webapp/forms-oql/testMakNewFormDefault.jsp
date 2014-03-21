<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Default for mak:input</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<% pageContext.setAttribute("defaultBirthday", new java.util.Date(85, 6, 31)); %>
<c:set var="defaultSurname" value="Enter a surname...." />

<mak:newForm type="test.Person" action="testMakAddForm.jsp" method="post" clientSideValidation="false">
  <mak:input name="indiv.name" default="Enter a name ...." /> <br/>
  <mak:input name="indiv.surname" default="$defaultSurname" /> <br/>
  <mak:input name="birthdate" default="$defaultBirthday" />
</mak:newForm>

</body>
</html>