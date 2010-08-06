package org.makumba.el;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.makumba.DataDefinition;
import org.makumba.Pointer;
import org.makumba.jsf.UIRepeatListComponent;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;

import com.sun.faces.facelets.compiler.UIInstructions;

/**
 * FIXME for ptr projections such as #{p}, return something alike to Java's [Object@REFERENCE String instead of the
 * placeholder or the ptr<br>
 * TODO test the resolution of p.ptr.field when nothing before is selected<br>
 * TODO refactor and introduce a decoupling from the list to fetch the data, so a unit test can be written with a mock
 * list/data provider<br>
 * 
 * @author manu
 * @author cristi
 */
public class MakumbaELResolver extends ELResolver {
    static final Logger log = java.util.logging.Logger.getLogger("org.makumba.jsf.el");

    public MakumbaELResolver() {

    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {

        UIComponent current = (UIComponent) FacesContext.getCurrentInstance().getAttributes().get(
            UIComponent.CURRENT_COMPONENT);

        // as per reference
        if (context == null) {
            throw new NullPointerException();
        }

        if (base != null && base instanceof ReadExpressionPathPlaceholder && property == null) {
            context.setPropertyResolved(true);
            // it was object, i think pointer is correct, not sure.
            // maybe a pointer converter will be needed then
            System.out.println(debugIdent() + " " + base + "." + property + " type Pointer" + " "
                    + current.getClientId());

            return String.class;
        }
        if (base != null && base instanceof ReadExpressionPathPlaceholder && property != null) {
            ReadExpressionPathPlaceholder expr = basicGetValue(context, base, property);
            if (expr == null) {
                System.out.println(debugIdent() + " " + base + "." + property + " type unresolved" + " "
                        + current.getClientId());
                return null;
            }
            context.setPropertyResolved(true);
            UIRepeatListComponent list = UIRepeatListComponent.getCurrentlyRunning();
            if (!list.getProjections().contains(expr.getExpressionPath())) {
                System.out.println(debugIdent() + " " + base + "." + property + " type Object" + " "
                        + current.getClientId());

                // this should not matter as we are not going to edit
                return Object.class;
            }
            // TODO: rewrite this to use query analysis instead
            // this will also catch pointers (SQLPointer)
            Object value = list.getExpressionValue(expr.getExpressionPath());
            Class<?> type = value.getClass();

            if (value instanceof Pointer) {
                // we will convert to Pointer in setValue()
                type = String.class;
            }
            System.out.println(debugIdent() + " " + base + "." + property + " type " + type.getName() + " "
                    + current.getClientId());

            return type;
        }
        System.out.println(debugIdent() + " " + base + "." + property + " type unresolved" + " "
                + current.getClientId());

        return null;
    }

    private String debugIdent() {
        if (UIRepeatListComponent.getCurrentlyRunning() != null) {
            return UIRepeatListComponent.getCurrentlyRunning().debugIdent();
        }
        return "";
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        UIComponent current = (UIComponent) FacesContext.getCurrentInstance().getAttributes().get(
            UIComponent.CURRENT_COMPONENT);

        ReadExpressionPathPlaceholder mine = basicGetValue(context, base, property);
        if (mine == null) {
            System.out.println(debugIdent() + " " + base + "." + property + " ----> " + null + " in "
                    + current.getClientId());

            return null;
        }
        UIRepeatListComponent list = UIRepeatListComponent.getCurrentlyRunning();
        if (base != null && base instanceof ReadExpressionPathPlaceholder
                && list.getProjections().contains(mine.getExpressionPath())) {
            {
                Object value = list.valuesSet.get(base + "." + property);

                if (value == null) {
                    value = list.getExpressionValue(mine.getExpressionPath());
                }

                if (value instanceof Pointer && !"id".equals(property)) {
                    // TODO: instead of checking the value, we can inquire the query whether the field is a pointer
                    // TODO: we could actually set the value in the placeholder, for whatever it could be useful

                    // return the placeholder
                    System.out.println(debugIdent() + " " + base + "." + property + " ----> " + mine + " in "
                            + current.getClientId());

                    return mine;
                }

                if (value instanceof Pointer && "id".equals(property)) {
                    /* we have a pointer #{p.x.y.id} 
                     * if we know we are in UIInstruction or in outputText, we convert toExternalForm */

                    // if we are in UIInstructions, we're in free text so the
                    // encoded form is better
                    // also in h:outputText?

                    if (current instanceof UIInstructions) {
                        return ((Pointer) value).toExternalForm();
                    }
                    if (current instanceof ValueHolder && ((ValueHolder) current).getConverter() == null) {
                        ValueExpression ev = current.getValueExpression("value");
                        if (ev != null && ev.getExpressionString().indexOf(mine.getExpressionPath()) != -1) {
                            return ((Pointer) value).toExternalForm();
                        }
                    }
                }
                System.out.println(debugIdent() + " " + base + "." + property + " ----> " + value + " in "
                        + current.getClientId());
                return value;
            }

        }
        System.out.println(debugIdent() + " " + base + "." + property + " ----> " + mine + " in "
                + current.getClientId());

        // log.fine(mine.toString());
        return mine;

    }

    public ReadExpressionPathPlaceholder basicGetValue(ELContext context, Object base, Object property) {
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
                return new ReadExpressionPathPlaceholder(value, property.toString());
            } else {
                // this may be a label that we don't know, like a managed bean

                // even if we would know the label, in between iterations uirepeat does a static traversal (with no
                // iterations) to save all the inputs inside it. no clue why but since not our lists are iterating, we
                // cannot provide these values

                // log.fine(property.toString() + " " + list.getComposedQuery().getClass().getName() + " "
                // + FacesContext.getCurrentInstance().getAttributes().get(UIComponent.CURRENT_COMPONENT));
                return null;
            }
        }

