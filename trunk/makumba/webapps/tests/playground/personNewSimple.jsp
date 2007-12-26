<% /* $Id: /usr/local/cvsroot/karamba/public_html/welcome.jsp,v 2.39 2007/06/28 17:18:22 manu Exp $ */ %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<html>
<head>
  <title>Playground</title>
  <link rel="StyleSheet" href="../style/style.css" type="text/css" media="all"/>
</head>

<body>
Person: <a href="personNew.jsp">New</a> | <a href="personList.jsp">List</a> ||| 
Languages:  <a href="languageNew.jsp">New</a> | <a href="languageList.jsp">List</a>
<br><br>

response:<mak:response/><br><br><br>

<mak:newForm type="test.Person" action="personList.jsp" clientSideValidation="live" annotation="after" reloadFormOnError="true" method="post">  
<table>
  <tr><th>indiv.name</th><td><mak:input name="indiv.name" /></td></tr>  
  <tr><th>indiv.surname</th><td><mak:input name="indiv.surname" /></td></tr>  
  <tr><th>age</th><td><mak:input name="age" /></td></tr>  
  <tr><th>weight</th><td><mak:input name="weight" /></td></tr>  
  <tr><th>email</th><td><mak:input name="email" /></td></tr>  
  <tr><th>hobbies</th><td><mak:input name="hobbies" /></td></tr>  
  <tr><th>firstSex</th><td><mak:input name="firstSex" /></td></tr>  
  <tr><th>birthdate</th><td><mak:input name="birthdate" /></td></tr>  
  <tr><th>beginDate</th><td><mak:input name="beginDate" /></td></tr> 
  <tr>
    <td colspan="2" align="center"><input type="submit" /> </td></tr>
</table>  
</mak:newForm>

</body>
</html>
