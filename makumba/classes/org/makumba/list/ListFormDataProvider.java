package org.makumba.list;

import javax.servlet.jsp.PageContext;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.Pointer;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.QueryExecution;
import org.makumba.list.engine.valuecomputer.ValueComputer;
import org.makumba.list.tags.MakumbaTag;
import org.makumba.list.tags.QueryTag;
import org.makumba.providers.FormDataProvider;
import org.makumba.util.MultipleKey;


/**
 * Native implementation of the FormDataProvider by the list. We pass on an AnalysableTag to all our methods in order to
 * provide context to the method, so it knows what kind of data to compute. In addition we also pass some other
 * parameters useful for data computation.
 * 
 * @author Manuel Gay
 * @version $Id: ListFormDataProvider.java,v 1.1 18.09.2007 18:31:07 Manuel Exp $
 */
public class ListFormDataProvider implements FormDataProvider {
    
    private static final String[] dummyQuerySections = { null, null, null, null, null };
    

    /**
     * Computes data at the beginning of form analysis.
     * 
     * @param tag
     *            the AnalysableTag for whom we do this
     * @param pageCache
     *            the pageCache of the current page
     * @param ptrExpr
     *            the base pointer expression
     */
    public void onFormStartAnalyze(AnalysableTag tag, PageCache pageCache, String ptrExpr) {
        ValueComputer vc;
        if ((Boolean) pageCache.retrieve(MakumbaTag.QUERY_LANGUAGE, MakumbaTag.QUERY_LANGUAGE).equals("hql")) {
            // if we use hibernate, we have to use select object.id, not the whole object
            vc = ValueComputer.getValueComputerAtAnalysis(tag, QueryTag.getParentListKey(tag, pageCache), ptrExpr + ".id", pageCache);
        } else {
            vc = ValueComputer.getValueComputerAtAnalysis(tag, QueryTag.getParentListKey(tag, pageCache), ptrExpr, pageCache);
        }
        pageCache.cache(MakumbaTag.VALUE_COMPUTERS, tag.getTagKey(), vc);
    }

    /**
     * Computes data at the beginning of BasicValueTag analysis (InputTag, OptionTag)
     * 
     * @param tag
     *            the AnalysableTag for whom we do this
     * @param pageCache
     *            the pageCache of the current page
     * @param ptrExpr
     *            the epxression of the base pointer
     */
    public void onBasicValueStartAnalyze(AnalysableTag tag, boolean isNull, MultipleKey parentFormKey, PageCache pageCache, String ptrExpr) {
            MultipleKey parentListKey = getBasicValueParentListKey(tag, isNull, parentFormKey, pageCache);
        
        //we first compute the pointer expression, which depends on the QueryProvider
        String ptrExpression = new String();

        if (parentListKey == null) { // If there is no enclosing mak:list
            ptrExpression = ptrExpr;
        } else {
            // FIXME this should be provided by the QueryProvider
            ptrExpression = QueryTag.getQuery(pageCache, parentListKey).transformPointer(ptrExpr);
        }
        pageCache.cache(MakumbaTag.VALUE_COMPUTERS, tag.getTagKey(), ValueComputer.getValueComputerAtAnalysis(tag,
            parentListKey, ptrExpression, pageCache));
    }

    private MultipleKey getBasicValueParentListKey(AnalysableTag tag, boolean isNull, MultipleKey parentFormKey, PageCache pageCache) {
        MultipleKey k = QueryTag.getParentListKey(tag, pageCache);
        if (k != null) {
            return k;
        } else if (isNull) {
            return null;
        } else {
        /* we don't have a query around us, so we must make a dummy query for computing the value via the database */
            QueryTag.cacheQuery(pageCache, parentFormKey, dummyQuerySections, null);
            return parentFormKey;
        }
    }

    /**
     * Computes data for analysis start in case of tags which aren't wrapped in a query context (of the kind mak:list).
     * This is the case of InputTag and OptionTag in particular contexts (e.g. a newForm, which does not depend itself
     * on data but where these tags need to fetch e.g. set values from a specific location).
     * 
     * @param tag
     * @param pageCache
     * @param expr
     */
    public void onNonQueryStartAnalyze(AnalysableTag tag, boolean isNull, MultipleKey parentFormKey, PageCache pageCache, String expr) {
        pageCache.cache(MakumbaTag.VALUE_COMPUTERS, tag.getTagKey(), ValueComputer.getValueComputerAtAnalysis(tag,
            getBasicValueParentListKey(tag, isNull, parentFormKey, pageCache), expr, pageCache));
    }

    /**
     * Computes data at the end of form analysis.
     * 
     * @param tag
     *            the AnalysableTag for whom we do this
     * @param pageCache
     *            the pageCache of the current page
     * @return the {@link FieldDefinition} corresponding to the object the tag is based on
     */
    public void onFormEndAnalyze(MultipleKey tagKey, PageCache pageCache) {
        ComposedQuery dummy = (ComposedQuery) pageCache.retrieve(MakumbaTag.QUERY, tagKey);
        if (dummy != null)
            dummy.analyze();
    }

