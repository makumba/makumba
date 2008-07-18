<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- Makumba Generator - START OF  *** EDIT ***  PAGE FOR OBJECT Car --%>
<mak:object from="Car car" where="car=$car">
  <fieldset style="text-align:right;">
  <legend>Edit Car <i><mak:value expr="car.name" /></i></legend>
  <mak:editForm object="car" action="carView.jsp" method="post">
    
    <%-- Makumba Generator - START OF NORMAL FIELDS --%>
    
    <label for="name">Na<span class="accessKey">m</span>e</label>
    <mak:input field="name" styleId="name" accessKey="m" />    <br/>
    
    <label for="price">Pr<span class="accessKey">i</span>ce</label>
    <mak:input field="price" styleId="price" accessKey="i" />    <br/>
    
        <input type="submit" value="Save changes" accessKey="S">    <input type="reset" accessKey="R">    <input type="reset" value="Cancel" accessKey="C" onClick="javascript:back();">    
    <br/>
    </fieldset>
  </mak:editForm>
  <%-- Makumba Generator - END OF NORMAL FIELDS --%>

  <%-- Makumba Generator - START OF SETS --%>

  <%-- Makumba Generator - END OF SETS --%>

</fieldset>
</mak:object>

<%-- Makumba Generator - END OF *** EDIT ***  PAGE FOR OBJECT Car --%>
