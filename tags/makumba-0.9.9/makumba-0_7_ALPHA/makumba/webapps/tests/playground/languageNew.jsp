<% /* $Id: /usr/local/cvsroot/karamba/public_html/welcome.jsp,v 2.39 2007/06/28 17:18:22 manu Exp $ */ %>
<jsp:include page="header.jsp" flush="false" />
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


<%-- Makumba Generator - START OF  *** NEW ***  PAGE FOR OBJECT test.Language --%>
<h1>New Language</h1>
<mak:newForm type="test.Language" action="languageList.jsp" name="language" >
  <table>
  <%-- Makumba Generator - START OF NORMAL FIELDS --%>
    <tr>
      <th><label for="name"><span class="accessKey">n</span>ame</label></th>
      <td><mak:input field="name" styleId="name" accessKey="n" /></td>
    </tr>
    <tr>
      <th><label for="isoCode"><span class="accessKey">i</span>soCode</label></th>
      <td><mak:input field="isoCode" styleId="isoCode" accessKey="i" /></td>
    </tr>
  <%-- Makumba Generator - END OF NORMAL FIELDS --%>
    <tr>
      <td>  <input type="submit" value="Add" accessKey="A">  <input type="reset" accessKey="R">  <input type="reset" value="Cancel" accessKey="C" onClick="javascript:back();">  </td>
    </tr>
  </table>
</mak:newForm>

<%-- Makumba Generator - END OF *** NEW ***  PAGE FOR OBJECT test.Language --%>
