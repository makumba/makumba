package org.makumba.devel.relations;

import org.makumba.analyser.TagData;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.ParseStatus;

public class RelationParseStatus extends ParseStatus {
    
    public void addTag(TagData td) {
        pageCache.cache(TagData.TAG_DATA_CACHE, new MultipleKey(new Object[] {td.getStartLine(), td.getStartColumn(), td.getEndLine(), td.getEndColumn()}), td);
    }

    public RelationParseStatus() {
        super();
    }
    
    
    
}
