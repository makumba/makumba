<% /* $Id: /usr/local/cvsroot/karamba/public_html/welcome.jsp,v 2.39 2007/06/28 17:18:22 manu Exp $ */ %>
<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head>
<title>Test Form Errors in Mak:response</title>
<link rel="StyleSheet" href="../style/style.css" type="text/css" media="all"/>
</head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<mak:response />

<mak:newForm type="test.Person" action="testFormErrorsInMakResponse.jsp" name="person" clientSideValidation="false" reloadFormOnError="false" method="post">  
  name <mak:input name="indiv.name" /><br>  
  surname <mak:input name="indiv.surname" /><br>  
  age <mak:input name="age" /><br>  
  weight <mak:input name="weight" /><br>  
  email <mak:input name="email" /><br>  
  hobbies <mak:input name="hobbies" /><br>  
  firstSex <mak:input name="firstSex" /><br>  
  birthdate <mak:input name="birthdate" /><br>  
  beginDate <mak:input name="beginDate" /><br> 
  uniqDate <mak:input name="uniqDate" /><br> 
  uniqInt <mak:input name="uniqInt" /><br> 
  uniqChar <mak:input name="uniqChar" /><br> 
  <input type="submit" />
</mak:newForm>

</body>
</html>
