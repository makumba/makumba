<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>AddToNew</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:response /><br/>
<mak:newForm type="test.Person" action="testMakAddToNewFormValidation.jsp" name="p" clientSideValidation="false" reloadFormOnError="true">
    <mak:input name="indiv.name"/><br>
    <mak:addForm object="p" field="address" clientSideValidation="false">
      <mak:input name="description"/>
      <mak:input name="email"/>
    </mak:addForm>
    <input type="submit" name="addemail" value="Add!">    
</mak:newForm>

</body>
</html>