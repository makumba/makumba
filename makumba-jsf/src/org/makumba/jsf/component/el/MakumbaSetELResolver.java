package org.makumba.jsf.component.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.makumba.jsf.MakumbaDataContext;
import org.makumba.jsf.component.UIRepeatListComponent;

public class MakumbaSetELResolver extends ELResolver {

    private Object basicGetValue(ELContext context, Object base, Object property) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (base != null && base instanceof ReadExpressionPathPlaceholder) {
            // is this a set?
            ReadExpressionPathPlaceholder r = (ReadExpressionPathPlaceholder) base;

            UIRepeatListComponent list = MakumbaDataContext.getDataContext().getCurrentList();
            if (list == null) {
                return null;
            }
            String path = r.getProjectionPath() + "." + property.toString();
            if (list.hasSetProjection(path)) {
                context.setPropertyResolved(true);
                return list.getSetData(path);
            }
        }

        return null;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        // TODO Auto-generated method stub
        return basicGetValue(context, base, property);
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        // TODO Auto-generated method stub
        return null;
    }

}
