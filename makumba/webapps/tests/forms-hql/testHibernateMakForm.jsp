<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/list-hql" prefix="mak" %>
<%@taglib uri="http://www.makumba.org/forms" prefix="form" %>

testMakFormStart!<form:form action="testHibernateMakAddForm.jsp" method="post" clientSideValidation="false">!endMakFormStart
	testCharInput!<form:input name="name" dataType="char"/>!endCharInput
	testIntInput!<form:input name="uniqInt" dataType="int"/>!endIntInput
	testDateInput!<form:input name="birthdate" dataType="date" calendarEditor="false" />!endDateInput
	testTextInput!<form:input name="comment" dataType="text"/>!endTextInput
    testSetInput!<form:input name="language" dataType="set test.Language"/>!endSetInput
    testPtrInput!<form:input name="brother.id" dataType="ptr test.Person"/>!endPtrInput
	<input type="submit">
testMakFormEnd!</form:form>!endMakFormEnd


<mak:object from="test.Person p" where="p.indiv.name='john'">
    testMakFormValueStart!<form:form action="testHibernateMakAddForm.jsp" method="post" clientSideValidation="false">!endMakFormValueStart
        testChar[]InputValue!<form:input name="nameValue" value="p.indiv.name" dataType="char[40]" />!endChar[]InputValue
        testIntInputValue!<form:input name="uniqIntValue" value="p.uniqInt" dataType="int"/>!endIntInputValue
        testDateInputValue!<form:input name="birthdateValue" value="p.birthdate" dataType="date" calendarEditor="false" />!endDateInputValue
        testTextInputValue!<form:input name="commentValue" value="p.comment" dataType="text"/>!endTextInputValue
        testPtrInputValue!<form:input name="brotherValue" value="p.brother.id" dataType="ptr test.Person"/>!endPtrInputValue
    testMakFormValueEnd!</form:form>!endMakFormValueEnd
</mak:object>

</body>
</html>