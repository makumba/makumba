<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />

<div class="addbox">
<mak:object from="company.Project p" where="p=$project">
<mak:editForm object="p" action="listProjects.jsp" method="post" message="Project edited">
<fieldset><label>Name:</label> <mak:input field="name" /><br/>
<label>Leader:</label> <mak:input field="leader" /><br/>
<div><label>Description:</label> <mak:input field="description" rows="5" cols="25" /></div><br/>
<label></label><input type="submit" name="Create" value="Edit project"/></fieldset>
</mak:editForm>

</mak:object>
</div>