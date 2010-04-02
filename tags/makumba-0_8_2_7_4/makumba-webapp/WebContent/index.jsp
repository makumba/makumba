<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />

<p><a href="#" onclick="showhide('show')" >quick add</a></p>

<div id="show" style="display: none">
<p><a href="#" onclick="getPage('addEmployee.jsp','add','')">Add employee</a> | 
<a href="#" onclick="getPage('addCompany.jsp','add','')">Create company</a> | 
<a href="#" onclick="getPage('addProject.jsp','add','')">Add project</a> |
<a href="#" onclick="getPage('addTargetMarkets.jsp','add','')">Add target markets</a></p> 

<div id="add"></div>
</div>

<div class="box">
<h1>Existing employees</h1>
<mak:list from="company.Employee p" limit="5">
<a href="viewEmployee.jsp?page=home&employee=<mak:value expr="p"/>"> <mak:value expr="p.nameSurname()"/></a>
<br/>
</mak:list>

<p align="right"><a href="listEmployees.jsp?page=home">view all</a></p></div>

<div class="box">
<h1>Projects</h1>
<mak:list from="company.Project p" limit="5">
<p><a href="viewProject.jsp?page=home&project=<mak:value expr="p"/>"><mak:value expr="p.name"/></a> 
( <a href="viewEmployee.jsp?page=home&employee=<mak:value expr="p.leader"/>"><mak:value expr="p.leader.nameSurname()"/></a> - leader) </p>
</mak:list>

<p align="right"><a href="listProjects.jsp?page=home">view all</a></p></div>

<div class="box">
<h1>Suppliers</h1>
<mak:list from="company.Company c" limit="5">
<p><mak:value expr="c.targetMarkets"/></p>
</mak:list>

<p align="right"><a href="listSuppliers.jsp?page=home">view all</a></p></div>
