<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/view-hql" prefix="mak" %>

<mak:object from="test.Person p join p.indiv i" where="i.name='john'">
    <mak:addForm object="p" field="address" action="testMakAddForm.jsp" method="post">
    	<mak:input name="email"/>
   	    <input type="submit" name="addemail" value="Add!">
    </mak:addForm>
</mak:object>

</body>
</html>