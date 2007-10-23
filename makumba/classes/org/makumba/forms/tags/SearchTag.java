package org.makumba.forms.tags;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.Pointer;
import org.makumba.analyser.PageCache;
import org.makumba.commons.Configuration;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.StringUtils;
import org.makumba.commons.attributes.HttpParameters;
import org.makumba.commons.attributes.RequestAttributes;
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

    private static final String RANGE_END = "RangeEnd";

    public static final String SUFFIX_INPUT_MATCH = "Match";

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
        return new SearchResponderOperation();
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
        Object keyComponents[] = { in.getName(), formName, fdp.getParentListKey(this), getClass() };
        tagKey = new MultipleKey(keyComponents);
    }

    protected class SearchResponderOperation extends ResponderOperation {

        private static final long serialVersionUID = 1L;

        private boolean haveValue(Object value) {
            return value instanceof Vector || isSingleValue(value);
        }

        private boolean isSingleValue(Object value) {
            return value != null && !Pointer.isNullObject(value) && value.toString().length() > 0;
        }

        public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix)
                throws LogicException {

            Dictionary data = resp.getHttpData(req, suffix);
            RequestAttributes attributes = RequestAttributes.getAttributes(req);
            HttpParameters parameters = RequestAttributes.getParameters(req);
            DataDefinition dd = (new DataDefinitionProvider(new Configuration())).getDataDefinition(resp.getSearchType());

            req.setAttribute(ResponseControllerHandler.MAKUMBA_FORM_RELOAD, "true");
            String objectName = "o";
            req.setAttribute(resp.getFormName() + "VariableFrom", resp.getSearchType() + " " + objectName);
            req.setAttribute(resp.getFormName() + "VariableFrom", "");
            String where = "";

            Enumeration enumeration = data.keys();
            while (enumeration.hasMoreElements()) {
                String inputName = (String) enumeration.nextElement();

                // we skip fields that are range-end fields
                if (inputName.endsWith(RANGE_END)
                        && data.get(inputName.substring(0, inputName.length() - RANGE_END.length())) != null) {
                    continue;
                }

                String[] multiFieldSearchCriterion = resp.getMultiFieldSearchCriterion(inputName);
                Object value = attributes.getAttribute(inputName);// parameters.getParameter(key);

                FieldDefinition fd = DataDefinitionProvider.getFieldDefinition(dd, inputName, inputName);

                if (haveValue(value)) {
                    if (where.length() > 0) {
                        where += " AND ";
                    }

                    for (int i = 0; i < multiFieldSearchCriterion.length; i++) {
                        if (i == 0) {
                            where += " ( ";
                        }
                        where = computeTypeSpecificQuery(req, attributes, parameters, objectName, where,
                            multiFieldSearchCriterion[i], inputName, fd);
                        if (i < multiFieldSearchCriterion.length - 1) {
                            where += " OR ";
                        } else {
                            where += " ) ";
                        }
                    }
                }
            }
            req.setAttribute(resp.getFormName() + "Where", where);
            req.setAttribute(resp.getFormName() + "Done", Boolean.TRUE);
            // System.out.println("where: " + where);
            // System.out.println("params: " + parameters);
            // System.out.println("attributes: " + attributes);
            return null;
        }

        public String verify(Responder resp) {
            return null;
        }

        private String computeTypeSpecificQuery(HttpServletRequest req, RequestAttributes attributes,
                HttpParameters parameters, String objectName, String where, String fieldName, String attributeName,
                FieldDefinition fd) throws LogicException {
            Object value = attributes.getAttribute(attributeName);
            if (value instanceof Vector) {
                where += objectName + "." + fieldName + " IN SET ($" + attributeName + ")";
            } else if (isSingleValue(value)) {
                Object advancedMatch = parameters.getParameter(attributeName + SearchTag.SUFFIX_INPUT_MATCH);

                if (StringUtils.equals(advancedMatch, SearchTag.MATCH_BETWEEN_ALL)) {
                    where += " ( " + objectName + "." + fieldName + ">$" + attributeName;
                    if (advancedMatch.equals(SearchTag.MATCH_BETWEEN_INCLUSIVE)) {
                        where += " OR " + objectName + "." + fieldName + "=$" + attributeName;
                    }
                    where += " ) AND ( " + objectName + "." + fieldName + "<$" + attributeName + RANGE_END;
                    if (advancedMatch.equals(SearchTag.MATCH_BETWEEN_INCLUSIVE)) {
                        where += " OR " + objectName + "." + fieldName + "=$" + attributeName + RANGE_END;
                    }
                    where += " ) ";
                } else {
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
            }
            return where;
        }
    }

}
