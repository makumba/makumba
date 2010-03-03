package org.makumba.providers.query.mql;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.OQLParseError;
import org.makumba.ProgrammerError;
import org.makumba.commons.NameResolver.TextList;
import org.makumba.providers.DataDefinitionProvider;

import antlr.ASTFactory;
import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.collections.AST;

/**
 * Analysis operations. This extends the class produced by mql-sql.g which is adapted from Hibernate. To simplify
 * porting of new versions, the class mostly redefines methods declared in mql-sql.g.
 * 
 * @author Cristian Bogdan
 * @version $Id: MqlSqlGenerator.java,v 1.1 Aug 5, 2008 5:47:16 PM cristi Exp $
 */
public class MqlSqlWalker extends MqlSqlBaseWalker {
    private static final String LINK_FUNCTION_DEF = "http://www.makumba.org/makumba-spec.html#tab_ql";

    // TODO:
    // finish subqueries
    // simplify FROM section for sets
    // use a subclass for analysing hql
    
    DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    ASTFactory fact;

    RecognitionException error;

    QueryContext currentContext;

    private boolean fromEnded;

    static PrintWriter pw = new PrintWriter(System.out);

    /** Parameters as paramN=type, where N is the parameter position in the query.
     * This is normally the structure returned to the query analyzer clients. 
     * It allows for a parameter to have different types on different positions (multi-type parameters). 
     */
    DataDefinition paramInfoByPosition;

    /** Parameters as name=type, where name is the string after $ for named parameters
     * or paramX for numbered parameters, where X is the parameter number (not its query position!).
     * This is used if a parameter is mentioned more times in a query and the type for some of these mentions
     * cannot be determined 
     */
    DataDefinition paramInfoByName;

    /** The set of parameters that have different types on different positions (multi-type parameters) */
    Set<String> multiTypeParams= new HashSet<String>();
    
    private AST select;

    QueryContext rootContext;
    
    LinkedHashMap<String, FunctionCall> orderedFunctionCalls = new LinkedHashMap<String, FunctionCall>();
    
    boolean hasSubqueries;

    String query;

    boolean optimizeJoins;

    boolean autoLeftJoin;

    private DataDefinition insertIn;

    public MqlSqlWalker(String query, DataDefinition insertIn, boolean optimizeJoins, boolean autoLeftJoin, boolean functionAsInliner) {
        this.query = query;
        this.functionAsInliner = functionAsInliner;
        this.insertIn= insertIn;
        this.optimizeJoins = optimizeJoins;
        this.autoLeftJoin = autoLeftJoin;
        setASTFactory(fact = new MqlSqlASTFactory(this));
        this.paramInfoByPosition = DataDefinitionProvider.getInstance().getVirtualDataDefinition("Temporary parameters by order for " + query);
        this.paramInfoByName = DataDefinitionProvider.getInstance().getVirtualDataDefinition("Temporary parameters by name for " + query);
        
    }

    public void reportError(RecognitionException e) {
        if (error == null)
            error = e;
    }

    public void reportError(String s) {
        if (error == null)
            error = new RecognitionException(s);
    }
    
