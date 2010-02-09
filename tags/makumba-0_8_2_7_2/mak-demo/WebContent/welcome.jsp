<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<jsp:include page="/layout/header.jsp?pageTitle=Welcome+page" flush="false"/>

<p>Welcome to the Makumba demo blog!</p>
<p>You can <a href="register.jsp">register a new user</a> with admin rights in order to post new blog entries.</p>
<br>

<mak:list from="blog.Post p" orderBy="p.TS_create desc" offset="$offset" limit="$limit" defaultLimit="10">
	<div class='blogpost'>
		<h1><mak:value expr="p.title"/></h1>
		<em>Added: <mak:value expr="p.TS_create" format="dd. MMMM yyyy" /> at <mak:value expr="p.TS_create" format="HH:mm" /></em>
		<mak:value expr="p.contents"/>
	</div>
	<c:if test="${mak:count() == mak:maxCount()}">
  		<mak:pagination itemName="blog posts"  />
	</c:if>
</mak:list>
<%--
<h1>Add new Post</h1>
<mak:newForm type="blog.Post" action="index.jsp" name="post" method="post" >
  <table>
   <tr class="${even?'even':'odd'}"> <c:set var="even" value="${!even}" />
   <td class="v_head"><label for="title"><span class="accessKey">t</span>itle</label></td>
   <td><mak:input field="title" styleId="title" accessKey="t" /></td>
   </tr>
   <tr class="${even?'even':'odd'}"> <c:set var="even" value="${!even}" />
   <td class="v_head"><label for="contents">C<span class="accessKey">o</span>ntents</label></td>
   <td><mak:input field="contents" styleId="contents" accessKey="o" /></td>
   </tr>
   <tr class="${even?'even':'odd'}"> <c:set var="even" value="${!even}" />
   <td>  <input type="submit" value="Add" accessKey="A">  <input type="reset" accessKey="R">  <input type="reset" value="Cancel" accessKey="C" onClick="javascript:back();">  </td>
   </tr>
  </table>
</mak:newForm>
--%>
<jsp:include page="/layout/footer.jsp" flush="false"/>
