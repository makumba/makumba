<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<html>
<head>
<title>Section</title>
<script src="http://localhost:8080/tests/mak-tools/makumbaResources/javaScript/prototype.js" type="text/javascript"></script>
<script src="http://localhost:8080/tests/mak-tools/makumbaResources/javaScript/makumba-sections.js" type="text/javascript" ></script>
</head>

<body>

<mak:section name="persons" reload="newPerson">
  <mak:list from="test.Person p">
  <mak:value expr="p.indiv.name"/> <mak:value expr="p.indiv.surname"/>
  </mak:list>
</mak:section>

</body>

</html>