    @Override
    protected String inlineFunction(AST functionCall, boolean inFunctionCall) throws SemanticException {
        
        final AST functionNode = functionCall.getFirstChild();
        final AST exprList = functionNode.getNextSibling();
        MqlNode paramNode = (MqlNode) exprList.getFirstChild();
        String name = functionNode.getText();
        
        DataDefinition type;
        
        // we get a.b.c.functionName, resolve type of the path so we can retrieve the DD
        // TODO I think this will work only on for a.b.c but not further
        String path = name;
        String additionalPath = name;
        int d = name.lastIndexOf(".");
        if(d > -1) {
            path = name.substring(0, d);
            additionalPath = path;
            name = name.substring(d + 1);
            String label = "";
            int d1 = path.indexOf(".");
            if(d1 > -1) {
                label = path.substring(0, d1);
                additionalPath = additionalPath.substring(d1 + 1);
            } else {
                label = path;
                additionalPath = "";
            }
            
            // first we try in the root FROM
            type = rootContext.labels.get(label);
            
            // let's see if we are in a subquery
            if(type == null && currentContext != null) {
                type = currentContext.labels.get(label);
            }
            
            additionalPath = additionalPath + (additionalPath.length() == 0 ? "" : ".") + name;
            
        } else {
            type = rootContext.labels.get(name);
        }
        
        // we take the first FROM element we find
        if(type == null) {
            type = rootContext.labels.values().iterator().next();
        }
        
        DataDefinition.QueryFragmentFunction funct = type.getFunctionOrPointedFunction(additionalPath);
        
        
        // we didn't find the function in the MDD, so it might be a MQL function
        // we ignore actors as they will be processed by the inliner
        if(funct == null && !additionalPath.startsWith("actor")) {
            processFunction(functionCall);
            
            // we still make a dummy function call because we need it to have the right index in the inliner
            FunctionCall c = new FunctionCall(null, null, null, null, additionalPath, false, true, false, getCurrentClauseType() == WHERE);
            orderedFunctionCalls.put(c.getKey(), c);
            return c.getKey();
        }       
        
        // fetch the function parameters of the call and store them so we can perform inlining in the QueryAnalyser
        // we have to store the arguments for each functionCall separately
        Vector<MqlNode> args = new Vector<MqlNode>();
        while(paramNode != null) {
            args.add(paramNode);
            paramNode = (MqlNode) paramNode.getNextSibling();
        }
        
        FunctionCall c = new FunctionCall(funct, args, null, type, path, inFunctionCall, false, additionalPath.startsWith("actor"), getCurrentClauseType() == WHERE);
        orderedFunctionCalls.put(c.getKey(), c);
        return c.getKey();
    }
    
    @Override
    protected void setBooleanType(AST a) {
        ((MqlNode)a).setMakType(DataDefinitionProvider.getInstance().makeFieldOfType("dummy", "boolean"));
    }
    
    @Override
    protected void processFunction(AST functionCall) throws SemanticException {
        // determine parameter types here
        final AST functionNode = functionCall.getFirstChild();
        final AST exprList = functionNode.getNextSibling();
        MqlNode paramNode = (MqlNode) exprList.getFirstChild();
        int index = 0;
        final String name = functionNode.getText();
        final MQLFunctionDefinition functionDef = MQLFunctionDefinition.getByName(MqlNode.mqlFunctions, name);
        if (functionDef == null) {
            throw new ProgrammerError("MQL Function '" + name + "' is not defined as query fragment, nor is it a known MQL function! Please refer to " + LINK_FUNCTION_DEF
                    + " for a list of known functions.");
        }
        final MQLFunctionArgument[] args = functionDef.getArguments();
        if (paramNode == null && !ArrayUtils.isEmpty(args)) {
            throw new ProgrammerError("The function '" + functionDef + "' requires arguments! Please refer to "
                    + LINK_FUNCTION_DEF + " for a list of known functions and arguments.");
        }
        while (paramNode != null) {
            String type = null;
            if (args != null) {
                if (args.length > index) { // not yet at the last defined argument
                    type = args[index].getType();
                } else if (args[args.length - 1].isMultiple()) {// if the last argument is a multiple argument, usethat
                    type = args[args.length - 1].getType();
                } else { // otherwise we have an error..
                    throw new ProgrammerError("The number of arguments for function '" + functionDef
                            + "' is wrong! Please refer to " + LINK_FUNCTION_DEF
                            + " for a list of known functions and arguments.");
                }
            } else {
                throw new ProgrammerError("MQL Function '" + functionDef + "' requires no arguments.");
            }
            if (paramNode.isParam()) 
                setParameterType(paramNode, DataDefinitionProvider.getInstance().makeFieldOfType("dummy", type));
                // FIXME: a param might also be a nested function
            paramNode = (MqlNode) paramNode.getNextSibling();
            index++;
        }
    }

    public void reportWarning(String s) {
        System.out.println(s);
    }

    protected void pushFromClause(AST fromClause, AST inputFromNode) {
        QueryContext c = new QueryContext(this);
        c.setParent(currentContext);
        if (currentContext == null)
            rootContext = c;
        else
            hasSubqueries = true;
        currentContext = c;
    }

    protected void setFromEnded() throws SemanticException {
        fromEnded = true;
    }

