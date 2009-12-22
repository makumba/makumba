<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setLocale value="${prefs.Language}" />
<fmt:setBundle basename="templates.default"/>
<%-- Provides a simple searchbox that can be easily included anywhere on the page --%>
<%-- Powered by jswpwiki-common.js//SearchBox --%>

<form action="<wiki:Link jsp='Search.jsp' format='url'/>"
           accept-charset="<wiki:ContentEncoding />">

  <input id="searchbox" onblur="if( this.value == '' ) { this.value = this.defaultValue }; return true; "
        onfocus="if( this.value == this.defaultValue ) { this.value = ''}; return true; "
           type="text" value="search"
           name="query" id="query"
      accesskey="f" />
</form>
