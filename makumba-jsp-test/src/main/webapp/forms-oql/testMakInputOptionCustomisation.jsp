<% /* $Id: /usr/local/cvsroot/karamba/public_html/welcome.jsp,v 2.39 2007/06/28 17:18:22 manu Exp $ */ %>
<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head>
<title>Test customisation of mak:input options</title>
<link rel="StyleSheet" href="../style/style.css" type="text/css" media="all"/>
</head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<mak:response />

<br/>
<b>plain uncustomised</b>
<mak:newForm type="test.Person" action="" name="person" clientSideValidation="false">  
  name <mak:input name="indiv.name" /><br>  
  surname <mak:input name="indiv.surname" /><br>
  brother <mak:input name="brother"/><br/>
  <input type="submit" />
</mak:newForm>

<br/> <br/>
<b>orderBy="o.fullName() DESC"</b>
<mak:newForm type="test.Person" action="" name="person" clientSideValidation="false">  
  name <mak:input name="indiv.name" /><br>  
  surname <mak:input name="indiv.surname" /><br>
  brother <mak:input name="brother" orderBy="o.fullName() DESC" /><br/>
  <input type="submit" />
</mak:newForm>

<br/> <br/>
<b>labelName='opt' where="opt.fullName() like '%b%'" </b>
<mak:newForm type="test.Person" action="" name="person" clientSideValidation="false">  
  name <mak:input name="indiv.name" /><br>  
  surname <mak:input name="indiv.surname" /><br>
  brother <mak:input name="brother" labelName="opt" where="opt.fullName() like '%b%'"/><br/>
  <input type="submit" />
</mak:newForm>

<br/> <br/>
<b>where="o.fullName() like '%j%'"</b>
<mak:newForm type="test.Person" action="" name="person" clientSideValidation="false">  
  name <mak:input name="indiv.name" /><br>  
  surname <mak:input name="indiv.surname" /><br>
  brother <mak:input name="brother" where="o.fullName() like '%j%'"/><br/>
  <input type="submit" />
</mak:newForm>
 
</body>
</html>
