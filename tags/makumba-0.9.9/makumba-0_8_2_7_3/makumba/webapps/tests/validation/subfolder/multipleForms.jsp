<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@page import="java.util.Arrays"%>

<mak:response />

A mak list with edit forms nested inside a mak:form<br/>
The edit forms thus are different, they have a specific suffix, which is appended to the input names.<br/>

<mak:form>
  <mak:list from="test.Person p">
    <mak:editForm object="p" action="../target.jsp" method="get" reloadFormOnError="true" annotation="after">
      E-mail: <mak:input field="email" /><br/>
    </mak:editForm>
  </mak:list>
  <input type="submit" value="Go!" />
</mak:form>