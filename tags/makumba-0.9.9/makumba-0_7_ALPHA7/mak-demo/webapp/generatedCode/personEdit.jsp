<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- Makumba Generator - START OF  *** EDIT ***  PAGE FOR OBJECT Person --%>
<mak:object from="Person person" where="person=$person">
  <fieldset style="text-align:right;">
  <legend>Edit Person <i><mak:value expr="person.name" /></i></legend>
  <mak:editvfsForm object="person" action="personView.jsp" method="post">
    
    <%-- Makumba Generator - START OF NORMAL FIELDS --%>
    
    <label for="name">Na<span class="accessKey">m</span>e</label>
    <mak:input field="name" styleId="name" accessKey="m" />    <br/>
    
    <label for="age">Ag<span class="accessKey">e</span></label>
    <mak:input field="age" styleId="age" accessKey="e" />    <br/>
    
        <input type="submit" value="Save changes" accessKey="S">    <input type="reset" accessKey="R">    <input type="reset" value="Cancel" accessKey="C" onClick="javascript:back();">    
    <br/>
    </fieldset>
  </mak:editForm>fdsfdsf
  <%-- Makumba Generator - END OF NORMAL FIELDS --%>

  <%-- Makumba Generator - START OF SETS --%>

  <%-- Makumba Generator - END OF SETS --%>

</fieldset>
</mak:object>

<%-- Makumba Generator - END OF *** EDIT ***  PAGE FOR OBJECT Person --%>
