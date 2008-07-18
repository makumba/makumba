<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- Makumba Generator - START OF  *** NEW ***  PAGE FOR OBJECT Car --%>
<fieldset style="text-align:right;">
  <legend>New Car</legend>
<mak:newForm type="Car" action="carView.jsp" name="car" >
  
  <%-- Makumba Generator - START OF NORMAL FIELDS --%>
  
  <label for="name"><span class="accessKey">n</span>ame</label>
  <mak:input field="name" styleId="name" accessKey="n" />  <br/>
  
  <label for="price"><span class="accessKey">p</span>rice</label>
  <mak:input field="price" styleId="price" accessKey="p" />  <br/>
  <%-- Makumba Generator - END OF NORMAL FIELDS --%>
  
    <input type="submit" value="Add" accessKey="A">  <input type="reset" accessKey="R">  <input type="reset" value="Cancel" accessKey="C" onClick="javascript:back();">  
  <br/>
  </fieldset>
</mak:newForm>

<%-- Makumba Generator - END OF *** NEW ***  PAGE FOR OBJECT Car --%>
