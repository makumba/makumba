<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:newForm type="test.Person" action="" method="get" clientSideValidation="false">
  <mak:input name="picture"/>
  <input type="submit">
</mak:newForm>

</body>
</html>