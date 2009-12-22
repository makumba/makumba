<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />


<mak:object from="company.Employee e" where="e=$employee" >
<div class="box">
<mak:addForm field="projects" object="e" action="listEmployees.jsp?">
<p><label>Project:</label><mak:input name="project">
<mak:list from="company.Project p">
<mak:option value="p"><mak:value expr="p.name"/></mak:option>
</mak:list>
</mak:input></p>
<p><label>Time:</label><mak:input field="timePercentage" /></p>
<p><label></label><input type="submit" name="Create" value="assign"/></p>
</mak:addForm>
</div>
</mak:object>