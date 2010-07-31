package org.makumba.jsf;

import java.io.IOException;

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

    // not cached since the cost of retrieving is very small
    private Integer exprIndex;

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
        // this doesn't seem right, expr should be a constant, otherwise the analysis is useless
        // ValueExpression ve = getValueExpression("expr");
        // if (ve != null) {
        // return (String) ve.getValue(getFacesContext().getELContext());
        // }
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
        super.encodeBegin(context);
        // System.out.println("ValueComponent.encodeBegin()");
        ResponseWriter writer = context.getResponseWriter();
        UIRepeatListComponent currentlyRunning = UIRepeatListComponent.getCurrentlyRunning();
        if (exprIndex == null) {
            // this will be preserved in all parent list iterations
            exprIndex = currentlyRunning.getExpressionIndex(getExpr());
        }
        Object value = currentlyRunning.getExpressionValue(exprIndex);
        // writer.startElement("div", this);
        writer.writeText(value.toString(), null);
        // writer.endElement("div");
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        // System.out.println("ValueComponent.encodeEnd()");
        super.encodeEnd(context);
    }
}
