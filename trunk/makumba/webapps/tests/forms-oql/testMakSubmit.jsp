<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>mak:submit</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>


<mak:newForm name="newForm" type="test.Person" action="testMakSubmit.jsp">
  <mak:input field="indiv.name" /><br/>
  <mak:input field="indiv.surname" /><br/>
  
  Simple: <mak:submit /><br/>
  Simple widget button: <mak:submit widget="button"/><br/>
  Simple widget link: <mak:submit widget="link"/><br/>
  Custom widget button: <mak:submit widget="button">Custom text in button</mak:submit><br/>
  Custom widget link: <mak:submit widget="link">Custom text in link</mak:submit><br/>
  
</mak:newForm>

<mak:newForm name="newForm" type="test.Person" triggerEvent="someEvent">
  <mak:input field="indiv.name" /><br/>
  <mak:input field="indiv.surname" /><br/>
  
  Simple: <mak:submit /><br/>
  Simple widget button: <mak:submit widget="button"/><br/>
  Simple widget link: <mak:submit widget="link"/><br/>
  Custom widget button: <mak:submit widget="button">Custom text in button</mak:submit><br/>
  Custom widget link: <mak:submit widget="link">Custom text in link</mak:submit><br/>
  
</mak:newForm>


</body>
</html>