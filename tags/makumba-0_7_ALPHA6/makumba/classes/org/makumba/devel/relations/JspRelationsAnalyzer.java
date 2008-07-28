package org.makumba.devel.relations;

import org.makumba.analyser.TagData;
import org.makumba.analyser.interfaces.JspAnalyzer;
import org.makumba.commons.MakumbaJspAnalyzer;

/**
 * Simple analyser for the relations of a page. We are interested only in the tags
 * 
 * @author Manuel Gay
 * @version $Id: JspRelationsAnalyzer.java,v 1.1 Apr 12, 2008 5:18:01 PM manu Exp $
 */
public class JspRelationsAnalyzer extends MakumbaJspAnalyzer implements JspAnalyzer {
    
    private JspRelationsAnalyzer() {
        super();
    }
    
    private static JspRelationsAnalyzer singleton;
    
    public static JspRelationsAnalyzer getInstance() {
        if(singleton == null) {
            singleton = new JspRelationsAnalyzer();
        }
        return singleton;
    }

    @Override
    public Object makeStatusHolder(Object initialStatus) {
        return new RelationParseStatus();
    }
    
    @Override
    protected void handleNonMakumbaTags(TagData td, Object status) {
        if(td.name.indexOf("jsp:include") > -1) {
            ((RelationParseStatus)status).addTag(td);
        }
        
    }
    
    @Override
    protected void handleNonMakumbaSystemTags(TagData td, Object status) {
        if(td.name.indexOf("include") > -1) {
            ((RelationParseStatus)status).addTag(td);
        }
    }
    
}