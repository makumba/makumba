<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Create new project</title>
</head>
<body>
<mak:response/>

<h1>Create new project</h1>
<mak:newForm type="company.Project" action="addProject.jsp">
Name: <mak:input field="name" /><br/>
Leader: <mak:input field="leader" /><br/>
<input type="submit" name="Create"/>
</mak:newForm>

<br/>

<h1>Project list</h1>
<mak:list from="company.Project p">
<mak:value expr="p.name"/> <mak:value expr="p.leader.name"/>
</mak:list>

</body>
</html>