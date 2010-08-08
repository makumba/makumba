package org.makumba.jsf;

import javax.el.ELException;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.makumba.AttributeNotFoundException;
import org.makumba.Attributes;
import org.makumba.LogicException;
import org.makumba.commons.attributes.RequestAttributes;

public class FacesAttributes implements Attributes {
    public static FacesAttributes getAttributes(FacesContext pc) {
        if (pc.getAttributes().get(RequestAttributes.ATTRIBUTES_NAME) == null) {
            pc.getAttributes().put(RequestAttributes.ATTRIBUTES_NAME, new FacesAttributes(pc));
        }
        return (FacesAttributes) pc.getAttributes().get(RequestAttributes.ATTRIBUTES_NAME);
    }

    FacesContext facesContext;

    FacesAttributes(FacesContext facesContext) {
        this.facesContext = facesContext;
    }

    /*
    // copied from page attributes
    static public void setAttribute(FacesContext pc, String var, Object o) {
        if (o != null) {
            pc.getAttributes().put(var, o);
            pc.getAttributes().remove(var + "_null");
        } else {
            pc.getAttributes().remove(var);
            pc.getAttributes().put(var + "_null", "null");
        }
    }
    */
    HttpServletRequest getRequest() {
        return (HttpServletRequest) facesContext.getExternalContext().getRequest();
    }

    /**
     * @see org.makumba.Attributes#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public Object setAttribute(String s, Object o) throws LogicException {
        return RequestAttributes.getAttributes(getRequest()).setAttribute(s, o);
    }

    /**
     * @see org.makumba.Attributes#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute(String s) throws LogicException {
        RequestAttributes.getAttributes(getRequest()).removeAttribute(s);
    }

    /**
     * @see org.makumba.Attributes#hasAttribute(java.lang.String)
     */
    @Override
    public boolean hasAttribute(String s) {
        try {
            return RequestAttributes.getAttributes(getRequest()).hasAttribute(s)
                    || checkFacesForAttribute(s) != RequestAttributes.notFound;
        } catch (LogicException e) {
            return false;
        }
    }

    /**
     * @see org.makumba.Attributes#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String s) throws LogicException {
        RequestAttributes reqAttrs = RequestAttributes.getAttributes(getRequest());

        Object o = reqAttrs.checkSessionForAttribute(s);
        if (o != RequestAttributes.notFound) {
            return o;
        }

        o = reqAttrs.checkServletLoginForAttribute(s);
        if (o != RequestAttributes.notFound) {
            return o;
        }

        o = checkFacesForAttribute(s);
        if (o != RequestAttributes.notFound) {
            return o;
        }

        /*   o = checkELForAttribute(s);
           if (o != RequestAttributes.notFound) {
               return o;
           }
        */
        o = reqAttrs.checkLogicForAttribute(s);
        if (o != RequestAttributes.notFound) {
            return o;
        }

        o = reqAttrs.checkParameterForAttribute(s);
        if (o != RequestAttributes.notFound) {
            return o;
        }

        throw new AttributeNotFoundException(s, false);

    }

    public Object checkFacesForAttribute(String s) {
        String snull = s + "_null";

        Object value = facesContext.getAttributes().get(s);
        if (value != null) {
            return value;
        }
        if (facesContext.getAttributes().get(snull) != null) {
            return null;
        }
        return RequestAttributes.notFound;
    }

    public Object checkELForAttribute(String s) {
        try {
            Object ret = facesContext.getApplication().evaluateExpressionGet(facesContext, "#{" + s + "}", Object.class);
            if (ret == null) {
                return RequestAttributes.notFound;
            }
            return ret;
        } catch (ELException e) {
            return RequestAttributes.notFound;
        }
    }

}
