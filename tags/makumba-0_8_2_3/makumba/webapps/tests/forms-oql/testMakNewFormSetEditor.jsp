<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Set Editors</title></head>

<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:newForm type="test.Person" action="testMakNewForm.jsp" method="post" clientSideValidation="false" name="someForm" >
  <mak:input name="speaks" type="seteditor" />
  <input type="submit">
</mak:newForm>
</body>
</html>
