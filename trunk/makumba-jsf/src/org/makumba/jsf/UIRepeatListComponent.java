package org.makumba.jsf;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.view.facelets.FaceletException;

import org.makumba.ProgrammerError;
import org.makumba.commons.ArrayMap;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.RegExpUtils;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.ComposedSubquery;
import org.makumba.list.engine.Grouper;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProvider;

import com.sun.faces.facelets.compiler.UIInstructions;
import com.sun.faces.facelets.component.UIRepeat;

public class UIRepeatListComponent extends UIRepeat {

    static final String CURRENT_DATA = "org.makumba.list.currentData";

    static final private Dictionary<String, Object> NOTHING = new ArrayMap();

    static final String CURRENT_LIST = "org.makumba.list.currentList";

    String[] queryProps = new String[6];

    String separator = "";

    // TODO: no clue what defaultLimit does
    int offset = 0, limit = -1, defaultLimit;

    transient ComposedQuery composedQuery;

    // all data, from all iterations of the parent list
    transient Grouper listData;

    // current iteration of this list
    transient ArrayMap currentData;

    // FLAGS, should be taken from configuration
    /**
     * We can execute the queries of a mak:list group either in the same transaction or separately. In JSP they are
     * executed separately and no major issues were found. In JSF we test executing them together but we provide this
     * flag.
     */
    public boolean useSeparateTransactions() {
        return false;
    }

    /**
     * Should be true in production and false in development. Tells whether we should recompute the queries at every
     * load. It may be possible to detect automatically whether the view script has changed. If it changes only a bit,
     * the keys don't change much.
     */
    public boolean useCaches() {
        return false;
    }

    // END OF FLAGS

    public UIRepeatListComponent() {
        // to allow proper visiting during analysis
        setNullModel();
    }

    private void setNullModel() {
        setValue("");
        setEnd(0);
    }

    public ComposedQuery getComposedQuery() {
        return composedQuery;
    }

    public List<String> getProjections() {
        return getComposedQuery().getProjections();
    }

    public void setFrom(String s) {
        queryProps[ComposedQuery.FROM] = s;
    }

    protected Object getCacheKey() {
        // TODO: find an implementation-independent cache key
        return this.getAttributes().get("com.sun.faces.facelets.MARK_ID");
    }

    public void setVariableFrom(String s) {
        queryProps[ComposedQuery.VARFROM] = s;
    }

    public void setWhere(String s) {
        queryProps[ComposedQuery.WHERE] = s;
    }

    public void setOrderBy(String s) {
        queryProps[ComposedQuery.ORDERBY] = s;
    }

    public void setGroupBy(String s) {
        queryProps[ComposedQuery.GROUPBY] = s;
    }

    public void setSeparator(String s) {
        separator = s;
    }

    public void setOffset(int n) {
        offset = n;
    }

    public void setLimit(int n) {
        limit = n;
    }

    public void setDefaultLimit(int n) {
        defaultLimit = n;
    }

    protected void onlyOuterListArgument(String s) {
        UIRepeatListComponent c = this.findMakListParent(false);
        if (c != null) {
            throw new FaceletException(s + "can be indicated only for root mak:lists");
        }
    }

    private UIRepeatListComponent findMakListParent(boolean objectToo) {
        UIComponent c = getParent();
        while (c != null && !(c instanceof UIRepeatListComponent)) {
            // TODO: honor also objectToo
            c = c.getParent();
        }

        return (UIRepeatListComponent) c;
    }

