<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<jsp:include page="/layout/header.jsp?pageTitle=New+user" flush="false"/>

<h1>Log in!</h1>

<form id="logMeIn" action="logMeIn" method="post">
	<input type='text' id='user' class='text' name='j_username' value='username'
	    onblur="if(this.value=='') this.value='username';" onfocus="if(this.value=='username') this.value='';" />
	<input type='password' id='pass' class='text' name='j_password' value='password'
	    onblur="if(this.value=='') this.value='password';" onfocus="if(this.value=='password') this.value='';" />
	<input type='submit' class='submit' value='Login' />
</form>

<jsp:include page="/layout/footer.jsp" flush="false"/>
