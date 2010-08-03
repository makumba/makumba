package org.makumba.jsf;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.ContextCallback;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;
import javax.faces.model.ListDataModel;
import javax.faces.view.StateManagementStrategy;
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
    static final Logger log = java.util.logging.Logger.getLogger("org.makumba.jsf");

    private static final class MakListDataModel extends ListDataModel<ArrayMap> implements Serializable {
        transient UIRepeatListComponent makList;

        private MakListDataModel() {
            super(null);
        }

        @Override
        public void setRowIndex(int rowIndex) {
            try {
                makList.setRowIndex(rowIndex);
            } catch (NullPointerException e) {
                // this only happens at construction
            }
            super.setRowIndex(rowIndex);

        }
    }

    public UIRepeatListComponent() {
        setValue(new MakListDataModel());
        getMakDataModel().makList = this;
    }

    static final private Dictionary<String, Object> NOTHING = new ArrayMap();

    String[] queryProps = new String[6];

    String separator = "";

    // TODO: no clue what defaultLimit does
    int offset = 0, limit = -1, defaultLimit;

    transient ComposedQuery composedQuery;

    // all data, from all iterations of the parent list
    transient Grouper listData;

    // current iteration of this list
    transient ThreadLocal<ArrayMap> currentData = new ThreadLocal<ArrayMap>();

    private String prefix;

    private UIRepeatListComponent parent;

    private static ThreadLocal<UIRepeatListComponent> currentList = new ThreadLocal<UIRepeatListComponent>();

    private static ThreadLocal<Stack<Dictionary<String, Object>>> currentDataStack = new ThreadLocal<Stack<Dictionary<String, Object>>>();

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

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

    void setRowIndex(int rowIndex) {
        visitedIndexes.add(rowIndex);
        currentIndex = rowIndex;
        // System.out.println(debugIdent() + " " + rowIndex);
        if (rowIndex >= 0 && rowIndex < iterationGroupData.size()) {

            // pop old value:
            currentDataStack.get().pop();
            currentData.set(iterationGroupData.get(rowIndex));
            // push new value:
            currentDataStack.get().push(currentData.get());
            // System.out.println(debugIdent() + " " + rowIndex + " " + iterationGroupData.size());

        } else {
            // System.out.println(debugIdent() + " " + rowIndex);
        }

    }

    protected void onlyOuterListArgument(String s) {
        UIRepeatListComponent c = UIRepeatListComponent.findMakListParent(this, false);
        if (c != null) {
            throw new FaceletException(s + "can be indicated only for root mak:lists");
        }
    }

    static UIRepeatListComponent findMakListParent(UIComponent current, boolean objectToo) {
        UIComponent c = current.getParent();
        while (c != null && !(c instanceof UIRepeatListComponent)) {
            // TODO: honor also objectToo
            c = c.getParent();
        }

        return (UIRepeatListComponent) c;
    }

    List<Integer> visitedIndexes = new ArrayList<Integer>();

    int currentIndex = -1;

    List<ArrayMap> iterationGroupData;

    private boolean beforeIteration(final Object o) {
        if (findMakListParent(this, true) == null) {
            startMakListGroup(o);
        }
        // TODO: check whether we really want to keep the data in the grouper after iteration
        // this is only useful before a postback which will not request this list to re-render

        iterationGroupData = listData != null ? listData.getData(currentDataStack.get(), false) : null;
        System.out.println(debugIdent() + " --- startTag ----  " + o);

        if (iterationGroupData == null) {
            return false;
        }
        /*
        for (ArrayMap a : iterationGroupData) {
            System.out.print("\t");
            for (Object o : a.data) {
                System.out.print("\t" + o);
            }
            log.fine();
        }
        */

        // push a placeholder, it will be popped at first iteration
        currentDataStack.get().push(NOTHING);
        visitedIndexes.clear();

        getMakDataModel().setWrappedData(iterationGroupData);

        setBegin(0);
        setEnd(iterationGroupData.size());
        parent = getCurrentlyRunning();

        currentList.set(this);
        return true;
    }

    private MakListDataModel getMakDataModel() {
        return (MakListDataModel) getValue();
    }

    protected UIRepeatListComponent findMakListRoot() {
        UIRepeatListComponent p = findMakListParent();
        if (p == null) {
            return this;
        }
        return p.findMakListRoot();
    }

    private void afterIteration(Object o) {
        System.out.println(debugIdent() + " --- endTag--- " + visitedIndexes + " " + o);
        iterationGroupData = null;
        currentIndex = -1;
        // this list is done, no more current value in stack
        currentDataStack.get().pop();
        currentList.set(parent);
        if (findMakListParent(this, true) == null) {
            endIterationGroup(o);
        }
    }

    @Override
    public void queueEvent(FacesEvent event) {
        /* 
         * here we can detect Ajax and ValueChanged events, but they are always sent to the root mak:list
        no matter which mak:list is the target of the f:ajax render= 
         */
        System.out.println(debugIdent() + " " + event.getComponent().getClientId());

        super.queueEvent(event);
    }

    @Override
    public void process(FacesContext context, PhaseId p) {

        // log.fine(p + " " + composedQuery);
        if (!beforeIteration(p)) {
            return;
        }
        try {
            super.process(context, p);
        } finally {
            afterIteration(p);
        }
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        // log.fine(p + " " + composedQuery);
        if (!beforeIteration(event)) {
            return;
        }
        try {
            super.broadcast(event);
        } finally {
            afterIteration(event);
        }
    }

    @Override
    public boolean invokeOnComponent(FacesContext faces, String clientId, ContextCallback callback)
            throws FacesException {
        System.out.println(debugIdent() + " INVOKE " + clientId + " " + callback);
        return super.invokeOnComponent(faces, clientId, callback);
    }

    @Override
    public boolean visitTree(final VisitContext context, final VisitCallback callback) {

        if (listData == null) {

            // there is no data, hopefully we are in the process of restoring it
            // so we call the visiting only on this component
            // since we had no data we probably had no structure either, so now we can wrap strage things
            if (findMakListParent(this, true) == null) {
                // wrapUIInstrutions();
            }
        }

        // we make sure we are visited despite UIRepeat
        context.invokeVisitCallback(this, callback);

        // if there's no data, we should not iterate
        if (!beforeIteration(callback)) {
            return true;
        }
        System.out.println(debugIdent()
                + " will visit "
                + (context.getSubtreeIdsToVisit(this) == VisitContext.ALL_IDS ? "all"
                        : context.getSubtreeIdsToVisit(this)));

        try {
            return super.visitTree(context, callback);
            /*
            
            , new VisitCallback() {
            VisitContext theContext = context;

            @Override
            public String toString() {
                return theContext.getClass().getName();
            }

            @Override
            public VisitResult visit(VisitContext c, UIComponent target) {
                if (target == UIRepeatListComponent.this) {
                    return VisitResult.ACCEPT;
                }
                // log.fine(target.getClass());
                return callback.visit(context, target);
            }

            });
            */
        } finally {
            afterIteration(callback);
        }
    }

    private boolean isSaveOrRestore(final VisitCallback callback) {
        Class<?> c = callback.getClass();
        if (c.isAnonymousClass()) {
            c = c.getEnclosingClass();
        }
        return Arrays.asList(c.getInterfaces()).contains(StateManagementStrategy.class);
    }

    public static UIRepeatListComponent getCurrentlyRunning() {
        return currentList.get();

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

    static void visitStaticTree(UIComponent target, VisitCallback c) {
        if (c.visit(null, target) == VisitResult.REJECT) {
            return;
        }
        for (UIComponent kid : target.getChildren()) {
            visitStaticTree(kid, c);
        }
    }

    public void startMakListGroup(final Object o) {

        readComposedQuery();

        final QueryProvider qep = useSeparateTransactions() ? null : getQueryExecutionProvider();

        startIterationGroup(o);

        try {

            // we only execute queries during RENDER_RESPONSE
            // we might even skip that if we have data (listData!=null)
            if (o == PhaseId.RENDER_RESPONSE) {
                if (listData == null) {
                    // if we had no data, we probably had no structure, so we explore it now
                    // wrapUIInstrutions();
                }
                executeGroupQueries(qep);
            }

            if (o == PhaseId.UPDATE_MODEL_VALUES) {
                visitStaticTree(this, new VisitCallback() {
                    @Override
                    public VisitResult visit(VisitContext context, UIComponent target) {
                        if (target instanceof ValueHolder) {
                            ((ValueHolder) target).setValue(null);
                        }
                        return VisitResult.ACCEPT;
                    }
                });
            }

        } finally {
            if (qep != null) {
                qep.close();
            }
        }

    }

    private void startIterationGroup(Object o) {
        System.out.println(debugIdent() + " ------------- start -------- " + o);
        // we are in root, we initialize the data stack
        currentDataStack.set(new Stack<Dictionary<String, Object>>());

        // and we push the key needed for the root mak:list to find its data (see beforeIteration)
        currentDataStack.get().push(NOTHING);
    }

    public String debugIdent() {
        return "f:" + idObject(FacesContext.getCurrentInstance()) + " " + "l:" + idList(this);
    }

    private String idList(UIRepeatListComponent comp) {
        if (comp == null) {
            return "";
        }
        String s = idList(comp.findMakListParent());
        if (s.length() > 0) {
            s += ":";
        }
        String iterData = "";
        if (comp != null) {
            iterData = "(" + comp.currentIndex;
            if (comp.iterationGroupData != null) {
                iterData += ":" + comp.iterationGroupData.size();
            } else {
                iterData += ":0";
            }
            iterData += ")";
        }

        return s + idObject(comp) + iterData;
    }

    private String idObject(Object pr) {
        return pr != null ? Integer.toHexString(pr.hashCode()) : "";
    }

    private void endIterationGroup(Object o) {

        currentDataStack.get().pop();

        // we are in root, we initialize the data stack
        currentDataStack.set(null);
        System.out.println(debugIdent() + " ----------- end ----------- " + o);
        if (FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            System.out.println(debugIdent() + " set saving");
            visitStaticTree(this, new VisitCallback() {

                @Override
                public VisitResult visit(VisitContext context, UIComponent target) {
                    if (target instanceof UIRepeatListComponent) {
                        ((UIRepeatListComponent) target).saving = true;
                    }
                    return null;
                }

            });
        }

    }

    private void executeGroupQueries(final QueryProvider qep) {
        visitStaticTree(this, new VisitCallback() {
            @Override
            public VisitResult visit(VisitContext context, UIComponent target) {
                if (target instanceof UIRepeatListComponent) {
                    ((UIRepeatListComponent) target).executeQuery(qep);
                }
                return VisitResult.ACCEPT;
            }
        });
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
        UIRepeatListComponent parent = UIRepeatListComponent.findMakListParent(this, true);
        if (parent == null) {
            // no parent, we are root
            this.composedQuery = new ComposedQuery(this.queryProps, this.getQueryLanguage(), true);
        } else {
            this.composedQuery = new ComposedSubquery(this.queryProps, parent.composedQuery, this.getQueryLanguage(),
                    true);
        }
        this.composedQuery.init();
        this.findExpressionsInChildren();
        if (parent == null) {
            this.analyzeMakListGroup();
        }
        // we make sure that all declared labels are selected separately
        this.composedQuery.analyze();
        // log.fine(this.composedQuery);
    }

    void analyzeMakListGroup() {
        visitStaticTree(this, new VisitCallback() {
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

    private void executeQuery(QueryProvider qep) {
        // by now the query was cached so we fetch it
        readComposedQuery();
        if (useSeparateTransactions()) {
            qep = getQueryExecutionProvider();
        }

        try {
            System.out.println(debugIdent() + " --run-- " + composedQuery);
            listData = composedQuery.execute(qep, null, evaluator, offset, limit);
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
        return currentData.get().data[exprIndex];
    }

    void findExpressionsInChildren() {
        visitStaticTree(this, new VisitCallback() {
            @Override
            public VisitResult visit(VisitContext context, UIComponent target) {
                if (target instanceof UIRepeatListComponent && target != UIRepeatListComponent.this) {
                    return VisitResult.REJECT;
                }

                // log.fine(target);

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
                /*                
                 // TODO: this successfully adds the converter but fails after form submission (classcast exception during state save or restore
                  
                                if (p.getName().equals("value") && component instanceof EditableValueHolder) {
                                    ((ValueHolder) component).setConverter(FacesContext.getCurrentInstance().getApplication().createConverter(
                                        "makPtr"));
                                }

                */

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

            while (exprFuncMatcher.find()) {
                String elFuncTxt = exprFuncMatcher.group();

                if (elFuncTxt.startsWith(prefix)) {
                    elFuncTxt = elFuncTxt.substring(prefix.length() + ":expr(".length(), elFuncTxt.length() - 1);

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

    // Hack: when the rendering finished, we enter saving state and never return digits (:0) in the key
    boolean saving = false;

    @Override
    public String getClientId(FacesContext faces) {
        String id = super.getClientId(faces);
        return cleanId(id);
    }

    @Override
    public String getContainerClientId(FacesContext faces) {
        String id = super.getContainerClientId(faces);
        return cleanId(id);
    }

    private String cleanId(String id) {
        if (!saving) {
            return id;
        }
        while (true) {
            int n = id.indexOf(":0");
            if (n == -1) {
                return id;
            }
            id = id.substring(0, n) + id.substring(n + 2);
        }
    }

    @Override
    public void restoreState(FacesContext faces, Object object) {
        if (faces == null) {
            throw new NullPointerException();
        }
        if (object == null) {
            return;
        }
        Object[] state = (Object[]) object;
        super.restoreState(faces, state[0]);
        // noinspection unchecked
        this.listData = (Grouper) state[1];
        this.composedQuery = (ComposedQuery) state[2];
        getMakDataModel().makList = this;
    }

    @Override
    public Object saveState(FacesContext faces) {
        if (faces == null) {
            throw new NullPointerException();
        }
        System.out.println(debugIdent() + " save with key " + this.getClientId(FacesContext.getCurrentInstance())
                + " superKey " + super.getClientId(FacesContext.getCurrentInstance()));

        Object[] state = new Object[8];

        state[0] = super.saveState(faces);

        state[1] = listData;
        state[2] = composedQuery;
        // TODO: save other needed stuff
        return state;
    }

    private UIRepeatListComponent findMakListParent() {
        return findMakListParent(UIRepeatListComponent.this, true);
    }

}
