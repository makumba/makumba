<%@ taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@page import="java.util.Arrays"%>

My sweet param: <%=Arrays.toString(request.getParameterValues("myParam")) %>


<mak:response />

<mak:newForm type="test.Person" action="../target.jsp" method="post" reloadFormOnError="true">
    E-mail: <mak:input field="email" /><br/>
    <input type="hidden" name="myParam" value="someValue" />
    <input type="submit" value="Go!" />
</mak:newForm>