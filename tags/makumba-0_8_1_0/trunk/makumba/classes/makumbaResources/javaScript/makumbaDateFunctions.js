/** $Id: AllowedException.java 1049 2005-06-25 13:16:52Z rosso_nero $ */
// returns a date as String, parsed from 3 makumba input controls
function getDateString(y_obj, m_obj, d_obj) {
  var y = y_obj.value;
  var m = parseInt(m_obj.options[m_obj.selectedIndex].value) + 1;
  var d = d_obj.options[d_obj.selectedIndex].value;
  if (y=="" || m=="") { return null; }
  if (d=="") { d=1; }
  return str= y+'-'+m+'-'+d;
}

// sets the day, month and year values to a makumba date input identified by the inputName and formIdentifier
// called e.g. by kruseCalendarPopup.js
function setMultipleValues(inputName, formIdentifier, y, m, d) {
  document.getElementById(inputName + "_0" + formIdentifier).selectedIndex=LZ(d)-1;
  document.getElementById(inputName + "_1" + formIdentifier).selectedIndex=LZ(m)-1;
  document.getElementById(inputName + "_2" + formIdentifier).value=y;
}
