<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />

<mak:object from="company.Project pr" where="pr=$employee">
<div class="addbox">
<h1><mak:value expr="pr.name" /></h1>

</div>
</mak:object>