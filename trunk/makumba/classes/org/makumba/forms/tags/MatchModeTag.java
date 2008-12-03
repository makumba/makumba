package org.makumba.forms.tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
 * Implements a match mode chooser, which can take the form of a select box or a radio button, and allows to select the
 * type of match mode for the enclosing search criterion. Different modes apply for string (char, text), number (int,
 * real) and date types. The latter two can also be matched against a range.
 * 
 * @author Rudolf Mayer
 * @version $Id: MatchModeTag.java,v 1.1 Oct 21, 2007 1:37:25 PM rudi Exp $
 */
public class MatchModeTag extends GenericMakumbaTag {

    public static final String[] allowedTypes = { "radio", "select" };

    private static final long serialVersionUID = 1L;

    private String[][] matchModes = null;

    /** {@value #knownRangeMatchModes} */
    public static final Hashtable<String, String> knownRangeMatchModes = new Hashtable<String, String>(4);
    static {
        knownRangeMatchModes.put(SearchTag.MATCH_BETWEEN, SearchTag.MATCH_BETWEEN);
        knownRangeMatchModes.put(SearchTag.MATCH_BETWEEN_INCLUSIVE, "between (inclusive)");
    }

    public static final Hashtable<String, String> knownStringMatchModes = new Hashtable<String, String>(4);
    static {
        knownStringMatchModes.put(SearchTag.MATCH_CONTAINS, SearchTag.MATCH_CONTAINS);
        knownStringMatchModes.put(SearchTag.MATCH_EQUALS, SearchTag.MATCH_EQUALS);
        knownStringMatchModes.put(SearchTag.MATCH_BEGINS, "begins with");
        knownStringMatchModes.put(SearchTag.MATCH_ENDS, "ends with");
    }

    public static final Hashtable<String, String> knownDateMatchModes = new Hashtable<String, String>(4);
    static {
        knownDateMatchModes.put(SearchTag.MATCH_EQUALS, SearchTag.MATCH_EQUALS);
        knownDateMatchModes.put(SearchTag.MATCH_BEFORE, "before");
        knownDateMatchModes.put(SearchTag.MATCH_AFTER, "after");
    }

    public static final Hashtable<String, String> knownNumberMatchModes = new Hashtable<String, String>(4);
    static {
        knownNumberMatchModes.put(SearchTag.MATCH_EQUALS, SearchTag.MATCH_EQUALS);
        knownNumberMatchModes.put(SearchTag.MATCH_LESS, "less than");
        knownNumberMatchModes.put(SearchTag.MATCH_GREATER, "greater than");
    }

    public static final Hashtable<String, String> allMatchModes = new Hashtable<String, String>(4);

    static {
        allMatchModes.putAll(knownRangeMatchModes);
        allMatchModes.putAll(knownStringMatchModes);
        allMatchModes.putAll(knownDateMatchModes);
        allMatchModes.putAll(knownNumberMatchModes);
    }

    public static Hashtable<String, String> getValidMatchmodes(boolean isRange, FieldDefinition fd) {
        if (isRange) {
            return knownRangeMatchModes;
        } else if (fd.isStringType()) {
            return knownStringMatchModes;
        } else if (fd.isNumberType()) {
            return knownNumberMatchModes;
        } else if (fd.isDateType()) {
            return knownDateMatchModes;
        }
        return null;
    }

    private String elementSeparator;

    private String labelSeparator;

    public void setMatchModes(String s) {
        if (getCriterionTag().getMatchMode() != null) {
            throw new ProgrammerError("Cannot have a matchMode tag if the criterion tag already defines a matchMode!");
        }
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
        if (keys.size() < 2) {
            throw new ProgrammerError("matchMode tag should contain at least two modes, but only '" + s
                    + "' was provided.");
        }
        matchModes = toMatchModeArrays(keys, values);
    }

    private String[][] toMatchModeArrays(Collection<String> keys, Collection<String> values) {
        return new String[][] { keys.toArray(new String[values.size()]), values.toArray(new String[values.size()]) };
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
            Hashtable<String, String> curentModes = getValidMatchmodes(getCriterionTag().isRange(), fd);
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
        // tell the criterion tag that we have this inner matchMode tag
        getCriterionTag().setHasMatchMode(true);

        String matchInputName = getCriterionTag().getInputName() + getCriterionTag().getForm().responder.getSuffix()
                + SearchTag.SUFFIX_INPUT_MATCH;
        String matchMode = pageContext.getRequest().getParameter(matchInputName);

        if (matchModes == null) {// if no match modes are provided, set the default ones
            FieldDefinition fd = getCriterionTag().getTypeFromContext(pageCache);
            Hashtable<String, String> defaultModes = new Hashtable<String, String>();
            if (getCriterionTag().isRange()) {
                defaultModes = knownRangeMatchModes;
            } else if (fd.isStringType()) {
                defaultModes = knownStringMatchModes;
            } else if (fd.isNumberType()) {
                defaultModes = knownNumberMatchModes;
            } else if (fd.isDateType()) {
                defaultModes = knownDateMatchModes;
            }
            matchModes = toMatchModeArrays(defaultModes.keySet(), defaultModes.values());
        }

        HtmlChoiceWriter hcw = new HtmlChoiceWriter(matchInputName);
        hcw.setValues(matchModes[0]);
        hcw.setLabels(matchModes[1]);
        hcw.setValues(Arrays.asList(matchModes[0]).iterator());
        hcw.setLabels(Arrays.asList(matchModes[1]).iterator());
        hcw.setSelectedValues(org.apache.commons.lang.StringUtils.isNotBlank(matchMode) ? matchMode : matchModes[0][0]);

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
        tagKey = new MultipleKey(new Object[] { getCriterionTag().tagKey, id, matchModes, getClass() });
    }

    @Override
    public boolean allowsIdenticalKey() {
        return false;
    }

}
