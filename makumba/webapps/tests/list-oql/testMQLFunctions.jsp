<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>

<%@page import="test.MakumbaTestSetup"%><html>
<head><title>MQL Functions</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<% pageContext.setAttribute("someBirthdate", MakumbaTestSetup.birthdate); %>

<b>MQL Functions</b>
<br><br><br>

someBirthdate: ${someBirthdate }<br/><br/>

<i>list from="test.Person p" where="p.birthdate=$someBirthdate"</i><br/>
<mak:list from="test.Person p" where="p.birthdate=$someBirthdate">
  name: <mak:value expr="p.indiv.name"/>, <mak:value expr="p.indiv.surname"/>, birthdate: <mak:value expr="p.birthdate"/><br>
</mak:list>
<br/>
 
<i>list from="test.Person p" where="p.birthdate=$someBirthdate OR (month(p.birthdate)=month($someBirthdate) AND dayOfMonth(p.birthdate)=dayOfMonth($someBirthdate))</i><br/>
<mak:list from="test.Person p" where="p.birthdate=$someBirthdate OR (month(p.birthdate)=month($someBirthdate) AND dayOfMonth(p.birthdate)=dayOfMonth($someBirthdate))">
  name: <mak:value expr="p.indiv.name"/>, <mak:value expr="p.indiv.surname"/>, birthdate: <mak:value expr="p.birthdate"/><br>
</mak:list>
<br/>

<i>list from="test.Person p" where="month(p.birthdate)=1</i><br/>
<mak:list from="test.Person p" where="month(p.birthdate)=1">
  name: <mak:value expr="p.indiv.name"/>, <mak:value expr="p.indiv.surname"/>, birthdate: <mak:value expr="p.birthdate"/><br>
</mak:list>
<br/>

<i>list from="test.Person p"</i><br/>
<mak:list from="test.Person p" >
  &nbsp;<b>name: <mak:value expr="p.indiv.name"/>, <mak:value expr="p.indiv.surname"/>, birthdate: <mak:value expr="p.birthdate"/></b><br/>
  &nbsp;&nbsp;&nbsp;
  year: <mak:value expr="year(p.birthdate)"/>, month: <mak:value expr="month(p.birthdate)"/>, dayOfMonth: <mak:value expr="dayOfMonth(p.birthdate)"/><br/>
  &nbsp;&nbsp;&nbsp;
  year($someBirthdate) <mak:value expr="year($someBirthdate)"/><br/>
  &nbsp;&nbsp;&nbsp;
  year(p.birthdate)=year($someBirthdate): <mak:value expr="year(p.birthdate)=year($someBirthdate)"/><br/>
  &nbsp;&nbsp;&nbsp;
  month(p.birthdate)=month($someBirthdate): <mak:value expr="month(p.birthdate)=month($someBirthdate)"/><br/>
  &nbsp;&nbsp;&nbsp;
  dayOfMonth(p.birthdate)=dayOfMonth($someBirthdate): <mak:value expr="dayOfMonth(p.birthdate)=dayOfMonth($someBirthdate)"/>
  <br>
</mak:list>
<br/>

</body>
</html>
