<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<div class="addbox">
<h1>Create new company</h1>
<mak:newForm type="company.Company" action="" method="post" message="Company created">
<fieldset><label>Name:</label> <mak:input field="name" /><br/>
<label>Turnover:</label> <mak:input field="turnover" /><br/>
<label>Suppliers:</label> <mak:input field="suppliers" size="5"/><br/>
<label>Target markets:</label><mak:input field="targetMarkets" size="5" /><br/>
<label></label><input type="submit" name="Create"/></fieldset>
</mak:newForm>

</div>
