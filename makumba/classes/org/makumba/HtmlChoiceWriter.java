package org.makumba;
import java.util.*;
import org.makumba.HtmlUtils;


public class HtmlChoiceWriter extends HtmlUtils {

  public static int NO_CONV   = 0;
  public static int TXT2HTML  = 1;
  // public int CONVERT_AUTO = 2; // not supported
  private static String NEWL = "\n";
  private static String[] EMPTY_ARRAY = { };

  
  private String   _name = null;        // input field's name ("NAME=" + name)
  private Iterator _values = null;      // iterator over the values (Strings)
  private Iterator _labels = null;      // iterator over the labels (Strings)
  private String[] _selectedValues = EMPTY_ARRAY;  // list of selected values (String)
  private boolean  _ismultiple   = false;       // is it a multiple choice?
  private int      _size = 1;                  // size of the select box
  private int      _convert2Html = NO_CONV;     // configuration whether to convert to HTML
  private String   _literalHtml  = "";          // literal html
  private String   _tickLabelSeparator = " ";   // separator between the tick box and the label
  private String[] _elementSeparator   = { " " };   // separator between different elements

  /** Constructor. */
  public HtmlChoiceWriter() {};  
  public HtmlChoiceWriter(String name) { setName(name); };

  public void setName(String name) {_name = name; }
  
  public void setValues(List values) { _values = values.iterator(); }     
  public void setValues(String[] values) { _values = Arrays.asList(values).iterator(); } 
  public void setValues(Iterator values) { _values = values; }     
  
  public void setLabels(List labels) { _labels = labels.iterator(); }     
  public void setLabels(String[] labels) { _labels = Arrays.asList(labels).iterator(); } 
  public void setLabels(Iterator labels) { _labels = labels; }
    
  /** Setter method. */
  public void setSelectedValues(String selected) {
      if (selected != null) {
          String[] ss = { selected }; // [java] can't assign literal array directly to existing variable.
          _selectedValues = ss; 
      } else {
      	  _selectedValues = EMPTY_ARRAY;
      }
  }
  /** Setter method. The input array is changed by this method (sorted). */
  public void setSelectedValues(String[] selected) { 
      if (selected != null) {
          Arrays.sort(_selectedValues = selected); 
      } else {
      	  _selectedValues = EMPTY_ARRAY;
      }
  }
  /** Setter method. The input List is not changed by this method. */
  public void setSelectedValues(List selected) { 
      if (selected != null) {
          Arrays.sort(_selectedValues = (String[])selected.toArray()); 
      } else {
      	  _selectedValues = EMPTY_ARRAY;
      }
  }
  
  public void setMultiple(boolean yn) { _ismultiple = yn; }
  /** Setter method, any string that contains 'multiple' will be regarded as 'true'. */
  public void setMultiple(String mult) { _ismultiple = (mult != null && mult.indexOf("multiple") >= 0) ; }
  public void setSize(int n) { if (n > 0) _size = n;  }
  public void setConvert2Html(int n) { _convert2Html = n; }
  
  public void setLiteralHtml(String html) {_literalHtml = html; }
      
  public void setTickLabelSeparator(String s) {_tickLabelSeparator = s; }

  public void setElementSeparator(String s) { String[] ss = {s}; _elementSeparator = ss; }
  public void setElementSeparator(String[] s) { if (s!=null && s.length > 0) _elementSeparator = s ; }
  public void setElementSeparator(List s) { if (s!=null && s.size() > 0) _elementSeparator = (String[])s.toArray(); }  

  public String getSelect() {
      if ( _ismultiple ) { return getSelectMultiple(); }
      else { return getSelectOne(); }
  }

