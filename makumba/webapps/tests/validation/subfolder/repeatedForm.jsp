<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@page import="java.util.Arrays"%>

<mak:response />

A mak list with (independent) edit forms<br/>
The edit forms are almost identical: the input names are the same, the input ids are different, and the __makumba__base__ has a different value.<br/>

<mak:list from="test.Person p">
<mak:editForm object="p" action="../target.jsp" method="get" reloadFormOnError="true" annotation="after">
    E-mail: <mak:input field="email" /><br/>
    <input type="submit" value="Go!" />
</mak:editForm>
</mak:list>