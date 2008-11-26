<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%@page import="org.makumba.commons.attributes.PageAttributes"%><jsp:include page="/layout/header.jsp?pageTitle=Start+a+new+topic" flush="false"/>
  <%// if no parent is given, it is the top one so the parentPtr=NIL, as does the topicPtr. 
    // A new topic will be made in the business logics. otherwise, just take that parent post
    // for quoting purposes and let the BL find out what the topic associated was.
%>

<script type="text/javascript" src="${pageContext.request.contextPath}/layout/forumScripts.js"></script>

<c:choose>
<c:when test="${!empty param.fPtr && !empty loggedInUser}">
<c:set var="fPtr" value="${param.fPtr}"/>

<c:choose>
  <c:when test="${empty param.parPtr}">
    <h1>New post</h1>
    <c:set var="parPtr" value=""/>
  </c:when>
  <c:otherwise>
    <h1>Post a reply</h1>
    <mak:object from="forum.Post parPost" where="parPost=$parPtr">
      <mak:value expr="parPost.contents" var="parentPostContents" printVar="parentPostContentsHTML"/>
      <mak:value expr="parPost.topic.title" printVar="parentPostTitle"/>
      <mak:value expr='parPost.author.firstName' printVar="parentPosterFN"/> 
      <mak:value expr='parPost.author.lastName' printVar="parentPosterLN"/>
      <mak:value expr="parPost" printVar="parPtr"/>
    </mak:object>
  </c:otherwise>
</c:choose>  

<mak:form handler="insertNewForumPost" action="./topicView.jsp" message="Succesfully posted your message">
<%// the BL make sure that the topic pointer (required by the action page) gets set %>
  <table class="forum">
    <c:if test="${empty parPtr}">
    <tr>
      <td colspan=2> 
      Title: <input name="title" style="width:100%;"/>
      </td>
    </tr>

    </c:if>
<c:choose>
  <c:when test="${!empty param.parPtr}">
    <tr>
      <td colspan=2>
         <div id="parentPostContents" style="display:none;"><c:out value="${parentPostContents}"/></div>
         <div id="parentPostContentsHTML">
            <strong>${parentPosterFN} ${parentPosterLN} wrote:</strong><br/>
            ${parentPostContentsHTML}
        </div>
      </td>
    </tr>
    <tr>
      <td>
        <strong>RE: ${parentPostTitle}</strong>
      </td>
      <td style="text-align:right;">
          <input type="button" onclick="insertQuote();this.enabled=false;" value="insert quote &gt;&gt;">
      </td>
    </tr>
  </c:when>
</c:choose>        
    <tr>
      <td colspan=2>
        <textarea name="contents" id="contents_area" style="width:100%;height:200px;"></textarea>
      </td>
    </tr>
    <tr>
      <td colspan=2><input type="submit"/></td>
    </tr>
  </table>
  <input type="hidden" name="forum" value="${fPtr}"/>
  <input type="hidden" name="fPtr" value="${fPtr}"/>
  <c:if test="${!empty parPtr}">
    <input type="hidden" name="parent" value='${parPtr}'/>
  </c:if>
</mak:form>

</c:when>
<c:otherwise>

You need to be logged in before you can make a post.

</c:otherwise>
</c:choose>

<jsp:include page="/layout/footer.jsp" flush="false"/>