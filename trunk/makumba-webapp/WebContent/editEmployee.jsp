<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />

<mak:object from="company.Employee e" where="e=$employee">
<div class="addbox">
<h1>Edit Employee</h1>
<mak:editForm object="e" action="listEmployees.jsp?page=home" method="post" message="Employee edited">
<label>Name:</label> <mak:input field="name"/><br/>
<label>Surname:</label> <mak:input field="surname"/><br/>
<label>Birthdate:</label> <mak:input field="birthdate"/><br/>
<label>Salary:</label><mak:input field="salary"/><br/>
<label>Department:</label><mak:input field="department"/><br/>
<label></label><input type="submit" name="Create" value="Edit employee"/>
</mak:editForm>
</div>
</mak:object>