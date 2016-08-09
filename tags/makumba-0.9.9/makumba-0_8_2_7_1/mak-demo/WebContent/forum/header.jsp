<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<c:remove var="isForumAdmin"/>
<mak:object from="forum.Forum f" where="f=$fPointer">
  <mak:list from="f.admins adm">
    <mak:value expr="adm.user" printVar="adminPtr" />
    <c:if test="${adminPtr == loggedInUser}">
      <c:set var="isForumAdmin" value="1" scope="request"/>
    </c:if>
  </mak:list>
</mak:object>

<c:set var="postsPerPage" value="5" scope="request"/>