    @Override
    public void encodeAll(FacesContext context) throws IOException {
        if (findMakListParent(true) == null) {
            startMakListGroup();
        }

        final List<ArrayMap> iterationGroupData = listData.getData(getCurrentDataStack());

        if (iterationGroupData == null) {
            return;
        }

        // push a placeholder, it will be popped at first iteration
        getCurrentDataStack().push(NOTHING);

        DataModel<ArrayMap> dm = new ListDataModel<ArrayMap>(iterationGroupData) {
            @Override
            public void setRowIndex(int rowIndex) {
                if (rowIndex >= 0 && rowIndex < iterationGroupData.size()) {
                    // pop old value:
                    getCurrentDataStack().pop();
                    currentData = iterationGroupData.get(rowIndex);
                    // push new value:
                    getCurrentDataStack().push(currentData);
                }

                super.setRowIndex(rowIndex);
                if (rowIndex >= iterationGroupData.size()) {
                    // data iteration is done
                    // we set the model to something dummy that will allow tree visiting but not more
                    // this will allow UIRepeat to save and restore its state (and perform other visiting) undisturbed
                    // by our data
                    setNullModel();
                }

            }
        };

        setValue(dm);
        setEnd(iterationGroupData.size());

        UIRepeatListComponent parent = getCurrentlyRunning();
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put(CURRENT_LIST, this);

        super.encodeAll(context);
        // this list is done, no more current value in stack
        getCurrentDataStack().pop();
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put(CURRENT_LIST, parent);
    }

    @SuppressWarnings("unchecked")
    static Stack<Dictionary<String, Object>> getCurrentDataStack() {
        return (Stack<Dictionary<String, Object>>) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get(
            CURRENT_DATA);
    }

    public static UIRepeatListComponent getCurrentlyRunning() {
        return (UIRepeatListComponent) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get(
            CURRENT_LIST);

    }

    static int composedQueries = NamedResources.makeStaticCache("JSF ComposedQueries", new NamedResourceFactory() {
        @Override
        public Object getHashObject(Object o) {
            return ((UIRepeatListComponent) o).getCacheKey();
        }

        @Override
        public Object makeResource(Object o, Object hashName) throws Throwable {
            UIRepeatListComponent comp = (UIRepeatListComponent) o;
            comp.computeComposedQuery();
            return comp.composedQuery;
        }
    });

    public void analyze() {
        // this method is called only for root mak:lists, thus it would be good for triggering analysis and executing
        // queries
        // however for some reason it is called twice if APPLY_REQUEST_VALUES 2 PROCESS_VALIDATIONS 3 and
        // UPDATE_MODEL_VALUES 4 are executed.
        // thus analysis is now done in encodeAll() (i.e. at the latest possible moment)
        // TODO: consider removing
    }

    public void startMakListGroup() {
        readComposedQuery();

        final QueryProvider qep = useSeparateTransactions() ? null : getQueryExecutionProvider();

        try {
            visitTree(VisitContext.createVisitContext(FacesContext.getCurrentInstance()), new VisitCallback() {
                @Override
                public VisitResult visit(VisitContext context, UIComponent target) {
                    if (target instanceof UIRepeatListComponent) {
                        ((UIRepeatListComponent) target).executeQuery(qep);
                    }
                    return VisitResult.ACCEPT;
                }
            });
        } finally {
            if (qep != null) {
                qep.close();
            }
        }
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put(CURRENT_DATA,
            new Stack<Dictionary<String, Object>>());

        getCurrentDataStack().push(NOTHING);

    }

    private void readComposedQuery() {
        if (composedQuery == null) {
            if (useCaches()) {
                composedQuery = (ComposedQuery) NamedResources.getStaticCache(composedQueries).getResource(this);
            } else {
                computeComposedQuery();
            }
        }
    }

    void computeComposedQuery() {
        UIRepeatListComponent parent = this.findMakListParent(true);
        if (parent == null) {
            // no parent, we are root
            this.composedQuery = new ComposedQuery(this.queryProps, this.getQueryLanguage());
        } else {
            this.composedQuery = new ComposedSubquery(this.queryProps, parent.composedQuery, this.getQueryLanguage());
        }
        this.composedQuery.init();
        this.findExpressionsInChildren();
        if (parent == null) {
            this.analyzeMakListGroup();
        }
        this.composedQuery.analyze();
        // System.out.println(this.composedQuery);
    }