  /** Create a select statement. 
   * If labels and values iterator has different sizes, the smaller applies. 
   * Even if selectedValues has more than 1 element, 
   */
  public String getSelectOne() {
      boolean yn_convert2Html = (_convert2Html == TXT2HTML);
      String  selectedValue = ( _selectedValues.length != 0) ? _selectedValues[0] : null;
      Iterator itv = _values;
      Iterator itl = _labels;

      StringBuffer selectStatement = new StringBuffer(512);
      selectStatement.append("<SELECT NAME=\"" + _name + "\" SIZE=" + _size + " " + _literalHtml + ">\n");

      for( ; itv.hasNext() && itl.hasNext() ; ) {
          String value = (String)itv.next().toString();
          String label = (String)itl.next();
          if (yn_convert2Html) {
              value = HtmlUtils.string2html(value);
              label = HtmlUtils.string2html(label);
          }
          String selected = value.equals(selectedValue) ? " SELECTED" : "" ;
          selectStatement.append("\t<OPTION value=\"" + value + "\"" + selected + ">" + label + "</OPTION>\n" );
      }
      selectStatement.append("</SELECT>\n");
  
      return selectStatement.toString();
  }

  /** Makes a select multiple HTML statement, based on the set fields. */
  public String getSelectMultiple () {
      boolean yn_convert2Html = (_convert2Html == TXT2HTML);
      Iterator itv = _values;
      Iterator itl = _labels;
          
      StringBuffer selectStatement = new StringBuffer(512);
      selectStatement.append("<SELECT MULTIPLE NAME=\"" + _name + "\" SIZE=" + _size + " " + _literalHtml + ">\n");
      
      for( ; itv.hasNext() && itl.hasNext() ; ) {
          String value = (String)itv.next().toString();
          String label = (String)itl.next();
          if (yn_convert2Html) {
              value = HtmlUtils.string2html(value);
              label = HtmlUtils.string2html(label);
          }
          String selected = (Arrays.binarySearch(_selectedValues, value) < 0) ? "" : " SELECTED" ;
          selectStatement.append("\t<OPTION VALUE=\"" + value + "\"" + selected + ">" + label + "</OPTION>\n" );
      }
      selectStatement.append("</SELECT>\n");
  
      return selectStatement.toString();
  }

  /**
   * Common function for writing checkboxes or radio inputs. 
   * @param type is either "RADIO" or "CHECKBOX"
   */
  private String makeCheckboxOrRadio( String type ) {
      boolean yn_convert2Html = (_convert2Html == TXT2HTML);
      Iterator itv = _values;
      Iterator itl = _labels;
  
      StringBuffer inputStatement = new StringBuffer(512);
      int j = -1; // j cycles through elementSeparator[]
  
      for( ; itv.hasNext() && itl.hasNext() ; ) {
          String value = (String)itv.next().toString();
          String label = (String)itl.next();
          if (yn_convert2Html) {
              value = HtmlUtils.string2html(value);
              label = HtmlUtils.string2html(label);
          }
          String selected = (Arrays.binarySearch(_selectedValues, value) < 0) ? " " : " CHECKED " ;
          j = (j+1) % _elementSeparator.length;
                    
          inputStatement.append("<INPUT TYPE=" + type + " NAME=\"" + _name + "\" " + _literalHtml + " ");
          inputStatement.append("VALUE=\"" + value + "\"" + selected + ">" + _tickLabelSeparator + label + _elementSeparator[j]);
      }
  
      // cut of the last elementSeparator and return
      return inputStatement.substring(0, inputStatement.length() - _elementSeparator[j].length());
  }
  
  public String getRadioSelect() {
      return makeCheckboxOrRadio("RADIO");
  }
  
