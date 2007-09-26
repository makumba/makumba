<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/list" prefix="mak" %>
<%@taglib uri="http://www.makumba.org/forms" prefix="form" %>

<mak:object from="test.Person p, p.indiv i" where="i.name='john'">
    <form:add object="p" field="address" action="testMakAddForm.jsp" method="post" clientSideValidation="false">
    	<form:input name="email"/>
   	    <input type="submit" name="addemail" value="Add!">
    </form:add>
</mak:object>

</body>
</html>