<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />



<mak:object from="company.Department d" where="d=$department">
<h1>Edit <mak:value expr="d.name" /></h1>
<mak:editForm object="d" action="listDepartments.jsp" message="Department edited">
<label>Name:</label> <mak:input field="name" /><br/>
<label>Leader:</label> <mak:input field="manager" /><br/>
<label></label><input type="submit" name="Edit"/>
</mak:editForm>
</mak:object>

<p><a href="listDepartments.jsp">Go back</a></p>