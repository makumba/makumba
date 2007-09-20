<% /* $Id: /usr/local/cvsroot/karamba/public_html/welcome.jsp,v 2.39 2007/06/28 17:18:22 manu Exp $ */ %>
<jsp:include page="header.jsp" flush="false" />
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


<h1>List Persons</h1>

<table>
  <tr>
    <th>indiv.name</th>
    <th>indiv.surname</th>
    <th>age</th>
    <th>weight</th>
    <th>email</th>
    <th>hobbies</th>
    <th>firstSex</th>
    <th>birthdate</th>
    <th>beginDate</th>
    <th>uniqPtr</th>
    <th>uniqDate</th>
    <th>uniqInt</th>
    <th>uniqChar</th>
    <th>action</th>
  </tr>
  <mak:list from="test.Person p">
    <tr>
      <td><mak:value expr="p.indiv.name" /></td>
      <td><mak:value expr="p.indiv.surname" /></td>
      <td><mak:value expr="p.age" /></td>
      <td><mak:value expr="p.weight" />
      <td><mak:value expr="p.email" />
      <td><mak:value expr="p.hobbies" />
      <td><mak:value expr="p.firstSex" />
      <td><mak:value expr="p.birthdate" />
      <td><mak:value expr="p.beginDate" />
      <td><mak:value expr="p.uniqPtr" />
      <td><mak:value expr="p.uniqDate" />
      <td><mak:value expr="p.uniqInt" />
      <td><mak:value expr="p.uniqChar" />
      <td><a href="personEdit.jsp?person=<mak:value expr='p'/>">edit</a></td>
    </tr>
  </mak:list>
</table>
</body>
</html>
