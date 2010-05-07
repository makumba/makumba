package org.makumba.analyser;

import java.io.Serializable;
import java.util.ArrayList;

import org.makumba.analyser.engine.SyntaxPoint;

/**
 * Data for an EL value or method
 * 
 * @author Manuel Gay
 * @version $Id: ELData.java,v 1.1 Jan 22, 2010 6:44:26 PM manu Exp $
 */
public class ELData extends ElementData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String expression;

    private ArrayList<String> arguments;

    public ELData(String expression, SyntaxPoint start, SyntaxPoint end) {
        this.expression = expression;
        this.sourceSyntaxPoints = start.getSourceSyntaxPoints();
        this.startLine = start.getLine();
        this.startColumn = start.getColumn();
        this.endLine = end.getLine();
        this.endColumn = end.getColumn();
    }

    public ELData(String expression, ArrayList<String> arguments, SyntaxPoint start, SyntaxPoint end) {
        this(expression, start, end);
        this.arguments = arguments;
    }

    public String getExpression() {
        return expression;
    }

    public ArrayList<String> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "Expression " + expression + " on " + getLocation()
                + (arguments.isEmpty() ? "" : ", arguments: " + arguments);
    }
}
