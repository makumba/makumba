<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Testing different types for mak:input</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


<mak:newForm type="test.Person" action="" clientSideValidation="true">
  age spinner: <mak:input name="age" type="spinner" /> <br/> <br/>
</mak:newForm>

<mak:newForm type="test.Person" action="" clientSideValidation="true">
  age drop down: <mak:input name="age" type="select" /> <br/> <br/>
</mak:newForm>

<mak:newForm type="test.Person" action="" clientSideValidation="true">
  age drop down with steps: <mak:input dataType="int" name="age" type="select" stepSize="3" /> <br/> <br/>
</mak:newForm>

<mak:newForm type="test.Person" action="" clientSideValidation="true">
  age radio buttons: <mak:input name="age" type="radio" /> <br/> <br/>
</mak:newForm>

<mak:newForm type="test.Person" action="" clientSideValidation="true">
  age radio buttons with steps: <mak:input  name="age" type="radio" stepSize="3" /> <br/> <br/>
  speaks set editor: <mak:input name="speaks" type="seteditor" /> <br/> <br/>
</mak:newForm>


</body>
</html>