package org.makumba.controller.html;

import org.makumba.view.jsptaglib.MakumbaJspAnalyzer;

/**
 * This interface shall be implemented by classes providing a calendar-choser/popup using java script.<br>
 * There are many different java-script solutions around, and different flavours can be provided through this interface.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public interface CalendarEditorProvider {
    /**
     * Write the code to display the calendar, and to connect it to the input boxes of the Makumba-internal
     * {@link dateEditor}. This method has to provide all needed java-script callse.
     */
    public StringBuffer formatEditorCode(String inputName, String calendarLinkFormatting);

    /**
     * Return an array of file names & paths, starting from the context-path, to libraries that shall be included.
     * Makumba will check via page analysis {@link MakumbaJspAnalyzer} if the libraries are already included by the
     * programmer, and add them if needed.
     */
    public String[] getNeededJavaScriptFileNames();

}
