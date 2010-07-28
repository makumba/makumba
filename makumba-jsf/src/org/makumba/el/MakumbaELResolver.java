package org.makumba.el;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;

import org.makumba.Pointer;
import org.makumba.jsf.UIRepeatListComponent;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;

/**
 * FIXME race condition (?) in {@link UIRepeatListComponent} on first evalutation of one expression<br>
 * FIXME for ptr projections such as #{p}, return something alike to Java's [Object@REFERENCE String instead of the
 * placeholder or the ptr<br>
 * TODO implement p.id<br>
 * TODO test the resolution of p.ptr.field when nothing before is selected<br>
 * TODO refactor and introduce a decoupling from the list to fetch the data, so a unit test can be written with a mock
 * list/data provider<br>
 * 
 * @author manu
 * @author cristi
 */
public class MakumbaELResolver extends ELResolver {

    public MakumbaELResolver() {

    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {

        // TODO when implementing setValue

        // as per reference
        if (context == null) {
            throw new NullPointerException();
        }

        if (base != null && base instanceof ExpressionPathPlaceholder && property == null) {
            context.setPropertyResolved(true);
            // it was object, i think pointer is correct, not sure.
            // maybe a pointer converter will be needed then
            return Pointer.class;
        }
        if (base != null && base instanceof ExpressionPathPlaceholder && property != null) {
            Object o = getValue(context, base, property).getClass();
            if (o == null) {
                return null;
            }
            context.setPropertyResolved(true);

            // returning o.getClass() will lead to "error setting value ... for null converter"
            return Object.class;
        }

        return null;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        // as per reference
        if (context == null) {
            throw new NullPointerException();
        }

        UIRepeatListComponent list = UIRepeatListComponent.getCurrentlyRunning();
        if (list == null) {
            return null;
        }

        if (base == null && property != null) {
            // lookup property in parent list, if it's a label we set a placeholder here
            if (list.getProjections().contains(property.toString())) {
                // this can only be a label projection, so it's gonna be a pointer
                Pointer value = (Pointer) list.getExpressionValue(property.toString());
                context.setPropertyResolved(true);
                return new ExpressionPathPlaceholder(value, property.toString());
            } else {
                // it's not a projection but it may be a label, we check for that
                QueryAnalysisProvider qap = QueryProvider.getQueryAnalzyer(list.getQueryLanguage());
                QueryAnalysis qa = qap.getQueryAnalysis(list.getComposedQuery().getTypeAnalyzerQuery());
                if (qa.getLabelTypes().get(property.toString()) != null) {
                    context.setPropertyResolved(true);
                    return new ExpressionPathPlaceholder(qa, property.toString());
                } else {
                    return null;
                }
            }
        }

        if (base != null && base instanceof ExpressionPathPlaceholder) {
            ExpressionPathPlaceholder placeholder = (ExpressionPathPlaceholder) base;

            // check with parent list if placeholderlabel.property exists.
            ExpressionPathPlaceholder mine = new ExpressionPathPlaceholder(placeholder, property.toString());

            if (list.getProjections().contains(mine.getExpressionPath())) {

                Object value = list.getExpressionValue(mine.getExpressionPath());

                if (value instanceof Pointer && !"id".equals(property)) {
                    // TODO: instead of checking the value, we can inquire the query whether the field is a pointer
                    context.setPropertyResolved(true);
                    // TODO: we could actually set the value in the placeholder, for whatever it could be useful

                    // return the placeholder
                    return mine;
                } else {
                    context.setPropertyResolved(true);
                    return value;
                }

            } else {

                boolean found = false;
                for (String s : list.getProjections()) {
                    if (s.startsWith(mine.getExpressionPath())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    // set a placeholder
                    context.setPropertyResolved(true);
                    // return the placeholder
                    return mine;
                } else {
                    throw new ELException("Field '" + property + "' of '" + base + "' is not known."
                            + (list.useCaches() ? " Turn caches off or try reloading the page." : ""));
                    // TODO we could even check here whether the property would makes sense in the query
                }

            }
        }
        return null;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object val) {

        // as per reference
        if (context == null) {
            throw new NullPointerException();
        }

        if (base instanceof ExpressionPathPlaceholder) {
            System.out.println(base + "." + property + "=" + val);
            context.setPropertyResolved(true);
        }

        return;

        // if base is null, and we have a label with the property name, i think we should return "not writable"
        // same goes when the base is a placeholder and the property is .id

        // for the case where we have placeholder and a property:
        // if placeholder has no pointer but just a query and label, we run
        // placeholder.basePointer= SELECT label FROM placeholder.query.from WHERE placeholder.query.where

        // then placeholder has a pointer, so we call
        // transaction.update(placeholder.basePointer, placeholder.fieldDotField, newValue)
        // we can probably collect such calls from the entire request
        // throw new PropertyNotWritableException();
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {

        // as per reference
        if (context == null) {
            throw new NullPointerException();
        }

        return true;
    }

    @Override
    // TODO
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {

        if (base != null && base instanceof Map) {
            Map map = (Map) base;
            Iterator iter = map.keySet().iterator();
            List<FeatureDescriptor> list = new ArrayList<FeatureDescriptor>();
            while (iter.hasNext()) {
                Object key = iter.next();
                FeatureDescriptor descriptor = new FeatureDescriptor();
                String name = key == null ? null : key.toString();
                descriptor.setName(name);
                descriptor.setDisplayName(name);
                descriptor.setShortDescription("");
                descriptor.setExpert(false);
                descriptor.setHidden(false);
                descriptor.setPreferred(true);
                descriptor.setValue("type", key == null ? null : key.getClass());
                descriptor.setValue("resolvableAtDesignTime", Boolean.TRUE);
                list.add(descriptor);
            }
            return list.iterator();
        }
        return null;
    }

    @Override
    // TODO
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (base != null && base instanceof Map) {
            return Object.class;
        }
        return null;
    }

    class ExpressionPathPlaceholder {
        // everything starts from a label
        private String label;

        // we either keep its pointer value, or a query through which the label can be resolved
        private QueryAnalysis qa;

        private Pointer basePointer;

        // after that, comes the field.field path to the desired property
        private String fieldDotField = "";

        public ExpressionPathPlaceholder(Pointer p, String label) {
            this.label = label;
            this.basePointer = p;
        }

        public ExpressionPathPlaceholder(QueryAnalysis qa, String label) {
            this.label = label;
            this.qa = qa;
        }

        public ExpressionPathPlaceholder(ExpressionPathPlaceholder expr, String field) {
            this.basePointer = expr.basePointer;
            this.qa = expr.qa;
            this.label = expr.label;
            this.fieldDotField = expr.fieldDotField + "." + field;
        }

        public String getExpressionPath() {
            return label + fieldDotField;
        }

        @Override
        public String toString() {
            return (basePointer != null ? basePointer : qa) + " " + getExpressionPath();
        }

    }
}
