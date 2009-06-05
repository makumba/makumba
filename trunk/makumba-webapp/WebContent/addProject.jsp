<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Create new project</title>
</head>
<body>
<mak:response/>


<div class="addbox">
<h1>Create new project</h1>
<mak:newForm type="company.Project" action="" method="post" message="Project added">
<label>Name:</label> <mak:input field="name" /><br/>
<label>Leader:</label> <mak:input field="leader" /><br/>
<label></label><input type="submit" name="Create"/>
</mak:newForm>

</div>

</body>
</html>