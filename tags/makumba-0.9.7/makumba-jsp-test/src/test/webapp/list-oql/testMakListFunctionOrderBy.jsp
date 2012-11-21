<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Persons and Brothers</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<b>Persons and brothers:</b>
<br/><br/>

<b>Ordered by p.indiv.name</b><br/>
<mak:list from="test.Person p" orderBy="p.indiv.name">
  <mak:value expr="p.fullName()"/>, brothers: <mak:value expr="p.numberOfBrothers()"/> <br/>
</mak:list>
<br/><br/>

<b>Ordered by p.numberOfBrothers() DESC</b><br/>
<mak:list from="test.Person p" orderBy="p.numberOfBrothers() DESC">
  <mak:value expr="p.fullName()"/>, brothers: <mak:value expr="p.numberOfBrothers()"/> <br/>
</mak:list>

</body>
</html>