package org.makumba.forms.tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import javax.servlet.jsp.JspException;

import org.makumba.FieldDefinition;
import org.makumba.HtmlChoiceWriter;
import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.StringUtils;
import org.makumba.commons.tags.GenericMakumbaTag;

/**
 * Implements a match mode choser, which can take the form of a select box or a radio button, and allows to select the
 * type of match mode for the enclosing search criterion. Different modes apply for string (char, text), number (int,
 * real) and date types. The latter two can also be mathed against a range.
 * 
 * @author Rudolf Mayer
 * @version $Id: MatchModeTag.java,v 1.1 Oct 21, 2007 1:37:25 PM rudi Exp $
 */
public class MatchModeTag extends GenericMakumbaTag {

    public static final String[] allowedTypes = { "radio", "select" };

    private static final long serialVersionUID = 1L;

    private String[][] matchModes = null;

    /** {@value #knownRangeMatchModes} */
    private static final Hashtable<String, String> knownRangeMatchModes = new Hashtable<String, String>(4);
    static {
        knownRangeMatchModes.put(SearchTag.MATCH_BETWEEN, SearchTag.MATCH_BETWEEN);
        knownRangeMatchModes.put(SearchTag.MATCH_BETWEEN_INCLUSIVE, "between (inclusive)");
    }

    private static final Hashtable<String, String> knownStringMatchModes = new Hashtable<String, String>(4);
    static {
        knownStringMatchModes.put(SearchTag.MATCH_CONTAINS, SearchTag.MATCH_CONTAINS);
        knownStringMatchModes.put(SearchTag.MATCH_EQUALS, SearchTag.MATCH_EQUALS);
        knownStringMatchModes.put(SearchTag.MATCH_BEGINS, "begins with");
        knownStringMatchModes.put(SearchTag.MATCH_ENDS, "ends with");
    }

    private static final Hashtable<String, String> knownDateMatchModes = new Hashtable<String, String>(4);
    static {
        knownDateMatchModes.put(SearchTag.MATCH_EQUALS, SearchTag.MATCH_EQUALS);
        knownDateMatchModes.put(SearchTag.MATCH_BEFORE, "before");
        knownDateMatchModes.put(SearchTag.MATCH_AFTER, "after");
    }

    private static final Hashtable<String, String> knownNumberMatchModes = new Hashtable<String, String>(4);
    static {
        knownNumberMatchModes.put(SearchTag.MATCH_EQUALS, SearchTag.MATCH_EQUALS);
        knownNumberMatchModes.put(SearchTag.MATCH_LESS, "less than");
        knownNumberMatchModes.put(SearchTag.MATCH_GREATER, "greater than");
    }

    private static final Hashtable<String, String> allMatchModes = new Hashtable<String, String>(4);

    private String elementSeparator;

    private String labelSeparator;

    static {
        allMatchModes.putAll(knownRangeMatchModes);
        allMatchModes.putAll(knownStringMatchModes);
        allMatchModes.putAll(knownDateMatchModes);
        allMatchModes.putAll(knownNumberMatchModes);
    }

    public void setMatchModes(String s) {
        ArrayList<String> keys = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();
        String[] modes = s.split(",");
        for (int i = 0; i < modes.length; i++) {
            modes[i] = modes[i].trim();
            String mode;
            if (modes[i].contains("=")) {
                mode = modes[i].substring(0, modes[i].indexOf("=")).trim();
            } else {
                mode = modes[i].trim();
            }
            keys.add(mode);
            if (modes[i].contains("=")) {
                values.add(modes[i].substring(modes[i].indexOf("=") + 1).trim());
            } else {
                values.add(allMatchModes.get(mode));
            }
        }
        matchModes = new String[][] { keys.toArray(new String[values.size()]),
                values.toArray(new String[values.size()]) };
    }

    @Override
    public void setType(String s) {
        checkValidAttributeValues("type", s, allowedTypes);
        super.setType(s);
    }

    @Override
    public void doStartAnalyze(PageCache pageCache) {
        if (getCriterionTag() == null) {
            throw new ProgrammerError("\'matchMode\' tag must be enclosed in a 'criterion' tag");
        }
        FieldDefinition fd = getCriterionTag().getTypeFromContext(pageCache);
        // match modes are only allowed for string types, i.e. char and text, and number (real, int) and dates
        if (matchModes != null && fd != null && !(fd.isStringType() || fd.isNumberType() || fd.isDateType())) {
            throw new ProgrammerError(
                    "'matchModes' tag is only valid for 'char', 'text', 'int', 'real' and 'date' types, field is of type '"
                            + fd.getType() + "'!");
        }

        // analyse the match modes.
        // can do it only here and not in setMatchModes as we need to know the type of the input.
        if (fd != null && matchModes != null) {
            Hashtable<String, String> curentModes = null;
            if (getCriterionTag().isRange()) {
                curentModes = knownRangeMatchModes;
            } else if (fd.isStringType()) {
                curentModes = knownStringMatchModes;
            } else if (fd.isNumberType()) {
                curentModes = knownNumberMatchModes;
            } else if (fd.isDateType()) {
                curentModes = knownDateMatchModes;
            }
            for (int i = 0; i < matchModes[0].length; i++) {
                String mode = matchModes[0][i];
                if (!curentModes.containsKey(mode)) {
                    throw new ProgrammerError("Unknown match mode '" + mode + "'. Valid options are: "
                            + StringUtils.toString(curentModes.keySet()));
                }
            }
        }
    }

    protected CriterionTag getCriterionTag() {
        return (CriterionTag) findAncestorWithClass(this, CriterionTag.class);
    }

    @Override
    public int doAnalyzedEndTag(PageCache pageCache) throws LogicException, JspException {
        String matchInputName = getCriterionTag().getInputName() + getCriterionTag().getForm().responder.getSuffix()
                + SearchTag.SUFFIX_INPUT_MATCH;
        String matchMode = pageContext.getRequest().getParameter(matchInputName);

        HtmlChoiceWriter hcw = new HtmlChoiceWriter(matchInputName);
        hcw.setValues(matchModes[0]);
        hcw.setLabels(matchModes[1]);
        hcw.setValues(Arrays.asList(matchModes[0]).iterator());
        hcw.setLabels(Arrays.asList(matchModes[1]).iterator());
        hcw.setSelectedValues(matchMode);

        String type = (String) params.get("type");
        try {
            if (StringUtils.equalsAny(type, new String[] { "radio" })) {
                if (elementSeparator != null)
                    hcw.setOptionSeparator(elementSeparator);
                if (labelSeparator != null)
                    hcw.setTickLabelSeparator(labelSeparator);
                pageContext.getOut().println(hcw.getRadioSelect());
            } else {
                pageContext.getOut().println(hcw.getSelect());
            }
        } catch (java.io.IOException e) {
            throw new JspException(e.toString());
        }
        return super.doAnalyzedEndTag(pageCache);
    }

    @Override
    public void setTagKey(PageCache pageCache) {
        tagKey = new MultipleKey(new Object[] { getCriterionTag().tagKey, id, matchModes });
    }

    @Override
    public boolean allowsIdenticalKey() {
        return false;
    }

}
