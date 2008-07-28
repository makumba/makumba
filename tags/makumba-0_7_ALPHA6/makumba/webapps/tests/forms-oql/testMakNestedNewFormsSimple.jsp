<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Nested forms</title></head>
<body>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:object from="test.Person p, p.indiv indiv" where="indiv.name='firstBrother'">
  name: <mak:value expr="indiv.name"/><br/>
  surname: <mak:value expr="indiv.surname"/><br/>
</mak:object>  
<mak:object from="test.Person p, p.indiv indiv" where="indiv.name='secondBrother'">
  name: <mak:value expr="indiv.name"/><br/>
  surname: <mak:value expr="indiv.surname"/><br/>
  brother: <mak:value expr="p.brother.indiv.name"/> <mak:value expr="p.brother.indiv.surname"/><br/>
</mak:object>
</body>
</html>