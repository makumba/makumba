<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Makumba example welcome page</title>
</head>
<body>
<mak:response/>

<h1>Create new Employee</h1>
<mak:newForm type="company.Employee" action="welcome.jsp">
Name: <mak:input field="name" autoComplete="true"/><br/>
Surname: <mak:input field="surname" autoComplete="true"/><br/>
Birthdate: <mak:input field="birthdate"/><br/>
<input type="submit" name="Create"/>
</mak:newForm>

<br/>

<h1>Existing employees</h1>
<mak:list from="company.Employee p">
<mak:value expr="p.nameSurname()"/> [<mak:deleteLink message="Employee deleted" action="welcome.jsp" object="p">delete</mak:deleteLink>]<br/>
</mak:list>

</body>
</html>