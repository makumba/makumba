package org.makumba.jsf;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;

import com.sun.faces.facelets.compiler.UIInstructions;
import com.sun.faces.facelets.component.UIRepeat;

public class UIRepeatListComponent extends UIRepeat {

    public UIRepeatListComponent() {
        // example forcing a value on the UIRepeat
        setValue(new Object[] { "a", "b" });
    }

    public void analyze() {

        System.out.println(this.getClass());

        final List<ExprTuple> expressions = new ArrayList<ExprTuple>();

        // iterate over all the children and find the value expressions they
        // declare
        this.visitTree(VisitContext.createVisitContext(getFacesContext()), new VisitCallback() {

            @Override
            public VisitResult visit(VisitContext context, UIComponent target) {

                if (target instanceof UIInstructions) {
                    // FIXME this is highly Mojarra-dependent and quite a hack
                    expressions.addAll(findFloatingExpressions((UIInstructions) target));
                } else {
                    expressions.addAll(findComponentExpressions(target));
                }
                return VisitResult.ACCEPT;
            }
        });

        for (ExprTuple c : expressions) {
            System.out.println("** Child component " + c.getComponent().getClass());
            System.out.println("** Expression " + c.getExpr());
        }

        // check whether we have not computed the queries of this mak:list group
        // before
        // if so, retrieve them from cache

        // look for all children mak:lists and start making their queries
        // look for all children mak:values and for all children that contain #{
        // mak:expr(QL) }, add the expressions as projection to the enclosing
        // mak:list query

        // look for all children that contain #{ label.field } where label is
        // defined in a mak:list's FROM, add label and label.field to the
        // projections of that mak:list

        // cache the queries of this mak:list group.

        // execute the queries and prepare the DataModel
        // use setValue() to give the DataModel to the UIRepeat
    }

    /**
     * Finds the properties of this {@link UIComponent} that have a {@link ValueExpression}.<br>
     * TODO: we can't really cache this since the programmer can change the view without restarting the whole servlet
     * context. We may be able to find out about a change in the view though and introduce a caching mechanism then.
     * 
     * @param component
     *            the {@link UIComponent} of which the properties should be searched for EL expressions
     * @return a list of {@link ExprTuple}
     */
    protected List<ExprTuple> findComponentExpressions(UIComponent component) {
        List<ExprTuple> result = new ArrayList<ExprTuple>();

        try {
            PropertyDescriptor[] pd = Introspector.getBeanInfo(component.getClass()).getPropertyDescriptors();
            for (PropertyDescriptor p : pd) {
                // we try to see if this is a ValueExpression by probing it
                ValueExpression ve = this.getValueExpression(p.getName());
                if (ve != null) {
                    result.add(new ExprTuple(trimExpression(ve.getExpressionString()), component));
                }
            }

        } catch (IntrospectionException e) {
            // TODO better error handling
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Finds the 'floating' EL expressions that are not a property of a component, but are directly part of the view
     * body. Since {@link UIInstructions} sometimes not only return the EL expression but also some surrounding HTML
     * tags, we do a rudimentary but robust parsing (everything that does not conform to <code>#{...}</code> is
     * ignored).<br>
     * FIXME this is a hack and renders this implementation dependent on the Sun Mojarra implementation. That is, the
     * JSF specification does not seem to say anything about such floating EL elements. There might be a way to get
     * those through the ELResolver facility though.
     * 
     * @param component
     *            the {@link UIInstructions} which should be searched for EL expressions.
     * @return a list of {@link ExprTuple}
     */
    private List<ExprTuple> findFloatingExpressions(UIInstructions component) {
        List<ExprTuple> result = new ArrayList<ExprTuple>();

        String txt = component.toString();
        // see if it has some EL
        int n = txt.indexOf("#{");
        if (n > -1) {
            txt = txt.substring(n + 2);
            int e = txt.indexOf("}");
            if (e > -1) {
                txt = txt.substring(0, e);
                result.add(new ExprTuple(txt, component));
            }
        }
        return result;
    }

    private String trimExpression(String expr) {
        return expr.substring(2, expr.length() - 1);
    }

    class ExprTuple {
        private String expr;

        private UIComponent component;

        public String getExpr() {
            return expr;
        }

        public void setExpr(String expr) {
            this.expr = expr;
        }

        public UIComponent getComponent() {
            return component;
        }

        public void setComponent(UIComponent component) {
            this.component = component;
        }

        public ExprTuple(String expr, UIComponent component) {
            super();
            this.expr = expr;
            this.component = component;
        }
    }

}