    /**
     * Computes data at the end of BasicValueTag analysis (InputTag, OptionTag)
     * 
     * @param tag
     *            the AnalysableTag for whom we do this
     * @param pageCache
     *            the pageCache of the current page
     * @return the {@link FieldDefinition} corresponding to the object the tag is based on
     */
    public FieldDefinition onBasicValueEndAnalyze(MultipleKey tagKey, PageCache pageCache) {
        return getTypeOnEndAnalyze(tagKey, pageCache);
    }

    /**
     * Computes data at the beginning of form runtime.
     * 
     * @param tag the FormTag that starts running
     * @param pageCache the pageCache of the current page
     * @param pageContext the pageContext in which the form is
     * @return the base pointer expression corresponding to the current tag
     * @throws LogicException
     */
    public void onFormStartTag(MultipleKey tagKey, PageCache pageCache, PageContext pageContext) throws LogicException {
        // if we have a dummy query, we simulate an iteration
        if (pageCache.retrieve(MakumbaTag.QUERY, tagKey) != null) {
            QueryExecution.startListGroup(pageContext);
            QueryExecution.getFor(tagKey, pageContext, null, null).onParentIteration();
        }

    }

    public void onFormEndTag(MultipleKey tagKey, PageCache pageCache, PageContext pageContext) {
        // if we have a dummy query, we simulate end iteration
        if (pageCache.retrieve(MakumbaTag.QUERY, tagKey) != null)
            QueryExecution.endListGroup(pageContext);

    }

    /**
     * Computes the type of the field based on the information collected at analysis.
     * 
     * @param tag
     *            the running tag
     * @param pageCache
     *            the pageCache of the current page
     * @return a {@link FieldDefinition} indicating the type of what we are interested in
     */
    public FieldDefinition getTypeOnEndAnalyze(MultipleKey tagKey, PageCache pageCache) {
        ValueComputer vc = (ValueComputer) pageCache.retrieve(MakumbaTag.VALUE_COMPUTERS, tagKey);
        vc.doEndAnalyze(pageCache);
        return vc.getType();
    }

    public String computeBasePointer(MultipleKey tagKey, PageContext pageContext) throws LogicException {

        Object o = ((ValueComputer) MakumbaTag.getPageCache(pageContext).retrieve(MakumbaTag.VALUE_COMPUTERS,
            tagKey)).getValue(pageContext);
        if (!(o instanceof Pointer))
            throw new RuntimeException("Pointer expected");
        return ((Pointer) o).toExternalForm();
    }

    /**
     * Returns the value of the currently running tag, for Input and Option tags.
     * 
     * @param tag
     *            the AnalysableTag that is currently running
     * @param pageCache
     *            the pageCache of the current page
     * @return the value corresponding to the tag.
     * @throws LogicException
     */
    public Object getValue(MultipleKey tagKey, PageContext pageContext, PageCache pageCache) throws LogicException {
        return ((ValueComputer) pageCache.retrieve(MakumbaTag.VALUE_COMPUTERS, tagKey)).getValue(pageContext);
    }

    public DataDefinition getBasePointerType(AnalysableTag tag, String baseObject) {
        PageContext pageContext = tag.getPageContext();
        return QueryTag.getQuery(MakumbaTag.getPageCache(pageContext), QueryTag.getParentListKey(tag, null)).getLabelType(
            baseObject);
    }

    /**
     * Gets the type of an input tag
     * 
     * @param base
     *            TODO
     * @param fieldName
     *            the name of the field of which the type should be returned
     * @param pageCache
     *            the page cache of the current page
     * @return A FieldDefinition corresponding to the type of the input field
     */
    public FieldDefinition getInputTypeAtAnalysis(DataDefinition dd, String fieldName, PageCache pageCache) {
        if (dd == null)
            return null;
        int dot = -1;
        while (true) {
            int dot1 = fieldName.indexOf(".", dot + 1);
            if (dot1 == -1)
                return dd.getFieldDefinition(fieldName.substring(dot + 1));
            String fname = fieldName.substring(dot + 1, dot1);
            FieldDefinition fd = dd.getFieldDefinition(fname);
            if (fd == null)
                throw new org.makumba.NoSuchFieldException(dd, fname);
            if (!(fd.getType().equals("ptr") && fd.isNotNull()) && !fd.getType().equals("ptrOne"))
                throw new org.makumba.InvalidFieldTypeException(fieldName + " must be linked via not null pointers, "
                        + fd.getDataDefinition().getName() + "->" + fd.getName() + " is not");
            dd = fd.getPointedType();
            dot = dot1;
        }
    }

    public MultipleKey getParentListKey(AnalysableTag tag) {
        return QueryTag.getParentListKey(tag, null);
    }

}
