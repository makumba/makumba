<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />


<mak:list from="company.Employee em">
<mak:option value="em"><mak:value expr="em.name"/></mak:option>
</mak:list>


<mak:object from="company.Employee e" where="e=emp" >
<div class="box">
<mak:addForm field="projects" object="e" action="listEmployees.jsp">
<p><label>Project:</label></p>
<p><label>Time:</label><mak:input field="timePercentage" /></p>
<p><label></label><input type="submit" name="Create" value="assign"/></p>
</mak:addForm>
</div>
</mak:object>