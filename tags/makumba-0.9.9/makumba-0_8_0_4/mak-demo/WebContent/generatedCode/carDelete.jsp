<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- Makumba Generator - START OF  *** DELETE ***  PAGE FOR OBJECT Car --%>
<fieldset style="text-align:right;">
  <legend>Delete confirmation</legend>
<mak:object from="Car car" where="car=$car">
  Delete car '<mak:value expr="car.name" />'?
  <a href="javascript:back();">No</a> &nbsp;
  <mak:delete object="car" action="carList.jsp">
    Delete
  </mak:delete>
</mak:object>

<%-- Makumba Generator - END OF *** DELETE ***  PAGE FOR OBJECT Car --%>
