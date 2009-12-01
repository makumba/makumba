<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<fmt:setLocale value="${prefs.Language}" />
<fmt:setBundle basename="templates.default"/>
<%
  WikiContext c = WikiContext.findContext(pageContext);
  String frontpage = c.getEngine().getFrontPage(); 
%>
<c:set var="currentCategory"><wiki:Plugin plugin="InsertCategoryMenu" args="showCurrentCategory='true'"/></c:set>

<div id="site">
  <div id="header">
    <div id="persistent">
      <wiki:Include page="UserBox.jsp" />
      <wiki:Include page="SearchBox.jsp" />
    </div>
    <div id="top_nav">
    
    
      <a href="<wiki:LinkTo format="url" page="QuickSart"/>"     class="menu_item item1<c:if test="${currentCategory =='CategoryQuickStart'}"> active</c:if>"><span>         quick start    </span></a>
      <a href="<wiki:LinkTo format="url" page="Configuration"/>" class="menu_item item2<c:if test="${currentCategory =='CategoryConfiguration'}"> active</c:if>"><span>  configuration  </span></a>
      <a href="<wiki:LinkTo format="url" page="Usage"/>"         class="menu_item item3<c:if test="${currentCategory =='CategoryUsage'}"> active</c:if>"><span>         usage          </span></a>
      <a href="<wiki:LinkTo format="url" page="Documentation"/>" class="menu_item item4<c:if test="${currentCategory =='CategoryDocumentation'}"> active</c:if>"><span>         documentation  </span></a>
      <a href="<wiki:LinkTo format="url" page="Showcase"/>"      class="menu_item item5<c:if test="${currentCategory =='CategoryShowcase'}"> active</c:if>"><span>         showcase       </span></a>
      <a href="<wiki:LinkTo format="url" page="Download"/>"      class="menu_item item6<c:if test="${currentCategory =='CategoryDownload'}"> active</c:if>"><span>         download       </span></a>
    </div>
  </div>
  
  <div id="sidemenu">
     <wiki:InsertPage page="LeftMenu" />
  </div>
