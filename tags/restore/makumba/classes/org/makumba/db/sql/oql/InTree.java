package org.makumba.db.sql.oql;
import antlr.collections.AST;

/** a tree checker for the IN operator. it makes sure that the type of the left operand is the same with the types of all operands in the compared set */
public class InTree extends AnalysisTree
{
  public InTree(Object left, Object right){ super(left, AnalysisTree.IN, right); }


 public void computeType() throws antlr.RecognitionException
  {
    if(leaf!=null)
      {
	makumbaType=leaf.makumbaType;
	return;
      }
    left.computeType();
    right.computeType();
    
    if(right.getMakumbaType()==null ||!right.getMakumbaType().equals("inSet"))
      throw new antlr.SemanticException("In operand type check failed:\n\t\'"+right+"\' is not a set");

    OQLAST a= right.leaf;

    if(a==null 
       // very strange fix was needed here, looks like "SET" is never set as text
       || !(a.getText().toLowerCase().equals("set") || a.getText().length()==0)
       || (a=(OQLAST)a.getNextSibling())==null 
       || !a.getText().equals("(")
       || (a=(OQLAST)a.getNextSibling())==null 
       )
      throw new antlr.SemanticException("IN operand should be followed by a set, found "+a.getNextSibling()+" \""+a.getText()+"\"");

    while(true)
      {
	if(a instanceof ParamAST)
	  a.makumbaType= left.getMakumbaType();
	else
	  checkOperandTypes(left.getMakumbaType(), a.getMakumbaType());
	a=(OQLAST)a.getNextSibling();
	if(a==null)
	  throw new antlr.SemanticException("unfinished SET after IN operand");
	if(a.getText()!=null && a.getText().equals(")"))
	  break;
	if(a.getText()!=null && a.getText().equals(","))
	  {
	    a=(OQLAST)a.getNextSibling();
	    if(a!=null)
	      continue;
	  }
	throw new antlr.SemanticException("unfinished SET after IN operand");
      }
    right.leaf.setText("");
    makumbaType= "int";
  }

}

