<%@page contentType="text/html"%>
<%@page pageEncoding="utf-8"%>
<html>
<head><title>Testing count function in multi-level nested lists</title></head>
<body>

<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>
<%@taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<mak:list from="test.Language l">
  <div style="position: relative; background-color: silver; " >
  countLanguage: ${mak:count()}<br>
  maxCountLanguage: ${mak:maxCount()}<br>
  <mak:list from="test.Person p">
    <div style="left: 40px; position: relative; background-color: olive" >
    nestedCountPerson: ${mak:count()}<br>
    nestedMaxCountPerson: ${mak:maxCount()}<br>
    <mak:list from="test.Individual i">
      <div style="left: 40px; position: relative; background-color: cyan" >
      nested^2CountIndividual: ${mak:count()}<br>
      nested^2MaxCountIndividual: ${mak:maxCount()}<br>
      <mak:list from="test.Person p2, p2.address a">
       <mak:value expr="a" printVar="a"/> <%-- to avoid bug http://bugs.makumba.org/show_bug.cgi?id=1201 --%>
        <div style="left: 40px; position: relative; background-color: lime" >
        nested^3CountPersonAddress: ${mak:count()}<br>
        nested^3MaxCountPersonAddress: ${mak:maxCount()}<br>        
        <c:if test="${mak:count()<mak:maxCount()}"><br/></c:if>
        </div>
      </mak:list>
      <span style="position: relative; left: 40px">lastCountPersonAddress: ${mak:lastCount()}<br></span>
      nested^2CountIndividual #2: ${mak:count()}<br>
      nested^2MaxCountIndividual #2: ${mak:maxCount()}<br>
      <c:if test="${mak:count()<mak:maxCount()}"><br/></c:if>
      </div>
    </mak:list>
    <span style="position: relative; left: 40px">lastCountIndividual: ${mak:maxCount()}<br></span>
    nestedCountPerson #2: ${mak:count()}<br>
    nestedMaxCountPerson #2: ${mak:maxCount()}<br>
    <c:if test="${mak:count()<mak:maxCount()}"><br/></c:if>
    </div>
  </mak:list>
  <span style="position: relative; left: 40px">lastCountPerson: ${mak:lastCount()}<br></span>
  countLanguage2: ${mak:count()}<br>
  maxCountLanguage2: ${mak:maxCount()}<br>
  lastCountPerson2: ${mak:lastCount()}<br>
  <c:if test="${mak:count()<mak:maxCount()}"><br/></c:if>
  </div>
</mak:list>
lastCountLanguage: ${mak:lastCount()}<br>
</body>
</html>