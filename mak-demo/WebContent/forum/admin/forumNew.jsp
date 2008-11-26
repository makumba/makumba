<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<jsp:include page="/layout/header.jsp?pageTitle=Welcome+page" flush="false"/>

<h1>Forum Administration</h1>
<jsp:include page="navigation.jsp" flush="false"/>

<!-- NEW FORUM ADDER -->
<h2>Add a new forum</h2>
<mak:newForm type="forum.Forum" method="post" action="./welcome.jsp">
	<table style="width:100%;">
		<tr>
			<th>Title</th>
	    <td>
		   <mak:input name="title"/>
	    </td>
	  </tr>
	  <tr>
	    <th>Description</th>
	    <td>
		   <mak:input name="description" style="width:300px;height:50px;"/>
	    </td>
	  </tr>
    <tr>
      <td colspan=2 style="text-align:right;">
        <input type=submit>
      </td>
    </tr>
	</table>
	<mak:input name="threadCount" value="0" type="hidden"/>
	<mak:input name="postCount" value="0" type="hidden"/>
</mak:newForm>

<jsp:include page="/layout/footer.jsp" flush="false"/>
