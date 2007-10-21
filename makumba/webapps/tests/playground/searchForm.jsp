<% /* $Id: /usr/local/cvsroot/karamba/public_html/welcome.jsp,v 2.39 2007/06/28 17:18:22 manu Exp $ */ %>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<jsp:include page="header.jsp" flush="false" />

<table border="0">
  <mak:searchForm in="test.Person" name="searchArchive" method="get">
    <tr>
      <th>name</th> 
      <td>
        <mak:criterion fields="indiv.name, indiv.surname">
          <mak:matchMode matchModes="contains, equals, begins, ends" type="radio" /><br>
          <mak:searchField /> 
        </mak:criterion>
      </td>
    </tr>
    <tr>
      <th>language</th> 
      <td>
        <mak:criterion fields="uniqPtr"> <mak:searchField nullOption="any" /> </mak:criterion>
      </td>
    </tr>
    <tr>
      <th>gender</th> 
      <td>
        <mak:criterion fields="gender"> <mak:searchField nullOption="any" type="tickbox" /> </mak:criterion>
      </td>
    </tr>
    <tr>
      <th>designer</th> 
      <td>
        <mak:criterion fields="designer"> <mak:searchField nullOption="any" type="tickbox" /> </mak:criterion> 
      </td>
    </tr>
    <tr>
      <th>birthday</th> 
      <td>
        <mak:criterion fields="birthdate">
          <mak:matchMode matchModes="equals, before, after" /> 
          <mak:searchField /> 
        </mak:criterion>
      </td>
    </tr>
    <tr>
      <th>firstSex</th> 
      <td>
        <mak:criterion fields="firstSex" isRange="true">
          <mak:matchMode matchModes="between, betweenInclusive" /> 
          <mak:searchField role="rangeBegin" /> 
          <mak:searchField role="rangeEnd" /> 
        </mak:criterion>
      </td>
    </tr>
    <td colspan="2" align="center"><input type="submit"></td>
  </mak:searchForm>

</table>
<br/>
<%--results:<br>
done: ${searchArchiveDone}<br>
from: ${searchArchiveVariableFrom}<br>
--%>
where: ${searchArchiveWhere}<br>
<br>

<c:if test="${searchArchiveDone}">
  <table width="100%">
    <tr>
      <th>indiv name</th>
      <th>indiv surname</th>
      <th>gender</th>
      <th>age</th>
      <th>weight</th>
      <th>email</th>
      <th>hobbies</th>
      <th>birth date</th>
      <th>begin Date</th>
      <th>uniq Ptr</th>
      <th>uniq Date</th>
      <th>uniq Int</th>
      <th>uniq Char</th>
      <th>action</th>
    </tr>

    <mak:list from="test.Person o" variableFrom="#{searchArchiveVariableFrom}" where="#{searchArchiveWhere}">
      <tr>
        <td><mak:value expr="o.indiv.name" /></td>
        <td><mak:value expr="o.indiv.surname" /></td>
        <td><mak:value expr="o.gender" /></td>
        <td><mak:value expr="o.age" /></td>
        <td><mak:value expr="o.weight" />
        <td><mak:value expr="o.email" />
        <td><mak:value expr="o.hobbies" />
        <td><mak:value expr="o.birthdate" />
        <td><mak:value expr="o.beginDate" />
        <td><mak:value expr="o.uniqPtr" />
        <td><mak:value expr="o.uniqDate" />
        <td><mak:value expr="o.uniqInt" />
        <td><mak:value expr="o.uniqChar" />
        <td><a href="personEdit.jsp?person=<mak:value expr='o'/>">edit</a> <mak:delete object="o" widget="button" action="personList.jsp" method="post" style="display:inline">Delete</mak:delete> </td>
      </tr>
    </mak:list>
  </table>
</c:if>
</body>
<html>