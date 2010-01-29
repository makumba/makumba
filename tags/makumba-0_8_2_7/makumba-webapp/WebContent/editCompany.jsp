<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />

<mak:object from="company.Company c" where="c=$company">
<mak:editForm object="c" action="company.jsp?page=companies" method="post" message="Company edited">
<fieldset><label>Name:</label> <mak:input field="name" /><br/>
<label>Turnover:</label> <mak:input field="turnover" /><br/>
<label>Suppliers:</label> <mak:input field="suppliers" size="5"/><br/>
<label>Target markets:</label><mak:input field="targetMarkets" size="5" /><br/>
<label></label><input type="submit" name="Create" value="Edit company"/></fieldset>
</mak:editForm>

</mak:object>