package org.makumba.jsf.component;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;

import org.makumba.DataDefinition;
import org.makumba.OQLParseError;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.jsf.update.ObjectInputValue;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.ComposedSubquery;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;

/**
 * Component that allows creation of new objects
 * 
 * @author manu
 */
public class CreateObjectComponent extends UIComponentBase implements MakumbaDataComponent {

    static final Logger log = java.util.logging.Logger.getLogger("org.makumba.jsf.component");

    private String[] queryProps = new String[6];

    private CreateObjectComponent parent;

    private ObjectInputValue currentValue;

    private ComposedQuery cQ;

    private QueryAnalysis qA;

    private static ThreadLocal<CreateObjectComponent> currentCreateObject = new ThreadLocal<CreateObjectComponent>();

    public String getFrom() {
        return queryProps[ComposedQuery.FROM];
    }

    public void setFrom(String from) {
        queryProps[ComposedQuery.FROM] = from;
    }

    public String getWhere() {
        return queryProps[ComposedQuery.WHERE];
    }

    public void setWhere(String where) {
        queryProps[ComposedQuery.WHERE] = where;
    }

    @Override
    public String getFamily() {
        return "makumba";
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        beforeObject();
        try {
            super.broadcast(event);
        } finally {
            afterObject();
        }
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        beforeObject();
        try {
            super.encodeChildren(context);
        } finally {
            afterObject();
        }
    }

    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        beforeObject();
        try {
            super.processEvent(event);
        } finally {
            afterObject();
        }
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        beforeObject();
        super.encodeBegin(context);
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        try {
            super.encodeEnd(context);
        } finally {
            afterObject();
        }
    }

    @Override
    public void processUpdates(FacesContext context) {
        beforeObject();
        try {
            super.processUpdates(context);
        } finally {
            afterObject();
        }
    }

    @Override
    public void processValidators(FacesContext context) {
        beforeObject();
        try {
            super.processValidators(context);
        } finally {
            afterObject();
        }
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        super.restoreState(context, state);
    }

    private QueryAnalysis initQueryAnalysis() {
        if (this.qA == null) {
            this.qA = computeQueryAnalysis();
        }
        return this.qA;
    }

    private ComposedQuery initComposedQuery(MakumbaDataComponent parent) {
        if (this.cQ == null) {
            this.cQ = computeComposedQuery(parent);
        }
        return this.cQ;
    }

    /**
     * Sets the currently running {@link CreateObjectComponent} to this, saves its parent if any, runs the query
     * analysis for this component, and initialises the ObjectInputValue
     */
    private void beforeObject() {
        parent = getCurrentlyRunning();
        currentCreateObject.set(this);
        initQueryAnalysis();
        if (FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.UPDATE_MODEL_VALUES) {
            initObjectInputValue();
        }
    }

    /**
     * Sets the currently running {@link CreateObjectComponent} to the parent of this object
     */
    private void afterObject() {
        currentCreateObject.set(parent);
        if (FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.UPDATE_MODEL_VALUES) {
            currentValue = null;
        }
    }

    /**
     * Gets the currently running {@link CreateObjectComponent}, null if none is running. Indeed we can't always rely on
     * the value returned by #{component} (i.e. on the JSF EL component stack) so we set our own.
     * 
     * @return the currently running {@link CreateObjectComponent}
     */
    public static CreateObjectComponent getCurrentlyRunning() {
        return currentCreateObject.get();
    }

    /**
     * Computes the label types of this mak:object
     * 
     * @return
     */
    private QueryAnalysis computeQueryAnalysis() {
        final QueryAnalysisProvider qap = QueryProvider.getQueryAnalzyer(getQueryLanguage());

        // figure out the type of the label
        QueryAnalysis qA = null;

        // try directly
        try {
            ComposedQuery q = computeComposedQuery();
            qA = qap.getQueryAnalysis(q.getComputedQuery());
            cQ = q;
        } catch (Throwable t) {

            // this really sucks, we should have a more uniform exception flow for the clients of QueryAnalysisProvider
            if (t instanceof RuntimeWrappedException || t instanceof RuntimeException || t instanceof OQLParseError) {
                if (t.getCause() instanceof OQLParseError) {
                    t = t.getCause();
                } else {
                    throw new RuntimeException(t);
                }

                // try to recover by checking if we can find a parent list or create object with which to combine
                MakumbaDataComponent parent = UIRepeatListComponent.findMakListParent(this, true);
                if (parent == null) {
                    parent = findParentObject(this);
                }
                // try to build a composed subquery together with our parent list
                initComposedQuery(parent);

                // analyze it
                qA = qap.getQueryAnalysis(cQ.getTypeAnalyzerQuery());
            }

        }

        if (qA != null) {
            return qA;

        } else {
            // TODO this shouldn't happen like that
            throw new RuntimeException("Could not compute type analysis query for mak:object");
        }
    }

    private ComposedQuery computeComposedQuery() {
        ComposedQuery cq = new ComposedQuery(this.queryProps, this.getQueryLanguage());
        cq.init();
        cq.analyze();
        return cq;
    }

    private ComposedQuery computeComposedQuery(MakumbaDataComponent parent) {
        ComposedQuery cq = new ComposedSubquery(this.queryProps, parent.getComposedQuery(), this.getQueryLanguage(),
                true);
        cq.init();
        cq.analyze();
        return cq;
    }

    /**
     * Returns the labels known by this mak:object and that could be candidates for creation
     */
    public Map<String, DataDefinition> getLabelTypes() {
        return this.qA.getLabelTypes();
    }

    // TODO refactor together with the list
    public String getQueryLanguage() {
        // TODO: get the query language from taglib URI, taglib name, or configuration
        return "oql";
    }

    public static CreateObjectComponent findParentObject(UIComponent current) {
        UIComponent c = current.getParent();
        while (c != null && !(c instanceof CreateObjectComponent)) {
            c = c.getParent();
        }
        if (c instanceof CreateObjectComponent) {
            return (CreateObjectComponent) c;
        } else {
            return null;
        }
    }

    @Override
    public void addValue(String label, String path, Object value, String clientId) {
        // filter out null values that the EL resolver sometimes sets
        if (value != null) {
            currentValue.addField(path, value, clientId);
        }
        // FIXME: if the value is null, we might need to set it to Pointer.Nullxxx
    }

    private void initObjectInputValue() {
        if (currentValue != null) {
            return;
        }
        // analyze the FROM section of this object to figure out it's command type
        String from = queryProps[ComposedQuery.FROM];

        // FIXME this only covers a few cases
        // a more robust way is: get our label from the WHERE section
        // then find the FROM section that declares our label by searching for the pattern a.b.c d, where d must be our
        // label (if it's not, we ignore)
        String[] s = from.split(" ");
        String definition = s[0];
        String label = s[1];

        currentValue = ObjectInputValue.makeCreationInputValue(label, definition);
    }

    @Override
    public ComposedQuery getComposedQuery() {
        return this.cQ;
    }

}
