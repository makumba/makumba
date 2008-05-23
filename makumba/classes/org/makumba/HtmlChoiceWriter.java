package org.makumba;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * HtmlChoiceWriter creates HTML statements for printing choice inputs. It's capable of creating dropdown and list
 * selects, and radio buttons and checkboxes.
 * <p>
 * Note: this class is still work in progress, and its API is not to be considered 100% stable yet. Especially the
 * static methods may change or be removed.
 * <p>
 * FIXME : documentation needs improvement.
 * 
 * @since makumba-0.5.9.15
 * @author Frederik Habils
 */
public class HtmlChoiceWriter extends HtmlUtils {

    /*******************************************************************************************************************
     * PRIVATE CONSTANTS
     ******************************************************************************************************************/

    private static String NEWL = "\n";

    private static String[] EMPTY_ARRAY = {};

    /*******************************************************************************************************************
     * PRIVATE MEMBERS
     ******************************************************************************************************************/

    private String _name = null; // input field's name ("NAME=" + name)

    private Iterator _values = null; // iterator over the values (Strings)

    private Iterator _labels = null; // iterator over the labels (Strings)

    private String[] _selectedValues = EMPTY_ARRAY; // list of selected values (String)

    private String[] _deprecatedValues = EMPTY_ARRAY; // list of deprecated values (String)

    private boolean _ismultiple = false; // is it a multiple choice?

    private int _size = 1; // size of the select box

    private int _convert2Html = NO_CONV; // configuration whether to convert to HTML

    private String _literalHtml = ""; // literal html

    private String _tickLabelSeparator = " "; // separator between the tick box and the label

    private String[] _optionSeparator = { " " }; // separator between different options

    /*******************************************************************************************************************
     * PUBLIC CONSTANTS
     ******************************************************************************************************************/

    /** No conversion. */
    public static int NO_CONV = 0;

    /** Convert text encoding to legal HTML encoding. */
    public static int TXT2HTML = 1;

    // public int CONVERT_AUTO = 2; // not supported

    /*******************************************************************************************************************
     * PUBLIC METHODS
     ******************************************************************************************************************/

    /** Default constructor. */
    public HtmlChoiceWriter() {
    };

    /** Constructor, sets the name of the choice control. */
    public HtmlChoiceWriter(String name) {
        setName(name);
    };

    /** Sets the name of the choice control. */
    public void setName(String name) {
        _name = name;
    }

    /** Sets the values of each of the options for the choice control. Labels and Values must be in the same order. */
    public void setValues(List values) {
        _values = values.iterator();
    }

    /** Sets the values of each of the options for the choice control. Labels and Values must be in the same order. */
    public void setValues(String[] values) {
        _values = Arrays.asList(values).iterator();
    }

    /** Sets the values of each of the options for the choice control. Labels and Values must be in the same order. */
    public void setValues(Iterator values) {
        _values = values;
    }

    /** Sets the labels of each of the options for the choice control. Labels and Values must be in the same order. */
    public void setLabels(List labels) {
        _labels = labels.iterator();
    }

    /** Sets the labels of each of the options for the choice control. Labels and Values must be in the same order. */
    public void setLabels(String[] labels) {
        _labels = Arrays.asList(labels).iterator();
    }

    /** Sets the labels of each of the options for the choice control. Labels and Values must be in the same order. */
    public void setLabels(Iterator labels) {
        _labels = labels;
    }

    /** Sets the selected value, in case there is only one. Most suitable if this is a select-one choice control. */
    public void setSelectedValues(String selected) {
        if (selected != null) {
            String[] ss = { selected }; // [java] can't assign literal array directly to existing variable.
            _selectedValues = ss;
        } else {
            _selectedValues = EMPTY_ARRAY;
        }
    }

    /**
     * Sets the selected values (zero or more). The input can be in any order, not related to the setValues and
     * setLabels methods. The input array is changed by this method (sorted).
     */
    public void setSelectedValues(String[] selected) {
        if (selected != null) {
            Arrays.sort(_selectedValues = selected);
        } else {
            _selectedValues = EMPTY_ARRAY;
        }
    }

