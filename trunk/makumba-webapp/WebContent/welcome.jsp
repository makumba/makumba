<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<mak:response/>

<h1>Create new Person</h1>
<mak:newForm type="general.Person" action="welcome.jsp">
Name: <mak:input field="name" autoComplete="true"/><br/>
<input type="submit" name="Create"/>
</mak:newForm>

<br/>

<h1>Existing persons</h1>
<mak:list from="general.Person p">
<mak:value expr="p.name"/> [<mak:deleteLink object="p" action="welcome.jsp">Trash</mak:deleteLink>]<br/>
</mak:list>

</body>
</html>