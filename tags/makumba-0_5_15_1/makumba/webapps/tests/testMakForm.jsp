<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>


testMakFormStart!<mak:form action="testMakAddForm.jsp" method="post">!endMakFormStart
	testCharInput!<mak:input name="name" dataType="char"/>!endCharInput
	testIntInput!<mak:input name="uniqInt" dataType="int"/>!endIntInput
	testDateInput!<mak:input name="birthdate" dataType="date"/>!endDateInput
	testTextInput!<mak:input name="comment" dataType="text"/>!endTextInput
    testSetInput!<mak:input name="language" dataType="set test.Language"/>!endSetInput
    testPtrInput!<mak:input name="brother" dataType="ptr test.Person"/>!endPtrInput
	<input type="submit">
testMakFormEnd!</mak:form>!endMakFormEnd


<mak:object from="test.Person p" where="p.indiv.name='john'">
    testMakFormValueStart!<mak:form action="testMakAddForm.jsp" method="post">!endMakFormValueStart
        testChar[]InputValue!<mak:input name="nameValue" value="p.indiv.name" dataType="char[40]" />!endChar[]InputValue
        testIntInputValue!<mak:input name="uniqIntValue" value="p.uniqInt" dataType="int"/>!endIntInputValue
        testDateInputValue!<mak:input name="birthdateValue" value="p.birthdate" dataType="date"/>!endDateInputValue
        testTextInputValue!<mak:input name="commentValue" value="p.comment" dataType="text"/>!endTextInputValue
        testPtrInputValue!<mak:input name="brotherValue" value="p.brother" dataType="ptr test.Person"/>!endPtrInputValue
    testMakFormValueEnd!</mak:form>!endMakFormValueEnd
</mak:object>

</body>
</html>