package org.makumba.providers;

import javax.servlet.jsp.PageContext;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MultipleKey;

/**
 * This provider aims at providing the data necessary for the Makumba forms to compute its results.
 *
 * @author Manuel Gay
 * @version $Id: FormDataProvider.java,v 1.1 21.09.2007 09:40:27 Manuel Exp $
 */
public interface FormDataProvider {

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
    public abstract void onFormStartAnalyze(AnalysableTag tag, PageCache pageCache, String ptrExpr);

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
    public abstract void onBasicValueStartAnalyze(AnalysableTag tag, boolean isNull, MultipleKey parentFormKey,
            PageCache pageCache, String ptrExpr);

    /**
     * Computes data for analysis start in case of tags which aren't wrapped in a query context (of the kind mak:list).
     * This is the case of InputTag and OptionTag in particular contexts (e.g. a newForm, which does not depend itself
     * on data but where these tags need to fetch e.g. set values from a specific location).
     * 
     * @param tag
     * @param pageCache
     * @param expr
     */
    public abstract void onNonQueryStartAnalyze(AnalysableTag tag, boolean isNull, MultipleKey parentFormKey,
            PageCache pageCache, String expr);

    /**
     * Computes data at the end of form analysis.
     * 
     * @param tag
     *            the AnalysableTag for whom we do this
     * @param pageCache
     *            the pageCache of the current page
     * @return the {@link FieldDefinition} corresponding to the object the tag is based on
     */
    public abstract void onFormEndAnalyze(MultipleKey tagKey, PageCache pageCache);

    /**
     * Computes data at the end of BasicValueTag analysis (InputTag, OptionTag)
     * 
     * @param tag
     *            the AnalysableTag for whom we do this
     * @param pageCache
     *            the pageCache of the current page
     * @return the {@link FieldDefinition} corresponding to the object the tag is based on
     */
    public abstract FieldDefinition onBasicValueEndAnalyze(MultipleKey tagKey, PageCache pageCache);

    /**
     * Computes data at the beginning of form runtime.
     * 
     * @param tag the FormTag that starts running
     * @param pageCache the pageCache of the current page
     * @param pageContext the pageContext in which the form is
     * @return the base pointer expression corresponding to the current tag
     * @throws LogicException
     */
    public abstract void onFormStartTag(MultipleKey tagKey, PageCache pageCache, PageContext pageContext)
            throws LogicException;

    public abstract void onFormEndTag(MultipleKey tagKey, PageCache pageCache, PageContext pageContext);

    /**
     * Computes the type of the field based on the information collected at analysis.
     * 
     * @param tag
     *            the running tag
     * @param pageCache
     *            the pageCache of the current page
     * @return a {@link FieldDefinition} indicating the type of what we are interested in
     */
    public abstract FieldDefinition getTypeOnEndAnalyze(MultipleKey tagKey, PageCache pageCache);

    /**
     * Gives the type corresponding to the base object of a tag, based on its name
     * @param tag the tag for which we need to discover the tag
     * @param pageCache the page cache of the current page
     * @param baseObject the label of the object we want to discover
     * @return the {@link DataDefinition} corresponding to the type of the object
     */
    public abstract DataDefinition getBasePointerType(AnalysableTag tag, PageCache pageCache, String baseObject);

    public abstract String computeBasePointer(MultipleKey tagKey, PageContext pageContext) throws LogicException;

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
    public abstract Object getValue(MultipleKey tagKey, PageContext pageContext, PageCache pageCache)
            throws LogicException;

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
    public abstract FieldDefinition getInputTypeAtAnalysis(DataDefinition dd, String fieldName, PageCache pageCache);

    public abstract MultipleKey getParentListKey(AnalysableTag tag);

}