  public String getCheckboxSelect() {
      return makeCheckboxOrRadio("CHECKBOX");
  }
  
  
  /**
   * Returns a select statement in HTML.
   * @param name     The name of the parameter that is set by this SELECT
   * @param values   List of the values (String) of the options, same order as 'labels'
   * @param labels   List of the labels (String) of the options, same order as 'values'
   * @param selectedValue The selected value, if any (String, or null)
   * @param literalHtml   Extra HTML to be added literally to the SELECT tag. 
   * @param convert2Html  Configuration whether <code>values</code> and <code>labels</code> must be converted ('escaped') to HTML during the writing out. Default is "NO_CONV". Any input other than {TXT2HTML, NO_CONV} has unpredictable result.
   */
  public static String makeHtmlSelectOne(String name, 
                           List values, 
                           List labels, 
                           String selectedValue, 
                           String literalHtml,
                           int    convert2Html) {
  
      if (values.size() != labels.size() ) {
          throw new IllegalArgumentException( "error: values, labels not equal length" );
      }
      
      boolean yn_convert2Html = (convert2Html == TXT2HTML);
          
      StringBuffer selectStatement = new StringBuffer(512);
      selectStatement.append("<SELECT NAME=\"" + name + "\" " + literalHtml + ">\n");
  
      Iterator itv = values.iterator();
      Iterator itl = labels.iterator();
  
      for( ; itv.hasNext(); ) {
          String value = (String)itv.next().toString();
          String label = (String)itl.next();
          if (yn_convert2Html) {
              value = HtmlUtils.string2html(value);
              label = HtmlUtils.string2html(label);
          }
          String selected = value.equals(selectedValue) ? " SELECTED" : "" ;
          selectStatement.append("\t<OPTION value=\"" + value + "\"" + selected + ">" + label + "</OPTION>\n" );
      }
      selectStatement.append("</SELECT>\n");
  
      return selectStatement.toString();
  }
  
  
  /**
   * Returns a select statement in HTML.
   * @see #makeHtmlSelectStatement(String, List, List, String, String, String) 
   */
  public static String makeHtmlSelectOne(String name, 
                           String[] values, 
                           String[] labels, 
                           String selectedValue, 
                           String literalHtml,
                           int    convert2Html) {
  
      // return makeHtmlSelectOne(name, Arrays.asList(values), Arrays.asList(labels), selectedValue, literalHtml, convert2Html);
  
      if (values.length != labels.length ) {
          throw new IllegalArgumentException( "error: values, labels not equal length" );
      }
      
      boolean yn_convert2Html = (convert2Html == TXT2HTML);
          
      StringBuffer selectStatement = new StringBuffer(512);
      selectStatement.append("<SELECT NAME=\"" + name + "\" " + literalHtml + ">\n");
      
      for(int i=0 ; i < values.length; i++) {
          String value = values[i];
          String label = labels[i];
          if (yn_convert2Html) {
              value = HtmlUtils.string2html(value);
              label = HtmlUtils.string2html(label);
          }
          String selected = value.equals(selectedValue) ? " SELECTED" : "" ;
          selectStatement.append("\t<OPTION value=\"" + value + "\"" + selected + ">" + label + "</OPTION>\n" );
      }
      selectStatement.append("</SELECT>\n");
  
      return selectStatement.toString();
  }
  
  
  /**
   * Returns a select multiple statement in HTML.
   * @param name           The name of the parameter that is set by this SELECT
   * @param values         Array of the values (String) of the options, same order as 'labels'
   * @param labels         Array of the labels (String) of the options, same order as 'values'
   * @param selectedValues Array of selected values (String), if any, or null.
   * @param size           Number of lines in the input box.
   * @param literalHtml    Extra HTML to be added literally to the SELECT tag. 
   * @param convert2Html  Configuration whether <code>values</code> and <code>labels</code> must be converted ('escaped') to HTML during the writing out. Default is "NO_CONV". Any input other than {TXT2HTML, NO_CONV} has unpredictable result.
   * @see #makeHtmlSelectOne(String, List, List, String, String, String) 
   */
  public static String makeHtmlSelectMultiple(String name, 
                           String[] values, 
                           String[] labels, 
                           String[] selectedValues, 
                           int size,
                           String literalHtml,
                           int    convert2Html) {
  
      if (values.length != labels.length ) {
          throw new IllegalArgumentException( "error: values, labels not equal length" );
      }
      if (selectedValues == null) {
          selectedValues = EMPTY_ARRAY ; 
      } else {
          Arrays.sort(selectedValues); // necessary for binarysearch. This changes the inputted array!
      }
      
      boolean yn_convert2Html = (convert2Html == TXT2HTML);
          
      StringBuffer selectStatement = new StringBuffer(512);
      selectStatement.append("<SELECT MULTIPLE NAME=\"" + name + "\" SIZE=" + size + " " + literalHtml + ">\n");
      
      for(int i=0 ; i < values.length; i++) {
          String value = values[i];
          String label = labels[i];
          if (yn_convert2Html) {
              value = HtmlUtils.string2html(value);
              label = HtmlUtils.string2html(label);
          }
          String selected = (Arrays.binarySearch(selectedValues, value) < 0) ? "" : " SELECTED" ;
          selectStatement.append("\t<OPTION VALUE=\"" + value + "\"" + selected + ">" + label + "</OPTION>\n" );
      }
      selectStatement.append("</SELECT>\n");
  
      return selectStatement.toString();
  }
  
