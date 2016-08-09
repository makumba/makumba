<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Testing mak:nextCount() with multiple nested lists</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<b>Persons</b> list will have ${mak:nextCountById('personList')} items<br/>
<mak:list from="test.Person p" id="personList">
  &nbsp; # countPerson: ${mak:count()}/${mak:maxCount()}<br/>
  &nbsp; &nbsp; <b>Languages</b> list will have ${mak:nextCount()} items<br/>
  <mak:list from="test.Language l" id="languageList">
    &nbsp; &nbsp; &nbsp; # countLanguage: ${mak:count()}/${mak:maxCount()}<br/>
    &nbsp; &nbsp; &nbsp; &nbsp; <b>Individual</b> list will have ${mak:nextCountById('indivList')} items<br/>
    <mak:list from="test.Individual i" id="indivList">
      &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; # countIndividual: ${mak:count()}/${mak:maxCount()}<br/>
    </mak:list>
    &nbsp; &nbsp; &nbsp; &nbsp; <b>Individual</b> list had ${mak:lastCount()} items<br/>
    &nbsp; &nbsp; &nbsp; # countLanguage 2: ${mak:count()}/${mak:maxCount()}<br/>
  </mak:list>
  &nbsp; &nbsp; <b>Languages</b> list had ${mak:lastCount()} items<br/>
  &nbsp; # countPerson 2: ${mak:count()}/${mak:maxCount()}<br/>
  <br/>
</mak:list>
<b>Persons</b> list had ${mak:lastCount()} items<br/>
</body>
</html>
