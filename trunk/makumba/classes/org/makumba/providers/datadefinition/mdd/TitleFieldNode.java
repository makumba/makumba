package org.makumba.providers.datadefinition.mdd;

/**
 * AST node that holds the information about the MDD title field
 * @author Manuel Gay
 * @version $Id: TitleFieldNode.java,v 1.1 May 3, 2009 10:18:14 PM manu Exp $
 */
public class TitleFieldNode extends MDDAST {
    
    protected MDDNode mdd;
    
    protected int titleType;
    
    public TitleFieldNode() {
        setType(MDDTokenTypes.TITLEFIELD);
    }
    
}
