<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Number editors</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:newForm type="test.Person" action="testMakNewForm.jsp" method="post" clientSideValidation="false">
  age normal:<br/>
  <mak:input name="age"/> <br/><br/>
  age select box:<br/>
  <mak:input name="age" type="select"/> <br/><br/>
  age select box, stepSize=5:<br/>
  <mak:input name="age" type="select" stepSize="5"/> <br/><br/>
  age radio buttons:<br/>
  <mak:input name="age" type="radio"/> <br/><br/>
  age radio buttons, stepSize=5:<br/>
  <mak:input name="age" type="radio" stepSize="5"/> <br/><br/>
  weight spinner:<br/>
  <mak:input name="weight" type="spinner"/> <br/><br/>
  uniqInt spinner, stepSize=5:<br/>
  <mak:input name="uniqInt" type="spinner" stepSize="5"/> <br/><br/>
  <input type="submit">
</mak:newForm>

</body>
</html>