        if (base != null && base instanceof ReadExpressionPathPlaceholder) {
            ReadExpressionPathPlaceholder placeholder = (ReadExpressionPathPlaceholder) base;

            // check with parent list if placeholderlabel.property exists.
            ReadExpressionPathPlaceholder mine = new ReadExpressionPathPlaceholder(placeholder, property.toString());

            if (list.getProjections().contains(mine.getExpressionPath())) {
                context.setPropertyResolved(true);
                return mine;

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
        // TODO check if the property is fixed
        // and the path to it goes thru fixed not null pointers?

        // as per reference
        if (context == null) {
            throw new NullPointerException();
        }

        UIComponent current = (UIComponent) FacesContext.getCurrentInstance().getAttributes().get(
            UIComponent.CURRENT_COMPONENT);

        if (base instanceof ReadExpressionPathPlaceholder) {

            UIRepeatListComponent list = UIRepeatListComponent.getCurrentlyRunning();
            if ("id".equals(property) && !(val instanceof Pointer)) {
                val = MakumbaELResolver.resolvePointer((String) val,
                    ((ReadExpressionPathPlaceholder) base).getExpressionPath() + "." + property, list);

            }
            System.out.println(debugIdent() + " " + base + "." + property + " <------- " + val + " "
                    + current.getClientId());

            ReadExpressionPathPlaceholder p = (ReadExpressionPathPlaceholder) base;

            String pathForUpdate = p.getPathForUpdate();
            if (pathForUpdate.length() == 0) {
                pathForUpdate = (String) property;
            } else {
                pathForUpdate += "." + property;
            }

            list.addUpdateValue(p.getPointer(), new UpdateValue(p.getPointer(), pathForUpdate, val));

            context.setPropertyResolved(true);
        } else {
            System.out.println(debugIdent() + " not setting " + base + "." + property + " to " + val + " "
                    + current.getClientId());
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

        System.out.println(debugIdent() + " isReadOnly " + base + "." + property);
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
            Map<?, ?> map = (Map<?, ?>) base;
            Iterator<?> iter = map.keySet().iterator();
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

    public static Pointer resolvePointer(String value, String expr, UIRepeatListComponent list) {
        // FIXME: this looks pretty laborious. Probably the query should prepare

        QueryAnalysisProvider qap = QueryProvider.getQueryAnalzyer(list.getQueryLanguage());
        QueryAnalysis qa = qap.getQueryAnalysis(list.getComposedQuery().getTypeAnalyzerQuery());

        DataDefinition dd = qa.getProjectionType();
        DataDefinition pointed = null;
        for (int i = 0; i < list.getProjections().size(); i++) {
            if (list.getProjections().get(i).equals(expr)) {
                pointed = dd.getFieldDefinition(i).getPointedType();
                break;
            }
        }

        try {
            // JSF seems to require a SQLPointer... Maybe because the old value is of that class
            Pointer ptr = new org.makumba.commons.SQLPointer(pointed.getName(),
                    new Pointer(pointed.getName(), value).getId());
            return ptr;
        } catch (Throwable t) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, t.getMessage(), ""));
        }
    }
}
