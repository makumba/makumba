<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak"%>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>

<html>
<head>
<title>Testing comparisons with pointers in mak:list</title>
</head>
<body>

<b>Data</b><br/>
<mak:list from="test.Language l">
  <mak:value expr="l" printVar="thisPtr" /> <mak:value expr="l.name"/> (${thisPtr}) 
  <c:choose>
    <c:when test="${mak:count()==1}"> <c:set var="singlePtr" value="${thisPtr}" /> <c:set var="allPtrs" value="'${thisPtr}'" /> </c:when>
    <c:otherwise> <c:set var="allPtrs" value="${allPtrs}, '${thisPtr}'" /> </c:otherwise>
  </c:choose>
</mak:list>
<br/>

<b>Variables</b><br />
singlePtr: ${singlePtr}<br />
allPtrs: ${allPtrs}<br /><br />

<table border="1">
  <tr>
    <th>Left hand</th>
    <th>Right hand</th>
    <th>Result</th>
  </tr>

  <tr>
    <td>Ptr label</td>
    <td>Single ptr value, hard-coded ('3vyr0ie')</td>
    <td><mak:list from="test.Language l" where="l='3vyr0ie'">
      <mak:value expr="l.name" />
    </mak:list></td>
  </tr>

  <tr>
    <td>Ptr label</td>
    <td>Single ptr value, parameter $singlePtr (${singlePtr})</td>
    <td><mak:list from="test.Language l" where="l=$singlePtr">
      <mak:value expr="l.name" />
    </mak:list></td>
  </tr>

  <tr>
    <td>Single ptr value, hard-coded ('5uzv2hj')</td>
    <td>Ptr label</td>
    <td><mak:list from="test.Language l" where="'5uzv2hj'=l">
      <mak:value expr="l.name" />
    </mak:list></td>
  </tr>

  <tr>
    <td>Single ptr value, parameter $singlePtr (${singlePtr})</td>
    <td>Ptr label</td>
    <td><mak:list from="test.Language l" where="$singlePtr=l">
      <mak:value expr="l.name" />
    </mak:list></td>
  </tr>

</table>

</body>
</html>
