<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Concurrent list test</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<h1>Persons</h1>
<mak:list from="test.Person p">
  name:<mak:value expr="p.indiv.name"/><br>
  speaks: <mak:list from="p.speaks s"><mak:value expr="s.name"/> </mak:list>
  <br/><br/>
</mak:list>

<h1>Persons, take 2, more complex</h1>
<mak:list from="test.Person p" id="1">
  name:<mak:value expr="p.indiv.name"/><br>
  speaks:
  <mak:list from="p.speaks s" id="2">
    <mak:value expr="s.name"/> spoken by <mak:list from="test.Person person, person.speaks speaking" where="speaking.name = s.name" ><mak:value expr="person.indiv.name" /> </mak:list>
  </mak:list>
  <br/><br/>
</mak:list>


</body>
</html>
