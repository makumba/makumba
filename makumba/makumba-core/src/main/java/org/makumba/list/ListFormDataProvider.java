package org.makumba.list;

import java.util.Map;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.analyser.AnalysableElement;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
import org.makumba.analyser.TagData;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.forms.tags.CriterionTag;
import org.makumba.forms.tags.FormTagBase;
import org.makumba.forms.tags.InputTag;
import org.makumba.forms.tags.SearchFieldTag;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.QueryExecution;
import org.makumba.list.engine.valuecomputer.ValueComputer;
import org.makumba.list.tags.QueryTag;
import org.makumba.list.tags.ValueTag;
import org.makumba.providers.DataDefinitionProvider;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.list.FormDataProvider#onFormStartAnalyze(org.makumba.analyser.AnalysableTag,
     *      org.makumba.analyser.PageCache, java.lang.String)
     */
    @Override
    public void onFormStartAnalyze(AnalysableTag tag, PageCache pageCache, String ptrExpr) {
        if (MakumbaJspAnalyzer.getQueryLanguage(pageCache).equals("hql")) {
            ptrExpr += ".id";
        }
        MultipleKey parentListKey = QueryTag.getParentListKey(tag, pageCache);
        if (parentListKey == null) {
            // if we are not enclosed in a list or object, try to find the input with the object name
            FormTagBase form = (FormTagBase) TagSupport.findAncestorWithClass(tag, FormTagBase.class);
            if (form == null) { // if we are also not inf a form, throw an error
                throw new ProgrammerError("mak:addForm needs to be enclosed in a list, object or form tag");
            }
            // get all the tags already parsed in the page
            Map<Object, Object> cache = pageCache.retrieveCache(MakumbaJspAnalyzer.TAG_DATA_CACHE);
            boolean found = false;
            DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();
            for (Object key : cache.keySet()) {
                // we loop all inputs, even if we already found a match; thus, we will always use the input closest to
                // the addForm, for good or bad..
                TagData tagData = (TagData) cache.get(key);
                if (tagData.getTagObject() instanceof InputTag) { // consider only input tags
                    if (tagData.attributes.get("name") != null && tagData.attributes.get("name").equals(ptrExpr)) {
                        // and only the one that has the same name as the addForm base object
                        String dataType = tagData.attributes.get("dataType");
                        if (StringUtils.isBlank(dataType)) {
                            throw new ProgrammerError(
                                    "Currently, only inputs with dataType=\"..\" are supported as addForm base objects");
                        }
                        FieldDefinition fd = ddp.makeFieldDefinition("dummyName", dataType);
                        if (fd.isPointer()) {
                            // FIXME: in nested forms, ptrExpr is most likely not specific enough to identify an input,
                            // as repeated inputs with the same name will get a suffix added, e.g. _1
                            pageCache.cache(MakumbaJspAnalyzer.ADD_FORM_DATA_TYPE, tag.getTagKey(),
                                new Object[] { fd.getPointedType(), ptrExpr });
                            found = true;
                        } else {
                            throw new ProgrammerError("Can use only 'ptr' type inputs for addForms, given expr '"
                                    + ptrExpr + "' is denoting an input of type '" + fd + "'");
                        }
                    }
                }
            }
            if (!found) { // if we did not find any input matching, throw an error
                throw new ProgrammerError("Did not find any input with the name '" + ptrExpr
                        + "' to be used as base object for the addForm");
            }
        } else { // enclosed in list or form

            boolean isValue = tag instanceof ValueTag;

            pageCache.cache(MakumbaJspAnalyzer.VALUE_COMPUTERS, tag.getTagKey(),
                ValueComputer.getValueComputerAtAnalysis(isValue, parentListKey, ptrExpr, pageCache));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.list.FormDataProvider#onBasicValueStartAnalyze(org.makumba.analyser.AnalysableTag, boolean,
     *      org.makumba.commons.MultipleKey, org.makumba.analyser.PageCache, java.lang.String)
     */
    @Override
    public void onBasicValueStartAnalyze(AnalysableTag tag, boolean isNull, MultipleKey parentFormKey,
            PageCache pageCache, String ptrExpr) {
        MultipleKey parentListKey = getBasicValueParentListKey(tag, isNull, parentFormKey, pageCache);

        boolean isValue = tag instanceof ValueTag;

        pageCache.cache(MakumbaJspAnalyzer.VALUE_COMPUTERS, tag.getTagKey(),
            ValueComputer.getValueComputerAtAnalysis(isValue, parentListKey, ptrExpr, pageCache));
    }

    private MultipleKey getBasicValueParentListKey(AnalysableTag tag, boolean isNull, MultipleKey parentFormKey,
            PageCache pageCache) {
        MultipleKey k = QueryTag.getParentListKey(tag, pageCache);
        if (k != null) {
            return k;
        } else if (isNull) {
            return null;
        } else {
            /* we don't have a query around us, so we must make a dummy query for computing the value via the database */
            // TODO: authorization?
            QueryTag.cacheQuery(pageCache, parentFormKey, dummyQuerySections, null, null);
            return parentFormKey;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.list.FormDataProvider#onNonQueryStartAnalyze(org.makumba.analyser.AnalysableTag, boolean,
     *      org.makumba.commons.MultipleKey, org.makumba.analyser.PageCache, java.lang.String)
     */
    @Override
    public void onNonQueryStartAnalyze(AnalysableTag tag, boolean isNull, MultipleKey parentFormKey,
            PageCache pageCache, String expr) {
        MultipleKey parentListKey = getBasicValueParentListKey(tag, isNull, parentFormKey, pageCache);

        // we're trying to make sure we don't produce a bad HQL query
        if (MakumbaJspAnalyzer.getQueryLanguage(pageCache).equals("hql")
                && ValueComputer.isPointer(pageCache, parentListKey, expr) && !expr.endsWith(".id")) {
            expr += ".id";
        }

        boolean isValue = tag instanceof ValueTag;

        pageCache.cache(MakumbaJspAnalyzer.VALUE_COMPUTERS, tag.getTagKey(),
            ValueComputer.getValueComputerAtAnalysis(isValue, parentListKey, expr, pageCache));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.list.FormDataProvider#onFormEndAnalyze(org.makumba.commons.MultipleKey,
     *      org.makumba.analyser.PageCache)
     */
    @Override
    public void onFormEndAnalyze(MultipleKey tagKey, PageCache pageCache) {
        ComposedQuery dummy = (ComposedQuery) pageCache.retrieve(MakumbaJspAnalyzer.QUERY, tagKey);
        if (dummy != null) {
            dummy.analyze();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.list.FormDataProvider#onBasicValueEndAnalyze(org.makumba.commons.MultipleKey,
     *      org.makumba.analyser.PageCache)
     */
    @Override
    public FieldDefinition onBasicValueEndAnalyze(MultipleKey tagKey, PageCache pageCache) {
        return getTypeOnEndAnalyze(tagKey, pageCache);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.list.FormDataProvider#onFormStartTag(org.makumba.commons.MultipleKey,
     *      org.makumba.analyser.PageCache, javax.servlet.jsp.PageContext)
     */
    @Override
    public void onFormStartTag(MultipleKey tagKey, PageCache pageCache, PageContext pageContext) throws LogicException {
        // if we have a dummy query, we simulate an iteration
        if (pageCache.retrieve(MakumbaJspAnalyzer.QUERY, tagKey) != null) {
            QueryExecution.startListGroup(pageContext);
            QueryExecution.getFor(tagKey, pageContext, null, null, null).onParentIteration();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.list.FormDataProvider#onFormEndTag(org.makumba.commons.MultipleKey,
     *      org.makumba.analyser.PageCache, javax.servlet.jsp.PageContext)
     */
    @Override
    public void onFormEndTag(MultipleKey tagKey, PageCache pageCache, PageContext pageContext) {
        // if we have a dummy query, we simulate end iteration
        if (pageCache.retrieve(MakumbaJspAnalyzer.QUERY, tagKey) != null) {
            QueryExecution.endListGroup(pageContext);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.list.FormDataProvider#getTypeOnEndAnalyze(org.makumba.commons.MultipleKey,
     *      org.makumba.analyser.PageCache)
     */
    @Override
    public FieldDefinition getTypeOnEndAnalyze(MultipleKey tagKey, PageCache pageCache) {
        if (retrieveBaseObjectInputInfo(tagKey, pageCache) != null) {
            DataDefinition addToPtrInput = (DataDefinition) retrieveBaseObjectInputInfo(tagKey, pageCache)[0];
            return addToPtrInput.getFieldDefinition(addToPtrInput.getIndexPointerFieldName());
        } else {
            ValueComputer vc = (ValueComputer) pageCache.retrieve(MakumbaJspAnalyzer.VALUE_COMPUTERS, tagKey);
            vc.doEndAnalyze(pageCache);
            return vc.getType();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.list.FormDataProvider#getBasePointerType(org.makumba.analyser.AnalysableTag,
     *      org.makumba.analyser.PageCache, java.lang.String)
     */
    @Override
    public DataDefinition getBasePointerType(AnalysableTag tag, PageCache pageCache, String baseObject) {
        if (retrieveBaseObjectInputInfo(tag.getTagKey(), pageCache) != null) {
            return (DataDefinition) retrieveBaseObjectInputInfo(tag.getTagKey(), pageCache)[0];
        } else {
            return QueryTag.getQuery(pageCache, getParentListKey(tag)).getLabelType(baseObject);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.list.FormDataProvider#computeBasePointer(org.makumba.commons.MultipleKey,
     *      javax.servlet.jsp.PageContext)
     */
    @Override
    public String computeBasePointer(MultipleKey tagKey, PageContext pageContext) throws LogicException {
        PageCache pageCache = AnalysableElement.getPageCache(pageContext, MakumbaJspAnalyzer.getInstance());
        if (retrieveBaseObjectInputInfo(tagKey, pageCache) != null) {
            return "valueOf_" + retrieveBaseObjectInputInfo(tagKey, pageCache)[1];
        }
        Object o = ((ValueComputer) pageCache.retrieve(MakumbaJspAnalyzer.VALUE_COMPUTERS, tagKey)).getValue(pageContext);
        if (!(o instanceof Pointer)) {
            throw new RuntimeException("Pointer expected, got instead " + o);
        }
        return ((Pointer) o).toExternalForm();
    }

    /** Retrieves the data type and the name of the input associated with this form from the page cache */
    private Object[] retrieveBaseObjectInputInfo(MultipleKey tagKey, PageCache pageCache) {
        return (Object[]) pageCache.retrieve(MakumbaJspAnalyzer.ADD_FORM_DATA_TYPE, tagKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.list.FormDataProvider#getValue(org.makumba.commons.MultipleKey, javax.servlet.jsp.PageContext,
     *      org.makumba.analyser.PageCache)
     */
    @Override
    public Object getValue(MultipleKey tagKey, PageContext pageContext, PageCache pageCache) throws LogicException {
        return ((ValueComputer) pageCache.retrieve(MakumbaJspAnalyzer.VALUE_COMPUTERS, tagKey)).getValue(pageContext);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.list.FormDataProvider#getInputTypeAtAnalysis(org.makumba.DataDefinition, java.lang.String,
     *      org.makumba.analyser.PageCache)
     */
    @Override
    public FieldDefinition getInputTypeAtAnalysis(AnalysableTag tag, DataDefinition dd, String fieldName,
            PageCache pageCache) {
        if (dd == null) {
            return null;
        }
        int dot = -1;
        while (true) {
            int dot1 = fieldName.indexOf(".", dot + 1);
            if (dot1 == -1) {
                return dd.getFieldDefinition(fieldName.substring(dot + 1));
            }
            String fname = fieldName.substring(dot + 1, dot1);
            FieldDefinition fd = dd.getFieldDefinition(fname);
            if (fd == null) {
                throw new org.makumba.NoSuchFieldException(dd, fname);
            }
            if (!(tag instanceof SearchFieldTag || tag instanceof CriterionTag)
                    && !(fd.getType().equals("ptr") && fd.isNotNull()) && !fd.getType().equals("ptrOne")) {
                throw new org.makumba.InvalidFieldTypeException(fieldName + " must be linked via not null pointers, "
                        + fd.getDataDefinition().getName() + "->" + fd.getName() + " is not");
            }
            dd = fd.getPointedType();
            dot = dot1;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.makumba.list.FormDataProvider#getParentListKey(org.makumba.analyser.AnalysableTag)
     */
    @Override
    public MultipleKey getParentListKey(AnalysableTag tag) {
        return QueryTag.getParentListKey(tag, null);
    }

    private static class SingletonHolder implements org.makumba.commons.SingletonHolder {
        private static ListFormDataProvider singleton = new ListFormDataProvider();

        @Override
        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }

    public static ListFormDataProvider getInstance() {
        return SingletonHolder.singleton;
    }

    /*
     * This needs to be public because of forms.tags.FormTagBase which uses reflection to get an instance
     */
    public ListFormDataProvider() {

    }

}
