package org.makumba.jsf;

import java.io.IOException;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

import org.makumba.OQLParseError;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProvider;

public class ObjectComponent extends UIComponentBase {

    private String from;

    private String where;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
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
    public void encodeChildren(FacesContext context) throws IOException {
        super.encodeChildren(context);
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        super.encodeBegin(context);

        final QueryAnalysisProvider qap = QueryProvider.getQueryAnalzyer(getQueryLanguage());

        System.out.println("ObjectComponent.encodeBegin() from: " + from + " where: " + where);

        // figure out the type of the label

        // FIXME somehow this looks a lot like a ComposedQuery...

        // try directly
        try {
            QueryAnalysis qA = qap.getQueryAnalysis("SELECT 1 FROM " + from + " WHERE " + where);

            System.out.println(qA.getLabelTypes());

        } catch (OQLParseError e) {

            // try to recover by checking if we can find a parent list with which to combine
            UIRepeatListComponent list = UIRepeatListComponent.getCurrentlyRunning();
            if (list != null) {

                // TODO do something meaningful

            } else {
                // TODO better error handling
                throw new RuntimeException(e);
            }

        }

        // take into account possible parent list
        // set a data holder to be used by the MakumbaCreateELResolver

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

}
