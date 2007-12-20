<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<html>
<head><title>Test @include</title></head>
<body>

<mak:list from="test.Person p" />
<%@include file="/pageIncludeTests/toBeIncluded.jsp" %><br>
<%@include file="../pageIncludeTests/toBeIncluded.jsp" %><br>
<%@include file="/pageIncludeTests/toBeIncluded2.jsp" %><br>


</body>
