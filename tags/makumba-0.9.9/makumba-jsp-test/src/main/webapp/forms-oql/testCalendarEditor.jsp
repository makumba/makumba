<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head>
<title>Test calendar editors</title>
<link rel="StyleSheet" href="../style/style.css" type="text/css" media="all"/>
</head>
<body>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:list from="test.Person p">
  <mak:editForm object="p" action="testCalendarEditor.jsp">
    name: <mak:input name="indiv.name" />
    surname: <mak:input name="indiv.surname" maxlength="5" />
    birthdate: <mak:input name="birthdate" format="yyyy-MM-dd" calendarEditor="true" />
    someDate: <mak:input name="indiv.someDate" format="yyyy-MM-dd" calendarEditor="true" /><br>
  </mak:editForm>
</mak:list>
<mak:form action="">
  beginDate: <mak:input name="beginDate" dataType="date" format="yyyy-MMMM" calendarEditor="true" /><br>
  <% pageContext.setAttribute("now", new java.util.Date(108, 6, 31)); %>
  <mak:input name="hiddenDate" dataType="date" type="hidden" value="$now" calendarEditor="true" />
</mak:form>
</body>
</html>