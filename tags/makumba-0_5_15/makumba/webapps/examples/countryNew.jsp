<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<h2>New country</h2>

<mak:newForm type="general.Country" action="partnerList.jsp">
   Name: <mak:input field="name"/> <br>
   ISO code: <mak:input field="iso2letterCode" size="5"/><br>
   <input type="submit" value="Add">
</mak:newForm>

<hr>
[<a href="partnerList.jsp">Back to list</a>]
