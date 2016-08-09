<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%@page import="org.makumba.commons.attributes.PageAttributes"%><jsp:include page="/layout/header.jsp?pageTitle=Start+a+new+topic" flush="false"/>
  <%// if no parent is given, it is the top one so the parentPtr=NIL, as does the topicPtr. 
    // A new topic will be made in the business logics. otherwise, just take that parent post
    // for quoting purposes and let the BL find out what the topic associated was.
%>

<c:if test="${!empty param.fPtr}">
<c:set var="fPtr" value="${param.fPtr}"/>

<script>
function insertQuote() {
	 
}
</script>

<c:choose>
  <c:when test="${empty param.parPtr}">
    <c:set var="parPtr" value=""/>
  </c:when>
  <c:otherwise>
    <mak:object from="forum.Post parPost" where="parPost=$parPtr">
      <mak:value expr="parPost.contents" printVar="parentPostContents"/>
      <mak:value expr="parPost.topic.title" printVar="parentPostTitle"/>
      <mak:value expr="parPost" printVar="parPtr"/>
    </mak:object>
  </c:otherwise>
</c:choose>  

<mak:form handler="insertNewForumPost" action="./topicView.jsp">
<%// the BL make sure that the topic pointer (required by the action page) gets set %>
  <table class="forum">
    <tr>
      <th>Your message</th>
    </tr>
    <tr>
      <td> 
        <c:choose>
          <c:when test="${empty parPtr}">
            title: <input name="title" style="width:100%;"/>
          </c:when>
          <c:otherwise>
            <strong> <c:out value="${parentPostTitle}"/> </strong>
            <input type="button" onclick="insertQuote();">
          </c:otherwise>
        </c:choose>
      </td>
    </tr>
<c:choose>
  <c:when test="${!empty parentPostContents}">
    <tr>
      <td>
         <c:out value="${parentPostContents}"/>
      </td>
    </tr>
  </c:when>
</c:choose>        
    <tr>
      <td>
        <textarea name="contents" id="contents_area" style="width:100%;height:200px;"/>
        </textarea>
      </td>
    </tr>
    <tr>
      <td><input type="submit"/></td>
    </tr>
  </table>
  <input type="hidden" name="forum" value="${fPtr}"/>
  <input type="hidden" name="fPtr" value="${fPtr}"/>
  <c:if test="${!empty parPtr}">
    <input type="hidden" name="parent" value='${parPtr}'/>
  </c:if>
  <input type="hidden" name="author" value="${loggedInUser}"/>
</mak:form>

</c:if>
<jsp:include page="/layout/footer.jsp" flush="false"/>

