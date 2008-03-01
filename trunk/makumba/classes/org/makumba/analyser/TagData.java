package org.makumba.analyser;

import java.io.Serializable;
import java.util.Map;

import org.makumba.analyser.engine.JspParseData;
import org.makumba.analyser.engine.SyntaxPoint;

/**
 * A composite object passed to the analyzers.
 * 
 * @author Cristian Bogdan
 */
public class TagData implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Name of the tag */
    public String name;
    
    /** Number of the node in the graph of tags of the page **/
    public int nodeNumber;

    /** Tag attributes */
    public Map<String, String> attributes;

    /** Tag object, if one is created by the analyzer */
    public Object tagObject;

    /** The syntax points where the whole thing begins and ends */
    public SyntaxPoint start, end;

    public static final String TAG_DATA_CACHE = "org.makumba.tagData";

    public SyntaxPoint getStart() {
        return start;
    }

    public Object getTagObject() {
        return tagObject;
    }

    public SyntaxPoint getEnd() {
        return end;
    }

}