    /**
     * Sets the selected values (zero or more). The input can be in any order, not related to the setValues and
     * setLabels methods. The input List is not changed by this method.
     */
    public void setSelectedValues(List selected) {
        if (selected != null) {
            Arrays.sort(_selectedValues = (String[]) selected.toArray());
        } else {
            _selectedValues = EMPTY_ARRAY;
        }
    }

    /**
     * Sets the deprecated values (zero or more). The input can be in any order, not related to the setValues and
     * setLabels methods. The input array is changed by this method (sorted).
     */
    public void setDeprecatedValues(String[] deprecated) {
        if (deprecated != null) {
            Arrays.sort(_deprecatedValues = deprecated);
        } else {
            _deprecatedValues = EMPTY_ARRAY;
        }
    }

    /**
     * Sets the deprecated values (zero or more). The input can be in any order, not related to the setValues and
     * setLabels methods. The input List is not changed by this method.
     */
    public void setDeprecatedValues(List deprecated) {
        if (deprecated != null) {
            Arrays.sort(_deprecatedValues = (String[]) deprecated.toArray());
        } else {
            _deprecatedValues = EMPTY_ARRAY;
        }
    }

    /** Sets whether the choice control accepts 'multiple' selections or not. */
    public void setMultiple(boolean yn) {
        _ismultiple = yn;
    }

    /**
     * Sets whether the choice control accepts 'multiple' selections or not. Any string that contains 'multiple' will be
     * regarded as 'true'.
     */
    public void setMultiple(String mult) {
        _ismultiple = (mult != null && mult.indexOf("multiple") >= 0);
    }

    /** Sets the size of the choice control. Relevant for 'select' controls only, not for tickbox controls. */
    public void setSize(int n) {
        if (n > 0)
            _size = n;
    }

    /**
     * Configures the encoding transformation to be applied to labels and values of the choice control. Use the public
     * constants of the class as inputs.
     */
    public void setConvert2Html(int n) {
        _convert2Html = n;
    }

    /** Sets a literal html text to be included in the choice control. E.g. &lt;select LITERALHTML&gt; */
    public void setLiteralHtml(String html) {
        _literalHtml = html;
    }

    /** Sets the separator between the 'tickbox' and the label. Default is a space. */
    public void setTickLabelSeparator(String s) {
        _tickLabelSeparator = s;
    }

    /**
     * Sets the separator between different options. Only relevant for tickbox type of choice controls. Default is a
     * space.
     */
    public void setOptionSeparator(String s) {
        String[] ss = { s };
        _optionSeparator = ss;
    }

    /**
     * Sets the separator between different options. Only relevant for tickbox type of choice controls. Default is a
     * space. When there is more than one separator, the writer will cycle through all separators, when writing each of
     * the options.
     */
    public void setOptionSeparator(String[] s) {
        if (s != null && s.length > 0)
            _optionSeparator = s;
    }

    /**
     * Sets the separator between different options. Only relevant for tickbox type of choice controls. Default is a
     * space. When there is more than one separator, the writer will cycle through all separators, when writing each of
     * the options.
     */
    public void setOptionSeparator(List s) {
        if (s != null && s.size() > 0)
            _optionSeparator = (String[]) s.toArray();
    }

    /**
     * Creates HTML statement for a select control. Will use the 'multiple' setting to choose a select-one or
     * select-multiple output. If labels and values iterator has different sizes, the smaller applies.
     */
    public String getSelect() {
        if (_ismultiple) {
            return getSelectMultiple();
        } else {
            return getSelectOne();
        }
    }

    /**
     * Creates HTML statement for a select-one control. If labels and values iterator has different sizes, the smaller
     * applies. If selectedValues has more than 1 element, then only the first is considered as selected value.
     */
    public String getSelectOne() {
        boolean yn_convert2Html = (_convert2Html == TXT2HTML);
        String selectedValue = (_selectedValues.length != 0) ? _selectedValues[0] : null;
        Iterator itv = _values;
        Iterator itl = _labels;

        StringBuffer selectStatement = new StringBuffer(512);
        selectStatement.append("<SELECT NAME=\"" + _name + "\" SIZE=\"" + _size + "\" " + _literalHtml + ">\n");

        for (; itv.hasNext() && itl.hasNext();) {
            Object val = itv.next();
            String label = (String) itl.next();
            if (val == null)
                throw new ProgrammerError("Non-option text " + label
                        + " found. Non-otion text cannot break simple SELECTs. Use type=\"tickbox\" instead");
            String value = val.toString();
            boolean yn_selected = value.equals(selectedValue);
            // show option if selected or not-deprecated
            if (yn_selected || Arrays.binarySearch(_deprecatedValues, value) < 0) {
                if (yn_convert2Html) {
                    value = HtmlUtils.string2html(value);
                    label = HtmlUtils.string2html(label);
                }
                String selected = yn_selected ? " SELECTED" : "";
                selectStatement.append("\t<OPTION value=\"" + value + "\"" + selected + ">" + label + "</OPTION>\n");
            }
        }
        selectStatement.append("</SELECT>");

        return selectStatement.toString();
    }

