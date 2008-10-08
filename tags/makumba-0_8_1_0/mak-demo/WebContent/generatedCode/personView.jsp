<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- Makumba Generator - START OF  *** OBJECT ***  PAGE FOR OBJECT Person --%>
<mak:object from="Person person" where="person=$person">
  <fieldset style="text-align:right;">
  <legend>Person <i><mak:value expr="person.name" /></i></legend>
  
  <%-- Makumba Generator - START OF NORMAL FIELDS --%>
  
  name
  <mak:value expr="person.name"/>  <br/>
  
  age
  <mak:value expr="person.age"/>  <br/>
  </fieldset>
  <%-- Makumba Generator - END OF NORMAL FIELDS --%>

  <%-- Makumba Generator - START OF SETS --%>

  <%-- Makumba Generator - END OF SETS --%>

</fieldset>
</mak:object>

<%-- Makumba Generator - END OF *** OBJECT ***  PAGE FOR OBJECT Person --%>
