<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<html>
<head>
<title>Makumba template welcome page</title>
</head>
<body>

<h1>Create a new person</h1>
<mak:newForm type="general.Person" triggerEvent="personCreated">
Name: <mak:input field="name" /><br/>
Surname: <mak:input field="surname" />
<mak:submit/>
</mak:newForm>

<h1>List of persons</h1>
<mak:section name="personList" reloadOn="personCreated">
  <mak:list from="general.Person p">
    <mak:value expr="p.name + ' ' + p.surname" /><br/>
  </mak:list>
</mak:section>
</body>
</html>