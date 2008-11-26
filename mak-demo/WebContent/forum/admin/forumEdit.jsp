<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ page import="java.util.ArrayList" %>
<jsp:include page="/layout/header.jsp?pageTitle=Welcome+page" flush="false"/>

<h1>Forum Administration</h1>
<jsp:include page="navigation.jsp" flush="false"/>

<!-- FORUM EDIT FORM -->
<mak:response/>
<mak:object from="forum.Forum f" where="f=$fPtr">
  
  <h2>Edit forum <mak:value expr="f.title"/></h2>
  	<table class="forum">
    <mak:editForm object="f" action="./welcome.jsp">
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
      </mak:editForm>
      <tr>
        <th>Admins</th>
        <td>
          <ul>
          <% 
          ArrayList currentAdmins = new ArrayList();
          %>
          <mak:list from="f.admins adm"  orderBy="adm.user.firstName">
            <li>
              <mak:value expr="adm.user" var="adminPtr" />
              <% currentAdmins.add(adminPtr); %>
              <mak:value expr="adm.user.firstName"/> <mak:value expr="adm.user.lastName"/>
              <mak:delete object="adm"><mak:action>./forumEdit.jsp?fPtr=${param.fPtr}</mak:action>delete</mak:delete>
            </li>
          </mak:list>
          </ul>
          <mak:addForm field="admins" object="f" method="post">
            <mak:action>./forumEdit.jsp?fPtr=${param.fPtr}</mak:action>
            <mak:input name="user">
              <mak:option> -- Select a user to add as admin --</mak:option>
              <% //JASPER: Slightly redundant code, but coulnd't figure a way to put the $currentAdmin in a variable where...
              if (!currentAdmins.isEmpty()) {
                  request.setAttribute("currentAdmins", currentAdmins);
              %>
                <mak:list  from="user.User u"  where="u NOT IN SET ($currentAdmins)" orderBy="u.firstName">
                  <mak:option value="u"><mak:value expr="u.firstName"/> <mak:value expr="u.lastName"/></mak:option>
                </mak:list>
              <%
              } else {
              %>
                <mak:list  from="user.User u"  orderBy="u.firstName">
                  <mak:option value="u"><mak:value expr="u.firstName"/> <mak:value expr="u.lastName"/></mak:option>
                </mak:list>
              <%
              }
                  
              %>

            </mak:input>
            <input type="submit" value="add admin"/>
          </mak:addForm>
      </tr>
  	</table>
  
</mak:object>
<jsp:include page="/layout/footer.jsp" flush="false"/>

