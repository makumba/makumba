<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@page import="java.util.Vector"%>
<%@page import="org.makumba.Database"%>
<%@page import="org.makumba.Transaction"%>
<%@page import="org.makumba.providers.TransactionProvider"%>
<%@page import="java.util.Dictionary"%>
<%@page import="org.apache.commons.lang.ArrayUtils"%>
<%@page import="java.util.Map"%><html>

<head><title>Test bug 910</title></head>
<body>

<% Vector ptrs = new Vector(); %>

<h4> mak:list that gets all persons, and builds the vector</h4>
<mak:list from="test.Person p">
  <mak:value expr="p.indiv.name"/><br/>
  <mak:value expr="p" var="personPtr"/> <% ptrs.add(personPtr); %>
</mak:list>

<% 
  pageContext.setAttribute("ptrs", ptrs); 
  Map map = ArrayUtils.toMap(new Object[][] { {"ptrs", ptrs} } );  
%>

<h4> mak:list that gets all persons that are IN SET of the previously built vector </h4>
<mak:list from="test.Person p" where="p IN SET ($ptrs)">
  <mak:value expr="p.indiv.name"/><br/>
</mak:list>

<h4> Transaction.executeQuery that gets all persons that are IN SET of the previously built vector, accessing the vector as $1 </h4>
<% 
  Transaction t = TransactionProvider.getInstance().getConnectionTo(TransactionProvider.getInstance().getDefaultDataSourceName());  
  Vector<Dictionary<String, Object>> v = t.executeQuery("SELECT p.indiv.name as name from test.Person p WHERE p IN SET ($1)", ptrs);
  for (Dictionary<String, Object> dic : v) {
     out.println(dic.get("name") + "<br/>");
  }
%>

<h4> Transaction.executeQuery that gets all persons that are IN SET of the previously built vector, using $1 and $2, passing the whole vector </h4>
<% 
  v = t.executeQuery("SELECT p.indiv.name as name from test.Person p WHERE p IN SET ($1, $2)", new Object[] {ptrs, ptrs});
  for (Dictionary<String, Object> dic : v) {
     out.println(dic.get("name") + "<br/>");
  }
%>

<h4> Transaction.executeQuery that gets all persons that are IN SET of the previously built vector, using $1 and $2, passing just the respective vector elements </h4>
<% 
  v = t.executeQuery("SELECT p.indiv.name as name from test.Person p WHERE p IN SET ($1, $2)", new Object[] {ptrs.elementAt(0), ptrs.elementAt(1)});
  for (Dictionary<String, Object> dic : v) {
     out.println(dic.get("name") + "<br/>");
  }
%>

<h4> Transaction.executeQuery that gets all persons that are IN SET of the previously built vector, using a map </h4>
<% 
  v = t.executeQuery("SELECT p.indiv.name as name from test.Person p WHERE p IN SET ($ptrs)", map);
  for (Dictionary<String, Object> dic : v) {
     out.println(dic.get("name") + "<br/>");
  }
%>

<% t.close(); %>

</body>
</html>