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
<c:set var="currentCategoryMenu"><wiki:Plugin plugin="InsertCategoryMenu" args="showCurrentCategoryMenu='true'"/></c:set>

<div id="site">
  <div id="header">
    <div id="persistent">
      <wiki:Include page="UserBox.jsp" />
      <wiki:Include page="SearchBox.jsp" />
    </div>
    <div id="top_nav">
    
      <a href="<wiki:LinkTo format="url" page="Main"/>"          class="menu_item<c:if test="${currentCategory =='CategoryMain'}"> active</c:if>">home</a>
      <a href="<wiki:LinkTo format="url" page="QuickStart"/>"    class="menu_item<c:if test="${currentCategory =='CategoryQuickStart'}"> active</c:if>">quick start</a>
      <a href="<wiki:LinkTo format="url" page="Configuration"/>" class="menu_item<c:if test="${currentCategory =='CategoryConfiguration'}"> active</c:if>">configuration</a>
      <a href="<wiki:LinkTo format="url" page="Usage"/>"         class="menu_item<c:if test="${currentCategory =='CategoryUsage'}"> active</c:if>">usage</a>
      <a href="<wiki:LinkTo format="url" page="Documentation"/>" class="menu_item<c:if test="${currentCategory =='CategoryDocumentation'}"> active</c:if>">documentation</a>
      <a href="<wiki:LinkTo format="url" page="Showcase"/>"      class="menu_item<c:if test="${currentCategory =='CategoryShowcase'}"> active</c:if>">showcase</a>
      <a href="<wiki:LinkTo format="url" page="Download"/>"      class="menu_item<c:if test="${currentCategory =='CategoryDownload'}"> active</c:if>">download</a>
    </div>
  </div>
  <%-- manu: if the page has no category, then the menu content is not generated --%>
  <c:if test="${not empty currentCategory && not empty currentCategoryMenu}">
  <div id="sidemenu">
     <wiki:InsertPage page="LeftMenu" />
  </div>
  </c:if>
