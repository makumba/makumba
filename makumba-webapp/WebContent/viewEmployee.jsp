<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />

<mak:object from="company.Employee e" where="e=$employee">
<div class="box">
<h1><mak:value expr="e.nameSurname()" /></h1>
<p><label>Birthdate:</label><mak:value expr="e.birthdate" /></p>
<p><label>Salary:</label><mak:value expr="e.salary" /></p>
<p><label>Department:</label><mak:value expr="e.department" /></p>
<p><label>Project(s):</label>
<mak:list from="e.projects p" >
<mak:value expr="p.project" />
</mak:list>
</p>
<br/>
<p align="right"><a href="editEmployee.jsp?page=home&employee=<mak:value expr="e" />">edit info</a></p>
</div>
</mak:object>