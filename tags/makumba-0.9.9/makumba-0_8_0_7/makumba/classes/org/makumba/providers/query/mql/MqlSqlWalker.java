package org.makumba.providers.query.mql;

import java.io.PrintWriter;
import org.hibernate.hql.ast.util.ASTPrinter;
import org.hibernate.hql.ast.util.ASTUtil;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.providers.DataDefinitionProvider;

import antlr.ASTFactory;
import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.collections.AST;

/**
 * Analysis operations. This extends the class produced by mql-sql.g which is adapted from Hibernate.
 * To simplify porting of new versions, the class mostly redefines methods declared in mql-sql.g.
 * @author Cristian Bogdan
 * @version $Id: MqlSqlGenerator.java,v 1.1 Aug 5, 2008 5:47:16 PM cristi Exp $
 */
public class MqlSqlWalker extends MqlSqlBaseWalker {
    // TODO:
    // finish subqueries
    // simplify FROM section for sets 
    // use a subclass for analysing hql
    
    ASTFactory fact ;
    RecognitionException error;
    QueryContext currentContext;
    private boolean fromEnded;
    static PrintWriter pw = new PrintWriter(System.out);
    static ASTPrinter printer = new ASTPrinter(HqlSqlTokenTypes.class);
    DataDefinition paramInfo;
    private AST select;
    QueryContext rootContext;
    
    public MqlSqlWalker(DataDefinition paramInfo){
        setASTFactory(fact= new MqlSqlASTFactory(this));
        this.paramInfo=paramInfo;
    }
 
    public void reportError(RecognitionException e) {
        if(error==null)
            error=e;
    }

    public void reportError(String s) {
        if(error== null)
            error= new RecognitionException(s);
    }

    public void reportWarning(String s) {
        System.out.println(s);
    }
    
    protected void pushFromClause(AST fromClause,AST inputFromNode) {
        QueryContext c= new QueryContext(this);
        c.setParent(currentContext);
        if(currentContext==null)
            rootContext=c;
        currentContext=c;
    }
    
    protected void setFromEnded() throws SemanticException{
        fromEnded=true;
    }
    
    protected void processQuery(AST select,AST query) throws SemanticException { 
        currentContext.close(); 
        currentContext=currentContext.getParent();
        this.select= select;
    }

    protected AST createFromElement(String path, AST alias, AST propertyFetch) throws SemanticException {
       return currentContext.createFromElement(path, alias, HqlSqlTokenTypes.INNER);
    }
    
    protected void createFromJoinElement(AST path,AST alias,int joinType,AST fetch,AST propertyFetch,AST with) throws SemanticException {
       if(!(path instanceof MqlDotNode))
           throw new SemanticException("can only support dot froms "+path);
       ((MqlDotNode)path).processInFrom();
       currentContext.createFromElement(path.getText(), alias, joinType);
    }
    
    protected AST lookupProperty(AST dot,boolean root,boolean inSelect) throws SemanticException {
        if(error!=null || !fromEnded)
            return dot;
        // root and inSelect are useless due to logicExpr being accepted now in SELECT
        MqlDotNode dotNode= (MqlDotNode)dot;
        dotNode.processInExpression();
        return dot;
    }

    protected void resolve(AST node) throws SemanticException { 
        if(error!=null || !fromEnded)
            return;
        if(node.getType()==HqlSqlTokenTypes.IDENT)
            ((MqlIdentNode)node).resolve();
    }
    
    protected void setAlias(AST selectExpr, AST ident) {
        if(error!=null)
            return;
        // we add the label to the output, this is a bit of a hack!
        MqlNode as = (MqlNode)ASTUtil.create(fact, HqlSqlTokenTypes.ALIAS_REF, ident.getText());
        selectExpr.setNextSibling(as);
        // the projection name is going to be the original text
        // now we set it to the SQL form
        as.setText(" AS "+as.getText());
        
        currentContext.projectionLabelSearch.put(ident.getText(), selectExpr);
    }

    
    protected AST generateNamedParameter(AST delimiterNode, AST nameNode) throws SemanticException {
        MqlNode para= (MqlNode)ASTUtil.create(fact, MqlSqlWalker.NAMED_PARAM, nameNode.getText());
        // old name will be preserved as para.originalText
        para.setText("?");
        return para;
    }

    protected AST generatePositionalParameter(AST inputNode) throws SemanticException {
        return ASTUtil.create(fact, MqlSqlWalker.PARAM, "?");
    }

    void setParameterType(MqlNode param, MqlNode likewise) {
        String paramName = "param"+paramInfo.getFieldNames().size();
        FieldDefinition fd = DataDefinitionProvider.getInstance().makeFieldWithName(paramName, likewise.getMakType());
        param.setMakType(fd);
        //if(paramInfo.getFieldDefinition(paramName)==null)
            paramInfo.addField(fd);
    }
    
    void setProjectionTypes(DataDefinition proj){
        if(select==null )
            // there are no projections, we leave this pass
            // we could chose to throw something
            return;
        
        int i=0;        
        for(AST a= select.getFirstChild(); a!=null; a=a.getNextSibling()){
            if(a.getType()==ALIAS_REF)
                continue;
            String name= "col"+(i+1);
            if(a.getNextSibling()!= null && a.getNextSibling().getType()==ALIAS_REF)
                name=((MqlNode)a.getNextSibling()).getOriginalText();
            
            FieldDefinition makType = ((MqlNode)a).getMakType();
            if(makType==null){
                printer.showAst(a, pw);
                throw new IllegalStateException("no type set for projection "+name);
            }
            else
                proj.addField(DataDefinitionProvider.getInstance().makeFieldWithName(name, makType));
            i++;
        }
    }
}
