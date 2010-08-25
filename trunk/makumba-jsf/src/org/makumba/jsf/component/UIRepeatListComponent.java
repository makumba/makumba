package org.makumba.jsf.component;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.ContextCallback;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectMany;
import javax.faces.component.UISelectOne;
import javax.faces.component.UIViewRoot;
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

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.commons.ArrayMap;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.RegExpUtils;
import org.makumba.jsf.FacesAttributes;
import org.makumba.jsf.MakumbaDataContext;
import org.makumba.jsf.PointerConverter;
import org.makumba.jsf.update.ObjectInputValue;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.ComposedSubquery;
import org.makumba.list.engine.Grouper;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProvider;

import com.sun.faces.facelets.compiler.UIInstructions;
import com.sun.faces.facelets.component.UIRepeat1;

public class UIRepeatListComponent extends UIRepeat1 implements MakumbaDataComponent {

    static final Logger log = java.util.logging.Logger.getLogger("org.makumba.jsf.component");

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

    transient Map<String, SetIterationContext> setComposedSubqueries = new HashMap<String, SetIterationContext>();

    // all data, from all iterations of the parent list
    transient Grouper listData;

    // current iteration of this list
    transient ArrayMap currentData;

    transient private String prefix;

    transient private UIRepeatListComponent parent;

    private static ThreadLocal<Stack<Dictionary<String, Object>>> currentDataStack = new ThreadLocal<Stack<Dictionary<String, Object>>>();

    transient private DataDefinition projections;

    transient List<Integer> visitedIndexes = new ArrayList<Integer>();

    transient int currentIndex = -1;

    transient List<ArrayMap> iterationGroupData;

    private boolean isObject;

    private List<String> editedLabels;

    private Map<String, ObjectInputValue> editedValues;

    private List<UISelectItem> selectItems = new ArrayList<UISelectItem>();

    private Map<String, SelectItemData> selectItemsSaved = new HashMap<String, SelectItemData>();

    private transient boolean selectItemReplacementDone = false;

    private UIComponent parentSelectComponent = null;

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setObject(Boolean isObject) {
        this.isObject = isObject;
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
        // System.out.println("UIRepeatListComponent.setRowIndex()" + rowIndex);
        visitedIndexes.add(rowIndex);
        currentIndex = rowIndex;
        // System.out.println(debugIdent() + " " + rowIndex);
        if (rowIndex >= 0 && rowIndex < iterationGroupData.size()) {

            // pop old value:
            currentDataStack.get().pop();
            currentData = iterationGroupData.get(rowIndex);
            // push new value:
            currentDataStack.get().push(currentData);
            // System.out.println(debugIdent() + " " + rowIndex + " " + iterationGroupData.size());

            for (SetIterationContext sc : setComposedSubqueries.values()) {
                sc.nextParentIteration();
            }

            setCurrentLabelInputValues();

            // make <f:selectItem> iterate
            if (isFirstRender() || getFacesContext().getCurrentPhaseId() == PhaseId.APPLY_REQUEST_VALUES
                    && !selectItemReplacementDone) {
                generateIteratingUISelectItems(rowIndex);
            }

        } else {
            // System.out.println(debugIdent() + " " + rowIndex);
        }

    }

    private void setCurrentLabelInputValues() {
        if (FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.UPDATE_MODEL_VALUES) {
            editedValues = new HashMap<String, ObjectInputValue>();
            for (String s : editedLabels) {
                editedValues.put(s, ObjectInputValue.makeUpdateInputValue(s, (Pointer) this.getExpressionValue(s)));
            }
        }
    }

    protected void onlyOuterListArgument(String s) {
        UIRepeatListComponent c = UIRepeatListComponent.findMakListParent(this, false);
        if (c != null) {
            throw new FaceletException(s + "can be indicated only for root mak:lists");
        }
    }

    public static UIRepeatListComponent findMakListParent(UIComponent current, boolean objectToo) {
        UIComponent c = current.getParent();
        while (c != null && !(c instanceof UIRepeatListComponent)) {
            // TODO: honor also objectToo
            c = c.getParent();
        }
        if (c instanceof UIRepeatListComponent) {
            return (UIRepeatListComponent) c;
        } else {
            return null;
        }
    }

