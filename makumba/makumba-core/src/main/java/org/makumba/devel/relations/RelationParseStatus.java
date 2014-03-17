package org.makumba.devel.relations;

import org.makumba.analyser.ElementData;
import org.makumba.analyser.MakumbaJspAnalyzer;
import org.makumba.analyser.ParseStatus;
import org.makumba.commons.MultipleKey;

public class RelationParseStatus extends ParseStatus {

    public void addTag(ElementData td) {
        pageCache.cache(MakumbaJspAnalyzer.TAG_DATA_CACHE, new MultipleKey(new Object[] { td.getStartLine(),
                td.getStartColumn(), td.getEndLine(), td.getEndColumn() }), td);
    }

    public RelationParseStatus() {
        super();
    }

}
