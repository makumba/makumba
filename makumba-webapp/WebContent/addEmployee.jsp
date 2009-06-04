<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<h1>Create new Employee</h1>
<mak:newForm type="company.Employee" action="" method="post" message="Employee added">
<label>Name:</label> <mak:input field="name" autoComplete="true"/><br/>
<label>Surname:</label> <mak:input field="surname" autoComplete="true"/><br/>
<label>Birthdate:</label> <mak:input field="birthdate"/><br/>
<label></label><input type="submit" name="Create" value="Add employee"/>
</mak:newForm>

<br/>

<h1>Existing employees</h1>
<mak:list from="company.Employee p">
<mak:value expr="p.nameSurname()"/>  <br/>
</mak:list>