    private boolean beforeIteration(final Object o) {
        if (findMakListParent(this, true) == null) {
            startMakListGroup(o);
        }
        // TODO: check whether we really want to keep the data in the grouper after iteration
        // this is only useful before a postback which will not request this list to re-render

        if (currentDataStack.get() != null) {
            iterationGroupData = listData != null ? listData.getData(currentDataStack.get(), false) : null;
        } else {
            iterationGroupData = null;
        }

        log.fine(debugIdent() + " --- startTag ----  " + o);

        if (iterationGroupData == null) {
            return false;
        }
        if (isObject && iterationGroupData.size() > 1) {
            throw new ProgrammerError("mak:object cannot have more than one row");
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
        // this is assumed by UIRepeat...
        getMakDataModel().setRowIndex(-1);

        setBegin(0);
        setEnd(iterationGroupData.size());
        parent = MakumbaDataContext.getDataContext().getCurrentList();

        MakumbaDataContext.getDataContext().setCurrentList(this);
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
        log.fine(debugIdent() + " --- endTag--- " + visitedIndexes + " " + o);
        iterationGroupData = null;

        currentIndex = -1;
        // this list is done, no more current value in stack
        currentDataStack.get().pop();
        MakumbaDataContext.getDataContext().setCurrentList(parent);
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
        log.fine(debugIdent() + " " + event.getComponent().getClientId() + " " + event);

        super.queueEvent(event);
    }

    @Override
    public void processDecodes(FacesContext faces) {

        // if we are nested inside of a UISelect, we make the <f:selectItem> elements iterate
        this.parentSelectComponent = findParentUISelect();
        if (parentSelectComponent != null) {
            addConverterToUISelectParent();

            visitTree(VisitContext.createVisitContext(getFacesContext()), new VisitCallback() {

                @Override
                public VisitResult visit(VisitContext context, UIComponent target) {
                    return VisitResult.ACCEPT;
                }
            });
            selectItemReplacementDone = true;
        }

        super.processDecodes(faces);
    }

    @Override
    public void process(FacesContext context, PhaseId p) {

        // log.fine(p + " " + composedQuery);
        if (!beforeIteration(p)) {
            return;
        }
        try {

            // if we are nested inside of a UISelect, we make the <f:selectItem> elements iterate
            if (isFirstRender()) {
                parentSelectComponent = findParentUISelect();
                if (parentSelectComponent != null) {
                    selectItems = collectSelectItems();
                    addConverterToUISelectParent();
                }
            }

            super.process(context, p);

            if (isFirstRender()) {
                for (UISelectItem ui : selectItems) {

                    // save for postback
                    SelectItemData d = new SelectItemData(ui.getValueExpression("itemDescription"),
                            ui.getItemDescription(), ui.getValueExpression("itemDisabled"), ui.isItemDisabled(),
                            ui.getValueExpression("itemEscaped"), ui.isItemEscaped(),
                            ui.getValueExpression("itemLabel"), ui.getItemLabel(), ui.getValueExpression("itemValue"),
                            ui.getItemValue(), ui.getValueExpression("noSelectOption"), ui.isNoSelectionOption());
                    selectItemsSaved.put(ui.getId(), d);

                    // remove the original component
                    ui.getParent().getChildren().remove(ui);
                }
                selectItemReplacementDone = true;
            }

            // we clean up after processing RENDER_RESPONSE
            if (getFacesContext().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
                if (parentSelectComponent != null) {
                    removeConverterFromUISelectParent();
                }
                selectItems.clear();
                parentSelectComponent = null;
            }

        } finally {
            afterIteration(p);
        }
    }

    private void addConverterToUISelectParent() {
        // we set a converter here so that the UISelect component can read the values from the list we give
        // it
        // TODO decide whether or not to keep this mechanism, or whether to return directly an array of
        // external pointer values in #getSetData
        ((EditableValueHolder) parentSelectComponent).setConverter(new PointerConverter());
    }

    private void removeConverterFromUISelectParent() {
        ((EditableValueHolder) parentSelectComponent).setConverter(null);
    }

    /**
     * Searches the tree for a parent UISelectOne or UISelectMany component.
     * 
     * @return a UIComponent if a parent is found, null otherwise
     */
    private UIComponent findParentUISelect() {
        UIComponent c = this;
        while (!(c.getParent() == null || c.getParent() instanceof UIViewRoot || c.getParent().getParent() instanceof UIRepeatListComponent)) {
            c = c.getParent();
        }
        if (c.getParent() != null && (c.getParent() instanceof UISelectOne || c.getParent() instanceof UISelectMany)) {
            return c.getParent();
        }
        return null;
    }

    /**
     * Does a static tree traversal and collects all {@link UISelectItem}-s it finds, until the next
     * {@link UIRepeatListComponent} is found
     * 
     * @return a List of {@link UISelectItem}-s that are children of this component (or of possible children).
     */
    private List<UISelectItem> collectSelectItems() {
        final List<UISelectItem> res = new ArrayList<UISelectItem>();

        // take all UISelectItem-s off the tree, store them in a list, so we put them back into the tree
        // later on, when we iterate
        Util.visitStaticTree(this, new VisitCallback() {

            private UIComponent initialTarget;

            @Override
            public VisitResult visit(VisitContext context, UIComponent target) {
                if (initialTarget == null) {
                    initialTarget = target;
                }
                if (target instanceof UISelectItem) {
                    UISelectItem original = (UISelectItem) target;
                    res.add(original);
                    // original.setId(original.getId() + "_REMOVE");

                    // target.getParent().getChildren().remove(target);
                    return VisitResult.REJECT;
                }
                if (target instanceof UIRepeatListComponent && !target.equals(initialTarget)) {
                    return VisitResult.REJECT;
                }
                return VisitResult.ACCEPT;
            }
        });

        return res;
    }

    // UIRepeat iterates more than it should so we keep a list of already iterated indexes for our processing
    // this list also takes into account whether the context is ready for meaningful iteration (i.e. if EL expressions
    // can be properly evaluated)
    private List<Integer> iteratedIndexes = new ArrayList<Integer>();

    /**
     * Makes <f:selectItem /> iterate by generating the right amount of children via iteration over them, based on the
     * original {@link UISelectItem}-s (collected or serialized). This iteration needs to happen at the end of
     * APPLY_REQUEST_VALUES or in any case before PROCESS_VALIDATIONS, so that the parent {@link UISelectOne} or
     * {@link UISelectMany} component have them at their disposal during that phase. UISelect* components use a special
     * iterator that looks for direct children to find out about possible options.
     * 
     * @param rowIndex
     *            the index of the current iteration
     */
    private void generateIteratingUISelectItems(int rowIndex) {
        if (parentSelectComponent != null) {

            if (!selectItemsSaved.isEmpty() && getFacesContext().isPostback()
                    && getFacesContext().getCurrentPhaseId() == PhaseId.APPLY_REQUEST_VALUES) {

                // we already had a postback, the original UISelectItem-s are no longer part of the tree
                // thus we take them from a reference list that we keep
                for (String id : selectItemsSaved.keySet()) {
                    UISelectItem item = selectItemsSaved.get(id).restore(id, rowIndex);
                    // we have to set these elements to be transient or faces will not be able to save its state
                    // correctly due to a problem with the clientIds of the select components
                    item.setTransient(true);

                    boolean isValidItem = item.getItemValue() != null;

                    if (!iteratedIndexes.contains(new Integer(rowIndex)) && isValidItem) {

                        parentSelectComponent.getChildren().add(item);
                        // System.out.println("********************************* generated item from saved one: "
                        // + item.getItemLabel() + " val:" + item.getItemValue());

                        iteratedIndexes.add(new Integer(rowIndex));
                    }

                }

            } else {
                // this is the first rendering
                // we generate the items from the collection we keep
                for (UISelectItem original : selectItems) {
                    UISelectItem it = new UISelectItem();
                    it.setId(original.getId() + rowIndex);
                    it.setItemDescription(original.getItemDescription());
                    it.setItemDisabled(original.isItemDisabled());
                    it.setItemEscaped(original.isItemEscaped());
                    it.setItemLabel(original.getItemLabel());
                    it.setItemValue(original.getItemValue());
                    it.setNoSelectionOption(original.isNoSelectionOption());

                    // we have to set these elements to be transient or faces will not be able to save its state
                    // correctly
                    // due to a problem with the clientIds of the select components
                    it.setTransient(true);

                    parentSelectComponent.getChildren().add(it);
                }
            }
        }
    }

    private boolean isFirstRender() {
        return !getFacesContext().isPostback() && getFacesContext().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE;
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
        log.fine(debugIdent() + " INVOKE " + clientId + " " + callback);
        return super.invokeOnComponent(faces, clientId, callback);
    }

    @Override
    public boolean visitTree(final VisitContext context, final VisitCallback callback) {
        Collection<String> c = context.getFacesContext().getPartialViewContext().getRenderIds();
        log.fine(debugIdent() + " renderedIds " + c);

        VisitCallback clbk = callback;

        // in restore_view we cannot run beforeIteration as we have no data
        // so we run it after the visit
        if (context.getFacesContext().getCurrentPhaseId() == PhaseId.RESTORE_VIEW) {
            context.invokeVisitCallback(this, callback);
        }
        if (!beforeIteration(callback)) {
            return false;
        }
        log.fine(debugIdent()
                + " will visit "
                + (context.getSubtreeIdsToVisit(this) == VisitContext.ALL_IDS ? "all"
                        : context.getSubtreeIdsToVisit(this)));

        try {
            return super.visitTree(context, clbk);

        } finally {
            afterIteration(callback);
            /*
             EXAMPLE validation error at the very end of update model
                        if (context.getFacesContext().getCurrentPhaseId() == PhaseId.UPDATE_MODEL_VALUES) {
                            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "wrong shit", "wrong stuff");
                            context.getFacesContext().addMessage("f:bigList:0:smallList:0:langIn", message);
                            // context.getFacesContext().validationFailed();
                        }
            */
        }

    }

    private boolean isSaveOrRestore(final VisitCallback callback) {
        Class<?> c = callback.getClass();
        if (c.isAnonymousClass()) {
            c = c.getEnclosingClass();
        }
        return Arrays.asList(c.getInterfaces()).contains(StateManagementStrategy.class);
    }

    static int composedQueries = NamedResources.makeStaticCache("JSF ComposedQueries", new NamedResourceFactory() {

        private static final long serialVersionUID = 6071679345211493029L;

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

    public void startMakListGroup(final Object o) {

        readComposedQuery();

        final QueryProvider qep = useSeparateTransactions() ? null : getQueryExecutionProvider();

        startIterationGroup(o);

        try {

            // we only execute queries during RENDER_RESPONSE
            // we might even skip that if we have data (listData!=null)
            if (FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
                executeGroupQueries(qep);
            }

        } finally {
            if (qep != null) {
                qep.close();
            }
        }

    }

    private void startIterationGroup(Object o) {
        log.fine(debugIdent() + " ------------- start -------- " + o);
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
        log.fine(debugIdent() + " ----------- end ----------- " + o);
    }

    private void executeGroupQueries(final QueryProvider qep) {
        Util.visitStaticTree(this, new VisitCallback() {
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

    private void computeComposedQuery() {
        UIRepeatListComponent parent = UIRepeatListComponent.findMakListParent(this, true);
        if (parent == null) {
            // no parent, we are root
            this.composedQuery = new ComposedQuery(this.queryProps, this.getQueryLanguage(), true);
        } else {
            this.composedQuery = new ComposedSubquery(this.queryProps, parent.composedQuery, this.getQueryLanguage(),
                    true);
        }
        this.composedQuery.init();
        this.editedLabels = new ArrayList<String>();
        this.findExpressionsInChildren();
        if (parent == null) {
            this.analyzeMakListGroup();
        }
        // we make sure that all declared labels are selected separately
        this.composedQuery.analyze();
        // log.fine(this.composedQuery);

        this.projections = getQueryAnalysis().getProjectionType();
    }

    private void analyzeMakListGroup() {
        Util.visitStaticTree(this, new VisitCallback() {
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

        if (Util.validationFailed()) {
            log.fine(debugIdent() + " -- not running query because of validation errors -- " + composedQuery);
            return;
        }
        try {
            log.fine(debugIdent() + " --run-- " + composedQuery);
            listData = composedQuery.execute(qep, null, evaluator, offset, limit);
            for (SetIterationContext sc : setComposedSubqueries.values()) {
                sc.execute(qep, offset, limit);
            }
        } finally {
            if (useSeparateTransactions()) {
                qep.close();
            }
        }
    }

    private QueryProvider getQueryExecutionProvider() {
        return QueryProvider.makeQueryRunner(TransactionProvider.getInstance().getDefaultDataSourceName(),
            getQueryLanguage(), FacesAttributes.getAttributes(FacesContext.getCurrentInstance()));

    }

    public String getQueryLanguage() {
        // TODO: get the query language from taglib URI, taglib name, or configuration
        return "oql";
    }

    private void addExpression(UIComponent component, String expr, boolean canBeInvalid) {
        // we compute a base label even if this expression may not be a.b.c.d
        String label = expr;
        String fieldPath = null;
        int n = expr.indexOf(".");
        if (n != -1) {
            label = expr.substring(0, n);
            fieldPath = expr.substring(label.length() + 1);
        }

        if (canBeInvalid) {
            // we assume here only expressions a.b.c.d
            QueryAnalysis qa = getQueryAnalysis();
            if (qa.getLabelType(label) == null) {
                // label unknown, we go out
                return;
            }

            // TODO: check whether the fields are ok!

        }

        // detect sets, make a virtual subquery for them so we can resolve them
        QueryAnalysis qa = getQueryAnalysis();
        FieldDefinition setFd = qa.getLabelType(label).getFieldOrPointedFieldDefinition(fieldPath);
        if (fieldPath != null && !expr.endsWith(".id") && setFd.isSetType()) {

            // FIXME the set expression needs to be added to the list component to which the expression correlates
            // i.e. to the list component that declares the base label of the expression.
            // currently, if a set expression referring to a parent list component is used in a child list component
            // it will utterly fail.
            // the trouble with fixing this is that the discovery needs to be done _before_ the parent list computes its
            // composed query
            SetIterationContext sc = new SetIterationContext(composedQuery, getQueryLanguage(), expr, setFd);
            setComposedSubqueries.put(expr, sc);

        } else {
            composedQuery.checkProjectionInteger(expr);
        }

        if (component instanceof EditableValueHolder) {
            // we assume here only expressions a.b.c.d
            MakumbaDataComponent c = MakumbaDataComponent.Util.findLabelDefinitionComponent(component, label);
            if (c instanceof UIRepeatListComponent) {
                ((UIRepeatListComponent) c).addEditedLabel(label);
            }
        }
    }

    private void addEditedLabel(String label) {
        composedQuery.getProjectionIndex(label);
        editedLabels.add(label);
    }

    private QueryAnalysis getQueryAnalysis() {
        return QueryProvider.getQueryAnalzyer(getQueryLanguage()).getQueryAnalysis(composedQuery.getComputedQuery());
    }

    public boolean hasExpression(String expr) {
        return composedQuery.getProjectionIndex(expr) != null;
    }

    public Integer getExpressionIndex(String expr) {
        Integer exprIndex = composedQuery.getProjectionIndex(expr);
        if (exprIndex == null) {
            if (useCaches()) {
                // FIXME: a better mak:list description
                throw new ProgrammerError("<mak:list> does not know the expression " + expr
                        + ", turn caches off, or try reloading the page, it might work.");
            } else {
                // we should never get here since a page analysis is done every request
                // so the expression must be known
                throw new MakumbaError("invalid state, unknown expression " + expr);
            }
        }
        return exprIndex;
    }

    public String convertToString(String expr) {
        int n = getExpressionIndex(expr);
        return convertToString(projections.getFieldDefinition(n), getExpressionValue(n));
    }

    private String convertToString(FieldDefinition fd, Object val) {
        if (fd.getType().startsWith("ptr")) {
            return ((Pointer) val).toExternalForm();
        }
        return "" + val;
    }

    public Object convertExpression(UIComponent component, Object value) {
        String expr = component.getValueExpression("value").getExpressionString();
        // take away #{ }
        expr = expr.substring(2, expr.length() - 1).trim();

        if (this.hasSetProjection(expr)) {
            String ext = (String) value;
            return new Pointer(getSetProjectionType(expr).getName(), ext);
        } else if (this.hasExpression(expr)) {
            // FIXME do conversion if necessary
            return value;
        }

        return value;
    }

    public Object validateExpression(UIComponent component, Object value) {
        String expr = component.getValueExpression("value").getExpressionString();
        // take away #{ }
        expr = expr.substring(2, expr.length() - 1).trim();

        if (this.hasExpression(expr)) {
            // FIXME validate
            return this.getExpressionType(expr).checkValue(value);
        } else if (this.hasSetProjection(expr)) {
            // FIXME validate too
            return value;
        }

        return value;

    }

    public FieldDefinition getExpressionType(String expr) {
        return projections.getFieldDefinition(getExpressionIndex(expr));
    }

    public Object getExpressionValue(String expr) {
        if (getMakDataModel().getRowIndex() == -1) {
            // we're not iterating, our value is not correct
            return null;
        }
        return getExpressionValue(getExpressionIndex(expr));
    }

    public Object getExpressionValue(int exprIndex) {
        return currentData.data[exprIndex];
    }

    public void setExpressionValue(String expr, Object val) {
        currentData.data[getExpressionIndex(expr)] = val;
    }

    private void findExpressionsInChildren() {
        Util.visitStaticTree(this, new VisitCallback() {
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
                    addExpression(component, trimExpression(ve.getExpressionString()), true);
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
                    addExpression(component, elFuncTxt, false);
                } else {
                    // TODO logger warning or namespace resolution
                }
            }
            // remove the EL function calls from the global expression to avoid wrong matches of the rest
            elExprTxt = exprFuncMatcher.replaceAll("");

            // we now have a cleared expression, we check for paths like "p.name"
            Matcher dotPathMatcher = dotPathPattern.matcher(elExprTxt);
            while (dotPathMatcher.find()) {
                addExpression(component, dotPathMatcher.group(), true);
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
            addExpression(component, component.getExpr(), false);
        }

    }

    protected String trimExpression(String expr) {
        return expr.substring(2, expr.length() - 1);
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
        this.listData = (Grouper) state[1];
        this.composedQuery = (ComposedQuery) state[2];
        @SuppressWarnings("unchecked")
        Map<String, SetIterationContext> csq = (Map<String, SetIterationContext>) state[3];
        this.setComposedSubqueries = csq;
        this.projections = getQueryAnalysis().getProjectionType();
        @SuppressWarnings("unchecked")
        List<String> x = (List<String>) state[4];
        this.editedLabels = x;
        @SuppressWarnings("unchecked")
        Map<String, SelectItemData> d = (Map<String, SelectItemData>) state[5];
        this.selectItemsSaved = d;

        getMakDataModel().makList = this;
    }

    @Override
    public Object saveState(FacesContext faces) {
        if (faces == null) {
            throw new NullPointerException();
        }
        log.fine(debugIdent() + " save with key " + this.getClientId(FacesContext.getCurrentInstance()));

        Object[] state = new Object[8];

        state[0] = super.saveState(faces);

        state[1] = listData;
        state[2] = composedQuery;
        state[3] = this.setComposedSubqueries;
        state[4] = editedLabels;
        state[5] = selectItemsSaved;

        // TODO: save other needed stuff
        return state;
    }

    private UIRepeatListComponent findMakListParent() {
        return findMakListParent(UIRepeatListComponent.this, true);
    }

    @Override
    public void addValue(String label, String path, Object value, String clientId) {
        editedValues.get(label).addField(path, value, clientId);
    }

    @Override
    public void addSetValue(String label, String path, Pointer[] value, String clientId) {
        editedValues.get(label).addSetField(path, value, clientId);
    }

    public boolean hasSetProjection(String path) {
        return setComposedSubqueries.get(path) != null;
    }

    public DataDefinition getSetProjectionType(String path) {
        return setComposedSubqueries.get(path).getSetElementType();
    }

    public Pointer[] getSetData(String path) {
        return setComposedSubqueries.get(path).getSetData();
    }

    private static String getSetLabel(String path) {
        return path.replace('.', '_');
    }

    private static class SetIterationContext implements Serializable {

        private static final long serialVersionUID = 1L;

        private ComposedQuery composedQuery;

        private Grouper grouper;

        private transient Pointer[] setData;

        private String setLabel;

        private String titleProjection;

        public SetIterationContext(ComposedQuery superQuery, String queryLanguage, String expr, FieldDefinition setFd) {
            // create a new composed query, search for the set member pointer and title
            this.setLabel = getSetLabel(expr);
            String queryProps[] = new String[5];
            // TODO in the JSP ValueComputer there was a JOIN added for HQL, not sure it's needed any longer
            queryProps[ComposedQuery.FROM] = expr + " " + setLabel;
            this.composedQuery = new ComposedSubquery(queryProps, superQuery, queryLanguage, true);
            this.composedQuery.init();
            this.composedQuery.checkProjectionInteger(setLabel);
            this.titleProjection = setLabel + "." + setFd.getPointedType().getTitleFieldName();
            this.composedQuery.checkProjectionInteger(titleProjection);
            this.composedQuery.analyze();
        }

        public void execute(QueryProvider qep, int offset, int limit) {
            this.grouper = composedQuery.execute(qep, null, evaluator, offset, limit);
        }

        public void nextParentIteration() {
            List<ArrayMap> data = grouper.getData(currentDataStack.get(), false);
            // setData = new SetList<String>();
            setData = new Pointer[data.size()];
            // the set might be empty for the current stack
            if (data == null || data.size() == 0) {
                return;
            }
            for (int i = 0; i < data.size(); i++) {
                // this.setData.add(((Pointer) a.data[this.composedQuery.getProjectionIndex(this.setLabel)]));
                setData[i] = (Pointer) data.get(i).data[this.composedQuery.getProjectionIndex(this.setLabel)];
                // this.setData.getTiteList().add(
                // (String) a.data[this.composedQuery.getProjectionIndex(this.titleProjection)]);
            }
        }

        public DataDefinition getSetElementType() {
            return composedQuery.getFromLabelTypes().get(setLabel);
        }

        public Pointer[] getSetData() {
            return setData;
        }
    }

    private static class SelectItemData implements Serializable {

        private static final long serialVersionUID = 1L;

        private ValueExpression description;

        private String descriptionValue;

        private ValueExpression disabled;

        private boolean disabledValue;

        private ValueExpression escaped;

        private boolean escapedValue;

        private ValueExpression label;

        private String labelValue;

        private ValueExpression value;

        private Object valueValue;

        private ValueExpression noSelectOption;

        private boolean noSelectOptionValue;

        public SelectItemData(ValueExpression description, String descriptionValue, ValueExpression disabled,
                boolean disabledValue, ValueExpression escaped, boolean escapedValue, ValueExpression label,
                String labelValue, ValueExpression value, Object valueValue, ValueExpression noSelectOption,
                boolean noSelectOptionValue) {
            super();
            this.description = description;
            this.descriptionValue = descriptionValue;
            this.disabled = disabled;
            this.disabledValue = disabledValue;
            this.escaped = escaped;
            this.escapedValue = escapedValue;
            this.label = label;
            this.labelValue = labelValue;
            this.value = value;
            this.valueValue = valueValue;
            this.noSelectOption = noSelectOption;
            this.noSelectOptionValue = noSelectOptionValue;
        }

        public UISelectItem restore(String id, int index) {
            UISelectItem it = new UISelectItem();
            it.setId(id + index);

            if (description != null) {
                it.setItemDescription((String) getValue(description));
            } else {
                it.setItemDescription(descriptionValue);
            }

            if (disabled != null) {
                it.setItemDisabled((Boolean) getValue(disabled));
            } else {
                it.setItemDisabled(disabledValue);
            }

            if (escaped != null) {
                it.setItemEscaped((Boolean) getValue(escaped));
            } else {
                it.setItemEscaped(escapedValue);
            }

            if (label != null) {
                it.setItemLabel((String) getValue(label));
            } else {
                it.setItemLabel(labelValue);
            }

            if (value != null) {
                it.setItemValue(getValue(value));
            } else {
                it.setItemValue(valueValue);
            }

            if (noSelectOption != null) {
                it.setNoSelectionOption((Boolean) getValue(noSelectOption));
            } else {
                it.setNoSelectionOption(noSelectOptionValue);
            }

            return it;
        }

        private Object getValue(ValueExpression ve) {
            return ve.getValue(FacesContext.getCurrentInstance().getELContext());
        }

    }

    private static final class MakListDataModel extends ListDataModel<ArrayMap> implements Serializable {

        private static final long serialVersionUID = 6764780265781314875L;

        transient UIRepeatListComponent makList;

        private MakListDataModel() {
            super(null);
        }

        @Override
        public void setRowIndex(int rowIndex) {
            super.setRowIndex(rowIndex);
            try {
                makList.setRowIndex(rowIndex);
            } catch (NullPointerException e) {
                // this only happens at construction
            }
        }
    }
}
