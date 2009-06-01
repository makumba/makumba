<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<h1>Create new company</h1>
<mak:newForm type="company.Company" action="" method="post">
<fieldset><label>Name:</label> <mak:input field="name" /><br/>
<label>Turnover:</label> <mak:input field="turnover" /><br/>
<label>Suppliers:</label> <mak:input field="suppliers" /><br/>
<label>Target markets:</label><mak:input field="targetMarkets" /><br/>
<label></label><input type="submit" name="Create"/></fieldset>
</mak:newForm>

<br/>

<h1>Company list</h1>
<mak:list from="company.Company p">
<mak:value expr="p.name"/>
</mak:list>
