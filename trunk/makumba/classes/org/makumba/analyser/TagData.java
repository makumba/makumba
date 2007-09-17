package org.makumba.analyser;

import java.util.Map;

import org.makumba.analyser.engine.JspParseData;
import org.makumba.analyser.engine.SyntaxPoint;

/**
 * A composite object passed to the analyzers.
 * 
 * @author Cristian Bogdan
 */
public class TagData {
    /** The parse data where this TagData was produced */
    public JspParseData parseData;

    /** Name of the tag */
    public String name;

    /** Tag attributes */
    public Map attributes;

    /** Tag object, if one is created by the analyzer */
    public Object tagObject;

    /** The syntax points where the whole thing begins and ends */
    public SyntaxPoint start, end;

    public SyntaxPoint getStart() {
        return start;
    }

    public SyntaxPoint getEnd() {
        return end;
    }

}