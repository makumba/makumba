<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak"%>
<html>
<head>
<title>Hello world!</title>
</head>
<body>

<h1>Create a new person</h1>
<mak:newForm type="Person" action="index.jsp">
Name: <mak:input field="name" />
	<br />
Surname: <mak:input field="surname" />
	<mak:submit />
</mak:newForm>

<h1>List of persons</h1>
<mak:list from="Person p">
	<mak:value expr="p.name + ' ' + p.surname" />
	<br />
</mak:list>

<br />
<a href="welcome.jspx">View source</a>
</body>
</html>