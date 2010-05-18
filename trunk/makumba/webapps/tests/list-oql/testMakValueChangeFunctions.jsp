<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak"%>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<html>
<head>
<title>Language list, grouping using the mak:hasValueChanged() and mak:willValueChange() functions</title>
</head>
<body>

<h4>Languages grouped using JSTL</h4>

<table border="1">
  <mak:list from="test.Language l" orderBy="l.family" id="manual">
    <mak:value expr="l.family" printVar="currentFamiliy" />
    <c:if test="${previousFamily != currentFamiliy}">
      <c:if test="${mak:count()>1}">
        </td>
        </tr>
      </c:if>
      <tr>
        <th>${currentFamiliy}</th>
        <td>
    </c:if>
    name: ${mak:value('l.name')}, isoCode: ${mak:value('l.isoCode')}<br />
    <c:set var="previousFamily" value="${currentFamiliy}" />
    <c:if test="${mak:count()==mak:maxCount()}">
      </td>
      </tr>
    </c:if>
  </mak:list>
</table>

<h4>Languages grouped using nested list</h4>

<table border="1">
  <mak:list from="test.Language lGroup" orderBy="lGroup.family" groupBy="lGroup.family" id="groupBy">
    <tr>
      <th><mak:value expr="lGroup.family" /></th>
      <td>
        <mak:list from="test.Language l" where="l.family=lGroup.family">
          name: ${mak:value('l.name')}, isoCode: ${mak:value('l.isoCode')}<br />
        </mak:list>
      </td>
    </tr>
  </mak:list>
</table>

<h4>Languages grouped using mak:valueChange functions</h4>

<table border="1">
  <mak:list from="test.Language l" orderBy="l.family" id="valueChangeFunctions">
    <c:if test="${mak:hasValueChanged('l.family')}">
      <tr>
        <th><mak:value expr="l.family"/></th>
        <td>
    </c:if>
    name: ${mak:value('l.name')}, isoCode: ${mak:value('l.isoCode')} <br />
    <c:if test="${mak:willValueChange('l.family')}">
        </td>
      </tr>
    </c:if>
  </mak:list>
</table>


</body>
</html>
