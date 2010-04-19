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

<% pageContext.setAttribute("someDate", new java.util.GregorianCalendar(2010, 3, 19).getTime()); %>
someDate: ${someDate}<br/>
<mak:list from="test.Person p" limit="1" id="someDate">
  +1 year: <mak:value expr="dateAdd($someDate, 1, 'year')"/> <br/>
  -1 year: <mak:value expr="dateAdd($someDate, -1, 'year')"/> <br/>
  +2 year: <mak:value expr="dateSub($someDate, 2, 'year')"/> <br/>
  +14 days: <mak:value expr="dateSub($someDate, - 14, 'day')"/> <br/>
  -14 days: <mak:value expr="dateSub($someDate, 14, 'day')"/> <br/>
</mak:list>

</body>
</html>