  /**
   * Returns a radio-input statement in HTML. Radio input is a "select one" alternative.
   * @param name           The name of the parameter that is set by this INPUT type=RADIO
   * @param values         Array of the values (String) of the options, same order as 'labels'
   * @param labels         Array of the labels (String) of the options, same order as 'values'
   * @param selectedValue  The selected value, if any (String, or null)
   * @param checkboxLabelSeparator  String to separate the clickable box and the label.
   * @param elementSeparator        String[] with separators between the different Option-Elements; repeatedly cycles thru this array.
   * @param literalHtml    Extra HTML to be added literally to the SELECT tag.
   * @param convert2Html  Configuration whether <code>values</code> and <code>labels</code> must be converted ('escaped') to HTML during the writing out. Default is "NO_CONV". Any input other than {TXT2HTML, NO_CONV} has unpredictable result.
   * @see #makeHtmlSelectStatement(String, String[], String[], String, String, String) 
   * @see #makeHtmlRadioStatement(String, String[], String[], String, String, String, String, String)
   */
  public static String makeHtmlRadioSelect(String name, 
                           String[] values, 
                           String[] labels, 
                           String   selectedValue, 
                           String   checkboxLabelSeparator,
                           String[] elementSeparator,
                           String   literalHtml,
                           int      convert2Html) {
  
      String[] selectedValues = { selectedValue };
      return makeHtmlCheckboxOrRadioStatement(name, values, labels, 
              selectedValues, checkboxLabelSeparator, elementSeparator, 
              literalHtml, convert2Html, "RADIO");
  }
  
  
  /**
   * Returns a checkbox-input statement in HTML. Radio input is a "select multiple" alternative.
   * @param name           The name of the parameter that is set by this INPUT type=CHECKBOX
   * @param values         Array of the values (String) of the options, same order as 'labels'
   * @param labels         Array of the labels (String) of the options, same order as 'values'
   * @param selectedValues Array of selected values (String), if any, or null.
   * @param checkboxLabelSeparator  String to separate the clickable box and the label.
   * @param elementSeparator        String[] with separators between the different Option-Elements; repeatedly cycles thru this array.
   * @param literalHtml    Extra HTML to be added literally to the SELECT tag.
   * @param convert2Html   Configuration whether <code>values</code> and <code>labels</code> must be converted ('escaped') to HTML during the writing out. Default is "false". Any input other than {"true", "false", null} has unpredictable result.
   * @see #makeHtmlSelectMultiple(String, String[], String[], String[], int, String, String) 
   * @see #makeHtmlCheckboxSelect(String, String[], String[], String[], String, String, String, String)
   */
  public static String makeHtmlCheckboxSelect(String name, 
                           String[] values, 
                           String[] labels, 
                           String[] selectedValues, 
                           String   checkboxLabelSeparator,
                           String[] elementSeparator,
                           String   literalHtml,
                           int      convert2Html) {
  
      return makeHtmlCheckboxOrRadioStatement(name, values, labels, 
              selectedValues, checkboxLabelSeparator, elementSeparator, 
              literalHtml, convert2Html, "CHECKBOX");
  }
  
