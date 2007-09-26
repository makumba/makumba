<% /* $Header$ */ %>
<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Test Bug 784</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<h1>Testing Bug <a href="http://bugs.best.eu.org/cgi-bin/bugzilla/show_bug.cgi?id=784">784</a>.</h1>

<form:response/><br><br>

<form:new type="test.Person" action="">
  Name: <form:input field="indiv.name" /><br>
  Surname: <form:input field="indiv.surname" /><br>
  Makumba till death: <form:input field="makumbaTillDeath" nullOption="-Make your choice-"/><br>
  Gender <form:input field="gender" nullOption=""/><br>
  Driving license <form:input field="driver" nullOption="-Yeah!-"/><br>
  Designer <form:input field="designer" /><br>
  <input type="submit" />
</form:new>

</body>
