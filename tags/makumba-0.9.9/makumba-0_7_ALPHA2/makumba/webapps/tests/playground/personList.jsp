<% /* $Id: /usr/local/cvsroot/karamba/public_html/welcome.jsp,v 2.39 2007/06/28 17:18:22 manu Exp $ */ %>
<jsp:include page="header.jsp" flush="false" />
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<style type="text/css">
td {
  font-size: x-small;
}
th {
  font-size: small;
}
</style>

<h1>List Persons</h1>

<table>
  <%@include file="personListHeaderInclude.jsp" %>
  <mak:list from="test.Person o">
    <%@include file="personListDisplayInclude.jsp" %>
  </mak:list>
</table>
</body>
</html>
