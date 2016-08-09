<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<jsp:include page="/layout/header.jsp?pageTitle=Welcome+page" flush="false"/>

<h1>Forum Administration</h1>
<jsp:include page="navigation.jsp" flush="false"/>

<!-- FORUM EDIT FORM -->


<mak:object from="forum.Forum f" where="f=$fPtr">
  <mak:editForm object="f" action="./welcome.jsp">
  
  <h2>Edit forum <mak:value expr="f.title"/></h2>
  	<table class="forum">
  		<tr>
  			<th>Title</th>
  	    <td>
  		   <mak:input name="title"/>
  	    </td>
  	  </tr>
  	  <tr>
  	    <th>Description</th>
  	    <td>
  		   <mak:input name="description" style="width:300px;heigth;50px;"/>
  	    </td>
  	  </tr>
      <tr>
        <td colspan=2 style="text-align:right;">
          <input type=submit>
        </td>
      </tr>
  	</table>
  </mak:editForm>
</mak:object>
<jsp:include page="/layout/footer.jsp" flush="false"/>
