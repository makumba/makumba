<%@ page import="org.apache.log4j.*" %>
<%@ page import="org.apache.commons.httpclient.*" %>
<%@ page import="org.apache.commons.httpclient.methods.*" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.util.*" %>
<%@ page import="com.ecyrd.jspwiki.ui.EditorManager" %>
<%@ page import="org.apache.commons.lang.time.StopWatch" %>
<%@ page errorPage="/Error.jsp" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="CoreResources"/>

<%!
    Logger log = Logger.getLogger("JSPWiki");
%>
<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    // Create wiki context and check for authorization
    WikiContext wikiContext = wiki.createContext( request, WikiContext.VIEW );
    if(!wikiContext.hasAccess( response )) return;
    String pagereq = wikiContext.getName();

    String content = request.getParameter("text");

    if( content != null )
    {
        String ticket = request.getParameter("Asirra_Ticket");
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod("http://challenge.asirra.com/cgi/Asirra?action=ValidateTicket&ticket="+ticket);

        int status = client.executeMethod(method);
        String body = method.getResponseBodyAsString();

        if( status == HttpStatus.SC_OK )
        {
            if( body.indexOf("Pass") != -1 )
            {
                session.setAttribute("captcha","ok");
                response.sendRedirect( wikiContext.getURL(WikiContext.EDIT, request.getParameter("page") ) );
                return;
            }
        }

        response.sendRedirect("Message.jsp?message=NOK");
    }

    // Set the content type and include the response content
    response.setContentType("text/html; charset="+wiki.getContentEncoding() );
%>
<html>

<head>
  <title><wiki:Variable var="applicationname" />: <wiki:PageName /></title>
  <%-- <wiki:Include page="commonheader.jsp"/> --%>
  <meta name="robots" content="noindex,nofollow" />
  <script type="text/javascript">
    function HumanCheckComplete(isHuman)
    {
       if (isHuman)
       {
          formElt = document.getElementById("mainForm");
          formElt.submit();
       }
       else
       {
          alert('<fmt:message key="captcha.js.humancheckcomplete.alert" />');
          return false;
       }
    }
    
    function i18nAsirra() {
       document.getElementById("asirra_InstructionsTextId").innerHTML = "<fmt:message key="captcha.asirra.please.select" />";
	   for ( var i = 0; i < 12; i++) 
       {
          document.getElementById("asirra_AdoptMeDiv" + i).getElementsByTagName("a")[0].innerHTML= '<font size="-1">' + '<fmt:message key="captcha.asirra.adopt.me" />' + '</font>' ;
       }
       document.getElementById("asirra_KnobsTable").getElementsByTagName("a")[0].title="<fmt:message key="captcha.asirra.a.get.challenge" />";
       document.getElementById("asirra_KnobsTable").getElementsByTagName("a")[1].title="<fmt:message key="captcha.asirra.a.whatsthis" />";
       document.getElementById("mainForm").style.display="block"; // show form when i18n is done 
    }
   </script>
</head>
<body onload="i18nAsirra()">
<div style="margin: 8px">
   <p><fmt:message key="captcha.description" /></p>

   <form action="<wiki:Link jsp='Captcha.jsp' format='url'/>" method="post" id="mainForm" style="display: none;">
      <input type="hidden" value="foo" name="text" />
      <input type="hidden" value='<%=request.getParameter("page")%>' name='page'/>
      <script type="text/javascript" src="http://challenge.asirra.com/js/AsirraClientSide.js"></script>
      <script type="text/javascript">
         asirraState.SetEnlargedPosition("right");
         // asirraState.SetCellsPerRow(6);
      </script>
      <br />
      <input type="button" value="<fmt:message key="captcha.submit" />" onclick="javascript:Asirra_CheckIfHuman(HumanCheckComplete)" />
  </form>
</div>
</body>