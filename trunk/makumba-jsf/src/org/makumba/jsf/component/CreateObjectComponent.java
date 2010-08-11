package org.makumba.jsf.component;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.FacesEvent;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.FieldDefinition;
import org.makumba.OQLParseError;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.jsf.update.DataHandler;
import org.makumba.jsf.update.InputValue;
import org.makumba.jsf.update.ObjectInputValue;
import org.makumba.jsf.update.ObjectInputValue.ValueType;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.ComposedSubquery;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;

/**
 * Component that allows creation of new objects
 * 
 * @author manu
 */
public class CreateObjectComponent extends UIComponentBase implements MakumbaDataComponent {

    private String[] queryProps = new String[6];

    private CreateObjectComponent parent;

    private ThreadLocal<ObjectInputValue> currentValues = new ThreadLocal<ObjectInputValue>();

    private ComposedQuery cQ;

    private QueryAnalysis qA;

    private DataHandler dataHandlder;

    private static ThreadLocal<CreateObjectComponent> currentCreateObject = new ThreadLocal<CreateObjectComponent>();

    @Override
    public void setDataHandler(DataHandler handler) {
        this.dataHandlder = handler;
    }

    @Override
    public String getKey() {
        return this.getId();
    }

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

            // TODO pass the currentValues to the consumer
        } finally {
            afterObject();

            // clean the values
            // currentValues.set(new ObjectInputValue());
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
        if (currentValues.get() == null) {
            currentValues.set(initObjectInputValue());
        }
    }

    /**
     * Sets the currently running {@link CreateObjectComponent} to the parent of this object
     */
    private void afterObject() {
        currentCreateObject.set(parent);
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
            System.out.println(qA.getLabelTypes());
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

                System.out.println(cQ.getTypeAnalyzerQuery());

                // analyze it
                qA = qap.getQueryAnalysis(cQ.getTypeAnalyzerQuery());
                System.out.println(qA.getLabelTypes());
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
        System.out.println("__________ " + label + " " + path + " value");
        InputValue v = new InputValue(value, clientId);

        currentValues.get().addField(path, v);
    }

    private ObjectInputValue initObjectInputValue() {
        ObjectInputValue oiv = new ObjectInputValue();

        // analyze the FROM section of this object to figure out it's command type
        String from = queryProps[ComposedQuery.FROM];

        // FIXME more robust
        String[] s = from.split(" ");
        String p = s[0];
        String l = s[1];

        oiv.setLabel(l);

        // case "general.Person p" --> NEW action, simple OIV with values
        DataDefinition t = null;
        try {
            t = DataDefinitionProvider.getInstance().getDataDefinition(p);
        } catch (DataDefinitionNotFoundError dne) {
            // ignore
        }

        if (t != null) {
            oiv.setCommand(ValueType.CREATE);
            oiv.setType(t);
            dataHandlder.addSimpleObjectInputValue(oiv);
            System.out.println("New CREATE " + t.getName() + " for label " + l);

        } else {
            // if we're not CREATE we should be ADD

            // find base label
            String baseLabel = "";
            String fieldPath = "";
            int n = p.indexOf(".");
            if (n > 0) {
                baseLabel = p.substring(0, n);
                fieldPath = p.substring(n + 1, p.length());
            } else {
                throw new RuntimeException("Invalid FROM section for mak:object: '" + from + "': " + p
                        + "should be either a valid type or a path to a relational type");
            }

            DataDefinition baseLabelType = qA.getLabelType(baseLabel);
            FieldDefinition fd = baseLabelType.getFieldOrPointedFieldDefinition(fieldPath);

            System.out.println("Trying add for " + p + ": " + baseLabelType.getName());

            oiv.setCommand(ValueType.ADD);
            oiv.setType(baseLabelType);

            switch (fd.getIntegerType()) {
                case FieldDefinition._ptrOne:
                case FieldDefinition._ptr:
                    dataHandlder.addPointerObjectInputValue(oiv, baseLabel, fieldPath);
                    break;
                case FieldDefinition._set:
                case FieldDefinition._setComplex:
                    dataHandlder.addSetObjectInputValue(oiv, baseLabel, fieldPath);
                    break;
                default:
                    throw new RuntimeException("Invalid FROM section for mak:object '" + from + "': '" + p
                            + "' should be a path to a relational type");
            }
        }
        return oiv;
    }

    @Override
    public boolean hasLabel(String label) {
        return getLabelTypes().containsKey(label);
    }

    @Override
    public ComposedQuery getComposedQuery() {
        return this.cQ;
    }

}
