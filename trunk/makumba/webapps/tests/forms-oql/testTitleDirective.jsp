<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

Testing selecting test.Individual
<mak:newForm type="test.Person" action="testMakNewForm.jsp" method="post" clientSideValidation="false">
  <mak:input name="indiv" />
</mak:newForm>

Testing selecting test.Person
<mak:newForm type="test.Person" action="testMakNewForm.jsp" method="post" clientSideValidation="false">
  <mak:input name="brother" />
</mak:newForm>

</body>
</html>