<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Language list</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<br/><hr><br/>
<c:set var="defaultLimit" value="2"/>
<mak:list from="test.Language l" offset="$offset" limit="$limit" defaultLimit="$defaultLimit">
  <c:if test="${mak:count()==1}"> <mak:pagination/> </c:if>
  name:<mak:value expr="l.name"/><br>
  isoCode:<mak:value expr="l.isoCode"/><br><br>
  <c:if test="${mak:count()==mak:maxCount()}"> <mak:pagination/> </c:if>
</mak:list>

<br/><hr><br/>
<c:set var="defaultLimit" value="3"/>
<c:set var="offset" value="2"/>
<mak:list from="test.Language l" offset="$offset" limit="$limit" defaultLimit="$defaultLimit" id="pagination1">
  <c:if test="${mak:count()==1}"> <mak:pagination/> </c:if>
  name:<mak:value expr="l.name"/><br>
  isoCode:<mak:value expr="l.isoCode"/><br><br>
  <c:if test="${mak:count()==mak:maxCount()}"> <mak:pagination/> </c:if>
</mak:list>

<br/><hr><br/>
<c:set var="defaultLimit" value="1"/>
<c:set var="offset" value="0"/>
<mak:list from="test.Language l" offset="$offset" limit="$limit" defaultLimit="$defaultLimit" id="pagination3">
  <c:if test="${mak:count()==1}"> <mak:pagination/> </c:if>
  name:<mak:value expr="l.name"/><br>
  isoCode:<mak:value expr="l.isoCode"/><br><br>
  <c:if test="${mak:count()==mak:maxCount()}"> <mak:pagination/> </c:if>
</mak:list>

</body>
</html>
