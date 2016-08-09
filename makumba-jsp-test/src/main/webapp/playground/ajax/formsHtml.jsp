<%@page import="java.util.Enumeration"%>
<%
String event = request.getParameter("some");
if(event == null) { %>

<%@page import="java.util.Iterator"%><html>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<head>

<title>Forms</title>
<script src="http://localhost:8080/tests/mak-tools/makumbaResources/javaScript/prototype.js" type="text/javascript"></script>
<script src="http://localhost:8080/tests/mak-tools/makumbaResources/javaScript/makumba-sections.js" type="text/javascript" ></script>
</head>
<body>

<script type="text/javascript">
var _mak_page_url_ = '<%=request.getContextPath() + "/" + request.getServletPath() %>';
</script>

<div id="target" >some</div>

<h1>New person</h1>
<form id="newForm_form1" method="GET" name="newForm" >
  <input name="indiv.name" type="text" value="" maxlength="40" id="indiv.namenewForm_form1" ><br/>
  <input name="indiv.surname" type="text" value="" maxlength="40" id="indiv.surnamenewForm_form1" ><br/>
  <input name="some" type="hidden" value="someval"/>
  <a href="#" onClick="$('newForm_form1').request({onComplete: function(t) { $('target').update(t.responseText)} });">Lala</a>
</form>

</body>
</html>

<% } else {Thread.sleep(1000); response.setContentType("application/json");%>{div1: '<strong>new content div 1</strong>', div3: '<strong>new content div 3</strong>'}<% } %>