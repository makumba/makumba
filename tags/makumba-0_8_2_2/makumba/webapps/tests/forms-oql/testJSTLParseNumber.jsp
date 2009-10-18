<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test bug 1071</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>

<fmt:parseNumber var="someNumber" value="125" />

<mak:form action="">
  <mak:input name="number" dataType="int" value="$someNumber" />
</mak:form>
</body>
</html>
