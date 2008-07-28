<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test Mak Delete </title></head>
<body>
<%@taglib uri="http://www.makumba.org/list" prefix="mak" %>
<%@taglib uri="http://www.makumba.org/forms" prefix="form" %>

<mak:list from="test.Person p">
  name: <mak:value expr="p.indiv.name"/><br>
  !<form:deleteLink object="p" >    <form:action>testMakDelete.jsp?person=<mak:value expr='p'/></form:action>    
  DeleteLink  </form:deleteLink>!<br>
  !<form:delete object="p" action="" widget="link" preserveWhitespace="true">
    <form:action>testMakDelete.jsp?person=<mak:value expr='p'/></form:action>
    DeleteLink2
  </form:delete>!
  <form:delete object="p" action="" widget="button" method="post"><form:action>testMakDelete.jsp?person=<mak:value expr='p'/></form:action>    Delete Button  </form:delete>
  <form:delete object="p" action="" widget="button" method="post" preserveWhitespace="true">
  <form:action>testMakDelete.jsp?person=<mak:value expr='p'/></form:action>    
  Delete Button 
   </form:delete><br>
  <hr>
</mak:list>


</body>
