<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Nested forms</title></head>
<body>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

response:<mak:response/>!
<mak:newForm type="test.Person" name="firstBrother" action="beginMakNestedNewFormsSimple.jsp" method="post" clientSideValidation="false">
  1st Name: <mak:input name="indiv.name"/><br/>
  1st Surname: <mak:input name="indiv.surname"/><br/>
  <c:set var="weight" value="${23.}" />
  <mak:input name="weight" type="hidden" value="$weight" /><br/>

  <mak:newForm type="test.Person" name="secondBrother">
    2nd Name: <mak:input name="indiv.name"/><br/>
    2nd Surname: <mak:input name="indiv.surname"/><br/>
    <mak:input name="brother" value="firstBrother" type="hidden"/>
  </mak:newForm>
  <input type="submit">
</mak:newForm>

</body>
</html>