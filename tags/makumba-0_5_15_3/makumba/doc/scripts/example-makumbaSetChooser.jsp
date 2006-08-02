<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
  <meta name="Author" content="Adler">
  <title>Example of JavaScript set choosers with Makumba</title>
  <script type="text/javascript" src="makumbaSetChooser.js"></script>
</head>
<body>

<!-- begin: example -->
<form action="#" method="get" name="myForm">
<table><tr>
<td>
<select multiple size="5" name="selectFrom"></select>
</td><td>
<input type="button" onClick="move(this.form.selectFrom,this.form.field1)" value="&raquo; add &raquo;">
<br><br>
<input type="button" onClick="move(this.form.field1,this.form.selectFrom)" value="&laquo; remove &laquo;">
</td><td>
<!-- select to -->
<!-- replace this select by the mak:input -->
<SELECT MULTIPLE NAME="field1" SIZE="5" >
<OPTION>Item 1</OPTION>
<OPTION>Item 2</OPTION>
<OPTION SELECTED>Item 3</OPTION>
<OPTION>Item 4</OPTION>
<OPTION>Item 5</OPTION>
</SELECT>
</td><td>
<input type="button" value="Move up" onclick="Moveup(this.form.field1)">
<br><br>
<input type="button" value="Move down" onclick="Movedown(this.form.field1)">
</td><td>
</tr></table>
<input type="submit" value="Submit" onClick="javascript:highlightAll(this.form.field1);">
</form>
<!-- code that initialises the select boxes -->
<SCRIPT LANGUAGE="JavaScript">
<!-- //Begin
moveNotSelected(document.forms['myForm'].fieldName,document.forms['myForm'].selectFrom)
//  End -->
</script>
<!-- end: example -->

</body>
</html>
