<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Person list</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<h1>Persons:</h1>
<mak:list from="Person p" editable="true">
  <mak:value expr="p.name"/>, <mak:value expr="p.age"/> <br/>
<mak:value expr="p.story" />
</mak:list>

</body>
</html>
