<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test Bug 946</title></head>
<body>

<%@taglib uri="http://www.makumba.org/forms" prefix="form" %>

<form:new type="test.Person" action="" clientSideValidation="false">
  Gender: <form:input field="gender" value="1" />
</form:new>

</body>
