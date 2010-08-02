/*
 * Created on Jul 31, 2010
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.jsf;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.makumba.DataDefinition;
import org.makumba.Pointer;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;

public class PointerConverter implements Converter, Serializable {
    static final Logger log = java.util.logging.Logger.getLogger("org.makumba.jsf.ptrConvert");

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        // log.fine("Resolving  " + value);
        // FIXME: this looks pretty laborious. Probably the query should prepare
        try {

            UIRepeatListComponent list = UIRepeatListComponent.findMakListParent(component, true);
            QueryAnalysisProvider qap = QueryProvider.getQueryAnalzyer(list.getQueryLanguage());
            QueryAnalysis qa = qap.getQueryAnalysis(list.getComposedQuery().getTypeAnalyzerQuery());

            // FIXME: note that there can be more pointers in the expression, though that is unlikely
            String expr = component.getValueExpression("value").getExpressionString();
            // take away #{ }
            expr = expr.substring(2, expr.length() - 1).trim();
            DataDefinition dd = qa.getProjectionType();
            DataDefinition pointed = null;
            for (int i = 0; i < list.getProjections().size(); i++) {
                if (list.getProjections().get(i).equals(expr)) {
                    pointed = dd.getFieldDefinition(i).getPointedType();
                    break;
                }
            }

            // JSF seems to require a SQLPointer... Maybe because the old value is of that class
            Pointer ptr = new org.makumba.commons.SQLPointer(pointed.getName(),
                    new Pointer(pointed.getName(), value).getId());
            System.out.println(UIRepeatListComponent.getCurrentlyRunning().debugIdent() + " " + value + " is "
                    + ptr.toString());
            return ptr;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeWrappedException(t);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((Pointer) value).toExternalForm();
    }
}
