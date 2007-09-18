<% /* $Id: /usr/local/cvsroot/karamba/public_html/welcome.jsp,v 2.39 2007/06/28 17:18:22 manu Exp $ */ %>
<jsp:include page="header.jsp" flush="false" />
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


<mak:object from="test.Person p" where="p=$person">
<h1>Edit Person <i><mak:value expr="p.indiv.name" /></i></h1>
<mak:editForm object="p" action="personList.jsp">
  <input name="person" type="hidden" value="<c:out value='${param.person}' />" />
  age: <mak:input name="age" /> <br>
  weight: <mak:input name="weight" /> <br>
  <hr>
  email: <mak:input name="email" /> <br>
  hobbies: <mak:input name="hobbies" /> <br>
  <hr>
  firstSex: <mak:input name="firstSex" /> <br>
  birthdate: <mak:input name="birthdate" /> <br>
  <input type="submit" />
</mak:editForm>
</mak:object>
</body>
</html>
