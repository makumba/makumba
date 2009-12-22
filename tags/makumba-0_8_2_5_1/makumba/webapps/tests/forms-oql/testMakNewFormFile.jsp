<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>File input</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:newForm type="test.Person" action="testMakNewFormFile.jsp" method="post" clientSideValidation="false">
  <mak:input name="someAttachment"/><br/>
  <input type="submit">
</mak:newForm>

</body>
</html>