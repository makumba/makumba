<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@page import="java.util.Vector"%>
<%@page import="org.makumba.Database"%>
<%@page import="org.makumba.Transaction"%>
<%@page import="org.makumba.providers.TransactionProvider"%>
<%@page import="java.util.Dictionary"%>

<html>

<head><title>Test bug 910</title></head>
<body>

<% Vector ptrs = new Vector(); %>

<h2> mak:list that gets all persons, and builds the vector</h2>
<mak:list from="test.Person p">
  <mak:value expr="p.indiv.name"/><br/>
  <mak:value expr="p" var="personPtr"/> <% ptrs.add(personPtr); %>
</mak:list>

<% pageContext.setAttribute("ptrs", ptrs); %>

<h2> mak:list that gets all persons that are IN SET of the previously built vector </h2>
<mak:list from="test.Person p" where="p IN SET ($ptrs)">
  <mak:value expr="p.indiv.name"/><br/>
</mak:list>

<h2> Transaction.executeQuery that gets all persons that are IN SET of the previously built vector </h2>
<% 
  Transaction t = TransactionProvider.getInstance().getConnectionTo(TransactionProvider.getInstance().getDefaultDataSourceName());  
  Vector<Dictionary<String, Object>> v = t.executeQuery("SELECT p.indiv.name as name from test.Person p WHERE p IN SET ($1)", ptrs);
  for (Dictionary<String, Object> dic : v) {
     out.println(dic.get("name") + "<br/>");
  }
%>

<h2> Transaction.executeQuery that gets all persons that are IN SET of the previously built vector </h2>
<% 
  v = t.executeQuery("SELECT p.indiv.name as name from test.Person p WHERE p IN SET ($1, $2)", new Object[] {ptrs, ptrs});
  for (Dictionary<String, Object> dic : v) {
     out.println(dic.get("name") + "<br/>");
  }
%>

<% t.close(); %>

</body>
</html>