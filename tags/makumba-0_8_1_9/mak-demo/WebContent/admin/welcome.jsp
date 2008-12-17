<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<jsp:include page="/layout/header.jsp?pageTitle=Welcome+page" flush="false"/>


<h1>Admin area</h1>

<strong>Add new post</strong>

<mak:newForm type="blog.Post" action="welcome.jsp" name="post" method="post" >
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

<jsp:include page="/layout/footer.jsp" flush="false"/>