  /**
   * Common function for writing checkboxes or radio inputs.
   */
  private static String makeHtmlCheckboxOrRadioStatement(String name, 
                           String[] values, 
                           String[] labels, 
                           String[] selectedValues, 
                           String   checkboxLabelSeparator,
                           String[] elementSeparator,
                           String   literalHtml,
                           int      convert2Html,
                           String   type) {
  
      if (values.length != labels.length ) {
          throw new IllegalArgumentException( "error: values, labels not equal length" );
      }
      if (selectedValues == null) {
          selectedValues = EMPTY_ARRAY ; 
      } else {
          Arrays.sort(selectedValues); // necessary for binarysearch. This changes the inputted array!
      }
      
      
      boolean yn_convert2Html = (convert2Html == TXT2HTML);
          
      StringBuffer inputStatement = new StringBuffer(512);
      int j = -1; // j cycles through elementSeparator[]
  
      for(int i=0 ; i < values.length; i++) {
          j = (j+1) % elementSeparator.length;
          inputStatement.append("<INPUT TYPE=" + type + " NAME=\"" + name + "\" " + literalHtml + " ");
          String value = values[i];
          String label = labels[i];
          if (yn_convert2Html) {
              value = HtmlUtils.string2html(value);
              label = HtmlUtils.string2html(label);
          }
          String selected = (Arrays.binarySearch(selectedValues, value) < 0) ? " " : " CHECKED " ;
          inputStatement.append("VALUE=\"" + value + "\"" + selected + ">" + checkboxLabelSeparator + label + elementSeparator[j]);
      }
  
      // cut of the last elementSeparator and return
      return inputStatement.substring(0, inputStatement.length() - elementSeparator[j].length());
  }
  
  
  /**
   * Returns a radio-input statement in HTML; shorthand for only one elementSeparator.
   * @see #makeHtmlRadioStatement(String name, String[] values, String[] labels, String selectedValue, String checkboxLabelSeparator, String[] elementSeparator, String literalHtml, String convert2Html) 
   */
  public static String makeHtmlRadioSelect(String name, 
                           String[] values, 
                           String[] labels, 
                           String   selectedValue, 
                           String   checkboxLabelSeparator,
                           String   elementSeparator,
                           String   literalHtml,
                           int      convert2Html) {
  
      String[] elementSeparatorArray = { elementSeparator };
      return makeHtmlRadioSelect(name, values, labels, selectedValue, checkboxLabelSeparator,
          elementSeparatorArray, literalHtml, convert2Html);
  }


  /**
   * Returns a checkbox-input statement in HTML; shorthand for only one elementSeparator.
   * @see #makeHtmlCheckboxStatement(String name, String[] values, String[] labels, String[] selectedValues, String checkboxLabelSeparator, String[] elementSeparator, String literalHtml, String convert2Html)
   */
  public static String makeHtmlCheckboxSelect(String name, 
                           String[] values, 
                           String[] labels, 
                           String[] selectedValues, 
                           String   checkboxLabelSeparator,
                           String   elementSeparator,
                           String   literalHtml,
                           int      convert2Html) {
  
      String[] elementSeparatorArray = { elementSeparator };
      return makeHtmlCheckboxSelect(name, values, labels, selectedValues, checkboxLabelSeparator,
          elementSeparatorArray, literalHtml, convert2Html);
  }
  

} // end class




/** This class bridges between Enumeration and Iterator. */
class EnumerationWrapper implements Iterator, Enumeration {
  private Enumeration enum;

  private EnumerationWrapper() { } // not allowed.
  public EnumerationWrapper(Enumeration e){
      enum = e ;
  }
  
  public void    remove()  { throw new UnsupportedOperationException(); }
  public Object  next()    { return enum.nextElement(); }
  public boolean hasNext() { return enum.hasMoreElements(); }
  
  public boolean hasMoreElements() { return enum.hasMoreElements(); }  	
  public Object  nextElement()     { return enum.nextElement(); }  	
}
