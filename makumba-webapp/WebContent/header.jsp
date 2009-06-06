<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="include/style.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" language="javascript" src="include/scripts.js"></script>
<script type="text/javascript" language="javascript" src="include/prototype.js"></script>

<title>Company management</title>
</head>
<mak:response/>

<c:set var="page"><%= request.getParameter("page") %></c:set>


<p>
<c:choose><c:when test="${page == 'home'}" >
<a class="menu_active" href="index.jsp?page=home">[ Home ]</a>
</c:when>
<c:otherwise>
<a class="menu" href="index.jsp?page=home">[ Home ]</a>
</c:otherwise>
</c:choose>

<c:choose><c:when test="${page== 'companies'}" >
<a class="menu_active" href="company.jsp?page=companies">[ Company info ]</a>
</c:when>
<c:otherwise>
<a class="menu" href="company.jsp?page=companies">[ Company info ]</a>
</c:otherwise>
</c:choose>

<c:choose><c:when test= "${page== 'departments'}" >
<a class="menu_active" href="listDepartments.jsp?page=departments">[ Departments ]</a>
</c:when>
<c:otherwise>
<a class="menu" href="listDepartments.jsp?page=departments">[ Departments ]</a>
</c:otherwise>
</c:choose>

<c:choose><c:when test= "${page== 'projects'}" >
<a class="menu_active" href="listProjects.jsp?page=projects">[ Projects ]</a>
</c:when>
<c:otherwise>
<a class="menu" href="listProjects.jsp?page=projects">[ Projects ]</a>
</c:otherwise>
</c:choose>

<c:choose><c:when test="${page== 'myTasks'}" >
<a class="menu_active" href="myTasks.jsp?page=myTasks">[ My Tasks ]</a>
</c:when>
<c:otherwise>
<a class="menu" href="myTasks.jsp?page=myTasks">[ My Tasks ]</a>
</c:otherwise>
</c:choose>
</p>

