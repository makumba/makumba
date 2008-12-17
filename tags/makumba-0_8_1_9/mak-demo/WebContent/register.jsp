<%@page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<jsp:include page="/layout/header.jsp?pageTitle=New+user" flush="false"/>

<h1>New User</h1>
<mak:response />
<script>
var unElUntouched = true;
function updateUsername(force) {
	var unEl = document.getElementById("username");
  if(unElUntouched || force){
	  var sugUn = document.getElementById("firstName").value+document.getElementById("lastName").value;
    sugUn = sugUn.replace(/ /g,'');
    sugUn = sugUn.toLowerCase();
    unEl.value = sugUn;
  }
}
function stopUpdateUsername() {
	unElUntouched = false;
}
</script>
<mak:newForm type="user.User" action="welcome.jsp" name="user" clientSideValidation="true" method="post" >
  <table>
  <%-- Makumba Generator - START OF NORMAL FIELDS --%>
   <tr class="${even?'even':'odd'}"> <c:set var="even" value="${!even}" />
   <td class="v_head"><label for="firstName"><span class="accessKey">F</span>irst name</label></td>
   <td><mak:input field="firstName" accessKey="f" styleId="firstName" onKeyUp="updateUsername(false);"/></td>
   </tr>
   <tr class="${even?'even':'odd'}"> <c:set var="even" value="${!even}" />
   <td class="v_head"><label for="lastName"><span class="accessKey">L</span>ast name</label></td>
   <td><mak:input field="lastName" accessKey="l" styleId="lastName" onKeyUp="updateUsername(false);"/>
    <input type=button value="suggest username" onclick="updateUsername(true);"/>
   </td>
   </tr>
   <tr class="${even?'even':'odd'}"> <c:set var="even" value="${!even}" />
   <td class="v_head"><label for="username"><span class="accessKey">U</span>sername</label></td>
   <td><mak:input field="username" accessKey="u" styleId="username" onKeyUp="stopUpdateUsername();"/></td>
   </tr>
   <tr class="${even?'even':'odd'}"> <c:set var="even" value="${!even}" />
   <td class="v_head"><label for="password"><span class="accessKey">P</span>assword</label></td>
   <td><mak:input field="password" styleId="password" accessKey="p" type="password" /></td>
   </tr>
   <tr class="${even?'even':'odd'}"> <c:set var="even" value="${!even}" />
   <td class="v_head"><label for="email"><span class="accessKey">E</span>-mail</label></td>
   <td><mak:input field="email" styleId="email" accessKey="e" /></td>
   </tr>
   <tr class="${even?'even':'odd'}"> <c:set var="even" value="${!even}" />
   <td class="v_head"><label for="isAdmin"><span class="accessKey">A</span>dmin rights</label></td>
   <td><mak:input field="isAdmin" styleId="isAdmin" accessKey="a" /></td>
   </tr>
  <%-- Makumba Generator - END OF NORMAL FIELDS --%>
   <tr class="${even?'even':'odd'}"> <c:set var="even" value="${!even}" />
   <td colspan="2">  <input type="submit" value="Add" accessKey="A">  <input type="reset" accessKey="R">  <input type="reset" value="Cancel" accessKey="C" onClick="javascript:back();">  </td>
   </tr>
  </table>
</mak:newForm>


<jsp:include page="/layout/footer.jsp" flush="false"/>
