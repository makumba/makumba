<% /* $Header$ */ %>
<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test Bug 784</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<h1>Testing Bug <a href="http://bugs.best.eu.org/cgi-bin/bugzilla/show_bug.cgi?id=784">784</a>.</h1>

<mak:response/><br><br>

<mak:newForm type="test.Person" action="">
  Name: <mak:input field="indiv.name" /><br>
  Surname: <mak:input field="indiv.surname" /><br>
  Makumba till death: <mak:input field="makumbaTillDeath" nullOption="-Make your choice-"/><br>
  Gender <mak:input field="gender" nullOption=""/><br>
  Driving license <mak:input field="driver" nullOption="-Yeah!-"/><br>
  Designer <mak:input field="designer" /><br>
  <input type="submit" />
</mak:newForm>

</body>
