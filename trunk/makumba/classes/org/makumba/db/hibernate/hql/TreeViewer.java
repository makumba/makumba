/*
 * Created on 14-Jul-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.db.hibernate.hql;

import org.hibernate.hql.ast.HqlParser;

import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;

public class TreeViewer {
    public static void main(String argv[])
    {
        
        HqlParser parser= HqlParser.getInstance(argv[0]);
          // Parse the input expression
          try{
            parser.statement();
            AST t1 = parser.getAST();
                       
            // Print the resulting tree out in LISP notation
            //      MakumbaSystem.getLogger("debug.db").severe(t.toStringTree());
            
            // see the tree in a window
            if(t1!=null)
            {
                ASTFrame frame = new ASTFrame("tree", t1);
                frame.setVisible(true);
                
                // pass here a session factory
                // HqlAnalyzerWalker passes itself (with the sf) to all object it creates
                HqlAnalyzeWalker w= new HqlAnalyzeWalker();
                
                w.statement(t1);
                AST t=w.getAST();
                
                if(t!=null){
                    frame = new ASTFrame("analyzed", t);
                    frame.setVisible(true);
                }
                
            }
          }
          catch(antlr.ANTLRException f){ 
              f.printStackTrace();
          }
    }
}
