<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setLocale value="${prefs.Language}" />
<fmt:setBundle basename="templates.default"/>
<%
  WikiContext c = WikiContext.findContext(pageContext);
  WikiPage wikipage = c.getPage();
%>
<wiki:CheckRequestContext context='view|diff|edit|upload|info'>
<div id='actionsBottom' class="pageactions"> 
  <wiki:PageExists>  

    <%-- changed by rudi: show a "add your comment" link --%>
    <%-- changed by manu: show the link for all users, even if they're not allowed to (will force login) --%>
      <hr />
      <wiki:PageType type="page">
        <a class="action comment" 
                 href="<wiki:CommentLink format='url' />" 
                title="<fmt:message key='actions.comment.title' />"><fmt:message key="actions.comment" />
        </a>
      </wiki:PageType>
    <%-- / changed by rudi: show a "add your comment" link --%>

    <hr />

    <a href="#top" 
      class="action quick2top" 
      title="<fmt:message key='actions.gototop'/>" >&laquo;</a>

    <wiki:UserCheck status="authenticated"> <%-- changed by rudi: only show revision details to authenticated users --%>
    <wiki:CheckVersion mode="latest">
       <fmt:message key="info.lastmodified">
          <fmt:param><wiki:PageVersion /></fmt:param>
          <fmt:param><wiki:DiffLink version="latest" newVersion="previous"><wiki:PageDate format='${prefs["DateFormat"]}'/></wiki:DiffLink></fmt:param>
          <fmt:param><wiki:Author /></fmt:param>
       </fmt:message>
    </wiki:CheckVersion>

    <wiki:CheckVersion mode="notlatest">
      <fmt:message key="actions.publishedon">
         <fmt:param><wiki:PageDate format='${prefs["DateFormat"]}'/></fmt:param>
         <fmt:param><wiki:Author /></fmt:param>
      </fmt:message>
    </wiki:CheckVersion>
    </wiki:UserCheck>
    
    <wiki:UserCheck status="notAuthenticated"> <%-- changed by rudi: to non-authenticated users, just show the last change date --%>
      This page was last updated on <wiki:PageDate format='MMMM d yyyy'/>
      
    </wiki:UserCheck>

   <wiki:RSSImageLink mode="wiki" />

  </wiki:PageExists>

  <wiki:NoSuchPage><fmt:message key="actions.notcreated"/></wiki:NoSuchPage> 
</div>
</wiki:CheckRequestContext>