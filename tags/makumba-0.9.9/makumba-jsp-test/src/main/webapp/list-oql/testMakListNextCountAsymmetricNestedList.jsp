<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Testing mak:nextCount() with asymmetricly long nested lists</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<b>Persons</b> list will have ${mak:nextCount()} items<br/>
<mak:list from="test.Person p">
  &nbsp; # countPerson: ${mak:count()}/${mak:maxCount()}<br/>
  &nbsp; &nbsp; <b>Speaks</b> list will have ${mak:nextCount()} items<br/>
  <mak:list from="p.speaks s">
    &nbsp; &nbsp; &nbsp; # countSpeaks: ${mak:count()}/${mak:maxCount()}<br/>
  </mak:list>
  &nbsp; &nbsp; <b>Speaks</b> list had ${mak:lastCount()} items<br/>
  &nbsp; # countPerson 2: ${mak:count()}/${mak:maxCount()}<br/>
  <br/>
</mak:list>
<b>Persons</b> list had ${mak:lastCount()} items<br/>
</body>
</html>
