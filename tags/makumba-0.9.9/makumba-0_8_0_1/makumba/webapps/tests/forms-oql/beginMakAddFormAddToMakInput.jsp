<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Add to result of mak:input</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:form action="testMakAddToNewForm.jsp" clientSideValidation="false">
  <mak:input name="p" dataType="ptr test.Person" /><br>
  <mak:addForm object="p" field="address" clientSideValidation="false">
    <mak:input name="description"/>
    <mak:input name="email"/>
  </mak:addForm>
  <input type="submit" name="addemail" value="Add!">    
</mak:form>

</body>
</html>