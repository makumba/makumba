<%@page import="java.util.Enumeration"%>
<%
String event = request.getParameter("_mak_event_");
if(event == null) { %>

<%@page import="java.util.Iterator"%><html>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<head>

<title>Section</title>
<script src="http://localhost:8080/tests/mak-tools/makumbaResources/javaScript/prototype.js" type="text/javascript"></script>
<script src="http://localhost:8080/tests/mak-tools/makumbaResources/javaScript/makumba-sections.js" type="text/javascript" ></script>
</head>
<body>

<script type="text/javascript">
var _mak_event_to_id_ = {myEvent1: ['div1','div3'], myEvent2: ['div2'], myEvent3: ['div1','div4'] };
var _mak_idevent_to_type_ = {div1___myEvent1: 'reload', div1___myEvent3: 'reload', div2___myEvent2: 'show', div4___myEvent3: 'hide' };
var _mak_page_url_ = '<%=request.getContextPath() + "/" + request.getServletPath() %>';
<%
String query = "";
for(Iterator<String> i = request.getParameterMap().keySet().iterator(); i.hasNext();) {
    String el = i.next();
    String[] vals = (String[]) request.getParameterMap().get(el);
    for(int k = 0; k < vals.length; k++) {
        query += el + ": '" + vals[k] + "'";
        if(k < vals.length - 1) {
            query += ",";
        }
    }
    if(i.hasNext()) {
        query += ",";
    } else {
        query = "{" + query + "}";
    }
}
if(query.length() == 0) query = "new Hash()";
%>
var _mak_page_params_ = <%=query%>;
</script>

<button onClick="makEvent('myEvent1')">event1</button>
<button onClick="makEvent('myEvent3')">event3</button>
<br/><br/>
Div1: <div id="div1">div 1 content</div><br/>
Div2: <div id="div2">div 2 content</div><br/>
Div3: <div id="div3">div 3 content</div><br/>
Div4: <div id="div4">div 4 content</div><br/>
<%=query%>
</body>
</html>

<% } else {Thread.sleep(1000); response.setContentType("application/json");%>{div1: '<strong>new content div 1</strong>', div3: '<strong>new content div 3</strong>'}<% } %>