<% /* $Id: /usr/local/cvsroot/karamba/public_html/welcome.jsp,v 2.39 2007/06/28 17:18:22 manu Exp $ */ %>
<jsp:include page="header.jsp" flush="false" />
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


<%-- Makumba Generator - START OF  *** LIST ***  PAGE FOR OBJECT test.Language --%>
<h1>List Languages</h1>

<table>
  <tr>
    <th><a href="languageList.jsp?sortBy=created">#</a></th>
    <th><a href="languageList.jsp?sortBy=name">name</a></th>
    <th><a href="languageList.jsp?sortBy=created">Created</a></th>
    <th><a href="languageList.jsp?sortBy=modified">Modified</a></th>
    <th>Actions</th>
  </tr>
  <mak:list from="test.Language language" orderBy="language.name">
    <tr>
      <td>${mak:count()}</td>
      <td><mak:value expr="language.name" /></td>
      <td><mak:value expr="language.TS_create" format="yyyy-MM-dd hh:mm:ss" /></td>
      <td><mak:value expr="language.TS_modify" format="yyyy-MM-dd hh:mm:ss" /></td>
      <td><a href="languageView.jsp?language=<mak:value expr="language" />">[View]</a> <a href="languageEdit.jsp?language=<mak:value expr="language" />">[Edit]</a> <a href="languageDelete.jsp?language=<mak:value expr="language" />">[Delete]</a> </td>    
    </tr>
  </mak:list>
</table>
<a href="languageNew.jsp">[New]</a>

<%-- Makumba Generator - END OF *** LIST ***  PAGE FOR OBJECT test.Language --%>
