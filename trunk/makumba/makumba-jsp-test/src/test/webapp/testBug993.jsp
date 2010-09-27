<% /* $Id: /usr/local/cvsroot/karamba/public_html/welcome.jsp,v 2.39 2007/06/28 17:18:22 manu Exp $ */ %>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@page import="java.util.Vector"%>
<%@page import="java.util.Arrays"%>

<c:set var="searchArchiveVariableFrom" value="o.speaks speaks" />
<c:set var="searchArchiveWhere" value="speaks IN SET ($speaks)" />
<% pageContext.setAttribute("speaks", new Vector(Arrays.asList(new String[] {"3vyr0id", "5uzv2hj"})) ); %>

<mak:list from="test.Person o" variableFrom="#{searchArchiveVariableFrom}" where="#{searchArchiveWhere}">
  <mak:value expr="o.indiv.name" />: unique: <mak:value expr="o.uniqPtr.name" />, speaks: <mak:value expr="o.speaks" /><br/>
</mak:list>
