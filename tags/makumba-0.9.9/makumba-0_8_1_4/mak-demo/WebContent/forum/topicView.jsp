<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<jsp:include page="/layout/header.jsp?pageTitle=Forum+-+topic+view" flush="false"/>

<mak:object from="forum.Topic t" where="t=$tPtr">
<h1><mak:value expr="t.title"/></h1>
<mak:value expr="t.forum.title" printVar="forumTitle"/>
<mak:value expr="t.forum" printVar="fPtr"/>
</mak:object>
<a href="./">Forum home</a> - 
<a href="./forumView.jsp?fPtr=${fPtr}">Back to '${forumTitle}'</a>

<mak:list from="forum.Post p" where="p.topic = $tPtr AND p.hidden=0" orderBy="p.TS_create ASC">
<table class="forum">
  <tr>
    <td>
      <mak:value expr="p.contents"/>
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
    </td>
  </tr>  
</table>
</mak:list>

<a href="./">Forum home</a> - 
<a href="./forumView.jsp?fPtr=${fPtr}">Back to '${forumTitle}'</a>

<jsp:include page="/layout/footer.jsp" flush="false"/>
