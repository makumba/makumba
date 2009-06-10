package org.makumba.forms.html;

/**
 * Implements a calendar editor via a chose pop-up using the library of Matt Kurse - <a
 * href="http://www.mattkruse.com/javascript/calendarpopup/">http://www.mattkruse.com/javascript/calendarpopup/</a>
 */
public class KruseCalendarEditor implements CalendarEditorProvider {

    public static final String calendarEditorClass = "calendarEditor";

    private static CalendarEditorProvider singleton;

    public StringBuffer formatEditorCode(String inputName, Object formIdentifier, String calendarLinkFormatting) {
        StringBuffer sb = new StringBuffer();
        String inputNameVar = inputName.replace('.', '_'); // escape potential ptrField.someField inputs
        String calendarName = inputNameVar + formIdentifier + "_Calendar";
        String anchorname = inputNameVar + formIdentifier + "_Anchor";
        String divname = inputNameVar + formIdentifier + "_makCalendarDiv";

        String dayInput = "document.getElementById('" + inputName + "_0" + formIdentifier + "')";
        String monthInput = "document.getElementById('" + inputName + "_1" + formIdentifier + "')";
        String yearInput = "document.getElementById('" + inputName + "_2" + formIdentifier + "')";

        String dateString = "getDateString(" + yearInput + ", " + monthInput + ", " + dayInput + ")";
        String linkText = calendarLinkFormatting != null ? calendarLinkFormatting : " ";

        // append the link
        // TODO: this will default to 1/1/1900, maybe we should default to today?
        sb.append("<a class=\"" + calendarEditorClass + "\" href=\"#\" onClick=\"" + calendarName + ".showCalendar('"
                + anchorname + "', " + dateString + "); return false;\" title=\"Pick date in calendar\" name=\""
                + anchorname + "\" ID=\"" + anchorname + "\">" + linkText + "</a>\n");

        // make the div
        sb.append("<div id=\""
                + divname
                + "\" style=\"position:absolute; visibility:hidden; background-color:white; layer-background-color:white;\"></div>\n");

        // write the script
        sb.append("\n<script language=\"javascript\" type=\"text/javascript\">\n");
        sb.append("var " + calendarName + " = new CalendarPopup(\"" + divname + "\");\n");
        sb.append(calendarName + ".showYearNavigation();\n");
        sb.append(calendarName + ".setReturnFunction(\"setMultipleValues" + inputNameVar + formIdentifier + "\");\n");
        sb.append("function setMultipleValues" + inputNameVar + formIdentifier + "(y, m, d) { setMultipleValues('"
                + inputName + "', '" + formIdentifier + "', y, m, d); };\n");
        sb.append("</script>\n");

        return sb;
    }

    public static CalendarEditorProvider getInstance() {
        if (singleton == null) {
            singleton = new KruseCalendarEditor();
        }
        return singleton;
    }

    public String[] getNeededJavaScriptFileNames() {
        return new String[] { "kruseCalendarPopup_combined_compact.js", "makumbaDateFunctions.js" };
    }

}
