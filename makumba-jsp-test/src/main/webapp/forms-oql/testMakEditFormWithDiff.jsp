<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head>
<title>Edit Form with value change diff (see bug 909, http://bugs.makumba.org/show_bug.cgi?id=909)</title>
</head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak"%>

<mak:object from="test.Person p, p.indiv i" where="i.name='john'">
  <mak:editForm object="p" action="testMakEditFormWithDiff.jsp" method="post" clientSideValidation="false"
    recordChangesIn="personChanges">
    <mak:input name="indiv.surname" /> <br/>
    <mak:input name="gender" type="tickbox" /> 
    <mak:input name="birthdate" format="yyyy-MM-dd" calendarEditor="false" />
    <mak:input name="weight" /> <br/>
    <mak:input name="intSet" />
    <mak:input name="charSet" /> <br/>
    <input type="submit" />
  </mak:editForm>
</mak:object>

${personChanges}

</body>
</html>