<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak"%>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<html>
<head>
<title>Testing In SET with ptrs</title>
</head>
<body>

<% java.util.Vector<String> allPtrsVector = new java.util.Vector<String>(); pageContext.setAttribute("allPtrsVector", allPtrsVector); %>
<b>Data</b><br/>
<mak:list from="test.Language l">
  <mak:value expr="l" printVar="thisPtr" /> <mak:value expr="l.name"/> (${thisPtr}) <%allPtrsVector.add(thisPtr);%>
  <c:choose>
    <c:when test="${mak:count()==1}"> <c:set var="singlePtr" value="${thisPtr}" /> <c:set var="allPtrsString" value="'${thisPtr}'" /> </c:when>
    <c:otherwise> <c:set var="allPtrsString" value="${allPtrsString}, '${thisPtr}'" /> </c:otherwise>
  </c:choose>
</mak:list>
<br/>

<b>Variables</b><br />
singlePtr: ${singlePtr}<br />
allPtrsString: ${allPtrsString}<br /><br />
allPtrsVector: ${allPtrsVector}<br /><br />

<table border="1">
  <tr>
    <th>Left hand</th>
    <th>Right hand</th>
    <th>Result</th>
  </tr>

  <tr>
    <td>Ptr label</td>
    <td>Single ptr value, hard-coded ('5uzv2hd')</td>
    <td><mak:list from="test.Language l" where="l IN SET ('5uzv2hd')"> <mak:value expr="l.name" /> </mak:list></td>
  </tr>

  <tr>
    <td>Ptr label</td>
    <td>Multiple ptr value, hard-coded ('3vyr0id', '3vyr0ie', '5uzv2hj', '3vyr0i8', '5uzv2hd')</td>
    <td><mak:list from="test.Language l" where="l IN SET ('3vyr0id', '3vyr0ie', '5uzv2hj', '3vyr0i8', '5uzv2hd')"> <mak:value expr="l.name" /> </mak:list></td>
  </tr>

  <tr>
    <td>Ptr label</td>
    <td>Single ptr value, parameter $singlePtr (${singlePtr})</td>
    <td><mak:list from="test.Language l" where="l IN SET ($singlePtr)"> <mak:value expr="l.name" /> </mak:list></td>
  </tr>

  <tr>
    <td>Ptr label</td>
    <td>Multiple ptr value, vector, parameter $allPtrsVector (${allPtrsVector})</td>
    <td><mak:list from="test.Language l" where="l IN SET ($allPtrsVector)"> <mak:value expr="l.name" /> </mak:list></td>
  </tr>

  <tr>
    <td colspan="4">&nbsp;</td>
  </tr>

  <tr>
    <td>Single ptr value, hard-coded ('5uzv2hj')</td>
    <td>Ptr label</td>
    <td><mak:list from="test.Language l" where="'5uzv2hj' IN SET (l)"> <mak:value expr="l.name" /> </mak:list></td>
  </tr>

  <tr>
    <td>Single ptr value, parameter $singlePtr (${singlePtr})</td>
    <td>Ptr label</td>
    <td><mak:list from="test.Language l" where="$singlePtr IN SET (l)"> <mak:value expr="l.name" /> </mak:list></td>
  </tr>

</table>

</body>
</html>
