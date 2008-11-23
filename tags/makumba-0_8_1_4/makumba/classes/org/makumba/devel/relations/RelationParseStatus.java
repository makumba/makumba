package org.makumba.devel.relations;

import org.makumba.analyser.TagData;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.ParseStatus;

public class RelationParseStatus extends ParseStatus {
    
    public void addTag(TagData td) {
        pageCache.cache(MakumbaJspAnalyzer.TAG_DATA_CACHE, new MultipleKey(new Object[] {td.getStartLine(), td.getStartColumn(), td.getEndLine(), td.getEndColumn()}), td);
    }

    public RelationParseStatus() {
        super();
    }
    
    
    
}
