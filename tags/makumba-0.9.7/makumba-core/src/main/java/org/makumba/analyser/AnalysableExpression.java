package org.makumba.analyser;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.commons.MultipleKey;

/**
 * An analysable EL expression, used for creating "implicit objects" as per EL definition (see
 * http://java.sun.com/products/jsp/2.1/docs/jsp-2_1-pfd2/javax/el/ELResolver.html) Implementations of this class can be
 * retrieved by {@link ELResolver}-s in order to get status information, perform analysis-time tasks, etc.
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: AnalysableExpression.java,v 1.1 Jan 22, 2010 6:13:56 PM manu Exp $
 */
public abstract class AnalysableExpression extends AnalysableElement {

    private static final long serialVersionUID = 1L;

    protected ELData elData;

    protected String expression;

    protected AnalysableTag parent;

    protected MultipleKey key;

    @Override
    public ElementData getElementData() {
        return this.elData;
    }

    /**
     * Sets the EL data and adapts the expression
     */
    public void setELDataAtAnalysis(ELData ed) {
        this.elData = ed;
    }

    public void treatELExpressionAtAnalysis(String expression) {
        this.expression = treatExpressionAtAnalysis(expression);
    }

    public void setParent(AnalysableTag parent) {
        this.parent = parent;
    }

    @Override
    public AnalysableTag getParent() {
        return this.parent;
    }

    public abstract void setKey(PageCache pageCache);

    public MultipleKey getKey() {
        return this.key;
    }

    /**
     * Performs analysis-time operations
     * 
     * @param cache
     *            the {@link PageCache} available to this expression
     */
    public abstract void analyze(PageCache pageCache);

    public abstract void doEndAnalyze(PageCache pageCache);

    /**
     * Modifies the expression before analysis. This is necessary because the expression that comes from page analysis
     * is not tailored to the specific expression we want to handle
     * 
     * @return the expression value, trimmed down to what is necessary for this kind of EL expression evaluator
     */
    public abstract String treatExpressionAtAnalysis(String expression);

    /**
     * Gets the prefix of the makumba EL expression, e.g. Value
     * 
     * @return the prefix of this EL expression
     */
    public abstract String getPrefix();

    /**
     * Resolves the expression at runtime
     * 
     * @param cache
     *            the {@link PageCache} available to this expression
     */
    public abstract Object resolve(PageContext pc, PageCache pageCache) throws LogicException;

    /**
     * Finds the first parent of this expression that is a tag of the specified type
     * 
     * @clazz the type of the parent tag we are looking for
     * @return a parent {@link Tag}
     */
    protected Tag findParentWithClass(Class<?> clazz) {

        boolean isInterface = false;
        Tag from = getParent();

        if (parent == null || clazz == null || !Tag.class.isAssignableFrom(clazz)
                && !(isInterface = clazz.isInterface())) {
            return null;
        }

        for (;;) {
            Tag parent = from;

            if (parent == null) {
                return null;
            }

            if (isInterface && clazz.isInstance(parent) || clazz.isAssignableFrom(parent.getClass())) {
                return parent;
            } else {
                from = from.getParent();
            }
        }
    }

    /**
     * Checks that the function has the expected number of arguments. This should be enforced by the JSP compiler, but
     * an additional check can't hurt
     */
    protected void checkNumberOfArguments(final int argumentCount) throws ProgrammerError {
        if (elData.getArguments().size() != argumentCount) {
            throw new ProgrammerError("Function '" + expression + "' accepts " + argumentCount + " arguments, but "
                    + elData.getArguments().size() + " were provided.");
        }
    }

}
