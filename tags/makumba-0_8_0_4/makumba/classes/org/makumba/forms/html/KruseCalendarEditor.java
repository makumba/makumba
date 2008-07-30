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
        String calendarName = inputName + "_" + formIdentifier + "_Calendar";
        String anchorname = inputName + "_" + formIdentifier + "_Anchor";
        String divname = inputName + "_" + formIdentifier + "_makCalendarDiv";

        // TODO: elementById is not necessarily unique, if you have the same field in different forms ?
        // (not sure, maybe it is unique?)
        // therefore, maybe better would be to know the form index, and then use forms[index].name ..
        String dayInput = "document.getElementById('" + inputName + "_0" + "_" + formIdentifier + "')";
        String monthInput = "document.getElementById('" + inputName + "_1" + "_" + formIdentifier + "')";
        String yearInput = "document.getElementById('" + inputName + "_2" + "_" + formIdentifier + "')";

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
        sb.append(calendarName + ".setReturnFunction(\"setMultipleValues" + inputName + "_" + formIdentifier + "\");\n");
        sb.append("function setMultipleValues" + inputName + "_" + formIdentifier + "(y,m,d) {\n");
        sb.append("  " + dayInput + ".selectedIndex=LZ(d)-1;\n");
        sb.append("  " + monthInput + ".selectedIndex=LZ(m)-1;\n");
        sb.append("  " + yearInput + ".value=y;\n");
        sb.append("};\n");
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
        return new String[] { "kruseAnchorPosition.js", "kruseCalendarPopup.js", "kruseDateFunctions.js",
                "krusePopupWindow.js", "makumbaDateFunctions.js" };
    }

}
