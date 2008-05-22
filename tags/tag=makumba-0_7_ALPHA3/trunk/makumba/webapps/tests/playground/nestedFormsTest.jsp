<%@taglib uri="http://www.makumba.org/presentation" prefix="mak" %>

<mak:list from="test.Language l">

Start of a mak:form<br>
  <mak:form action="nestedFormsTest.jsp" name="form">
  
    <mak:editForm object="l" name="editForm"> 
      mak:editForm input: <mak:input field="name" /><br>
    </mak:editForm>
    
    <mak:newForm type="test.Language" name="newForm">
      mak:newForm input: <mak:input field="name" /><br>
    </mak:newForm>
    <br>
    <input type="submit">

  </mak:form>
End of mak:form<br><br>  
  
  
</mak:list>
  
 