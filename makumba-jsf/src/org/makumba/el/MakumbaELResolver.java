package org.makumba.el;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.MapELResolver;

/**
 * {@link ELResolver} for makumba, based on the {@link MapELResolver}. This resolver simply makes sure that the map is a
 * {@link MakumbaData}
 * 
 * @author manu
 */
public class MakumbaELResolver extends MapELResolver {

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (checkMakumbaData(context, base)) {
            base = ((MakumbaData) base).getWrapped();
            return super.getValue(context, base, property);
        }
        return null;

    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object val) {
        if (checkMakumbaData(context, base)) {
            base = ((MakumbaData) base).getWrapped();
            super.setValue(context, base, property, val);
        } else {
            return;
        }
    }

    private boolean checkMakumbaData(ELContext context, Object base) {
        // as per specification
        if (context == null) {
            throw new NullPointerException();
        }

        // only resolve MakumbaDataMap-s
        if (base != null && base instanceof MakumbaData) {
            return true;
        } else {
            return false;
        }
    }

}