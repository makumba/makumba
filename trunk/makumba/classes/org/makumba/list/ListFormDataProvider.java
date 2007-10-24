package org.makumba.list;

import javax.servlet.jsp.PageContext;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.Pointer;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.forms.tags.CriterionTag;
import org.makumba.forms.tags.SearchFieldTag;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.QueryExecution;
import org.makumba.list.engine.valuecomputer.ValueComputer;
import org.makumba.list.tags.GenericListTag;
import org.makumba.list.tags.QueryTag;
import org.makumba.providers.FormDataProvider;


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
    

    /* (non-Javadoc)
     * @see org.makumba.list.FormDataProvider#onFormStartAnalyze(org.makumba.analyser.AnalysableTag, org.makumba.analyser.PageCache, java.lang.String)
     */
    public void onFormStartAnalyze(AnalysableTag tag, PageCache pageCache, String ptrExpr) {
        ValueComputer vc;
        if ((Boolean) pageCache.retrieve(MakumbaJspAnalyzer.QUERY_LANGUAGE, MakumbaJspAnalyzer.QUERY_LANGUAGE).equals("hql")) {
            // if we use hibernate, we have to use select object.id, not the whole object
            vc = ValueComputer.getValueComputerAtAnalysis(tag, QueryTag.getParentListKey(tag, pageCache), ptrExpr + ".id", pageCache);
        } else {
            vc = ValueComputer.getValueComputerAtAnalysis(tag, QueryTag.getParentListKey(tag, pageCache), ptrExpr, pageCache);
        }
        pageCache.cache(GenericListTag.VALUE_COMPUTERS, tag.getTagKey(), vc);
    }

    /* (non-Javadoc)
     * @see org.makumba.list.FormDataProvider#onBasicValueStartAnalyze(org.makumba.analyser.AnalysableTag, boolean, org.makumba.commons.MultipleKey, org.makumba.analyser.PageCache, java.lang.String)
     */
    public void onBasicValueStartAnalyze(AnalysableTag tag, boolean isNull, MultipleKey parentFormKey, PageCache pageCache, String ptrExpr) {
            MultipleKey parentListKey = getBasicValueParentListKey(tag, isNull, parentFormKey, pageCache);
        
        //we first compute the pointer expression, which depends on the QueryProvider
        String ptrExpression = new String();

        if (parentListKey == null) { // If there is no enclosing mak:list
            ptrExpression = ptrExpr;
        } else {
            // FIXME this should be provided by the QueryProvider
            ptrExpression = QueryTag.getQuery(pageCache, parentListKey).qep.transformPointer(ptrExpr, QueryTag.getQuery(pageCache, parentListKey).getFromSection());
        }
        pageCache.cache(GenericListTag.VALUE_COMPUTERS, tag.getTagKey(), ValueComputer.getValueComputerAtAnalysis(tag,
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

    /* (non-Javadoc)
     * @see org.makumba.list.FormDataProvider#onNonQueryStartAnalyze(org.makumba.analyser.AnalysableTag, boolean, org.makumba.commons.MultipleKey, org.makumba.analyser.PageCache, java.lang.String)
     */
    public void onNonQueryStartAnalyze(AnalysableTag tag, boolean isNull, MultipleKey parentFormKey, PageCache pageCache, String expr) {
        pageCache.cache(GenericListTag.VALUE_COMPUTERS, tag.getTagKey(), ValueComputer.getValueComputerAtAnalysis(tag,
            getBasicValueParentListKey(tag, isNull, parentFormKey, pageCache), expr, pageCache));
    }

    /* (non-Javadoc)
     * @see org.makumba.list.FormDataProvider#onFormEndAnalyze(org.makumba.commons.MultipleKey, org.makumba.analyser.PageCache)
     */
    public void onFormEndAnalyze(MultipleKey tagKey, PageCache pageCache) {
        ComposedQuery dummy = (ComposedQuery) pageCache.retrieve(GenericListTag.QUERY, tagKey);
        if (dummy != null)
            dummy.analyze();
    }

    /* (non-Javadoc)
     * @see org.makumba.list.FormDataProvider#onBasicValueEndAnalyze(org.makumba.commons.MultipleKey, org.makumba.analyser.PageCache)
     */
    public FieldDefinition onBasicValueEndAnalyze(MultipleKey tagKey, PageCache pageCache) {
        return getTypeOnEndAnalyze(tagKey, pageCache);
    }

    /* (non-Javadoc)
     * @see org.makumba.list.FormDataProvider#onFormStartTag(org.makumba.commons.MultipleKey, org.makumba.analyser.PageCache, javax.servlet.jsp.PageContext)
     */
    public void onFormStartTag(MultipleKey tagKey, PageCache pageCache, PageContext pageContext) throws LogicException {
        // if we have a dummy query, we simulate an iteration
        if (pageCache.retrieve(GenericListTag.QUERY, tagKey) != null) {
            QueryExecution.startListGroup(pageContext);
            QueryExecution.getFor(tagKey, pageContext, null, null).onParentIteration();
        }

    }

    /* (non-Javadoc)
     * @see org.makumba.list.FormDataProvider#onFormEndTag(org.makumba.commons.MultipleKey, org.makumba.analyser.PageCache, javax.servlet.jsp.PageContext)
     */
    public void onFormEndTag(MultipleKey tagKey, PageCache pageCache, PageContext pageContext) {
        // if we have a dummy query, we simulate end iteration
        if (pageCache.retrieve(GenericListTag.QUERY, tagKey) != null)
            QueryExecution.endListGroup(pageContext);

    }

    /* (non-Javadoc)
     * @see org.makumba.list.FormDataProvider#getTypeOnEndAnalyze(org.makumba.commons.MultipleKey, org.makumba.analyser.PageCache)
     */
    public FieldDefinition getTypeOnEndAnalyze(MultipleKey tagKey, PageCache pageCache) {
        ValueComputer vc = (ValueComputer) pageCache.retrieve(GenericListTag.VALUE_COMPUTERS, tagKey);
        vc.doEndAnalyze(pageCache);
        return vc.getType();
    }
    
    /* (non-Javadoc)
     * @see org.makumba.list.FormDataProvider#getBasePointerType(org.makumba.analyser.AnalysableTag, org.makumba.analyser.PageCache, java.lang.String)
     */
    public DataDefinition getBasePointerType(AnalysableTag tag, PageCache pageCache, String baseObject) {
        return QueryTag.getQuery(pageCache, getParentListKey(tag)).getLabelType(baseObject);
     }

    /* (non-Javadoc)
     * @see org.makumba.list.FormDataProvider#computeBasePointer(org.makumba.commons.MultipleKey, javax.servlet.jsp.PageContext)
     */
    public String computeBasePointer(MultipleKey tagKey, PageContext pageContext) throws LogicException {

        Object o = ((ValueComputer) GenericListTag.getPageCache(pageContext, MakumbaJspAnalyzer.getInstance()).retrieve(GenericListTag.VALUE_COMPUTERS,
            tagKey)).getValue(pageContext);
        if (!(o instanceof Pointer))
            throw new RuntimeException("Pointer expected");
        return ((Pointer) o).toExternalForm();
    }

    /* (non-Javadoc)
     * @see org.makumba.list.FormDataProvider#getValue(org.makumba.commons.MultipleKey, javax.servlet.jsp.PageContext, org.makumba.analyser.PageCache)
     */
    public Object getValue(MultipleKey tagKey, PageContext pageContext, PageCache pageCache) throws LogicException {
        return ((ValueComputer) pageCache.retrieve(GenericListTag.VALUE_COMPUTERS, tagKey)).getValue(pageContext);
    }

    /* (non-Javadoc)
     * @see org.makumba.list.FormDataProvider#getInputTypeAtAnalysis(org.makumba.DataDefinition, java.lang.String, org.makumba.analyser.PageCache)
     */
    public FieldDefinition getInputTypeAtAnalysis(AnalysableTag tag, DataDefinition dd, String fieldName, PageCache pageCache) {
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
            if (!(tag instanceof SearchFieldTag || tag instanceof CriterionTag) && !(fd.getType().equals("ptr") && fd.isNotNull()) && !fd.getType().equals("ptrOne"))
                throw new org.makumba.InvalidFieldTypeException(fieldName + " must be linked via not null pointers, "
                        + fd.getDataDefinition().getName() + "->" + fd.getName() + " is not");
            dd = fd.getPointedType();
            dot = dot1;
        }
    }

    /* (non-Javadoc)
     * @see org.makumba.list.FormDataProvider#getParentListKey(org.makumba.analyser.AnalysableTag)
     */
    public MultipleKey getParentListKey(AnalysableTag tag) {
        return QueryTag.getParentListKey(tag, null);
    }

}
