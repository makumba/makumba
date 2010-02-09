package org.makumba.analyser;

import java.io.Serializable;

import org.makumba.analyser.engine.SyntaxPoint;

/**
 * Data for an EL value or method
 * @author Manuel Gay
 * 
 * @version $Id: ELData.java,v 1.1 Jan 22, 2010 6:44:26 PM manu Exp $
 */
public class ELData extends ElementData implements Serializable {

    public String getExpression() {
        return expression;
    }


    String expression;
    public ELData(String expression, SyntaxPoint start, SyntaxPoint end) {
        this.expression = expression;
        this.sourceSyntaxPoints = start.getSourceSyntaxPoints();
        this.startLine = start.getLine();
        this.startColumn = start.getColumn();
        this.endLine = end.getLine();
        this.endColumn = end.getColumn();
    }
    
    

}
