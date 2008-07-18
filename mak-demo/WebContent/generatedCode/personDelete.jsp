BBBBBBBBBBBBBAAAAAAAAAAAAAAAAAAAAAAA
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

vfvd

cdsc
<%-- Makumba Generator - START OF  *** DELETE ***  PAGE FOR OBJECT Person --%>
<fieldset style="text-align:right;">
  <legend>Delete confirmation</legend>
<mak:object from="Person person" where="person=$person">
  Delete person '<mak:value expr="person.name" />'?
  <a href="javascript:back();">No</a> &nbsp;
  <mak:delete object="person" action="personList.jsp">
    Delete
  </mak:delete>
</mak:object>

<%-- Makumba Generator - END OF *** DELETE ***  PAGE FOR OBJECT Person --%>
