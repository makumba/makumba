package org.makumba.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

/**
 * {@link ELResolver} managing the creation of new makumba objects. It does so by:
 * <ul>
 * <li>if the base is null, check if there is a mak:object that declared itself as creator for the label given by the
 * property</li>
 * <li>if a creation mak:object is found, retrieve its CreateDataHolder which contains the base object label and type</li>
 * <li>for each new path element (property), check if it is valid</li>
 * <li>disallow setting .id</li>
 * <li></li>
 * </ul>
 * 
 * @author manu
 */
public class MakumbaCreateELResolver extends ELResolver {

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
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
