<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>

<%@page import="test.MakumbaTestData"%><html>
<head><title>Makumba MQL Functions</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


<b>makumba MQL Functions</b>
<br><br><br>

<i>list from="test.Person p" where="p.birthdate=$someBirthdate"</i><br/>
<mak:list from="test.Person p">
  name: <mak:value expr="p.fullName()"/>, birthdate: <mak:value expr="p.birthdate"/>, birthdate in next 12 months? <mak:value expr="p.birthdayWithin12Months()"/>, birthdate before '2010-05-01': <mak:value expr="p.birthdayBefore('2010-05-01')"/><br>
</mak:list>
<br/>
 
</body>
</html>