    protected void processQuery(AST select, AST query) throws SemanticException {
        if (error != null)
            return;

        // if we have no projections, we add the explicitly declared labels
        if (select == null)
            addDefaultProjections(query);

        this.select = query.getFirstChild();

        // now we can compute our joins
        currentContext.close();

        // if the currentContext has a filter, we make sure we have a WHERE and we add it there
        if (!currentContext.filters.isEmpty())
            addFilters(query);

        currentContext = currentContext.getParent();
    }

    private void addDefaultProjections(AST query) throws SemanticException {
        AST clause = ASTUtil.create(fact, SELECT_CLAUSE, "");
        clause.setNextSibling(query.getFirstChild());
        AST lastProjection = null;
        for (String label : currentContext.explicitLabels) {
            MqlIdentNode proj = (MqlIdentNode) ASTUtil.create(fact, IDENT, label);
            proj.resolve();
            if (lastProjection == null)
                clause.setFirstChild(proj);
            else
                lastProjection.setNextSibling(proj);
            lastProjection = proj;
        }
        query.setFirstChild(clause);
    }

    private void addFilters(AST query) {
        // we find the FROM
        AST from = query.getFirstChild();
        while (from.getType() != FROM)
            from = from.getNextSibling();

        // we make sure that there is a where
        AST where = from.getNextSibling();
        if (where == null || where.getType() != WHERE) {
            AST a = where;
            where = ASTUtil.create(fact, WHERE, "");
            from.setNextSibling(where);
            where.setNextSibling(a);
        }

        // we add a FILTERS to the where
        AST a = where.getFirstChild();
        AST filters = ASTUtil.create(fact, FILTERS, "");
        where.setFirstChild(filters);
        filters.setNextSibling(a);

        // now we add all the filter conditions as children to the FILTERS
        AST lastCond = null;
        for (TextList f : currentContext.filters) {
            MqlNode sqlToken = (MqlNode) ASTUtil.create(fact, SQL_TOKEN, "");
            if (lastCond != null)
                lastCond.setNextSibling(sqlToken);
            else
                filters.setFirstChild(sqlToken);
            lastCond = sqlToken;
            sqlToken.setTextList(f);
        }
    }

    protected AST createFromElement(String path, AST alias, AST propertyFetch) throws SemanticException {
        return currentContext.createFromElement(path, alias, HqlSqlTokenTypes.INNER);
    }

    protected void createFromJoinElement(AST path, AST alias, int joinType, AST fetch, AST propertyFetch, AST with)
            throws SemanticException {
        if (error != null)
            return;
        if (!(path instanceof MqlDotNode))
            throw new SemanticException("can only support dot froms " + path, "", path.getLine(), path.getColumn());
        ((MqlDotNode) path).processInFrom();
        currentContext.createFromElement(path.getText(), alias, joinType);
    }

    protected AST lookupProperty(AST dot, boolean root, boolean inSelect) throws SemanticException {
        if (error != null || !fromEnded)
            return dot;
        // root and inSelect are useless due to logicExpr being accepted now in SELECT
        MqlDotNode dotNode = (MqlDotNode) dot;
        dotNode.processInExpression();
        return dot;
    }

    protected void resolve(AST node) throws SemanticException {
        if (error != null || !fromEnded || (functionAsInliner && inFunctionCall))
            return;
        if (node.getType() == HqlSqlTokenTypes.IDENT)
            ((MqlIdentNode) node).resolve();
    }
    
    protected void setAlias(AST selectExpr, AST ident) {
        if (error != null)
            return;
        // we add the label to the output, this is a bit of a hack!
        MqlNode as = (MqlNode) ASTUtil.create(fact, HqlSqlTokenTypes.ALIAS_REF, ident.getText());
        selectExpr.setNextSibling(as);
        // the projection name is going to be the original text
        // now we set it to the SQL form
        as.setText(" AS " + as.getText());

        currentContext.projectionLabelSearch.put(ident.getText(), (MqlNode) selectExpr);
    }

    protected AST generateNamedParameter(AST delimiterNode, AST nameNode) throws SemanticException {
        MqlNode para = (MqlNode) ASTUtil.create(fact, MqlSqlWalker.NAMED_PARAM, nameNode.getText());
        // old name will be preserved as para.originalText
        para.setText("?");
        return para;
    }

    protected AST generatePositionalParameter(AST inputNode) throws SemanticException {
        return ASTUtil.create(fact, MqlSqlWalker.PARAM, "?");
    }

