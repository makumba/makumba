<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


<div class="addbox">
<h1>Create new Employee</h1>
<mak:newForm type="company.Employee" action="" method="post" message="Employee added">
<label>Name:</label> <mak:input field="name"/><br/>
<label>Surname:</label> <mak:input field="surname"/><br/>
<label>Birthdate:</label> <mak:input field="birthdate"/><br/>
<label>Salary:</label><mak:input field="salary"/><br/>
<label>Department:</label><mak:input field="department"/><br/>
<label></label><input type="submit" name="Create" value="Add employee"/>
</mak:newForm>
</div>
