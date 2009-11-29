<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<fmt:setLocale value="${prefs.Language}" />
<fmt:setBundle basename="templates.default"/>
<%
  WikiContext c = WikiContext.findContext(pageContext);
  String frontpage = c.getEngine().getFrontPage(); 
%>

<div id="site">
  <div id="header">
    <div id="persistent">
      <wiki:Include page="UserBox.jsp" />
      <wiki:Include page="SearchBox.jsp" />
    </div>
    <div id="top_nav">
      <a href="#" class="menu_item item1"><span>         quick start    </span></a>
      <a href="#" class="menu_item item2 active"><span>  configuration  </span></a>
      <a href="#" class="menu_item item3"><span>         usage          </span></a>
      <a href="#" class="menu_item item4"><span>         documentation  </span></a>
      <a href="#" class="menu_item item5"><span>         showcase       </span></a>
      <a href="#" class="menu_item item6"><span>         download       </span></a>
    </div>
  </div>
  
  <div id="sidemenu">
     <div class="title">configuration</div>
     <a href="#"><span class="menu_item active">overview</span></a>
     <a href="#"><span class="menu_item">web-app configuration</span></a>
     <a href="#"><span class="menu_item">makumba configuration</span></a>
  </div>