    /**
     * Creates HTML statement for a select-multiple HTML control. If labels and values iterator has different sizes, the
     * smaller applies.
     */
    public String getSelectMultiple() {
        boolean yn_convert2Html = (_convert2Html == TXT2HTML);
        Iterator itv = _values;
        Iterator itl = _labels;

        StringBuffer selectStatement = new StringBuffer(512);
        String selectString = "<SELECT MULTIPLE NAME=\"" + _name + "\" SIZE=\"" + _size + "\" " + _literalHtml + ">\n";
        boolean selectStarted = false;

        for (; itv.hasNext() && itl.hasNext();) {
            Object val = itv.next();
            String label = (String) itl.next();
            if (val == null) {
                selectStatement.append(label);
                if (selectStarted) {
                    selectStatement.append("</SELECT>");
                }
                selectStarted = false;
                continue;
            }
            if (!selectStarted)
                selectStatement.append(selectString);
            selectStarted = true;
            String value = val.toString();

            boolean yn_selected = Arrays.binarySearch(_selectedValues, value) >= 0;
            // show option if selected or not-deprecated
            if (yn_selected || Arrays.binarySearch(_deprecatedValues, value) < 0) {
                if (yn_convert2Html) {
                    value = HtmlUtils.string2html(value);
                    label = HtmlUtils.string2html(label);
                }
                String selected = yn_selected ? " SELECTED" : "";
                selectStatement.append("\t<OPTION VALUE=\"" + value + "\"" + selected + ">" + label + "</OPTION>\n");
            }
        }
        selectStatement.append("</SELECT>"); // FIXME: add only if the string is not empty, i.e. the select was started

        return selectStatement.toString();
    }

    /**
     * Common private function for writing checkboxes or radio inputs.
     * 
     * @param type
     *            is either "RADIO" or "CHECKBOX"
     */
    private String makeCheckboxOrRadio(String type) {
        boolean yn_convert2Html = (_convert2Html == TXT2HTML);
        Iterator itv = _values;
        Iterator itl = _labels;

        StringBuffer inputStatement = new StringBuffer(512);
        int j = -1; // j cycles through optionSeparator[]
        String sep = "";
        for (; itv.hasNext() && itl.hasNext();) {
            inputStatement.append(sep);
            String label = (String) itl.next();
            Object val = itv.next();
            if (val == null) {
                inputStatement.append(label);
                continue;
            }
            String value = (String) val.toString();
            boolean yn_selected = Arrays.binarySearch(_selectedValues, value) >= 0;
            // show option if selected or not-deprecated
            if (yn_selected || Arrays.binarySearch(_deprecatedValues, value) < 0) {
                if (yn_convert2Html) {
                    value = HtmlUtils.string2html(value);
                    label = HtmlUtils.string2html(label);
                }
                String selected = yn_selected ? " CHECKED " : " ";
                j = (j + 1) % _optionSeparator.length;
                sep = _optionSeparator[j];

                String id = "AutoLabel_"
                        + java.lang.Long.toString(java.lang.Math.round(java.lang.Math.random() * 100000000));
                inputStatement.append("<INPUT TYPE=" + type + " NAME=\"" + _name + "\" " + _literalHtml + " ");
                inputStatement.append("VALUE=\"" + value + "\"" + selected + " id=\"" + id + "\">"
                        + _tickLabelSeparator + "<LABEL for=\"" + id + "\">" + label + "</LABEL>");
            }
        }

        return inputStatement.toString();
    }

