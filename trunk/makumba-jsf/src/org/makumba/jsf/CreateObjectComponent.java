package org.makumba.jsf;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.FacesEvent;

import org.makumba.DataDefinition;
import org.makumba.OQLParseError;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.ComposedSubquery;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProvider;

public class CreateObjectComponent extends UIComponentBase {

    private String[] queryProps = new String[6];

    private ComposedQuery cQ;

    private QueryAnalysis qA;

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
        readQueryAnalysis();
        super.broadcast(event);
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        readQueryAnalysis();
        super.encodeChildren(context);
    }

    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        readQueryAnalysis();
        super.processEvent(event);
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        readQueryAnalysis();
        super.encodeBegin(context);
    }

    @Override
    public void processUpdates(FacesContext context) {
        readQueryAnalysis();
        super.processUpdates(context);
    }

    @Override
    public void processValidators(FacesContext context) {
        readQueryAnalysis();
        super.processValidators(context);
    }

    private QueryAnalysis readQueryAnalysis() {
        if (this.qA == null) {
            this.qA = computeQueryAnalysis();
        }
        return this.qA;
    }

    private ComposedQuery readComposedQuery(UIRepeatListComponent parent) {
        if (this.cQ == null) {
            this.cQ = computeComposedQuery(parent);
        }
        return this.cQ;
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
            qA = qap.getQueryAnalysis("SELECT 1 FROM " + getFrom() + " WHERE " + getWhere());
            System.out.println(qA.getLabelTypes());
        } catch (Throwable t) {

            // this really sucks, we should have a more uniform exception flow for the clients of QueryAnalysisProvider
            if (t instanceof RuntimeWrappedException || t instanceof OQLParseError) {
                if (t.getCause() instanceof OQLParseError) {
                    t = t.getCause();
                } else {
                    throw new RuntimeException(t);
                }

                // try to recover by checking if we can find a parent list with which to combine
                UIRepeatListComponent parent = UIRepeatListComponent.findMakListParent(this, true);
                if (parent == null) {
                    // no parent, we are root
                    // so we can't recover
                    throw new RuntimeException(t);
                } else {
                    // try to build a composed subquery together with our parent list
                    readComposedQuery(parent);

                    System.out.println(cQ.getTypeAnalyzerQuery());

                    // analyze it
                    qA = qap.getQueryAnalysis(cQ.getTypeAnalyzerQuery());
                    System.out.println(qA.getLabelTypes());
                }
            }
        }

        if (qA != null) {
            return qA;

        } else {
            // TODO this shouldn't happen like that
            throw new RuntimeException("Could not compute type analysis query for mak:object");
        }
    }

    private ComposedQuery computeComposedQuery(UIRepeatListComponent parent) {
        ComposedQuery cq = new ComposedSubquery(this.queryProps, parent.composedQuery, this.getQueryLanguage(), true);
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
    private QueryProvider getQueryExecutionProvider() {
        return QueryProvider.makeQueryRunner(TransactionProvider.getInstance().getDefaultDataSourceName(),
            getQueryLanguage());
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

}
