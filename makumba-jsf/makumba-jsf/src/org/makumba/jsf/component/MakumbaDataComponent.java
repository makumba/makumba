package org.makumba.jsf.component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;

import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.jsf.update.ObjectInputValue;
import org.makumba.list.engine.ComposedQuery;

/**
 * A makumba component that performs data handling operations
 * 
 * @author manu
 */
public interface MakumbaDataComponent {

    /**
     * Adds a new value in the component tree. The component receiving this value is then responsible for adding it to
     * the {@link ObjectInputValue} of the component declaring the base label of path.
     * 
     * @param label
     *            the base label of this value
     * @param path
     *            the path of the field to be set
     * @param value
     *            the value of the field
     * @param clientId
     *            the clientId of the input for the value
     */
    public void addValue(String label, String path, Object value, String clientId);

    /**
     * Adds a set value in the component tree. The component receiving this value is then responsible for adding it to
     * the {@link ObjectInputValue} of the component declaring the base label of path.
     * 
     * @param label
     *            the base label of this value
     * @param path
     *            the path of the field to be set
     * @param value
     *            a list of {@link Pointer}-s representing the members of the set
     * @param clientId
     *            the clientId of the input for the value
     */
    public void addSetValue(String label, String path, List<Pointer> value, String clientId);

    /**
     * Whether the action of the mak:object component (create / update) changed since the tree was first built.
     * 
     * @return <code>true</code> if the action changed, <code>false</code> otherwise.
     */
    public boolean hasActionChanged();

    public ComposedQuery getComposedQuery();

    class Util {
        private static final String ORG_MAKUMBA_JSF_INPUT = "org.makumba.jsf.input";

        public static MakumbaDataComponent findLabelDefinitionComponent(UIComponent current, String label) {
            UIComponent parent = current;
            MakumbaDataComponent candidate = null;
            while (parent != null) {
                if (parent instanceof MakumbaDataComponent) {
                    MakumbaDataComponent c = (MakumbaDataComponent) parent;
                    if (c.getComposedQuery().getFromLabelTypes().containsKey(label)) {
                        candidate = c;
                    }
                }
                parent = parent.getParent();
            }
            return candidate;
        }

        /**
         * there are many ways in which we can detect if validation was ok, so we isolate this method and improve it
         * later
         * 
         * @return
         */
        static public boolean validationFailed() {
            Severity sev = FacesContext.getCurrentInstance().getMaximumSeverity();
            return sev != null && FacesMessage.SEVERITY_ERROR.compareTo(sev) >= 0;
        }

        static boolean visitStaticTree(UIComponent target, VisitCallback c) {
            VisitResult visit = c.visit(null, target);
            if (visit == VisitResult.REJECT) {
                return false;
            }
            if (visit == VisitResult.COMPLETE) {
                return true;
            }

            for (UIComponent kid : target.getChildren()) {
                if (visitStaticTree(kid, c)) {
                    return true;
                }
            }
            return false;
        }

        public static UIComponent findInput(final UIComponent base, final String expr) {
            final String expr1 = "#{" + expr + "}";
            // FIXME: this is a workaround for UIComponent.getCurrentComponent() which fails in full postback during
            // update
            visitStaticTree(base, new VisitCallback() {
                @Override
                public VisitResult visit(VisitContext context, UIComponent target) {
                    if (base.getClass().isInstance(target) && target != base) {
                        return VisitResult.REJECT;
                    }
                    if (target instanceof EditableValueHolder
                            && target.getValueExpression("value").getExpressionString().equals(expr1)) {
                        base.getAttributes().put(ORG_MAKUMBA_JSF_INPUT, target);
                        return VisitResult.COMPLETE;
                    }
                    return VisitResult.ACCEPT;
                }
            });
            UIComponent ret = (UIComponent) base.getAttributes().get(ORG_MAKUMBA_JSF_INPUT);
            base.getAttributes().remove(ORG_MAKUMBA_JSF_INPUT);
            if (ret == null) {
                // we fall back to whatever there is
                ret = UIComponent.getCurrentComponent(FacesContext.getCurrentInstance());
            }
            return ret;
        }

        static boolean isCreateObject(String w) throws ProgrammerError {
            if (w.indexOf(Util.NEW_MARKER) > -1) {
                return true;
            } else {
                if (w.contains("$")) {
                    FacesContext ctx = FacesContext.getCurrentInstance();

                    // find param name
                    Matcher m = Util.p.matcher(w);
                    boolean hasNew = false;
                    if (m.find()) {
                        // try to find if one of them is a new()
                        String s = m.group().substring(1);
                        String paramValue = ctx.getApplication().evaluateExpressionGet(ctx, "#{param['" + s + "']}",
                            String.class);
                        String sessionValue = ctx.getApplication().evaluateExpressionGet(ctx,
                            "#{sessionScope['" + s + "']}", String.class);
                        String elValue = ctx.getApplication().evaluateExpressionGet(ctx, "#{" + s + "}", String.class);
                        if (paramValue.contains(Util.NEW_MARKER)
                                || sessionValue.contains(Util.NEW_MARKER)
                                || elValue.contains(Util.NEW_MARKER)) {
                            hasNew = true;
                        }

                        // check if we have more of them, in that case the where is not valid
                        // i.e. we do not allow complex expression in a where with a new()
                        if (m.find()) {
                            throw new ProgrammerError(
                                    "The where expression '"
                                            + w
                                            + "' for <mak:object> is not allowed: more than one $parameter was found having as value 'new()'");
                        }
                    }
                    return hasNew;
                }
            }
            return false;
        }

        static String NEW_MARKER = "new()";
        static final Pattern p = Pattern.compile("\\$\\w+");
    }

}
