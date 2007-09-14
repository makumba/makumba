<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<h2>New partner</h2>

<mak:newForm type="organisation.Partner" action="partnerList.jsp">
   Name: <mak:input name="name"/> <br>

   City: <mak:input name="city"/> <br>
   Country: <mak:input name="country"/> <a href="countryNew.jsp">Add countries</a><br>
   <input type="submit" value="Create">
</mak:newForm>

<hr>
[<a href="partnerList.jsp">Back to list</a>]

