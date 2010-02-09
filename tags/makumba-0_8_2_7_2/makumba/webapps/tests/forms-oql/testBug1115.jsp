<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test Bug 1115</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:form name="someForm">
  <mak:action>test.jsp?param=test&param2=test#myAnchor</mak:action>
  Gender: <mak:input field="gender" value="1" />
</mak:form>

</body>
</html>