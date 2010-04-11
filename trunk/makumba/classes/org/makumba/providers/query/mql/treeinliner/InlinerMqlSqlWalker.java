package org.makumba.providers.query.mql.treeinliner;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.providers.query.mql.ASTUtil;
import org.makumba.providers.query.mql.FunctionCall;
import org.makumba.providers.query.mql.MqlNode;
import org.makumba.providers.query.mql.MqlSqlWalker;
import org.makumba.providers.query.mql.QueryContext;

import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.collections.AST;

public class InlinerMqlSqlWalker extends MqlSqlWalker{
    public InlinerMqlSqlWalker(String query, DataDefinition insertIn, boolean optimizeJoins, boolean autoLeftJoin) {
        super(query, insertIn, optimizeJoins, autoLeftJoin);
        this.functionAsInliner = true;

    }
    RecognitionException getError(){ return error;}

    HashMap<String, FunctionCall> getOrderedFunctionCalls() {
        return orderedFunctionCalls;
    }
    void setOrderedFunctionCalls(LinkedHashMap<String, FunctionCall> orderedFunctionCalls) {
        this.orderedFunctionCalls= orderedFunctionCalls; 
        
    }
    QueryContext getRootContext() {
        return rootContext;
    }
    
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
            type = rootContext.getLabels().get(label);
            
            // let's see if we are in a subquery
            if(type == null && currentContext != null) {
                type = currentContext.getLabels().get(label);
            }
            
            additionalPath = additionalPath + (additionalPath.length() == 0 ? "" : ".") + name;
            
        } else {
            type = rootContext.getLabels().get(name);
        }
        
        // we take the first FROM element we find
        if(type == null) {
            type = rootContext.getLabels().values().iterator().next();
        }
        
        DataDefinition.QueryFragmentFunction funct = type.getFunctionOrPointedFunction(additionalPath);
        
        
        // we didn't find the function in the MDD, so it might be a MQL function
        // we ignore actors as they will be processed by the inliner
        if(funct == null && !additionalPath.startsWith("actor")) {
            processFunction(functionCall);
            
            // we still make a dummy function call because we need it to have the right index in the inliner
            FunctionCall c = new FunctionCall(null, null, null, null, additionalPath, false, true, false, getCurrentClauseType() == WHERE);
            addFunctionCall(c);
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
        addFunctionCall(c);
        
        // if this is an actor with path, set the type
        // FIXME maybe not necessary
        if(c.isActorFunction() && c.getPath().startsWith("actor")) {
            setActorType(functionCall);
        } else if(c.isActorFunction() && !c.getPath().startsWith("actor")) {
            // do something different
        }
        
        return c.getKey();
    }
    
    protected void resolve(AST node) throws SemanticException {    
        if(functionAsInliner && inFunctionCall)
            return;
        super.resolve(node);
    }
    
    protected void setActorType(AST a) {
        // ( [78] 
        //   actor [120] 
        //   exprList [72] 
        //      . [15] 
        //         some [120] 
        //         Type [120] 
        String type = ASTUtil.getPath(a.getFirstChild().getNextSibling().getFirstChild());
        DataDefinition typeDD = ddp.getDataDefinition(type);
        ((MqlNode)a).setMakType(typeDD.getFieldDefinition(0));
    }
   
}
