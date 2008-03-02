package org.makumba.forms.tags;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.Pointer;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.StringUtils;
import org.makumba.commons.attributes.HttpParameters;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.forms.responder.FormResponder;
import org.makumba.forms.responder.Responder;
import org.makumba.forms.responder.ResponderOperation;
import org.makumba.forms.responder.ResponseControllerHandler;
import org.makumba.providers.DataDefinitionProvider;

/**
 * This class provides a search form. It slightly differs from other forms in the way that it uses it's own tags, namely
 * <ul>
 * <li>{@link CriterionTag}, neede for each search criterion to appear in the form</>
 * <li> {@link SearchFieldTag}, nested in a {@link CriterionTag}, generates an input</li>
 * <li> {@link MatchModeTag} will generate an input that allows to select the match mode, e.g. exact or range searching.
 * </li>
 * </ul>
 * 
 * @author Rudolf Mayer
 * @version $Id: SearchTag.java,v 1.1 Oct 9, 2007 1:50:47 AM rudi Exp $
 */
public class SearchTag extends FormTagBase {
    private static final long serialVersionUID = 1L;

    public static final String ATTRIBUTE_NAME_DONE = "Done";

    public static final String ATTRIBUTE_NAME_QUERYSTRING = "QueryString";

    public static final String ATTRIBUTE_NAME_VARIABLE_FROM = "VariableFrom";

    public static final String ATTRIBUTE_NAME_WHERE = "Where";

    public static final String MATCH_AFTER = "after";

    public static final String MATCH_BEFORE = "before";

    public static final String MATCH_BEGINS = "begins";

    public static final String MATCH_BETWEEN = "between";

    public static final String MATCH_BETWEEN_INCLUSIVE = "betweenInclusive";

    public static final String MATCH_CONTAINS = "contains";

    public static final String MATCH_ENDS = "ends";

    public static final String MATCH_EQUALS = "equals";

    public static final String MATCH_GREATER = "greaterThan";

    public static final String MATCH_LESS = "lessThan";

    private static final String[] MATCH_AFTER_GREATER = { MATCH_AFTER, MATCH_GREATER };

    private static final String[] MATCH_BEFORE_LESS = { MATCH_BEFORE, MATCH_LESS };

    private static final String[] MATCH_BETWEEN_ALL = { MATCH_BETWEEN, MATCH_BETWEEN_INCLUSIVE };

    // hold comparison operators for between matches
    private static final Hashtable<String, String[]> MATCH_BETWEEN_OPERATORS = new Hashtable<String, String[]>();
    static {
        MATCH_BETWEEN_OPERATORS.put(MATCH_BETWEEN, new String[] { ">", "<" });
        MATCH_BETWEEN_OPERATORS.put(MATCH_BETWEEN_INCLUSIVE, new String[] { ">=", "<=" });
    }

    private static final String RANGE_END = "RangeEnd";

    public static final String SUFFIX_INPUT_MATCH = "Match";

    public static final String OBJECT_NAME = "o";

    DataDefinition in = null;

    private void fillFormAction() {
        if (formAction == null) { // if no action is given, we take the current page as action
            String requestURI = ((HttpServletRequest) pageContext.getRequest()).getRequestURI();
            if (requestURI.indexOf("/") != -1) {
                requestURI = requestURI.substring(requestURI.lastIndexOf("/") + 1);
            }
            formAction = requestURI;
        }
    }

    public DataDefinition getDataTypeAtAnalysis(PageCache pageCache) {
        return in;
    }

    public ResponderOperation getResponderOperation(String operation) {
        return searchOp;
    }

    /**
     * Inherited
     */
    public void initialiseState() {
        fillFormAction();
        super.initialiseState();
        if (in != null) {
            responder.setSearchType(in);
        }
        if (formMessage == null) {
            // FIXME: this should not be set from here
            responder.setMessage(Responder.defaultMessageSearchForm);
        }
        responder.setFormName(formName);
    }

    public void setAction(String s) {
        formAction = s;
        fillFormAction();
        // System.out.println("form action in search tag set action: " + formAction);
    }

    public void setIn(String s) {
        in = ddp.getDataDefinition(s);
    }

    /**
     * Inherited
     */
    public void setTagKey(PageCache pageCache) {
        tagKey = new MultipleKey(new Object[] { formName });
    }

