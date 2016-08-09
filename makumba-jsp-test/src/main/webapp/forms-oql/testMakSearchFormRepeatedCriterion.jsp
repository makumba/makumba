<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head>
<title>Test Search Form with repeated criterions, showing bug 1197 (http://bugs.makumba.org/show_bug.cgi?id=1197)</title>
</head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:searchForm in="test.Person" name="searchPerson" action="testMakSearchFormRepeatedCriterion.jsp" resultLabel="p">
  Language: 
  <mak:criterion fields="speaks">
    <mak:searchField>
      <mak:list from="test.Language l" where="l.name < 'h'">
        <mak:option value="l"> <mak:value expr="l.name" /> </mak:option>
      </mak:list>
    </mak:searchField>
  </mak:criterion>
  <mak:criterion fields="speaks" id="speaks2">
    <mak:searchField>
      <mak:list from="test.Language l" where="l.name >= 'h'">
        <mak:option value="l"> <mak:value expr="l.name" /> </mak:option>
      </mak:list>
    </mak:searchField>
  </mak:criterion>
  <input type="submit" value="search">
</mak:searchForm>

<table id="results">
<mak:resultList resultsFrom="searchPerson" orderBy="#{param.sortby}" mode="filter">
  <tr>
    <td>${mak:count()}</td>
    <td><mak:value expr="p.indiv.name" /> <mak:value expr="p.indiv.surname" /></td>
    <td><mak:value expr="p.birthdate" /></td>
    <td><mak:list from="p.speaks speaks"><mak:value expr="speaks.name"/> </mak:list></td>
  </tr>
</mak:resultList>
</table>

</body>
</html>