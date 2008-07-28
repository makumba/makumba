<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test Mak Delete </title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:list from="test.Person p">
  name: <mak:value expr="p.indiv.name"/><br>
  !<mak:deleteLink object="p" >    <mak:action>testMakDelete.jsp?person=<mak:value expr='p'/></mak:action>    
  DeleteLink  </mak:deleteLink>!<br>
  !<mak:delete object="p" action="" widget="link" preserveWhitespace="true">
    <mak:action>testMakDelete.jsp?person=<mak:value expr='p'/></mak:action>
    DeleteLink2
  </mak:delete>!
  <mak:delete object="p" action="" widget="button" method="post"><mak:action>testMakDelete.jsp?person=<mak:value expr='p'/></mak:action>    Delete Button  </mak:delete>
  <mak:delete object="p" action="" widget="button" method="post" preserveWhitespace="true">
  <mak:action>testMakDelete.jsp?person=<mak:value expr='p'/></mak:action>    
  Delete Button 
   </mak:delete><br>
  <hr>
</mak:list>


</body>
