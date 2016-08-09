<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:object from="organisation.Partner p" where="p=$partner">
    <h2><mak:value expr="p.name"/></h2>

    <mak:value expr="p.city"/>,
      <mak:value expr="p.country.name"/>
    Home Page: <b><mak:value expr="p.homepage"/></b>
    <p>
    You can send them an email to: <mak:value expr="p.email"/><br>
    Call me: <b><mak:value expr="p.phone"/></b><br>
    Comment: <mak:value expr="p.comment"/>
    <p>
    <a href="partnerEdit.jsp?partner=<mak:value expr="p"/>">edit</a>
</mak:object>

<hr>
[<a href="partnerList.jsp">List them all</a>]
