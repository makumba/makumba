<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<html>
<head>
<title>Forms</title>
</head>

<body>

<mak:section name="persons" reloadOn="personsEdited">
  <mak:list from="test.Person p">
    <mak:value expr="p.indiv.name"/> <mak:value expr="p.indiv.surname"/><br/>
  </mak:list>
</mak:section>
<br/>

<h1>New person</h1>
<mak:form triggerEvent="personsEdited" clientSideValidation="live" annotation="after">
  <mak:list from="test.Person o">
    <mak:editForm object="o">
    Name: <mak:input field="indiv.name" />&nbsp;
    Surname: <mak:input field="indiv.surname" /><br/>
  </mak:editForm>
</mak:list>

  <mak:submit /><br/>

</mak:form>
</body>
</html>