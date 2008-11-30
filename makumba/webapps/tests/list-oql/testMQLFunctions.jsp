<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>

<%@page import="test.MakumbaTestSetup"%><html>
<head><title>MQL Functions</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<% pageContext.setAttribute("someBirthdate", MakumbaTestSetup.birthdate.getTime()); %>

<b>MQL Functions</b>
<br><br><br>

<mak:list from="test.Person p" where="month(p.birthdate)=month($someBirthdate) AND dayOfMonth(p.birthdate)=dayOfMonth($someBirthdate)">
  name: <mak:value expr="p.indiv.name"/>, <mak:value expr="p.indiv.surname"/>, birthdate: <mak:value expr="p.birthdate"/><br>
</mak:list>

</body>
</html>
