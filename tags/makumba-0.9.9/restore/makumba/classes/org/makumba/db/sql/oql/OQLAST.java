package org.makumba.db.sql.oql;
import org.makumba.db.Database;
import org.makumba.*;
import org.makumba.abstr.*;
import antlr.collections.AST;
import antlr.CommonAST;

public class OQLAST extends CommonAST
{
  public OQLAST(){};
  public OQLAST(antlr.Token t) { super(t); }
  public String writeInSQLQuery(Database d){return getText(); }

  /* used in expressions */
  Object makumbaType;

  AnalysisTree tree;

  public Object getMakumbaType() throws antlr.RecognitionException
  {
    if(tree!=null)
      return tree.getMakumbaType();
    else
      return makumbaType;
  }
}