    private final static ResponderOperation searchOp = new ResponderOperation() {

        private static final long serialVersionUID = 1L;

        private boolean notEmpty(Object value) {
            if (value instanceof Vector) {
                return ((Vector) value).size() > 0;
            } else {
                return isSingleValue(value);
            }
        }

        private boolean isSingleValue(Object value) {
            return value != null && !(value instanceof Vector) && !Pointer.isNullObject(value)
                    && value.toString().length() > 0;
        }

        /**
         * Respond to the search form, by constructing the querie's variableFrom and where parts. The following request
         * attributes will be set and made available in the page.
         * <ul>
         * <li>&lt:formname&gt;From - the from part of the query, i.e. the part specified in the in='' attribute of the
         * search form; will be automatically used by mak:resultList</li>
         * <li>&lt:formname&gt;Where - the where part of the query; can be used in mak:list, and will be automatically
         * used by mak:resultList</li>
         * <li>&lt:formname&gt;VariableFrom - the variable from part of the query, i.e. basically selecting sets linked
         * from the main data definition; can be used in mak:list, and will be automatically used by mak:resultList</li>
         * <li>&lt:formname&gt;From - the from part of the query, i.e. the part specified in the in='' attribute of the
         * search form; will be automatically used by mak:resultList</li>
         * <li>&lt:formname&gt;Done - boolean value set to true if the search was conducted - should be used in a
         * &lt;c:if test="${&lt:formname&gt;Done}"&gt; around the list displaying the results, to avoid the list being
         * evaluated when the Where and VariableFrom parts are not set.</li>
         * </ul>
         */
        public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                throws LogicException {

            Dictionary data = resp.getHttpData(req, suffix);
            RequestAttributes attributes = RequestAttributes.getAttributes(req);
            HttpParameters parameters = RequestAttributes.getParameters(req);
            DataDefinition dd = new DataDefinitionProvider().getDataDefinition(resp.getSearchType());

            // indicate that the form is reloaded, similar as for validation errors
            req.setAttribute(ResponseControllerHandler.MAKUMBA_FORM_RELOAD, "true");

            // set the from part & set a lable name
            req.setAttribute(resp.getFormName() + "From", resp.getSearchType() + " " + OBJECT_NAME);

            HashSet<String> variableFroms = new HashSet<String>(1); // hold variable from selections
            String where = "";
            StringBuffer queryString = new StringBuffer();

            // iterate over all fields in the form
            Enumeration enumeration = data.keys();
            while (enumeration.hasMoreElements()) {
                String inputName = (String) enumeration.nextElement();

                Object value = attributes.getAttribute(inputName);

                if (notEmpty(value)) {
                    appendParams(queryString, inputName, value);
                }

                // special treatment for range end fields
                if (inputName.endsWith(RANGE_END)) {
                    if (notEmpty(attributes.getAttribute(getRangeBeginName(inputName)))) {
                        continue; // those fields will get treated with the range begin check
                    } else {
                        // seems like a hack, but is needed to get the correct field names in the mdd, etc..
                        inputName = getRangeBeginName(inputName);
                    }
                }

                String[] multiFieldSearchCriterion = resp.getMultiFieldSearchCriterion(inputName);
                FieldDefinition fd = DataDefinitionProvider.getFieldDefinition(dd, inputName, inputName);

                if (notEmpty(value)) { // we only regard fields that have a value entered
                    if (where.length() > 0) { // combine different fields with AND
                        where += " AND ";
                    }

                    String whereThisField = "";
                    // iterate over all data fields this input is associated with
                    for (int i = 0; i < multiFieldSearchCriterion.length; i++) {
                        String objectName = OBJECT_NAME;
                        String fieldName = multiFieldSearchCriterion[i];
                        String attributeName = inputName;
                        Object matchMode = parameters.getParameter(inputName + SearchTag.SUFFIX_INPUT_MATCH);
                        if (StringUtils.notEmpty(matchMode)) {
                            appendParams(queryString, inputName + SearchTag.SUFFIX_INPUT_MATCH, matchMode);
                        }

                        if (whereThisField.length() > 0) {
                            // if we are having a multi-field match, we might need to combine rules
                            whereThisField = " OR " + whereThisField;
                        }
                        if (StringUtils.equals(matchMode, SearchTag.MATCH_BETWEEN_ALL)) {
                            // range comparison
                            whereThisField += computeRangeQuery(parameters, objectName, fieldName, attributeName,
                                matchMode);
                        } else {
                            // other comparison
                            whereThisField += computeTypeSpecificQuery(req, parameters, objectName, fieldName,
                                attributeName, fd);
                        }
                    }
                    if (whereThisField.trim().length() > 0) {
                        where += " ( " + whereThisField + " ) ";
                        if (fd.isSetType()) { // enhance the variableFrom part if we select sets
                            variableFroms.add(OBJECT_NAME + "." + inputName + " " + OBJECT_NAME + "_" + inputName);
                        }
                    }
                }
            }
            appendParams(queryString, FormResponder.responderName, parameters.getParameter(FormResponder.responderName));

            // set the attributes, and do logging
            Hashtable<String, Object> searchResults = new Hashtable<String, Object>(4);
            searchResults.put(resp.getFormName() + ATTRIBUTE_NAME_WHERE, where);
            searchResults.put(resp.getFormName() + ATTRIBUTE_NAME_VARIABLE_FROM, StringUtils.toString(variableFroms,
                false));
            searchResults.put(resp.getFormName() + ATTRIBUTE_NAME_QUERYSTRING, queryString.toString());
            searchResults.put(resp.getFormName() + ATTRIBUTE_NAME_DONE, Boolean.TRUE);

            for (String key : searchResults.keySet()) {
                req.setAttribute(key, searchResults.get(key));
                Logger.getLogger("org.makumba.searchForm").fine(
                    "Set search form result attribute '" + key + "': " + req.getAttribute(key));
            }
            return searchResults;
        }

        private void appendParams(StringBuffer link, String inputName, Object value) {
            if (link.length() > 0) {
                link.append("&");
            }
            if (value instanceof Vector) {
                for (int i = 0; i < ((Vector) value).size(); i++) {
                    link.append(inputName).append("=").append(treatValue(inputName, ((Vector) value).get(i)));
                    if (i + 1 < ((Vector) value).size()) {
                        link.append("&");
                    }
                }
            } else {
                link.append(inputName).append("=").append(treatValue(inputName, value));
            }
        }

        private String treatValue(String inputName, Object value) {
            if (value instanceof Pointer) {
                return ((Pointer) value).toExternalForm();
            } else {
                return value.toString();
            }
        }

        private String computeRangeQuery(HttpParameters parameters, String objectName, String fieldName,
                String attributeName, Object advancedMatch) {
            String where = "";
            String attributeNameEnd = attributeName + RANGE_END;
            boolean haveBegin = true;
            boolean haveEnd = fieldName.endsWith(RANGE_END) || notEmpty(parameters.getParameter(attributeNameEnd));
            if (!notEmpty(parameters.getParameter(fieldName))) {
                haveBegin = false;
            }
            // only compare with lower end if we have it
            if (haveBegin) {
                where += objectName + "." + fieldName + MATCH_BETWEEN_OPERATORS.get(advancedMatch)[0] + "$"
                        + attributeName;
            }
            if (haveBegin && haveEnd) {
                where += " AND ";
            }
            // only compare with upper end of range if we have it
            if (haveEnd) {
                where += objectName + "." + fieldName + MATCH_BETWEEN_OPERATORS.get(advancedMatch)[1] + "$"
                        + attributeNameEnd;
            }
            if (haveBegin && haveEnd) { // need extra parantheses only if we have both range ends
                where = " ( " + where + " ) ";
            }
            return where;
        }

        public String verify(Responder resp) {
            return null;
        }

        private String computeTypeSpecificQuery(HttpServletRequest req, HttpParameters parameters, String objectName,
                String fieldName, String attributeName, FieldDefinition fd) throws LogicException {
            String where = "";
            Object value = parameters.getParameter(attributeName);
            if (value instanceof Vector || fd.isSetType()) {
                // we need to check for the field type as well - we have different labels for the sets
                String labelName;
                if (!fd.isSetType()) {
                    labelName = objectName + "." + fieldName;
                } else {
                    labelName = objectName + "_" + fieldName;
                }
                where += labelName + " IN SET ($" + attributeName + ")";
            } else if (isSingleValue(value)) {
                Object advancedMatch = parameters.getParameter(attributeName + SearchTag.SUFFIX_INPUT_MATCH);
                where += objectName + "." + fieldName;

                if (advancedMatch == null || advancedMatch.equals(SearchTag.MATCH_EQUALS)) {
                    // do a normal match
                    where += "=$" + attributeName;
                } else { // do a more sophisticated matching
                    if (fd.isStringType()) {
                        String keyLike = attributeName + "__Like";
                        if (advancedMatch.equals(SearchTag.MATCH_CONTAINS)) {
                            value = "%" + value + "%";
                        } else if (advancedMatch.equals(SearchTag.MATCH_BEGINS)) {
                            value = value + "%";
                        } else if (advancedMatch.equals(SearchTag.MATCH_ENDS)) {
                            value = "%" + value;
                        }
                        req.setAttribute(keyLike, value);
                        where += " LIKE $" + keyLike + "";
                    } else if (fd.isDateType() || fd.isNumberType()) { // matches for numbers & dates
                        // before or < match
                        if (StringUtils.equals(advancedMatch, SearchTag.MATCH_BEFORE_LESS)) {
                            where += "<$" + attributeName;
                            // after or > match
                        } else if (StringUtils.equals(advancedMatch, SearchTag.MATCH_AFTER_GREATER)) {
                            where += ">$" + attributeName;
                        }
                    }
                }
            }
            return where;
        }

        private String getRangeBeginName(String fieldName) {
            return fieldName.substring(0, fieldName.length() - RANGE_END.length());
        }
    };

    @Override
    protected void doAnalyzedCleanup() {
        super.doAnalyzedCleanup();
        in = null;
    }

}
