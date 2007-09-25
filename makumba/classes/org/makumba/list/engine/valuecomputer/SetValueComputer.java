package org.makumba.list.engine.valuecomputer;

import java.util.Vector;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.PageAttributes;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.QueryExecution;
import org.makumba.list.tags.QueryTag;
import org.makumba.list.tags.ValueTag;

/**
 * The manager of a setValueQuery.
 * 
 * @author Cristian Bogdan
 */
public class SetValueComputer extends QueryValueComputer {
    /** If we are in a value tag, the name of the queryProjection that computes the title field, otherwise null */
    String name = null;

    /** If we are in a value tag, the index of the queryProjection that computes the title field, otherwise null */
    int nameIndex;

    /**
     * Makes a query that has an extra FROM: the set requested. As projections, add the key of the set type and, if we
     * are in a value tag, the title field.
     * 
     * @param analyzed
     *            the tag that is analyzed
     * @param parentListKey
     *            the key of the parent list
     * @param set
     *            the FieldDefinition of the set we want to compute a value of
     * @param setExpr
     *            the expression of the set
     * @param pageCache
     *            the page cache of the current page
     */
    SetValueComputer(AnalysableTag analyzed, MultipleKey parentListKey, FieldDefinition set, String setExpr, PageCache pageCache) {
        type = set;
        String label = setExpr.replace('.', '_');
        String queryProps[] = new String[5];
        queryProps[ComposedQuery.FROM] = setExpr + " " + label;

        if (analyzed instanceof ValueTag) {
            name = label + "." + set.getForeignTable().getTitleFieldName();
            queryProps[ComposedQuery.ORDERBY] = name;
        }

        makeQueryAtAnalysis(parentListKey, set.getName(), queryProps, label, pageCache);

        if (analyzed instanceof ValueTag)
            QueryTag.getQuery(pageCache, queryKey).checkProjectionInteger(name);
    }

    /**
     * Computes nameIndex
     * @param pageCache
     *            the page cache of the current page
     */
    @Override
    public void doEndAnalyze(PageCache pageCache) {
        super.doEndAnalyze(pageCache);
        if (name != null)
            nameIndex = QueryTag.getQuery(pageCache, queryKey).checkProjectionInteger(name).intValue();
    }

    /**
     * Goes through the iterationGroupData and returns a vector with the set values. Used only by InputTag
     * 
     * @param running
     *            the tag that is currently running
     * @throws LogicException
     */
    @Override
    public Object getValue(PageContext pageContext) throws LogicException {
        QueryExecution ex = runQuery(pageContext);
        int n = ex.dataSize();
        Vector v = new Vector();

        for (ex.iteration = 0; ex.iteration < n; ex.iteration++)
            v.addElement(ex.currentListData().data[projectionIndex]);
        return v;
    }

    /**
     * Goes through the iterationGroupData and prints the set values, comma-separated; also sets var (Vector with the
     * set values) and printVar
     * 
     * @param running
     *            the tag that is currently running
     * @param pageCache
     *            the pageCache of the current page
     * @throws JspException
     * @throws LogicException
     */
    @Override
    // FIXME (fred) shouldn't the formatting be in view.html package, instead of here?
    public void print(ValueTag running, PageCache pageCache) throws JspException, LogicException {
        QueryExecution ex = runQuery(running.getPageContext());
        int n = ex.dataSize();
        Vector v = null;

        if (running.getVar() != null)
            v = new Vector();

        String sep = "";
        StringBuffer print = new StringBuffer();
        for (ex.iteration = 0; ex.iteration < n; ex.iteration++) {
            print.append(sep);
            sep = ",";
            if (running.getVar() != null)
                v.addElement(ex.currentListData().data[projectionIndex]);
            print.append(ex.currentListData().data[nameIndex]);
        }
        String s = print.toString();

        // replace by 'default' or 'empty' if necessary
        if (n == 0 && running.getParams().get("default") != null)
            s = (String) running.getParams().get("default");

        if (s.length() == 0 && running.getParams().get("empty") != null)
            s = (String) running.getParams().get("empty");

        if (running.getVar() != null)
            PageAttributes.setAttribute(running.getPageContext(), running.getVar(), v);
        if (running.getPrintVar() != null)
            running.getPageContext().setAttribute(running.getPrintVar(), s);
        if (running.getPrintVar() == null && running.getVar() == null) {
            try {
                running.getPageContext().getOut().print(s);
            } catch (Exception e) {
                throw new JspException(e.toString());
            }
        }
    }
}

