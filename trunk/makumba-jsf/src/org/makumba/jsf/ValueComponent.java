package org.makumba.jsf;

import java.io.IOException;

import javax.el.ValueExpression;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * mak:value component
 * 
 * @author manu
 */
public class ValueComponent extends UIComponentBase {

    private String expr;

    @Override
    public String getFamily() {
        return "makumba";
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public String getExpr() {
        if (this.expr != null) {
            return this.expr;
        }
        ValueExpression ve = getValueExpression("expr");
        if (ve != null) {
            return (String) ve.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        super.encodeChildren(context);
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        System.out.println("ValueComponent.encodeBegin()");
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("div", this);
        writer.writeText("mak:value expr " + getExpr(), null);
        writer.endElement("div");
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        System.out.println("ValueComponent.encodeEnd()");
        super.encodeEnd(context);
    }

}
