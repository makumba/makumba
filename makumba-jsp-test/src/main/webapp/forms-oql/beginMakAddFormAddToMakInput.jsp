<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Add to result of mak:input</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:form action="beginMakAddFormAddToMakInput.jsp" clientSideValidation="false">
  Select person: <mak:input name="p" dataType="ptr test.Person" /><br/>
  Add address:<br/>
  <mak:addForm object="p" field="address" clientSideValidation="false">
    Description: <mak:input name="description"/>
    E-mail: <mak:input name="email"/>
  </mak:addForm>
  <br/>
  <input type="submit" name="addemail" value="Add!">    
</mak:form>

</body>
</html>