<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<jsp:include page="/layout/header.jsp?pageTitle=Forum+-+Overview" flush="false"/>

<h1>Forum List</h1>

<table class="forum">
  <mak:list from="forum.Forum f" orderBy="f.title">
    <mak:value expr="f" printVar="fPtr"/>
    <tr>
      <td><a href="forumView.jsp?fPtr=${fPtr}"><mak:value expr="f.title"/></a>
          <div class="description"><mak:value expr="f.description"/></div>
      </td>
      <td class="forumdetails">
          threads: <mak:value expr="f.threadCount"/><br/>
          posts: <mak:value expr="f.postCount"/><br/>
          last post by <mak:value expr="f.lastPost.author.firstName"/> <mak:value expr="f.lastPost.author.lastName"/>
          on <mak:value expr="f.lastPost.TS_create"/>
      </td>
    </tr>
  </mak:list>
</table>

<jsp:include page="/layout/footer.jsp" flush="false"/>
