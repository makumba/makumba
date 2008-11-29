<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Language list</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<b>Languages:</b>
<br><br><br>

<mak:object from="test.Person p, p.indiv i" where="i.name='bart'">
<mak:list from="p.speaks language"/>
lastCountLanguageExternalSet:${mak:lastCount()}<br>

languagesExternalSet:<mak:list from="p.speaks language" id="1"><mak:value expr="language"/></mak:list>
lastCountLanguageExternalSetWithValue:${mak:lastCount()}<br>

<mak:list from="p.toys t"/>
lastCountToysSetComplex:${mak:lastCount()}<br>

toysSetComplex:<mak:list from="p.toys t" id="2"><mak:value expr="t"/></mak:list>
lastCountToysSetComplexWithValue:${mak:lastCount()}<br>


</mak:object>
</body>
</html>