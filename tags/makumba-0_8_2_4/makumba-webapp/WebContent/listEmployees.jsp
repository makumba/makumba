<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />

<table>
<tr><th>Name</th>
<th>Surname</th>
<th>Birthdate</th>
<th>Salary</th>
<th>Department</th>
<th></th></tr>
<mak:list from="company.Employee e">
<tr><td><mak:value expr="e.name"/></td>
<td><mak:value expr="e.surname"/></td>
<td><mak:value expr="e.birthdate"/></td>
<td><mak:value expr="e.salary"/></td>
<td><mak:value expr="e.department.name"/></td>
<td><a href="viewEmployee.jsp?page=home&employee=<mak:value expr="e"/>">view</a> | 
<a href="editEmployee.jsp?page=home&employee=<mak:value expr="e" />">edit info</a></td>
</tr>
</mak:list>
</table>