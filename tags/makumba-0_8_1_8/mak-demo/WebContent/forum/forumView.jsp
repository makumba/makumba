
<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%@page import="java.util.Currency"%>
<%@page import="org.omg.CORBA.Current"%><jsp:include page="/layout/header.jsp?pageTitle=Forum+-+topic+overview" flush="false"/>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.lang.Boolean" %>

<mak:object from="forum.Forum f" where="f=$fPtr">

<jsp:include page="./header.jsp?fPointer=${param.fPtr}" flush="false"/>

<h1><mak:value expr="f.title"/></h1>
<a href="./">Forum home</a> - 
<a href="postNew.jsp?fPtr=${param.fPtr}">Start a new topic</a>
<table class="forum">
  <tr>
    <th style="width:70%;">Topic</th>
    <th style="width:20%;">Details</th>
    <c:if test="${!empty isForumAdmin}"><th style="width:10%;">Action</th></c:if>
  </tr>
  <mak:list from="forum.Topic t" orderBy="t.sticky DESC, t.lastPostDate DESC" where="t.forum=$fPtr">
    <mak:value expr="t" printVar="tPtr"/>
    <tr>
      <td>
        <mak:if test="t.sticky='true'">[STICKY]</mak:if>
        <a href="topicView.jsp?tPtr=${tPtr}&page=1"><mak:value expr="t.title"/></a>
      </td>
      <td class="forumdetails">
          initiated by <mak:value expr="t.author.firstName"/> <mak:value expr="t.author.lastName"/><br/>
          last post by <mak:value expr="t.lastPost.author.firstName"/> 
          <mak:value expr="t.lastPost.author.lastName"/><br/> on <mak:value expr="t.lastPost.TS_create"/><br/>
          posts: <mak:value expr="t.postCount"/>
      </td>
      <c:if test="${!empty isForumAdmin}">
        <td>
          <mak:form handler="deleteTopic" method="post">
            <mak:action>forumView.jsp?fPtr=${param.fPtr}</mak:action>
            <mak:input type="hidden" name="topic" value="t"/>
            <mak:input type="hidden" name="forum" value="f"/>
            <input type="button" value="delete" onclick="if(confirm('This will delete the topic and all posts inside!\nAre you sure you wish to continue?')) this.form.submit();"/> 

          </mak:form>

          <mak:if test="t.sticky=0">
            <mak:editForm object="t" method="post">
              <mak:action>forumView.jsp?fPtr=${param.fPtr}</mak:action>
              <input type="hidden" name="sticky" value="true"/>
              <input type="submit" value="set sticky"/>
            </mak:editForm>
          </mak:if>
          <mak:if test="t.sticky=1">
            <mak:editForm object="t" method="post">
              <mak:action>forumView.jsp?fPtr=${param.fPtr}</mak:action>
              <input type="hidden" name="sticky" value="false"/>
              <input type="submit" value="unset sticky"/>
            </mak:editForm>
          </mak:if>
        </td>
      </c:if>
    </tr>
  </mak:list>
</table>
<a href="./">Back to forum list</a> - 
<a href="postNew.jsp?fPtr=${param.fPtr}">Start a new topic</a>



</mak:object>
<jsp:include page="/layout/footer.jsp" flush="false"/>
