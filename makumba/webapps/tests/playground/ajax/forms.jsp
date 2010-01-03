<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<html>
<head>
<title>Forms</title>
</head>

<body>

<mak:section name="persons" reloadOn="personCreated">
  <mak:list from="test.Person p">
    <mak:value expr="p.indiv.name"/> <mak:value expr="p.indiv.surname"/>
  </mak:list>
</mak:section>
<br/>

<h1>New person</h1>
<mak:newForm name="newForm" type="test.Person" triggerEvent="personCreated" styleId="newForm" clientSideValidation="live" annotation="after">
  <mak:input field="indiv.name" /><br/>
  <mak:input field="indiv.surname" /><br/>
  <a href="" onClick="mak.sendForm('newForm_form1')">Lala</a>
</mak:newForm>
</body>

</html>