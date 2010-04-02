<%@ page import="org.apache.log4j.*" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.filters.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.ecyrd.jspwiki.ui.EditorManager" %>
<%@ page errorPage="/Error.jsp" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%! 
    Logger log = Logger.getLogger("JSPWiki"); 
%>

<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context and check for authorization
    WikiContext wikiContext = wiki.createContext( request, WikiContext.PREVIEW );
    if(!wikiContext.hasAccess( response )) return;
    String pagereq = wikiContext.getName();

    pageContext.setAttribute( EditorManager.ATTR_EDITEDTEXT,
                              session.getAttribute( EditorManager.REQ_EDITEDTEXT ),
                              PageContext.REQUEST_SCOPE );

    String lastchange = SpamFilter.getSpamHash( wikiContext.getPage(), request );

    pageContext.setAttribute( "lastchange",
                              lastchange,
                              PageContext.REQUEST_SCOPE );
   
    // Set the content type and include the response content
    response.setContentType("text/html; charset="+wiki.getContentEncoding() );
    String contentPage = wiki.getTemplateManager().findJSP( pageContext,
                                                            wikiContext.getTemplate(),
                                                            "ViewTemplate.jsp" );
%><wiki:Include page="<%=contentPage%>" />

