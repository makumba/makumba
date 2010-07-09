<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Testing mak:nextCount()</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<b>Persons</b> list will have ${mak:nextCount()} items<br/>
<mak:list from="test.Person p">
  &nbsp; # countPerson: ${mak:count()}/${mak:maxCount()}<br/>
</mak:list>
<b>Persons</b> list had ${mak:lastCount()} items<br/>

<br/><br/>

<b>Languages</b> list will have ${mak:nextCount()} items<br/>
<mak:list from="test.Language l">
  &nbsp; # countLanguage: ${mak:count()}/${mak:maxCount()}<br/>
</mak:list>
<b>Languages</b> list had ${mak:lastCount()} items<br/>

</body>
</html>
