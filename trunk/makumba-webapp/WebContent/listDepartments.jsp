<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />

<h1>Department list</h1>

<table>
<tr>
<th>
Department
</th>
<th>
Department manager
</th>
<th>
edit
</th>
</tr>
<mak:list from="company.Department d">
<tr>
<td>
<a href="viewDepartment.jsp?department=<mak:value expr="d"/>"><mak:value expr="d.name"/></a> 
</td>
<td>
<a href="viewPerson.jsp?department=<mak:value expr="d.manager"/>"> <mak:value expr="d.manager.name"/></a>
</td>
<td>
[ <a href="editDepartment.jsp?department=<mak:value expr="d"/>">edit</a> ] 
</td>
</tr>
</mak:list>
</table>

<p><a href="#" onclick="showhide('1')">Add department</a></p>

<div id="1" style="display: none;">
<jsp:include page="addDepartment.jsp" />
</div>