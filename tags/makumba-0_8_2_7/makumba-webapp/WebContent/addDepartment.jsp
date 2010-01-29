<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<div class="addbox">
<h1>Create new department</h1>
<mak:newForm type="company.Department" action="listDepartments.jsp" message="Department created">
<label>Name:</label> <mak:input field="name" /><br/>
<label>Leader:</label> <mak:input field="manager" /><br/>
<label></label><input type="submit" name="Create" value="Create new"/>
</mak:newForm>

</div>