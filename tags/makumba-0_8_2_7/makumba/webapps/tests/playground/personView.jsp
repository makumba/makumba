<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

${person}<br>
${param.person}<br>
response:<mak:response/><br>

<%-- Makumba Generator - START OF  *** OBJECT ***  PAGE FOR OBJECT test.Person --%>
<mak:object from="test.Person person" where="person=$person">
  <fieldset style="text-align:right;">
  <legend>Person <i><mak:value expr="person.indiv" /></i></legend
  <table>
  <%-- Makumba Generator - START OF NORMAL FIELDS --%>
    <tr>
      <th>birthdate</th>
      <td><mak:value expr="person.birthdate"/></td>
    </tr>
    <tr>
      <th>firstSex</th>
      <td><mak:value expr="person.firstSex"/></td>
    </tr>
    <tr>
      <th>beginDate</th>
      <td><mak:value expr="person.beginDate"/></td>
    </tr>
    <tr>
      <th>gender</th>
      <td><mak:value expr="person.gender"/></td>
    </tr>
    <tr>
      <th>field</th>
      <td><mak:value expr="person.field"/></td>
    </tr>
    <tr>
      <th>militarySucksAndEverybodyKnowsItButDoesNotSpeakOutLoudAboutIt</th>
      <td><mak:value expr="person.militarySucksAndEverybodyKnowsItButDoesNotSpeakOutLoudAboutIt"/></td>
    </tr>
    <tr>
      <th>driver</th>
      <td><mak:value expr="person.driver"/></td>
    </tr>
    <tr>
      <th>age</th>
      <td><mak:value expr="person.age"/></td>
    </tr>
    <tr>
      <th>makumbaTillDeath</th>
      <td><mak:value expr="person.makumbaTillDeath"/></td>
    </tr>
    <tr>
      <th>designer</th>
      <td><mak:value expr="person.designer"/></td>
    </tr>
    <tr>
      <th>weight</th>
      <td><mak:value expr="person.weight"/></td>
    </tr>
    <tr>
      <th>length</th>
      <td><mak:value expr="person.length"/></td>
    </tr>
    <tr>
      <th>hobbies</th>
      <td><mak:value expr="person.hobbies"/></td>
    </tr>
    <tr>
      <th>comment</th>
      <td><mak:value expr="person.comment"/></td>
    </tr>
    <tr>
      <th>picture</th>
      <td><mak:value expr="person.picture"/></td>
    </tr>
    <tr>
      <th>favouriteToy</th>
      <td><mak:value expr="person.favouriteToy"/></td>
    </tr>
    <tr>
      <th>brother</th>
      <td><mak:value expr="person.brother"/></td>
    </tr>
    <tr>
      <th>uniqInt</th>
      <td><mak:value expr="person.uniqInt"/></td>
    </tr>
    <tr>
      <th>uniqChar</th>
      <td><mak:value expr="person.uniqChar"/></td>
    </tr>
    <tr>
      <th>uniqDate</th>
      <td><mak:value expr="person.uniqDate"/></td>
    </tr>
    <tr>
      <th>speaks</th>
      <td><mak:value expr="person.speaks"/></td>
    </tr>
    <tr>
      <th>email</th>
      <td><mak:value expr="person.email"/></td>
    </tr>
    <tr>
      <th>groupMembers</th>
      <td><mak:value expr="person.groupMembers"/></td>
    </tr>
    <tr>
      <th>uniqPtr</th>
      <td><mak:value expr="person.uniqPtr"/></td>
    </tr>
  </table>
</fieldset>
  <%-- Makumba Generator - END OF NORMAL FIELDS --%>

  <%-- Makumba Generator - START OF SETS --%>

  <%-- Makumba Generator - START LIST FIELD address --%>
  <fieldset style="text-align:right;">
  <legend>Address</legend>
  <table>
    <tr>
      <th>description</th>
      <th>streetno</th>
      <th>zipcode</th>
      <th>city</th>
      <th>country</th>
      <th>phone</th>
      <th>fax</th>
      <th>email</th>
      <th>usagestart</th>
      <th>usageend</th>
    </tr>
    <mak:list from="person.address address">
      <tr>
        <td><mak:value expr="address.description"/></td>
        <td><mak:value expr="address.streetno"/></td>
        <td><mak:value expr="address.zipcode"/></td>
        <td><mak:value expr="address.city"/></td>
        <td><mak:value expr="address.country"/></td>
        <td><mak:value expr="address.phone"/></td>
        <td><mak:value expr="address.fax"/></td>
        <td><mak:value expr="address.email"/></td>
        <td><mak:value expr="address.usagestart"/></td>
        <td><mak:value expr="address.usageend"/></td>
      </tr>
    </mak:list>
  </table>
</fieldset>
  <%-- Makumba Generator - END LIST FOR FIELD address --%>

  <%-- Makumba Generator - START LIST FIELD toys --%>
  <fieldset style="text-align:right;">
  <legend>Toys</legend>
  <table>
    <tr>
      <th>name</th>
    </tr>
    <mak:list from="person.toys toys">
      <tr>
        <td><mak:value expr="toys.name"/></td>
      </tr>
    </mak:list>
  </table>
</fieldset>
  <%-- Makumba Generator - END LIST FOR FIELD toys --%>

  <%-- Makumba Generator - END OF SETS --%>

</table>
</fieldset>
</mak:object>

<%-- Makumba Generator - END OF *** OBJECT ***  PAGE FOR OBJECT test.Person --%>
