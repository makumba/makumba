package org.makumba.analyser;

import org.makumba.analyser.engine.SourceSyntaxPoints;

/**
 * @author Manuel Gay
 * @author Rudolf Mayer
 * @version $Id: ElementData.java,v 1.1 7 May 2010 15:48:42 rudi Exp $
 */
public class ElementData {

    private static final long serialVersionUID = 1L;

    protected int startLine;

    protected int startColumn;

    protected int endLine;

    protected int endColumn;

    protected SourceSyntaxPoints sourceSyntaxPoints;

    public int getStartLine() {
        return startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public SourceSyntaxPoints getSourceSyntaxPoints() {
        return sourceSyntaxPoints;
    }

    /** Checks whether this {@link ElementData} is declared before the given {@link ElementData} */
    public boolean before(ElementData el) {
        return endLine < el.getStartLine() || (endLine == el.getStartLine() && endColumn < el.getStartColumn());
    }

    /** Checks whether this {@link ElementData} is declared after the given {@link ElementData} */
    public boolean after(ElementData el) {
        return startLine > el.getEndLine() || (startLine == el.getEndLine() && startColumn > el.getEndColumn());
    }

    public String getLocation() {
        return getStartLine() + ":" + getStartColumn() + " - " + getEndLine() + ":" + getEndColumn();
    }

}
