<html>
<head><title>Partner list</title></head>
<body bgcolor="00FF33">

<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<%@include file="menu.jsp" %>

<h2>Partners:</h2>

<mak:list from="organisation.Partner p" limit="500">
   <a href="partner.jsp?partner=<mak:value expr="p"/>">
                <mak:value expr="p.name"/>
   </a>,
   <i><mak:value expr="p.city"/></i>  <br>
</mak:list>

<hr>
[<a href="partnerNew.jsp">New partner entry</a>]
</body>
</html>