<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak"%>
<html>
<head><title>Test OQL Functions</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

p.youngestGroupMemberNoParentheses()<br>
<mak:list from="test.Person p">
  ${mak:count()} - <mak:value expr="p.indiv.name"/>, <mak:value expr="p.indiv.name"/>, <mak:value expr="p.youngestGroupMemberNoParentheses()"/> <br>
</mak:list>

<br><br>

p.youngestGroupMember()<br>
<mak:list from="test.Person p">
  ${mak:count()} - <mak:value expr="p.indiv.name"/>, <mak:value expr="p.indiv.name"/>, <mak:value expr="p.youngestGroupMember()"/> <br>
</mak:list>

<br><br>