    void setParameterType(MqlNode param, FieldDefinition likewise) {
        String paramName = param.getOriginalText();
        
        // we separate the parameter position from the name, as both are registered in the same string
        int paramPositionIndex = paramName.indexOf("###");
        int paramPosition = Integer.parseInt(paramName.substring(paramPositionIndex + 3));
        paramName = paramName.substring(0, paramPositionIndex);

        // if the parameter is not already registered as multi-type (with different types on different position)
        if (!multiTypeParams.contains(paramName)) {
            FieldDefinition fd = DataDefinitionProvider.getInstance().makeFieldWithName(paramName, likewise);
            FieldDefinition fd1 = paramInfoByName.getFieldDefinition(paramName);

            // if we already have a type for that name and the types are not compatible
            if (fd1 != null && !fd1.isAssignableFrom(fd)) {
                // FIXME: if(fd.isAssignableFrom(fd1) we are still ok
                // so we should not declare the param as multitype
                // but then we'd have to replace fd1 with fd in paramInfoByName
                // and that's currently not possible
                
                // we register the parameter as multi-type
                multiTypeParams.add(paramName);
            } else if(fd1==null) {
                    // we register the type if we don't have any for this name
                    paramInfoByName.addField(fd);
            }
        }

        // we now register the type for this position. 
        // we don't check for duplicate type on this position
        // as each tree node should be visited only once...
        FieldDefinition fd = DataDefinitionProvider.getInstance().makeFieldWithName("param" + paramPosition, likewise);
        param.setMakType(fd);
        paramInfoByPosition.addField(fd);
    }

    void setProjectionTypes(DataDefinition proj) {
        if (select == null)
            // there are no projections, we leave this pass
            // we could chose to throw something
            return;

        int i = 0;
        for (AST a = select.getFirstChild(); a != null; a = a.getNextSibling()) {
            if (a.getType() == ALIAS_REF)
                continue;
            String name = "col" + (i + 1);
            if (a.getNextSibling() != null && a.getNextSibling().getType() == ALIAS_REF)
                name = ((MqlNode) a.getNextSibling()).getOriginalText();

            MqlNode mqlNode = (MqlNode) a;
            FieldDefinition makType = mqlNode.getMakType();

            // if we have no type but we are a parameter, we maybe found the type somewhere else
            if (makType == null && mqlNode.isParam()) {
                // we separate the type from the position
                String paramName = mqlNode.getOriginalText();
                paramName = paramName.substring(0, paramName.indexOf("###"));
                
                // if we are an actor param, we can infer the type from the parameter name
                if(paramName.startsWith("actor_")) {
                    String type = paramName.substring("actor_".length()).replace("_", ".");
                    makType = DataDefinitionProvider.getInstance().getDataDefinition(type).getFieldDefinition(0);
                    mqlNode.setMakType(makType);
                    
                } else {
                    // we accept the type registered on some other position unless it is a multi-type param
                    if (!multiTypeParams.contains(paramName))
                        makType = paramInfoByName.getFieldDefinition(paramName);
                }
                
            }
            
            //  if we have no type but know in which table we'll insert the result
            if(makType == null && insertIn!=null){
                makType= insertIn.getFieldDefinition(name);
                
                // and such we are most probably a parameter
                if(makType!=null && mqlNode.isParam())
                    setParameterType((MqlNode)a, makType);
            }
             
            if(makType==null)
                throw new IllegalStateException("no type set for projection " + name + " "
                        + MqlQueryAnalysis.showAst(a));
            

            proj.addField(DataDefinitionProvider.getInstance().makeFieldWithName(name, makType));
            i++;
        }
    }

    public boolean isAnalysisQuery() {
        if (select == null || select.getFirstChild() == null)
            return false;
        return select.getFirstChild().getNextSibling() == null
                && select.getFirstChild().getType() == HqlSqlTokenTypes.NUM_INT;
    }
    
    @Override
    protected void setActorType(AST a) {
        // ( [78] 
        //   actor [120] 
        //   exprList [72] 
        //      . [15] 
        //         some [120] 
        //         Type [120] 
        String type = ASTUtil.constructPath(a.getFirstChild().getNextSibling().getFirstChild());
        DataDefinition typeDD = ddp.getDataDefinition(type);
        ((MqlNode)a).setMakType(typeDD.getFieldDefinition(0));
    }
    
}