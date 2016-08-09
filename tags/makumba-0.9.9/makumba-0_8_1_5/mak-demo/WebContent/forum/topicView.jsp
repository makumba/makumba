<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<jsp:include page="/layout/header.jsp?pageTitle=Forum+-+topic+view" flush="false"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/layout/forumScripts.js"></script>
<%@ page import="java.lang.*" %>
<%@ page import="java.lang.Math" %>



<c:choose>
  <c:when test="${empty param.tPtr}"><%
  // In this case there is either an error or the user just made a new post
  // so i'll show the thread of the last post he made%>
    <mak:list from="forum.Post p, forum.Topic t" where="p.author = $loggedInUser AND p.topic=t" orderBy="p.TS_create DESC" limit="1">
      <mak:value expr="t" printVar="tPtr"/>
      <c:set var="tPtr" value="${tPtr}" scope="request"/>
      <c:set var="page" value="1" scope="request"/>
    </mak:list>
  </c:when>
  <c:otherwise>
    <c:set var="tPtr" value="${param.tPtr}" scope="request"/>
  </c:otherwise>
</c:choose> 

<mak:object from="forum.Topic t" where="t=$tPtr">
  <h1><mak:value expr="t.title"/></h1>
  <mak:value expr="t.forum.title" printVar="forumTitle"/>
  <mak:value expr="t.forum" printVar="fPtr"/>
</mak:object>

<jsp:include page="./header.jsp?fPointer=${fPtr}" flush="false"/>

<c:set var="vWhere" value=""/>
<c:if test="${empty isForumAdmin}">
  <c:set var="vWhere" value="AND p.hidden='false'"/>
</c:if>

<mak:list from="forum.Post p" where="p.topic = $tPtr #{vWhere}" maxCountVar="numberOfPosts"/>
<%
Integer nop = new Integer(numberOfPosts);
Integer ppp = new Integer((String)request.getAttribute("postsPerPage"));
Integer numberOfPages = ((Double)(Math.ceil(nop.doubleValue()/(ppp.doubleValue())))).intValue();
%>
<c:choose>
  <c:when test="${empty param.page}">
    <%
    //When no page is given, go to the last one
    request.setAttribute("page",numberOfPages.toString()); %>
  </c:when>
  <c:otherwise>
    <c:set var="page" value="${param.page}"  scope="request"/>
  </c:otherwise>
</c:choose> 
<%
Integer currentpage = new Integer((String)request.getAttribute("page"));
String pagination = new String();
pagination += "<div class=\"pagination\">\n";
for (int i=1;i<=numberOfPages;i++){
    if(currentpage == i) { 
        pagination += "<span class=\"activePage\">" + i + "</span>\n";
    } else {
        pagination += "";
        pagination += "<a href=\"topicView.jsp?tPtr="+ request.getAttribute("tPtr") +"&page="+i+"\">";
        pagination += "<span class=\"page\">"+i+"</span></a>\n";
    }
}
pagination += "</div>\n";
out.print(pagination);
%>

<a href="./">Forum home</a> - 
<a href="./forumView.jsp?fPtr=${fPtr}">Back to '${forumTitle}'</a>

<c:set var="offset" value="${postsPerPage*(page-1)}"/>

<mak:list from="forum.Post p" where="p.topic = $tPtr #{vWhere}" orderBy="p.TS_create ASC" offset="$offset" limit="$postsPerPage">
<table class="forum">
  <tr>
    <td name="forumContents">
      <mak:value expr="p.contents" lineSeparator="<br/>"/>
    </td>
    <td class="forumdetails">
      Posted by <strong><mak:value expr="p.author.firstName"/> <mak:value expr="p.author.lastName"/></strong>
      on <mak:value expr="p.TS_create"/>
      <mak:if test="p.TS_modify <> p.TS_create">
        <strong>edited on <mak:value expr="p.TS_modify"/></strong>
      </mak:if>
    </td>
  </tr>
  <tr>
    <td colspan=2  class="forumbottom">
      <a href="postNew.jsp?fPtr=<mak:value expr="p.forum"/>&parPtr=<mak:value expr="p"/>">reply</a>
      <c:if test="${!empty isForumAdmin}">
        <mak:if test="p.hidden=0">
          <mak:editForm object="p" method="post">
            <mak:action>topicView.jsp?tPtr=${tPtr}</mak:action>
            <input type="hidden" name="hidden" value="true"/>
            <input type="submit" value="hide"/>
          </mak:editForm>
        </mak:if>
        <mak:if test="p.hidden=1">
          <mak:editForm object="p" method="post">
            <mak:action>topicView.jsp?tPtr=${tPtr}</mak:action>
            <input type="hidden" name="hidden" value="false"/>
            <input type="submit" value="unhide"/>
          </mak:editForm>
        </mak:if>
      </c:if>
    </td>
  </tr>  
</table>

</mak:list>
<%=pagination %>
<script>quoteReplacer();</script>

<a href="./">Forum home</a> - 
<a href="./forumView.jsp?fPtr=${fPtr}">Back to '${forumTitle}'</a>

<jsp:include page="/layout/footer.jsp" flush="false"/>
