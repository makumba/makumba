<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Language list</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<b>Number of Languages:</b>
<mak:list from="test.Language l">
  <mak:value expr="count(*)" />
</mak:list>

<br/> <br/>

<b>Languages per person:</b><br/>
<mak:list from="test.Person p">
  <mak:value expr="p.indiv.name" /> <mak:list from="p.speaks l" groupBy="p" > <mak:value expr="count(*)" /> </mak:list> <br/>
</mak:list>

<br/> <br/>

<b>Max uniqInt:</b><br/>
<mak:list from="test.Person p" id="max">
  <mak:value expr="max(p.uniqInt)" />
</mak:list>

<br/> <br/>

<b>Min uniqInt:</b><br/>
<mak:list from="test.Person p" id="min">
  <mak:value expr="min(p.uniqInt)" />
</mak:list>

<br/> <br/>

<b>Sum uniqInt:</b><br/>
<mak:list from="test.Person p" id="sum">
  <mak:value expr="sum(p.uniqInt)" />
</mak:list>

</body>
</html>