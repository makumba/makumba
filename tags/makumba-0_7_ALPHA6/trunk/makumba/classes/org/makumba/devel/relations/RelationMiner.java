package org.makumba.devel.relations;

import java.util.logging.Logger;

/**
 * A RelationMiner extracts relations from a file
 * 
 * @author Manuel Gay
 * @version $Id: RelationMiner.java,v 1.1 Apr 19, 2008 8:35:58 PM manu Exp $
 */
public abstract class RelationMiner {
    
    protected Logger logger = Logger.getLogger("org.makumba.relationCrawler");
    
    protected RelationCrawler rc;
    
    public abstract void crawl(String path);
    
    public RelationMiner(RelationCrawler rc) {
        this.rc = rc;
    }

}
