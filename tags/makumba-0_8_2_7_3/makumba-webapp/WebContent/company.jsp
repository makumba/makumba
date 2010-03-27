<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />

<mak:list from="company.Company c">
<div class="box">
<h1><mak:value expr="c.name" /></h1>
<p><label>Turnover:</label> <mak:value expr="c.turnover" /></p>
<p><label>Suppliers:</label><mak:value expr="c.suppliers" /></p>
<p align="right"><a href="editCompany.jsp?page=companies&company=<mak:value expr='c' />" >edit</a></p>
</div>
</mak:list>