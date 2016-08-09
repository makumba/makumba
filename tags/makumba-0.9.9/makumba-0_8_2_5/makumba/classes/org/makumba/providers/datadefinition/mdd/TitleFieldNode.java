package org.makumba.providers.datadefinition.mdd;

import java.util.Vector;

/**
 * AST node that holds the information about the MDD title field
 * 
 * TODO add support for functions
 * 
 * @author Manuel Gay
 * @version $Id: TitleFieldNode.java,v 1.1 May 3, 2009 10:18:14 PM manu Exp $
 */
public class TitleFieldNode extends MDDAST {
    
    private static final long serialVersionUID = -3717029364503597220L;

    protected MDDNode mdd;
   
    protected int titleType;
    
    protected String functionName;
    
    protected Vector<String> functionArgs = new Vector<String>();
    
    public TitleFieldNode() {
        setType(MDDTokenTypes.TITLEFIELD);
    }
    
    public TitleFieldNode(String[] nameAndArgs) {
        setType(MDDTokenTypes.TITLEFIELD);
        titleType = MDDTokenTypes.FUNCTION;
        functionName = nameAndArgs[0];
        for(int i = 1; i < nameAndArgs.length; i++) {
            if(nameAndArgs[i] != null)
                functionArgs.add(nameAndArgs[i]);
        }

    }
    
}
