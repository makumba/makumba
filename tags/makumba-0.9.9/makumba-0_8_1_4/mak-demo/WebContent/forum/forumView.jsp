<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<jsp:include page="/layout/header.jsp?pageTitle=Forum+-+topic+overview" flush="false"/>

<mak:object from="forum.Forum f" where="f=$fPtr">
<h1><mak:value expr="f.title"/></h1>
<a href="./">Forum home</a> - 
<a href="postNew.jsp?fPtr=${param.fPtr}">Start a new topic</a>
<table class="forum">
  <tr>
    <th>Topic</th>
    <th>Details</th>
  </tr>
  <mak:list from="forum.Topic t" orderBy="t.lastPostDate DESC" where="t.forum=$fPtr">
    <mak:value expr="t" printVar="tPtr"/>
    <tr>
      <td><a href="topicView.jsp?tPtr=${tPtr}"><mak:value expr="t.title"/></a>
      </td>
      <td class="forumdetails">
          by <mak:value expr="t.author.firstName"/> <mak:value expr="t.author.lastName"/><br/>
          last post by <mak:value expr="t.lastPost.author.firstName"/> 
          <mak:value expr="t.lastPost.author.lastName"/><br/> on <mak:value expr="t.lastPost.TS_create"/><br/>
          posts: <mak:value expr="t.postCount"/>
      </td>
    </tr>
  </mak:list>
</table>
<a href="./">Back to forum list</a> - 
<a href="postNew.jsp?fPtr=${param.fPtr}">Start a new topic</a>



</mak:object>
<jsp:include page="/layout/footer.jsp" flush="false"/>
