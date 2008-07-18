<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- Makumba Generator - START OF  *** LIST ***  PAGE FOR OBJECT Car --%>
<fieldset style="text-align:right;">
  <legend>List Cars</legend>

<c:choose>
  <c:when test="${param.sortBy == 'created'}">
    <c:set var="sortBy" value="car.TS_create" />
  </c:when>
  <c:when test="${param.sortBy == 'modified'}">
    <c:set var="sortBy" value="car.TS_modify" />
  </c:when>
  <c:when test="${!empty param.sortBy}">
    <c:set var="sortBy" value="car.${param.sortBy}" />
  </c:when>
  <c:otherwise>
    <c:set var="sortBy" value="car.name" />
  </c:otherwise>
</c:choose>



<a href="carList.jsp?sortBy=created">#</a>
<a href="carList.jsp?sortBy=name">name</a>
<a href="carList.jsp?sortBy=created">Created</a>
<a href="carList.jsp?sortBy=modified">Modified</a>
Actions
<br/>
  <mak:list from="Car car" orderBy="#{sortBy}">
  
  ${mak:count()}
  <mak:value expr="car.name" />
  <mak:value expr="car.TS_create" format="yyyy-MM-dd hh:mm:ss" />
  <mak:value expr="car.TS_modify" format="yyyy-MM-dd hh:mm:ss" />
  
<a href="carView.jsp?car=<mak:value expr="car" />">[View]</a> <a href="carEdit.jsp?car=<mak:value expr="car" />">[Edit]</a> <a href="carDelete.jsp?car=<mak:value expr="car" />">[Delete]</a>   <br/>
  </mak:list>
</fieldset>
<a href="carNew.jsp">[New]</a>

<%-- Makumba Generator - END OF *** LIST ***  PAGE FOR OBJECT Car --%>
