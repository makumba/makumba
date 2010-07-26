package org.makumba.el;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotWritableException;

import org.makumba.Pointer;
import org.makumba.jsf.UIRepeatListComponent;

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

        if (base != null && base instanceof LabelPlaceholder && property == null) {
            context.setPropertyResolved(true);
            return Object.class;
        }
        if (base != null && base instanceof LabelPlaceholder && property != null) {
            // fetch value of property and return its type
        }

        return null;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {

        // as per reference
        if (context == null) {
            throw new NullPointerException();
        }

        if (base == null && property != null) {
            // lookup property in parent list, if it's a label we set a placeholder here
            UIRepeatListComponent list = UIRepeatListComponent.getCurrentlyRunning();
            Object value = list.getExpressionValue(property.toString());
            if (value != null && value instanceof Pointer) {
                base = new LabelPlaceholder(property.toString());
                context.setPropertyResolved(true);

                // return the placeholder
                return base;
            } else if (value != null) {
                // ??
                throw new ELException("Should not be here");
            }
        }

        if (base != null && base instanceof LabelPlaceholder) {
            LabelPlaceholder placeholder = (LabelPlaceholder) base;

            // check with parent list if placeholderlabel.property exists.
            UIRepeatListComponent list = UIRepeatListComponent.getCurrentlyRunning();
            String path = placeholder.getLabel() + "." + property.toString();
            Object value = list.getExpressionValue(path);
            if (value != null) {

                if (value instanceof Pointer) {
                    base = new LabelPlaceholder(path);
                    context.setPropertyResolved(true);
                    // return the placeholder
                    return base;
                } else {
                    context.setPropertyResolved(true);
                    return value;
                }

            } else {

                boolean found = false;
                for (String s : list.getCurrentProjections()) {
                    if (s.startsWith(path)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    // set a placeholder
                    base = new LabelPlaceholder(path);
                    context.setPropertyResolved(true);
                    // return the placeholder
                    return base;
                } else {
                    throw new ELException("Field '" + property + "' of '" + base + "' does not exist");
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

        throw new PropertyNotWritableException();
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

    class LabelPlaceholder {

        private String label;

        public LabelPlaceholder(String label) {
            super();
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
