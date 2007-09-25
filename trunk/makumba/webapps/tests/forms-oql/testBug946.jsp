<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test Bug 946</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:newForm type="test.Person" action="" clientSideValidation="false">
  Gender: <mak:input field="gender" value="1" />
</mak:newForm>

</body>