    /**
     * Creates HTML statement for a radio input control. If labels and values iterator has different sizes, the smaller
     * applies. If selectedValues has more than 1 element, then all these will be set as CHECKED (which is incorrect
     * HTML).
     */
    public String getRadioSelect() {
        return makeCheckboxOrRadio("RADIO");
    }

    /**
     * Creates HTML statement for a select-one control. If labels and values iterator has different sizes, the smaller
     * applies.
     */
    public String getCheckboxSelect() {
        return makeCheckboxOrRadio("CHECKBOX");
    }

    /**
     * Returns a select statement in HTML.
     * 
     * @param name
     *            The name of the parameter that is set by this SELECT
     * @param values
     *            List of the values (String) of the options, same order as 'labels'
     * @param labels
     *            List of the labels (String) of the options, same order as 'values'
     * @param selectedValue
     *            The selected value, if any (String, or null)
     * @param literalHtml
     *            Extra HTML to be added literally to the SELECT tag.
     * @param convert2Html
     *            Configuration whether <code>values</code> and <code>labels</code> must be converted ('escaped') to
     *            HTML during the writing out. Default is "NO_CONV". Any input other than {TXT2HTML, NO_CONV} has
     *            unpredictable result.
     */
    public static String makeHtmlSelectOne(String name, List values, List labels, String selectedValue,
            String literalHtml, int convert2Html) {

        if (values.size() != labels.size()) {
            throw new IllegalArgumentException("error: values, labels not equal length");
        }

        boolean yn_convert2Html = (convert2Html == TXT2HTML);

        StringBuffer selectStatement = new StringBuffer(512);
        selectStatement.append("<SELECT NAME=\"" + name + "\" " + literalHtml + ">\n");

        Iterator itv = values.iterator();
        Iterator itl = labels.iterator();

        for (; itv.hasNext();) {
            String value = (String) itv.next().toString();
            String label = (String) itl.next();
            if (yn_convert2Html) {
                value = HtmlUtils.string2html(value);
                label = HtmlUtils.string2html(label);
            }
            String selected = value.equals(selectedValue) ? " SELECTED" : "";
            selectStatement.append("\t<OPTION value=\"" + value + "\"" + selected + ">" + label + "</OPTION>\n");
        }
        selectStatement.append("</SELECT>");

        return selectStatement.toString();
    }

    /**
     * Returns a select statement in HTML.
     * 
     * @see #makeHtmlSelectOne(String, List, List, String, String, int)
     */
    public static String makeHtmlSelectOne(String name, String[] values, String[] labels, String selectedValue,
            String literalHtml, int convert2Html) {

        // return makeHtmlSelectOne(name, Arrays.asList(values), Arrays.asList(labels), selectedValue, literalHtml,
        // convert2Html);

        if (values.length != labels.length) {
            throw new IllegalArgumentException("error: values, labels not equal length");
        }

        boolean yn_convert2Html = (convert2Html == TXT2HTML);

        StringBuffer selectStatement = new StringBuffer(512);
        selectStatement.append("<SELECT NAME=\"" + name + "\" " + literalHtml + ">\n");

        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            String label = labels[i];
            if (yn_convert2Html) {
                value = HtmlUtils.string2html(value);
                label = HtmlUtils.string2html(label);
            }
            String selected = value.equals(selectedValue) ? " SELECTED" : "";
            selectStatement.append("\t<OPTION value=\"" + value + "\"" + selected + ">" + label + "</OPTION>\n");
        }
        selectStatement.append("</SELECT>");

        return selectStatement.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    // The following are static methods that do similar things.
    // -----------------------------------------------------------------------
    // This class was first built as purely having static methods, but got too
    // complex, and got set and get methods instead.
    // Remains to be determined what to do with these static methods.
    // ////////////////////////////////////////////////////////////////////////

