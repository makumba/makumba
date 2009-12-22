<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test bug 589</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<c:set var="someString" value="abcd" />
<mak:list from="test.Person p">
  <mak:if test="$someString = 'abcd'"> ${someString}=='abcd' <br/></mak:if>
</mak:list>  
</body>
</html>