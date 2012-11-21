<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Language list</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

Test 1, simple<br>
<mak:list from="test.Language l" limit="2">
  countLanguage:${mak:count()}; maxCountLanguage:${mak:maxCount()}; maxResultsLanguage:${mak:maxResults()}<br>
</mak:list>

Test 2, empty nested list<br>
<mak:list from="test.Language l" limit="2" id="2">
  countLanguage:${mak:count()}; maxCountLanguage:${mak:maxCount()}; maxResultsLanguage:${mak:maxResults()}<br>
  <mak:list from="test.Person p" id="person2" />
</mak:list>

Test 3 pre-work, checking how many persons when not nested (should be same as below!)<br>
<mak:list from="test.Person p" id="person3_pre" >
  countPerson:${mak:count()}; maxCountPerson:${mak:maxCount()}; maxResultsPerson:${mak:maxResults()}<br>
</mak:list>

Test 3 pre-work #2, checking how many persons when not nested, using limit (should be same as below!)<br>
<mak:list from="test.Person p" limit="1" id="person3_pre2" >
  countPerson:${mak:count()}; maxCountPerson:${mak:maxCount()}; maxResultsPerson:${mak:maxResults()}<br>
</mak:list>

Test 3, nested list using maxResults<br>
<mak:list from="test.Language l" limit="2" id="3">
  countLanguage:${mak:count()}; maxCountLanguage:${mak:maxCount()}; maxResultsLanguage:${mak:maxResults()}<br>
  <mak:list from="test.Person p" id="person3" >
    countPerson:${mak:count()}; maxCountPerson:${mak:maxCount()}; maxResultsPerson:${mak:maxResults()}<br>
  </mak:list>
   maxResultsLanguageAfterList:${mak:maxResults()}<br>
</mak:list>

</body>
</html>