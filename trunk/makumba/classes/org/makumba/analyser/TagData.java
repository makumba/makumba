package org.makumba.analyser;

import java.io.Serializable;
import java.util.Map;

import org.makumba.analyser.engine.JspParseData;
import org.makumba.analyser.engine.SourceSyntaxPoints;
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

    int startLine, startColumn, endLine, endColumn;
    
    protected SourceSyntaxPoints sourceSyntaxPoints;

    public static final String TAG_DATA_CACHE = "org.makumba.tagData";

    public TagData(String name, SyntaxPoint start, SyntaxPoint end, Map<String, String> attributes) {
        this.name=name;
        this.sourceSyntaxPoints= start.getSourceSyntaxPoints();
        this.startLine=start.getLine();
        this.startColumn= start.getColumn();
        this.endLine= end.getLine();
        this.endColumn= end.getColumn();
        this.attributes=attributes;
    }

    public Object getTagObject() {
        return tagObject;
    }

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

    
}