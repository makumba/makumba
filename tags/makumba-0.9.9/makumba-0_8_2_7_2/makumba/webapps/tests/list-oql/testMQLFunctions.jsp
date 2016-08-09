<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>

<%@page import="test.MakumbaTestData"%><html>
<head><title>MQL Functions</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<% pageContext.setAttribute("someBirthdate", MakumbaTestData.birthdateJohn); %>
<% pageContext.setAttribute("day", MakumbaTestData.testDate); %>
<c:set var="startIndex" value="${2}" />
<c:set var="substringLength" value="${2}" />
<c:set var="separator" value=" " />
<c:set var="exclamationMark" value="!" />


<b>MQL Functions</b>
<br><br><br>

birthdateJohn: ${someBirthdate }<br/><br/>
testDate: ${day }<br/><br/>

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

<i>list from="test.Person p"</i><br/>
<mak:list from="test.Person p" id="2">
  &nbsp;<b>name: <mak:value expr="p.indiv.name"/>, <mak:value expr="p.indiv.surname"/>, birthdate: <mak:value expr="p.birthdate"/></b><br/>
  &nbsp;&nbsp;&nbsp;
  year($day)-year(p.birthdate) - case when (month(p.birthdate) > month($day) or (month(p.birthdate)=month($day) and day(p.birthdate)>day($day))) then 1 else 0 end<br/>
  &nbsp;&nbsp;&nbsp;
  month(p.birthdate) > month($day): <mak:value expr="month(p.birthdate) > month($day)"/>, month(p.birthdate)=month($day): <mak:value expr="month(p.birthdate)=month($day)"/>, dayOfMonth: <mak:value expr="dayOfMonth(p.birthdate)>dayOfMonth($day)"/><br/>
  &nbsp;&nbsp;&nbsp;
  age: <mak:value expr="year($day)-year(p.birthdate) - case when (month(p.birthdate) > month($day) or (month(p.birthdate)=month($day) and dayOfMonth(p.birthdate)>dayOfMonth($day))) then 1 else 0 end"/>
  <br/>
</mak:list>
<br/>

<i>list from="test.Person p"</i><br/>
<mak:list from="test.Person p" id="3">
  &nbsp;<b>name: <mak:value expr="p.indiv.name"/> <mak:value expr="p.indiv.surname"/></b><br>
  &nbsp;&nbsp;&nbsp;First 2 characters (substring(p.indiv.name, 1, 2): <mak:value expr="substring(p.indiv.name, 1, 2)"/>  <br/>
  &nbsp;&nbsp;&nbsp;${substringLength} characters starting from ${startIndex} (substring(p.indiv.name, $startIndex, $substringLength): <mak:value expr="substring(p.indiv.name, $startIndex, $substringLength)"/>  <br/>
  &nbsp;&nbsp;&nbsp;Concat (concat(p.indiv.name, $separator, p.indiv.surname, $exclamationMark): <mak:value expr="concat(p.indiv.name, $separator, p.indiv.surname, $exclamationMark)"/>  <br/>
  &nbsp;&nbsp;&nbsp;Concat with separator (concat_ws(p.indiv.name, p.indiv.surname): <mak:value expr="concat_ws($separator, p.indiv.name, p.indiv.surname, p.email)"/>  <br/>
</mak:list>
<br/>

</body>
</html>
