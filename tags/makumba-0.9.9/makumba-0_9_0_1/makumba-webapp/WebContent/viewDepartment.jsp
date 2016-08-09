<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />

<mak:object from="company.Department d" where="d=$department">
<div class="box">
<h1><mak:value expr="d.name" /></h1>
<a href="editDepartment.jsp?department=<mak:value expr="d"/>">edit</a>
<p><label>Department manager:</label><mak:value expr="d.manager.name" /></p>
<p><label>Employees:</label></p>
<div><mak:list from="company.Employee e" where="e.department=d" >
<a href="viewEmployee.jsp?page=home&employee=<mak:value expr="e"/>"> <mak:value expr="e.nameSurname()"/></a>
<br/>
</mak:list></div>
</div>
</mak:object>