    /**
     * Returns a select multiple statement in HTML.
     * 
     * @param name
     *            The name of the parameter that is set by this SELECT
     * @param values
     *            Array of the values (String) of the options, same order as 'labels'
     * @param labels
     *            Array of the labels (String) of the options, same order as 'values'
     * @param selectedValues
     *            Array of selected values (String), if any, or null.
     * @param size
     *            Number of lines in the input box.
     * @param literalHtml
     *            Extra HTML to be added literally to the SELECT tag.
     * @param convert2Html
     *            Configuration whether <code>values</code> and <code>labels</code> must be converted ('escaped') to
     *            HTML during the writing out. Default is "NO_CONV". Any input other than {TXT2HTML, NO_CONV} has
     *            unpredictable result.
     * @see #makeHtmlSelectOne(String, List, List, String, String, int)
     */
    public static String makeHtmlSelectMultiple(String name, String[] values, String[] labels, String[] selectedValues,
            int size, String literalHtml, int convert2Html) {

        if (values.length != labels.length) {
            throw new IllegalArgumentException("error: values, labels not equal length");
        }
        if (selectedValues == null) {
            selectedValues = EMPTY_ARRAY;
        } else {
            Arrays.sort(selectedValues); // necessary for binarysearch. This changes the inputted array!
        }

        boolean yn_convert2Html = (convert2Html == TXT2HTML);

        StringBuffer selectStatement = new StringBuffer(512);
        selectStatement.append("<SELECT MULTIPLE NAME=\"" + name + "\" SIZE=\"" + size + "\" " + literalHtml + ">\n");

        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            String label = labels[i];
            if (yn_convert2Html) {
                value = HtmlUtils.string2html(value);
                label = HtmlUtils.string2html(label);
            }
            String selected = (Arrays.binarySearch(selectedValues, value) < 0) ? "" : " SELECTED";
            selectStatement.append("\t<OPTION VALUE=\"" + value + "\"" + selected + ">" + label + "</OPTION>\n");
        }
        selectStatement.append("</SELECT>");

