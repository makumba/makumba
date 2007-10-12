<% /* $Id: /usr/local/cvsroot/karamba/public_html/welcome.jsp,v 2.39 2007/06/28 17:18:22 manu Exp $ */ %>
<jsp:include page="header.jsp" flush="false" />
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<style type="text/css">
td {
  font-size: x-small;
}
th {
  font-size: small;
}
</style>

<h1>List Persons</h1>

<table>
  <tr>
    <th>indiv name</th>
    <th>indiv surname</th>
    <th>gender</th>
    <th>age</th>
    <th>weight</th>
    <th>email</th>
    <th>hobbies</th>
    <th>first Sex</th>
    <th>birth date</th>
    <th>begin Date</th>
    <th>uniq Ptr</th>
    <th>uniq Date</th>
    <th>uniq Int</th>
    <th>uniq Char</th>
    <th>action</th>
  </tr>
  <mak:list from="test.Person p">
    <tr>
      <td><mak:value expr="p.indiv.name" /></td>
      <td><mak:value expr="p.indiv.surname" /></td>
      <td><mak:value expr="p.gender" /></td>
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
      <td><a href="personEdit.jsp?person=<mak:value expr='p'/>">edit</a> <mak:delete object="p" widget="button" action="personList.jsp" method="post">Delete</mak:delete> </td>
    </tr>
  </mak:list>
</table>
</body>
</html>
