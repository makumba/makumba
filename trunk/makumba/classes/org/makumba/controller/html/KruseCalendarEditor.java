package org.makumba.controller.html;

import org.makumba.MakumbaSystem;

/**
 * Implements a calendar editor via a chose pop-up using the library of Matt Kurse - <a
 * href="http://www.mattkruse.com/javascript/calendarpopup/">http://www.mattkruse.com/javascript/calendarpopup/</a>
 */
public class KruseCalendarEditor implements CalendarEditorProvider {

    public static final String calendarEditorClass = "calendarEditor";

    private static CalendarEditorProvider singleton;

    public StringBuffer formatEditorCode(String inputName, String calendarLinkFormatting) {
        StringBuffer sb = new StringBuffer();
        MakumbaSystem.getCalendarProvider();
        sb.append("\n<script language=\"javascript\" type=\"text/javascript\">\n");
        String calendarName = inputName + "Calendar";
        String anchorname = inputName + "Anchor";
        String divname = "makCalendarDiv";
        sb.append("var " + calendarName + " = new CalendarPopup(\"" + divname + "\");\n");
        sb.append(calendarName + ".showYearNavigation();\n");
        sb.append(calendarName + ".setReturnFunction(\"setMultipleValues" + inputName + "\");\n");
        sb.append("function setMultipleValues" + inputName + "(y,m,d) {\n");

        String dayInput = "document.getElementById('" + inputName + "_0')";
        // TODO: elementById is not necessarily unique, if you have the same field in different forms ? (not sure!,
        // maybe it is unique?)
        // therefore, maybe better would be to know the form index, and then use forms[index].name ..
        sb.append("  " + dayInput + ".selectedIndex=LZ(d)-1;\n");
        String monthInput = "document.getElementById('" + inputName + "_1')";
        sb.append("  " + monthInput + ".selectedIndex=LZ(m)-1;\n");
        String yearInput = "document.getElementById('" + inputName + "_2')";
        sb.append("  " + yearInput + ".value=y;\n");
        sb.append("};\n");
        sb.append("</script>\n");
        String dateString = "getDateString(" + yearInput + ", " + monthInput + ", " + dayInput + ")";
        String linkText = calendarLinkFormatting != null ? calendarLinkFormatting : " ";
        // TODO: this will default to 1/1/1900, maybe we should default to today?
        sb.append("<a class=\"" + calendarEditorClass + "\" href=\"#\" onClick=\"" + calendarName + ".showCalendar('"
                + anchorname + "', " + dateString + "); return false;\" title=\"Pick date in calendar\" name=\""
                + anchorname + "\" ID=\"" + anchorname + "\">" + linkText + "</a>\n");
        sb.append("<div id=\""
                + divname
                + "\" style=\"position:absolute; visibility:hidden; background-color:white; layer-background-color:white;\"></div>\n");

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
