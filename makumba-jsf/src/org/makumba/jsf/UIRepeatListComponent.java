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

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.view.facelets.FaceletException;

import org.makumba.commons.ArrayMap;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
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

    ComposedQuery composedQuery;

    // all data, from all iterations of the parent list
    Grouper listData;

    // data from current iteration of the parent list
    List<ArrayMap> iterationGroupData;

    // current iteration of this list
    protected ArrayMap currrentData;

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

    UIRepeatListComponent parent;

    @Override
    public void encodeAll(FacesContext context) throws IOException {
        iterationGroupData = listData.getData(getCurrentDataStack());

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
                    currrentData = iterationGroupData.get(rowIndex);
                    // push new value:
                    getCurrentDataStack().push(currrentData);
                }
                super.setRowIndex(rowIndex);

            }
        };

        setValue(dm);
        parent = getCurrentlyRunning();
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
            return ((UIRepeatListComponent) ((Object[]) o)[1]).getCacheKey();
        }

        @Override
        public Object makeResource(Object o, Object hashName) throws Throwable {
            UIRepeatListComponent comp = (UIRepeatListComponent) ((Object[]) o)[1];
            if (((Object[]) o)[0] == null) {
                // no parent, we are root
                comp.composedQuery = new ComposedQuery(comp.queryProps, comp.getQueryLanguage());

            } else {
                comp.composedQuery = new ComposedSubquery(comp.queryProps,
                        ((UIRepeatListComponent) ((Object[]) o)[0]).composedQuery, comp.getQueryLanguage());
            }
            comp.composedQuery.init();
            UIRepeatListComponent.findExpressionsInChildren(comp, comp);
            comp.composedQuery.analyze();

            return comp.composedQuery;
        }
    });

    public void analyze() {
        composedQuery = (ComposedQuery) NamedResources.getStaticCache(composedQueries).getResource(
            new Object[] { null, this });

        for (UIComponent kid : this.getChildren()) {
            analyzeChildrenLists(this, this, kid);
        }
        executeQuery();

        FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put(CURRENT_DATA,
            new Stack<Dictionary<String, Object>>());

        getCurrentDataStack().push(NOTHING);

    }

    private void analyzeWithRoot(UIRepeatListComponent root, UIRepeatListComponent parent) {
        // TODO: check if limits or offsets were declared, this is illegal for sublists
        composedQuery = (ComposedQuery) NamedResources.getStaticCache(composedQueries).getResource(
            new Object[] { parent, this });

        executeQuery();
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

    private void executeQuery() {
        QueryProvider qep = QueryProvider.makeQueryRunner(TransactionProvider.getInstance().getDefaultDataSourceName(),
            getQueryLanguage());

        try {
            listData = composedQuery.execute(qep, args, evaluator, offset, limit);
        } finally {
            qep.close();
        }
    }

    private String getQueryLanguage() {
        // TODO: get the query language from taglib URI, taglib name, or configuration
        return "oql";
    }

    private void addExpression(String expr, boolean canBeInvalid) {
        // TODO: analyze the expression in the composedquery. mak:value and mak:expr() expressions may not be invalid,
        // while other EL expressions may be invalid, in which case they are not added
        composedQuery.checkProjectionInteger(expr);
    }

    public Object getExpressionValue(String expr) {
        return getExpressionValue(composedQuery.checkProjectionInteger(expr));
    }

    public Object getExpressionValue(int exprIndex) {
        return currrentData.data[exprIndex];
    }

    static private void analyzeChildrenLists(UIRepeatListComponent root, UIRepeatListComponent parent, UIComponent c) {
        if (c instanceof UIRepeatListComponent) {
            ((UIRepeatListComponent) c).analyzeWithRoot(root, parent);
            parent = (UIRepeatListComponent) c;
        }
        for (UIComponent kid : c.getChildren()) {
            analyzeChildrenLists(root, parent, kid);
        }
    }

    static private void findExpressionsInChildren(UIRepeatListComponent parent, UIComponent target) {
        // System.out.println(target);

        if (target instanceof UIInstructions) {
            findFloatingExpressions(parent, (UIInstructions) target);
        } else if (target instanceof ValueComponent) {
            findMakValueExpressions(parent, (ValueComponent) target);
        } else {
            findComponentExpressions(parent, target);
        }
        for (UIComponent kid : target.getChildren()) {
            if (kid instanceof UIRepeatListComponent) {
                // this component will analyze its own kids
                continue;
            }
            findExpressionsInChildren(parent, kid);
        }
    }

    /**
     * Finds the properties of this {@link UIComponent} that have a {@link ValueExpression}.<br>
     * TODO: we can't really cache this since the programmer can change the view without restarting the whole servlet
     * context. We may be able to find out about a change in the view though and introduce a caching mechanism then.
     * 
     * @param component
     *            the {@link UIComponent} of which the properties should be searched for EL expressions
     * @return a map of {@link ExprTuple} keyed by property name
     */
    static private void findComponentExpressions(UIRepeatListComponent parent, UIComponent component) {

        try {
            PropertyDescriptor[] pd = Introspector.getBeanInfo(component.getClass()).getPropertyDescriptors();
            for (PropertyDescriptor p : pd) {
                // we try to see if this is a ValueExpression by probing it
                ValueExpression ve = component.getValueExpression(p.getName());
                if (ve != null) {
                    parent.addExpression(trimExpression(ve.getExpressionString()), true);
                }
            }

        } catch (IntrospectionException e) {
            // TODO better error handling
            e.printStackTrace();
        }
    }

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
     * @return a map of {@link ExprTuple} keyed by property name
     */
    static private void findFloatingExpressions(UIRepeatListComponent parent, UIInstructions component) {

        String txt = component.toString();
        // see if it has some EL
        int n = txt.indexOf("#{");
        if (n > -1) {
            txt = txt.substring(n + 2);
            int e = txt.indexOf("}");
            if (e > -1) {
                txt = txt.substring(0, e);

                // we may have a mak:expr EL function here
                int f = txt.indexOf("mak:expr(");
                if (f > -1) {
                    txt = txt.substring(f + 9);
                    int fe = txt.indexOf(")");
                    if (fe > -1) {
                        txt = txt.substring(0, fe);

                        // trim surrounding quotes, might need to be more robust
                        txt = txt.substring(1, txt.length() - 1);

                        parent.addExpression(txt, false);
                    }
                } else {
                    parent.addExpression(txt, true);
                }
            }
        }
    }

    /**
     * Finds QL expressions inside a mak:value component
     * 
     * @param component
     *            the mak:value component
     * @return a map of {@link ExprTuple} keyed by property name
     */
    static private void findMakValueExpressions(UIRepeatListComponent parent, ValueComponent component) {

        // go thru all properties as for normal components, and also take into account non-EL (literal) expr values
        findComponentExpressions(parent, component);
        if (component.getExpr() == null) {
            // FIXME ProgrammerError
            throw new RuntimeException("no expr provided in mak:value!");
        } else {
            // TODO: setvalue expression
            // TODO: nullable value? i guess that's not in use any longer
            parent.addExpression(component.getExpr(), false);
        }

    }

    static private String trimExpression(String expr) {
        return expr.substring(2, expr.length() - 1);
    }

}
