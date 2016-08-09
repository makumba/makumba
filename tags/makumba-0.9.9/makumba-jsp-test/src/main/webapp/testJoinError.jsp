<% /* $Id: /usr/local/cvsroot/karamba/public_html/welcome.jsp,v 2.39 2007/06/28 17:18:22 manu Exp $ */ %>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<%--
<mak:list from="test.Person p, p.speaks s, p.address a, p.toys t">
  <mak:list from="s">
    <mak:value expr="s.name"/>
  </mak:list>
  <mak:list from="a">
    <mak:value expr="a.city"/>
  </mak:list>
    <mak:list from="t">
    <mak:value expr="t.name"/>
  </mak:list>
</mak:list>
--%>

<mak:list from="test.Person p, p.indiv i, i.person p1">
  <mak:list from="p1.speaks s">
    <mak:value expr="s.name"/>
  </mak:list>
  <mak:list from="p1.address a">
    <mak:value expr="a.city"/>
  </mak:list>
    <mak:list from="p1.toys t">
    <mak:value expr="t.name"/>
  </mak:list>
</mak:list>