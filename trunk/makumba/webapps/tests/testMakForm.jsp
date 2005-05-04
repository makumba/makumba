<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:object from="test.Person p" where="p.indiv.name='john'">
    testMakFormStart!<mak:form action="testMakAddForm.jsp" method="post">!endMakFormStart
        testCharInput!<mak:input name="name" dataType="char"/>!endCharInput
        testChar[]InputValue!<mak:input name="nameValue" value="p.indiv.name" dataType="char[40]" />!endChar[]InputValue
        testIntInput!<mak:input name="uniqInt" dataType="int"/>!endIntInput
        testIntInputValue!<mak:input name="uniqIntValue" value="p.uniqInt" dataType="int"/>!endIntInputValue
        testDateInput!<mak:input name="birthdate" dataType="date"/>!endDateInput
        testDateInputValue!<mak:input name="birthdateValue" value="p.birthdate" dataType="date"/>!endDateInputValue
        testTextInput!<mak:input name="comment" dataType="text"/>!endTextInput
        testTextInputValue!<mak:input name="commentValue" value="p.comment" dataType="text"/>!endTextInputValue        
        <input type="submit">
    testMakFormEnd!</mak:form>!endMakFormEnd
</mak:object>

</body>
</html>