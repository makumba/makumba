<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<jsp:include page="/layout/header.jsp?pageTitle=Welcome+page" flush="false"/>

<h1>Forum Administration</h1>
<jsp:include page="navigation.jsp" flush="false"/>

<!-- CURRENT FORUM LISTING -->
<h2>List of fora</h2>
<a href="forumNew.jsp">Add new forum</a>
<table class="forum">
  <tr>
    <th>title</th>
    <th>description</th>
    <th>action</th>
  </tr>
  <mak:list from="forum.Forum f" orderBy="f.title">
    <mak:value expr="f" printVar="fPtr"/>
    <tr>
      <td><mak:value expr="f.title"/>
      </td>
      <td><span class="description"><mak:value expr="f.description"/></span>
      </td>
      <td class="forumdetails">
        <mak:delete object="f" action="./">delete</mak:delete>
        <a href="./forumEdit.jsp?fPtr=${fPtr}">edit</a>
      </td>
    </tr>
  </mak:list>
</table>

<jsp:include page="/layout/footer.jsp" flush="false"/>
