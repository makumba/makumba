<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@page import="org.makumba.MakumbaSystem"%>
<html>
<head>
  <title>Testing annotation</title>
  <link rel="StyleSheet" href="style/style.css" type="text/css" media="all"/>
  <% String[] neededJS = MakumbaSystem.getCalendarProvider().getNeededJavaScriptFileNames(); 
     for (int i=0; i<neededJS.length; i++) { %>
        <script type="text/javascript" src="scripts/<%= neededJS[i] %>"></script><%
     }
  %>
</head>
<body>
  
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

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
