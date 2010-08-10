package org.makumba.jsf.component.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;

import org.makumba.FieldDefinition;
import org.makumba.Pointer;
import org.makumba.jsf.ComponentDataHandler;
import org.makumba.jsf.component.MakumbaDataComponent;
import org.makumba.jsf.component.UIRepeatListComponent;

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

    private ComponentDataHandler handler;

    static final Logger log = java.util.logging.Logger.getLogger("org.makumba.jsf.el");

    public MakumbaELResolver(ComponentDataHandler handler) {
        this.handler = handler;
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
            if (!list.hasExpression(expr.getProjectionPath())) {
                System.out.println(debugIdent() + " " + base + "." + property + " type Object" + " "
                        + current.getClientId());

                // this should not matter as we are not going to edit
                return Object.class;
            }
            FieldDefinition fd = list.getExpressionType(expr.getProjectionPath());
            Class<?> type = fd.getJavaType();
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
        String expr = mine.getProjectionPath();
        if (base != null && base instanceof ReadExpressionPathPlaceholder && list.hasExpression(expr)) {
            {
                if (list.getExpressionType(expr).getType().startsWith("ptr") && !"id".equals(property)) {
                    // TODO: instead of checking the value, we can inquire the query whether the field is a pointer
                    // TODO: we could actually set the value in the placeholder, for whatever it could be useful

                    // return the placeholder
                    System.out.println(debugIdent() + " " + base + "." + property + " ----> " + mine + " in "
                            + current.getClientId());

                    return mine;
                }

                if (current instanceof UINamingContainer) {
                    // we are in a container like a ui:repeat or mak:list, which probably means we ae in a floating
                    // expression
                    return list.convertToString(expr);
                }
                // the PointerConverter should take over for other cases
                Object value = list.getExpressionValue(expr);
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
            if (list.hasExpression(property.toString())) {
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

            if (list.hasExpression(mine.getProjectionPath())) {
                context.setPropertyResolved(true);
                return mine;

            } else {

                boolean found = false;
                for (String s : list.getProjections()) {
                    if (s.startsWith(mine.getProjectionPath())) {
                        found = true;
                        context.setPropertyResolved(true);
                        // return the placeholder
                        return mine;
                    }
                }

                throw new ELException("Field '" + property + "' of '" + base + "' is not known."
                        + (list.useCaches() ? " Turn caches off or try reloading the page." : ""));
                // TODO we could even check here whether the property would makes sense in the query

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
            System.out.println(debugIdent() + " " + base + "." + property + " <------- " + val + " "
                    + current.getClientId());

            ReadExpressionPathPlaceholder p = (ReadExpressionPathPlaceholder) base;
            // FIXME return the clientId of the input, not the list
            MakumbaDataComponent c = MakumbaDataComponent.Util.findLabelDefinitionComponent(list, p.getLabel());
            c.addValue(p.getLabel(), p.getPath((String) property), val, current.getClientId());
            // handler.addInputValue(list,
            // new InputValue(p.getPointer(), list.getKey(), p.getPath((String) property), val));
            // list.setExpressionValue(((ExpressionPathPlaceholder) base).getProjectionPath() + "." + property, val);
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
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        // TODO
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        // TODO
        return null;
    }
}
