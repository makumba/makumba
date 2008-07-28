<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<%@include file="menu.jsp" %>

<mak:object from="organisation.Partner p" where="p=$partner">
   <h2>Edit <mak:value expr="p.name"/></h2>
   <mak:editForm object="p" action="partnerList.jsp">

      City: <mak:input field="city"/> <br>
      Country: <mak:input field="country"/>
        <a href="countryNew.jsp">Add countries</a><br>
        
      Home Page: <mak:input field="homepage"/>
      <p>
      Email: <mak:input field="email"/><br>
      Phone: <mak:input field="phone"/><br>
      Comment: <mak:input field="comment"/><br>
      <input type="submit" value="Save">
   </mak:editForm>

   <mak:deleteLink object="p" action="partnerList.jsp">Delete this</mak:deleteLink>

</mak:object>

<hr>
[<a href="partnerList.jsp">Back to list</a>]
