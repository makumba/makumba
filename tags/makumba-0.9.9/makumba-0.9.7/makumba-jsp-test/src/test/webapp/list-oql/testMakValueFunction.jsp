<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Language list using the mak:value() function</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<b>Languages:</b>
<br><br><br>
<mak:list from="test.Language l">
  #${mak:count()}: 
  name:${mak:value('l.name')},  
  isoCode:${mak:value('l.isoCode')},
  <c:choose>
    <c:when test="${mak:value('l.name')=='English'}">Yes!</c:when>
    <c:when test="${mak:value('l.name')=='French'}">Oui!</c:when>
    <c:when test="${mak:value('l.name')=='German'}">Ja!</c:when>
    <c:otherwise>D'oh!</c:otherwise>
  </c:choose>
  <br/>
</mak:list>

</body>
</html>
