<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<jsp:include page="header.jsp" />

<p><a href="#" onclick="showhide('1')">Add employee</a> | 
<a href="#" onclick="showhide('2')">Create company</a> | 
<a href="#" onclick="showhide('3')">Add target markets</a></p>

<div id="1" style="display: none;">
<jsp:include page="addEmployee.jsp" />
</div>

<div id="2" style="display: none;">
<jsp:include page="addCompany.jsp" />
</div>

<div id="3" style="display: none;">
<jsp:include page="addTargetMarkets.jsp" />
</div>

