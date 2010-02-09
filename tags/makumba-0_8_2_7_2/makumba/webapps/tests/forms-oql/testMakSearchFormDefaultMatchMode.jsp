<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Field types</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<mak:searchForm in="test.Person" name="searchPerson">
  name <mak:criterion fields="indiv.name"> <mak:matchMode matchModes="contains, equals, begins, ends" default="begins"/> <mak:searchField /> </mak:criterion>
  surname <mak:criterion fields="indiv.surname"> <mak:matchMode default="ends" /> <mak:searchField /> </mak:criterion>
  email <mak:criterion fields="email"> <mak:matchMode matchModes="contains, equals, begins, ends"/> <mak:searchField /> </mak:criterion>
  <input type="submit" value="search">
</mak:searchForm>

</body>
</html>