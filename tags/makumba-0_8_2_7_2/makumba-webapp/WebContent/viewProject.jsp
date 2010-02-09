<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />

<mak:object from="company.Project p" where="p=$project" >
<div class="box">
<h1><mak:value expr="p.name"/></h1>
<a href="editProject.jsp?project=<mak:value expr="p"/>">edit</a>
<p><label>Leader:</label><a href="viewEmloyee.jsp?page=home&employee=<mak:value expr="p.leader" />"><mak:value expr="p.leader.name" /></a></p>
<p><label>Description:</label><mak:value expr="p.description" /></p><br/>
<div><label>Involved:</label>
<br/>
<mak:list from="company.Employee e, e.projects pr" where="p=pr.project" >
<a href="viewEmployee.jsp?page=home&employee=<mak:value expr="e" />"><mak:value expr="e.nameSurname()" /></a>
</mak:list></div>
</div>
</mak:object>


