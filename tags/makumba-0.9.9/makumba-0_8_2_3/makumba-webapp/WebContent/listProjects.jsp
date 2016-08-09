<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />

<h1>Projects list</h1>

<table>
<tr>
<th>
Project</th>
<th>
Project leader
</th>
<th>
edit
</th>
</tr>
<mak:list from="company.Project p">
<tr>
<td>
<a href="viewProject.jsp?page=projects&project=<mak:value expr="p"/>"><mak:value expr="p.name"/></a> 
</td>
<td>
<a href="viewEmployee.jsp?employee=<mak:value expr="p.leader"/>"> <mak:value expr="p.leader.name"/></a>
</td>
<td>
[ <a href="editProject.jsp?project=<mak:value expr="p"/>">edit</a> ] 
</td>
</tr>
</mak:list>
</table>

<p><a href="#" onclick="getPage('addProject.jsp','2','')">Add project</a></p>

<div id="2"></div>