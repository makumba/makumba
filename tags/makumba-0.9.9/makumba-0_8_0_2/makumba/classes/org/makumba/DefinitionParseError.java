package org.makumba;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class provides basic support for definition parse errors.
 * 
 * @author Rudolf Mayer
 * @version $Id: DefinitionParseError.java,v 1.1 Sep 16, 2007 11:11:38 PM rudi Exp $
 */
public abstract class DefinitionParseError extends MakumbaError {

    private static final long serialVersionUID = 1L;

    /** put a marker for a given column */
    public static StringBuffer pointError(int column) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < column; i++) {
            sb.append(' ');
        }
        return sb.append('^');
    }

    protected static String showTypeName(String typeName) {
        if (typeName.startsWith("temp"))
            return "";
        return typeName + ":";
    }

    protected int column;

    protected Vector components;

    protected String line;

    protected Hashtable lines;

    protected String typeName;

    public DefinitionParseError() {
        super();
    }

    /** Construct a message for a line */
    public DefinitionParseError(String typeName, String reason, String line) {
        this(showTypeName(typeName) + reason + "\n" + line);
        this.typeName = typeName;
        this.line = line;
    }

    public DefinitionParseError(String explanation) {
        super(explanation);
    }

    public DefinitionParseError(Throwable reason) {
        super(reason);
    }

    public DefinitionParseError(Throwable reason, String expl) {
        super(reason, expl);
    }

    /** add another error to the main error */
    public void add(DefinitionParseError e) {
        if (components == null) {
            components = new Vector();
        }

        components.addElement(e);
        if (e.line != null) {
            if (lines == null) {
                lines = new Hashtable();
            }
            lines.put(e.line, e);
        }
    }

    /** If the error is single, call the default action, else compose all components' messages */
    public String getMessage() {
        if (isSingle()) {
            return super.getMessage();
        }

        StringBuffer sb = new StringBuffer();

        for (Enumeration e = components.elements(); e.hasMoreElements();) {
            DefinitionParseError error = (DefinitionParseError) e.nextElement();
            sb.append('\n').append(error.getMessage()).append('\n');
        }
        return sb.toString();
    }

    /** tells whether this error is empty or contains sub-errors */
    public boolean isSingle() {
        return components == null || components.isEmpty();
    }

    /** return the type for which the error occured */
    public String getTypeName() {
        return typeName;
    }

}