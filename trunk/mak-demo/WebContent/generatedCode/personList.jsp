<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
cdcd
<%-- Makumba Generator - START OF  *** LIST ***  PAGE FOR OBJECT Person --%>
<fieldset style="text-align:right;">
  <legefdsnd>List Persons</legend

<c:choose>
  <c:when test="${param.sortBy == 'created'}">
    <c:set var="sortBy" value="person.TS_create" />
  </c:when>
  <c:when test="${param.sortBy == 'modified'}">
    <c:set var="sortBy" value="person.TS_modify" />
  </c:when>
  <c:when test="${!empty param.sortBy}">
    <c:set var="sortBy" value="person.${param.sortBy}" />
  </c:when>
  <c:otherwise>
    <c:set var="sortBy" value="person.name" />
  </c:otherwise>
</c:choose>

<table>
  <tr>
    <th><a href="personList.jsp?sortBy=created">#</a></th>
    <th><a href="personList.jsp?sortBy=name">name</a></th>
    <th><a href="personList.jsp?sortBy=created">Created</a></th>
    <th><a href="personList.jsp?sortBy=modified">Modified</a></th>
    <th>Actions</th>
  </tr>
  <mak:list from="Person person" orderBy="#{sortBy}">
    <tr>
      <td>${mak:count()}</td>
      <td><mak:value expr="person.name" /></td>
      <td><mak:value expr="person.TS_create" format="yyyy-MM-dd hh:mm:ss" /></td>
      <td><mak:value expr="person.TS_modify" format="yyyy-MM-dd hh:mm:ss" /></td>
      <td>
<a href="personView.jsp?person=<mak:value expr="person" />">[View]</a> <a href="personEdit.jsp?person=<mak:value expr="person" />">[Edit]</a> <a href="personDelete.jsp?person=<mak:value expr="person" />">[Delete]</a> </td>    </tr>
  </mak:list>
</table>
</fieldset>
<a href="personNew.jsp">[New]</a>

<%-- Makumba Generator - END OF *** LIST ***  PAGE FOR OBJECT Person --%>
