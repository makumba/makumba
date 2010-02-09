<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Form with partial postback</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<mak:section name="persons" reloadOn="personCreated">
  <mak:list from="test.Person p">
    <mak:value expr="p.indiv.name"/> <mak:value expr="p.indiv.surname"/><br/>
  </mak:list>
</mak:section>
<br/>

<h1>New person</h1>
<mak:newForm name="newForm" type="test.Person" triggerEvent="personCreated" clientSideValidation="false">
  <mak:input field="indiv.name" /><br/>
  <mak:input field="indiv.surname" /><br/>
  <br/>
  <mak:submit />
</mak:newForm>

</body>
</html>