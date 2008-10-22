      <tr>
        <td class="smallText"><mak:value expr="o.indiv.name" /></td>
        <td class="smallText"><mak:value expr="o.indiv.surname" /></td>
        <td class="smallText"><mak:value expr="o.indiv.person" /></td>
        <td class="smallText"><mak:value expr="o.gender" /></td>
        <td class="smallText"><mak:value expr="o.age" /></td>
        <td class="smallText"><mak:value expr="o.weight" /></td>
        <td class="smallText"><mak:value expr="o.email" /></td>
        <td class="smallText"><mak:value expr="o.hobbies" /></td>
        <td class="smallText"><mak:value expr="o.firstSex" /></td>
        <td class="smallText"><mak:value expr="o.birthdate" /></td>
        <td class="smallText"><mak:value expr="o.beginDate" /></td>
        <td class="smallText"><mak:value expr="o.uniqPtr.name" /> (<mak:value expr="o.uniqPtr" />)</td>
        <td class="smallText"><mak:value expr="o.uniqDate" /></td>
        <td class="smallText"><mak:value expr="o.uniqInt" /></td>
        <td class="smallText"><mak:value expr="o.uniqChar" /></td>
        <td class="smallText"><mak:value expr="o.speaks" /></td>
        <td class="smallText"><a href="personEdit.jsp?person=<mak:value expr='o'/>">edit</a> <mak:delete object="o" widget="button" action="personList.jsp" method="post" style="display:inline">Delete</mak:delete> </td>
      </tr>
