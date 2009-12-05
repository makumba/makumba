<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<fmt:setLocale value="${prefs.Language}" />
<fmt:setBundle basename="templates.default"/>
<%@ page import="com.ecyrd.jspwiki.*" %>

<wiki:UserCheck status="authenticated"> <%-- changed by rudi: only show to authenticated users --%>

<div id="favorites">

  <div id="hiddenmorepopup">
  <ul id="morepopup">
     <wiki:CheckRequestContext context='view|info|diff|upload'>
     <wiki:PageExists>  	
     <wiki:Permission permission="comment">
       <wiki:PageType type="page">
         <li><a class="action comment" 
                 href="<wiki:CommentLink format='url' />" 
                title="<fmt:message key='actions.comment.title' />"><fmt:message key="actions.comment" />
		 </a></li>
       </wiki:PageType>
       <wiki:PageType type="attachment">
         <li><a class="action comment" 
                 href="<wiki:BaseURL/>Comment.jsp?page=<wiki:ParentPageName />"
                title="<fmt:message key='actions.comment.title' />"><fmt:message key="actions.comment" />
	     </a></li>
       </wiki:PageType>
     </wiki:Permission>
     </wiki:PageExists>  
     </wiki:CheckRequestContext>
    
     <wiki:PageExists>  
     <wiki:CheckRequestContext context='view|info|diff|upload|edit|comment|preview' >
       <li>
       <wiki:CheckVersion mode="latest">
       <a class="action rawpage wikipage" 
               href="<wiki:Link format='url'><wiki:Param name='skin' value='raw'/></wiki:Link>"
              title="<fmt:message key='actions.rawpage.title' />"><fmt:message key='actions.rawpage' />
       </a>
       </wiki:CheckVersion>
       <wiki:CheckVersion mode="notlatest">
       <a class="action rawpage wikipage" 
               href="<wiki:Link format='url' version='${param.version}'><wiki:Param name='skin' value='raw'/></wiki:Link>"
              title="<fmt:message key='actions.rawpage.title' />"><fmt:message key='actions.rawpage' />
       </a>
       </wiki:CheckVersion>
       </li>
      </wiki:CheckRequestContext>
      </wiki:PageExists>  
  
      <wiki:CheckRequestContext context='!workflow'>
      <wiki:UserCheck status="authenticated">
        <li><a class="action workflow" 
                href="<wiki:Link jsp='Workflow.jsp' format='url' />" 
               title="<fmt:message key='actions.workflow.title' />"><fmt:message key='actions.workflow' />
        </a></li>
      </wiki:UserCheck>
      </wiki:CheckRequestContext>

      <wiki:Permission permission="createGroups">
        <li><a class="action creategroup" 
                href="<wiki:Link jsp='NewGroup.jsp' format='url' />" 
               title="<fmt:message key='actions.creategroup.title' />"><fmt:message key='actions.creategroup' />
        </a></li>
      </wiki:Permission>
	  <li class='separator'>
        <div id="moremenu" ><wiki:InsertPage page="MoreMenu" /></div>
      </li>
  </ul>
  </div>
</wiki:UserCheck> <%-- changed by rudi: only show to authenticated users --%>
  
  <wiki:CheckRequestContext context='!login'>

  <wiki:UserCheck status="known">
  <wiki:Translate>[{If page='{$username}Favorites' exists='true'

%%collapsebox-closed
! [My Favorites|{$username}Favorites]
[{InsertPage page='{$username}Favorites' }]
%% }]
  </wiki:Translate>
  </wiki:UserCheck>

  </wiki:CheckRequestContext>
  
  <div class="wikiversion"><%=Release.APPNAME%> v<%=Release.getVersionString()%>
  <span class="rssfeed">
    <wiki:RSSImageLink title='<%=LocaleSupport.getLocalizedMessage(pageContext,"fav.aggregatewiki.title")%>' />
  </span>
  </div>  
  
</div>
