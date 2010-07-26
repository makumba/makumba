package org.makumba.el;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotWritableException;

public class MakumbaELResolver extends ELResolver {

    public MakumbaELResolver() {

    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {

        // as per reference
        if (context == null) {
            throw new NullPointerException();
        }

        // TODO
        if (base != null && base instanceof Map) {
            context.setPropertyResolved(true);
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

        if (base == null && property != null) {
            // lookup property in parent list, if it's a label we set a placeholder here
        }

        if (base != null && base instanceof LabelPlaceholder) {
            context.setPropertyResolved(true);
            LabelPlaceholder map = (LabelPlaceholder) base;

            // TODO return stuff
            return null;
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