    void analyzeMakListGroup() {
        visitTree(VisitContext.createVisitContext(FacesContext.getCurrentInstance()), new VisitCallback() {
            @Override
            public VisitResult visit(VisitContext context, UIComponent target) {
                if (target != UIRepeatListComponent.this && target instanceof UIRepeatListComponent) {
                    ((UIRepeatListComponent) target).readComposedQuery();
                }
                return VisitResult.ACCEPT;
            }
        });
    }

    static final ComposedQuery.Evaluator evaluator = new ComposedQuery.Evaluator() {
        @Override
        public String evaluate(String s) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            // FIXME: no clue if this is what we should do here
            return ctx.getApplication().evaluateExpressionGet(ctx, s, String.class);
        }
    };

    // TODO: fix a proper query parameter map
    static final Map args = new HashMap();

    private void executeQuery(QueryProvider qep) {
        // by now the query was cached so we fetch it
        readComposedQuery();
        if (useSeparateTransactions()) {
            qep = getQueryExecutionProvider();
        }

        try {
            listData = composedQuery.execute(qep, args, evaluator, offset, limit);
        } finally {
            if (useSeparateTransactions()) {
                qep.close();
            }
        }
    }

    private QueryProvider getQueryExecutionProvider() {
        return QueryProvider.makeQueryRunner(TransactionProvider.getInstance().getDefaultDataSourceName(),
            getQueryLanguage());
    }

    public String getQueryLanguage() {
        // TODO: get the query language from taglib URI, taglib name, or configuration
        return "oql";
    }

    private void addExpression(String expr, boolean canBeInvalid) {
        // TODO: analyze the expression in the composedquery. mak:value and mak:expr() expressions may not be invalid,
        // while other EL expressions may be invalid, in which case they are not added
        composedQuery.checkProjectionInteger(expr);
    }

    Integer getExpressionIndex(String expr) {
        Integer exprIndex = composedQuery.checkProjectionInteger(expr);
        if (exprIndex == null) {
            if (useCaches()) {
                // FIXME: a better mak:list description
                throw new ProgrammerError("<mak:list> does not know the expression " + expr
                        + ", turn caches off, or try reloading the page, it might work.");
            } else {
                // second call should return not null
                // however, we should never get here since a page analysis is done every request
                // so the expression must be known
                exprIndex = composedQuery.checkProjectionInteger(expr);
            }
        }
        return exprIndex;
    }

    public Object getExpressionValue(String expr) {
        return getExpressionValue(getExpressionIndex(expr));
    }

    public Object getExpressionValue(int exprIndex) {
        return currentData.data[exprIndex];
    }

    void findExpressionsInChildren() {
        visitTree(VisitContext.createVisitContext(FacesContext.getCurrentInstance()), new VisitCallback() {
            @Override
            public VisitResult visit(VisitContext context, UIComponent target) {
                if (target instanceof UIRepeatListComponent && target != UIRepeatListComponent.this) {
                    return VisitResult.REJECT;
                }

                // System.out.println(target);

                if (target instanceof UIInstructions) {
                    findFloatingExpressions((UIInstructions) target);
                } else if (target instanceof ValueComponent) {
                    findMakValueExpressions((ValueComponent) target);
                } else {
                    findComponentExpressions(target);
                }
                return VisitResult.ACCEPT;
            }
        });
    }

    /**
     * Finds the properties of this {@link UIComponent} that have a {@link ValueExpression}.<br>
     * TODO: we can't really cache this since the programmer can change the view without restarting the whole servlet
     * context. We may be able to find out about a change in the view though and introduce a caching mechanism then.
     * 
     * @param component
     *            the {@link UIComponent} of which the properties should be searched for EL expressions
     */
    private void findComponentExpressions(UIComponent component) {

        try {
            PropertyDescriptor[] pd = Introspector.getBeanInfo(component.getClass()).getPropertyDescriptors();
            for (PropertyDescriptor p : pd) {
                // we try to see if this is a ValueExpression by probing it
                ValueExpression ve = component.getValueExpression(p.getName());
                if (ve != null) {
                    addExpression(trimExpression(ve.getExpressionString()), true);
                }
            }

        } catch (IntrospectionException e) {
            // TODO better error handling
            e.printStackTrace();
        }
    }

    private final static Pattern ELExprFunctionPattern = Pattern.compile("\\w+:expr\\(" + RegExpUtils.LineWhitespaces
            + "(\\'[^\\']+\\')" + RegExpUtils.LineWhitespaces + "?\\)");

    private final static Pattern JSFELPattern = Pattern.compile("\\#\\{[^\\}]*\\}");

    private final static Pattern dotPathPattern = Pattern.compile(RegExpUtils.dotPath);

    /**
     * Finds the 'floating' EL expressions that are not a property of a component, but are directly part of the view
     * body. Since {@link UIInstructions} sometimes not only return the EL expression but also some surrounding HTML
     * tags, we do a rudimentary but robust parsing (everything that does not conform to <code>#{...}</code> is
     * ignored).<br>
     * FIXME this is a hack and renders this implementation dependent on the Sun Mojarra implementation. That is, the
     * JSF specification does not seem to say anything about such floating EL elements. There might be a way to get
     * those through the ELResolver facility though.
     * 
     * @param component
     *            the {@link UIInstructions} which should be searched for EL expressions.
     */
    private void findFloatingExpressions(UIInstructions component) {

        String txt = component.toString();

        // find EL expressions
        Matcher elExprMatcher = JSFELPattern.matcher(txt);
        while (elExprMatcher.find()) {
            String elExprTxt = elExprMatcher.group();
            elExprTxt.substring(2, elExprTxt.length() - 1);

            // first we find functions inside of it
            Matcher exprFuncMatcher = ELExprFunctionPattern.matcher(elExprTxt);

            // TODO find the prefix for the makumba namespace, for now we assume it is 'mak'
            while (exprFuncMatcher.find()) {
                String elFuncTxt = exprFuncMatcher.group();

                // add the EL expression as expression, assuming it starts with "mak"
                if (elFuncTxt.startsWith("mak")) {
                    elFuncTxt = elFuncTxt.substring("mak:expr(".length(), elFuncTxt.length() - 1);

                    // TODO: decide whether we want to support dynamic function expressions
                    // if not, check that txt is precisely a 'string' or "string"
                    // to support dynamic function expressions, an evaluator should be applied here
                    elFuncTxt = elFuncTxt.substring(1, elFuncTxt.length() - 1);
                    addExpression(elFuncTxt, false);
                } else {
                    // TODO logger warning or namespace resolution
                }
            }
            // remove the EL function calls from the global expression to avoid wrong matches of the rest
            elExprTxt = exprFuncMatcher.replaceAll("");

            // we now have a cleared expression, we check for paths like "p.name"
            Matcher dotPathMatcher = dotPathPattern.matcher(elExprTxt);
            while (dotPathMatcher.find()) {
                addExpression(dotPathMatcher.group(), true);
            }
        }
    }

    /**
     * Finds QL expressions inside a mak:value component
     * 
     * @param component
     *            the mak:value component
     */
    private void findMakValueExpressions(ValueComponent component) {

        // go thru all properties as for normal components, and also take into account non-EL (literal) expr values
        findComponentExpressions(component);
        if (component.getExpr() == null) {
            // FIXME ProgrammerError
            throw new RuntimeException("no expr provided in mak:value!");
        } else {
            // TODO: setvalue expression
            // TODO: nullable value? i guess that's not in use any longer
            addExpression(component.getExpr(), false);
        }

    }

    static private String trimExpression(String expr) {
        return expr.substring(2, expr.length() - 1);
    }
}