        return selectStatement.toString();
    }

    /**
     * Returns a radio-input statement in HTML. Radio input is a "select one" alternative.
     * 
     * @param name
     *            The name of the parameter that is set by this INPUT type=RADIO
     * @param values
     *            Array of the values (String) of the options, same order as 'labels'
     * @param labels
     *            Array of the labels (String) of the options, same order as 'values'
     * @param selectedValue
     *            The selected value, if any (String, or null)
     * @param checkboxLabelSeparator
     *            String to separate the clickable box and the label.
     * @param optionSeparator
     *            String[] with separators between the different Option-Elements; repeatedly cycles thru this array.
     * @param literalHtml
     *            Extra HTML to be added literally to the SELECT tag.
     * @param convert2Html
     *            Configuration whether <code>values</code> and <code>labels</code> must be converted ('escaped') to
     *            HTML during the writing out. Default is "NO_CONV". Any input other than {TXT2HTML, NO_CONV} has
     *            unpredictable result.
     * @see #makeHtmlSelectOne(String, String[], String[], String, String, int)
     * @see #makeHtmlRadioSelect(String, String[], String[], String, String, String, String, int)
     */
    public static String makeHtmlRadioSelect(String name, String[] values, String[] labels, String selectedValue,
            String checkboxLabelSeparator, String[] optionSeparator, String literalHtml, int convert2Html) {

        String[] selectedValues = { selectedValue };
        return makeHtmlCheckboxOrRadioStatement(name, values, labels, selectedValues, checkboxLabelSeparator,
            optionSeparator, literalHtml, convert2Html, "RADIO");
    }

    /**
     * Returns a checkbox-input statement in HTML. Radio input is a "select multiple" alternative.
     * 
     * @param name
     *            The name of the parameter that is set by this INPUT type=CHECKBOX
     * @param values
     *            Array of the values (String) of the options, same order as 'labels'
     * @param labels
     *            Array of the labels (String) of the options, same order as 'values'
     * @param selectedValues
     *            Array of selected values (String), if any, or null.
     * @param checkboxLabelSeparator
     *            String to separate the clickable box and the label.
     * @param optionSeparator
     *            String[] with separators between the different Option-Elements; repeatedly cycles thru this array.
     * @param literalHtml
     *            Extra HTML to be added literally to the SELECT tag.
     * @param convert2Html
     *            Configuration whether <code>values</code> and <code>labels</code> must be converted ('escaped') to
     *            HTML during the writing out. Default is "false". Any input other than {"true", "false", null} has
     *            unpredictable result.
     * @see #makeHtmlSelectMultiple(String, String[], String[], String[], int, String, int)
     * @see #makeHtmlCheckboxSelect(String, String[], String[], String[], String, String, String, int)
     */
    public static String makeHtmlCheckboxSelect(String name, String[] values, String[] labels, String[] selectedValues,
            String checkboxLabelSeparator, String[] optionSeparator, String literalHtml, int convert2Html) {

        return makeHtmlCheckboxOrRadioStatement(name, values, labels, selectedValues, checkboxLabelSeparator,
            optionSeparator, literalHtml, convert2Html, "CHECKBOX");
    }

    /**
     * Common function for writing checkboxes or radio inputs.
     */
    private static String makeHtmlCheckboxOrRadioStatement(String name, String[] values, String[] labels,
            String[] selectedValues, String checkboxLabelSeparator, String[] optionSeparator, String literalHtml,
            int convert2Html, String type) {

        if (values.length != labels.length) {
            throw new IllegalArgumentException("error: values, labels not equal length");
        }
        if (selectedValues == null) {
            selectedValues = EMPTY_ARRAY;
        } else {
            Arrays.sort(selectedValues); // necessary for binarysearch. This changes the inputted array!
        }

        boolean yn_convert2Html = (convert2Html == TXT2HTML);

        StringBuffer inputStatement = new StringBuffer(512);
        int j = -1; // j cycles through optionSeparator[]

        for (int i = 0; i < values.length; i++) {
            j = (j + 1) % optionSeparator.length;
            inputStatement.append("<INPUT TYPE=" + type + " NAME=\"" + name + "\" " + literalHtml + " ");
            String value = values[i];
            String label = labels[i];
            if (yn_convert2Html) {
                value = HtmlUtils.string2html(value);
                label = HtmlUtils.string2html(label);
            }
            String selected = (Arrays.binarySearch(selectedValues, value) < 0) ? " " : " CHECKED ";
            inputStatement.append("VALUE=\"" + value + "\"" + selected + ">" + checkboxLabelSeparator + label
                    + optionSeparator[j]);
        }

        // cut of the last optionSeparator and return
        return inputStatement.substring(0, inputStatement.length() - optionSeparator[j].length());
    }

    /**
     * Returns a radio-input statement in HTML; shorthand for only one optionSeparator.
     * 
     * @see #makeHtmlRadioSelect(String name, String[] values, String[] labels, String selectedValue, String
     *      checkboxLabelSeparator, String[] optionSeparator, String literalHtml, int convert2Html)
     */
    public static String makeHtmlRadioSelect(String name, String[] values, String[] labels, String selectedValue,
            String checkboxLabelSeparator, String optionSeparator, String literalHtml, int convert2Html) {

        String[] optionSeparatorArray = { optionSeparator };
        return makeHtmlRadioSelect(name, values, labels, selectedValue, checkboxLabelSeparator, optionSeparatorArray,
            literalHtml, convert2Html);
    }

    /**
     * Returns a checkbox-input statement in HTML; shorthand for only one optionSeparator.
     * 
     * @see #makeHtmlCheckboxSelect(String name, String[] values, String[] labels, String[] selectedValues, String
     *      checkboxLabelSeparator, String[] optionSeparator, String literalHtml, int convert2Html)
     */
    public static String makeHtmlCheckboxSelect(String name, String[] values, String[] labels, String[] selectedValues,
            String checkboxLabelSeparator, String optionSeparator, String literalHtml, int convert2Html) {

        String[] optionSeparatorArray = { optionSeparator };
        return makeHtmlCheckboxSelect(name, values, labels, selectedValues, checkboxLabelSeparator,
            optionSeparatorArray, literalHtml, convert2Html);
    }

} // end class

/** This class bridges between Enumeration and Iterator. */
class EnumerationWrapper implements Iterator, Enumeration {
    private Enumeration _enum;

    private EnumerationWrapper() {
    } // not allowed.

    public EnumerationWrapper(Enumeration e) {
        _enum = e;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Object next() {
        return _enum.nextElement();
    }

    public boolean hasNext() {
        return _enum.hasMoreElements();
    }

    public boolean hasMoreElements() {
        return _enum.hasMoreElements();
    }

    public Object nextElement() {
        return _enum.nextElement();
    }
}
