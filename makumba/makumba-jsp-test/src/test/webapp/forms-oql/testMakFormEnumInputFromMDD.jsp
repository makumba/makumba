<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


<mak:form action="testMakFormSetInput.jsp" method="post" clientSideValidation="false">
  <mak:input dataType="test.Person.intSet" name="intSet" />
  <mak:input dataType="test.ParserTest.education.minimumlength" name="minimumlength" />
  <input type="submit">
</mak:form>

</body>
</html>