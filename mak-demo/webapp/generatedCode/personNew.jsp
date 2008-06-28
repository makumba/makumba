<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%-- Makumba Generator - START OF  *** NEW ***  PAGE FOR OBJECT Person --%>
<fieldset style="text-align:right;">
  <legend>New Person</legend>
<mak:newForm type="Person" action="personView.jsp" name="person" >
  vds
  <%-- Makumba Generator - START OF NORMAL FIELDS --%>
  
  <label for="name"><span class="accessKey">N</span>ame</label>
  <mak:input field="name" styleId="name" accessKey="n" />  <br/>
  
  <label for="age">A<span class="accessKey">g</span>e</label>
  <mak:input field="age" styleId="age" accessKey="g" />  <br/>
  <%-- Makumba Generator - END OF NORMAL FIELDS --%>
  
    <input type="submit" value="Add" accessKey="A">  <input type="reset" accessKey="R">  <input type="reset" value="Cancel" accessKey="C" onClick="javascript:back();">  
  <br/>
  </fieldset>
</mak:newForm>

<%-- Makumba Generator - END OF *** NEW ***  PAGE FOR OBJECT Person --%>

cd
