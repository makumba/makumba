<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak"%>
<html>
<head><title>Test OQL Functions</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

p.nameMin2CharsLong()<br>
<mak:list from="test.Person p" where="p.nameMin2CharsLong()">
  ${mak:count()} - <mak:value expr="p.indiv.name"/>, <mak:value expr="p.indiv.name"/>, <mak:value expr="character_length(p.indiv.name)"/> <br>
</mak:list>

<br><br>


p.nameMin4CharsLong()<br>
<mak:list from="test.Person p" where="p.nameMin2CharsLong() AND p.nameMin4CharsLong()">
  ${mak:count()} - <mak:value expr="p.indiv.name"/>, <mak:value expr="p.indiv.name"/>, <mak:value expr="character_length(p.indiv.name)"/> <br>
</mak:list>

<br><br>


p.nameMin3CharsLong()<br>
<mak:list from="test.Person p" where="p.nameMin3CharsLong()">
  ${mak:count()} - <mak:value expr="p.indiv.name"/>, <mak:value expr="p.indiv.name"/>, <mak:value expr="character_length(p.indiv.name)"/> <br>
</mak:list>

<br><br>


p.oldEnough2(): age + 3 > 20<br>
<mak:list from="test.Person p" where="p.oldEnough2(3, 13, 14)">
  ${mak:count()} - <mak:value expr="p.indiv.name"/>, <mak:value expr="p.indiv.name"/>, <mak:value expr="p.age"/> <br>
</mak:list>
