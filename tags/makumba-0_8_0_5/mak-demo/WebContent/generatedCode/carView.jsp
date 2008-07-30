<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- Makumba Generator - START OF  *** OBJECT ***  PAGE FOR OBJECT Car --%>
<mak:object from="Car car" where="car=$car">
  <fieldset style="text-align:right;">
  <legend>Car <i><mak:value expr="car.name" /></i></legend>
  
  <%-- Makumba Generator - START OF NORMAL FIELDS --%>
  
  name
  <mak:value expr="car.name"/>  <br/>
  
  price
  <mak:value expr="car.price"/>  <br/>
  </fieldset>
  <%-- Makumba Generator - END OF NORMAL FIELDS --%>

  <%-- Makumba Generator - START OF SETS --%>

  <%-- Makumba Generator - END OF SETS --%>

</fieldset>
</mak:object>

<%-- Makumba Generator - END OF *** OBJECT ***  PAGE FOR OBJECT Car --%>
