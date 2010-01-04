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
<mak:newForm name="newForm" type="test.Person" method="get" triggerEvent="personCreated" clientSideValidation="false" annotation="after">
  <mak:input field="indiv.name" /><br/>
  <mak:input field="indiv.surname" /><br/>
  
  Simple: <mak:submit /><br/>
  Simple widget button: <mak:submit widget="button"/><br/>
  Simple widget link: <mak:submit widget="link"/><br/>
  Custom widget button: <mak:submit widget="button">Custom text</mak:submit><br/>
  Custom widget link: <mak:submit widget="link">Custom text</mak:submit><br/>
  
  
</mak:newForm>

</body>
</html>