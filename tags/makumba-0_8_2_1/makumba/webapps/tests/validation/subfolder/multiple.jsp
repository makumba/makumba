<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@page import="java.util.Arrays"%>

<mak:response />

<mak:list from="test.Person p">
<mak:editForm object="p" action="../target.jsp" method="get" reloadFormOnError="true">
    E-mail: <mak:input field="email" /><br/>
    <input type="submit" value="Go!" />
</mak:editForm>
</mak:list>