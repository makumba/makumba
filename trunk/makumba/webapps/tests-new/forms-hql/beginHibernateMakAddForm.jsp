<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>
<%@taglib uri="http://www.makumba.org/list-hql" prefix="mak" %>
<%@taglib uri="http://www.makumba.org/forms" prefix="form" %>

<mak:object from="test.Person p join p.indiv i" where="i.name='john'">
    <form:add object="p" field="address" action="testHibernateMakAddForm.jsp" method="post">
    	<form:input name="email"/>
   	    <input type="submit" name="addemail" value="Add!">
    </form:add>
</mak:object>

</body>
</html>