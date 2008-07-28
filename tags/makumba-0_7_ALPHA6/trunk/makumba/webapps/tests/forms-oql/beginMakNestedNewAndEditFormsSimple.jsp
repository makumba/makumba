<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Nested forms</title></head>
<body>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

response:<mak:response/>!
<mak:newForm type="test.Person" name="stepBrother" action="testMakNestedNewAndEditFormsSimple.jsp" method="post" clientSideValidation="false">
  Step brother name: <mak:input name="indiv.name"/><br/>
  Step brother surname: <mak:input name="indiv.surname"/><br/>
  <c:set var="weight" value="${43.5}" />
  <mak:input name="weight" type="hidden" value="$weight" /><br/>

  <mak:object from="test.Person p, p.indiv indiv" where="indiv.name='firstBrother'">
    <mak:editForm object="p">
      <mak:input name="brother" value="stepBrother" type="hidden"/>
    </mak:editForm>
  </mak:object>
  <input type="submit">
</mak:newForm>

</body>
</html>