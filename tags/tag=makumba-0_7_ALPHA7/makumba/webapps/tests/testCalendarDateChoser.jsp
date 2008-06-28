<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@page import="org.makumba.MakumbaSystem"%>
<html>
<head>
  <title>Testing annotation</title>
  <link rel="StyleSheet" href="style/style.css" type="text/css" media="all"/>
</head>
<body>
  
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

response:<br>
<mak:response/><br><br>

<mak:newForm type="test.Person" action="testMultipleInvalidValuesList.jsp" method="post">  
  uniqDate: <mak:input name="uniqDate" calendarEditorLink="[cal]" /> <br>
  birthdate: <mak:input name="birthdate" calendarEditorLink="calendar" /> <br>
  firstsex: <mak:input name="firstSex" calendarEditorLink="<img src='style/calendar.gif' border='0'>" /> <br>
  <input type="submit" />
</mak:newForm>

</body>
</html>
