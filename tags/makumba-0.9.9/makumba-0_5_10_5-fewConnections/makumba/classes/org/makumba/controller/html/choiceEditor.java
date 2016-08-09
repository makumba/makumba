///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.controller.html;
import org.makumba.*;
import java.util.*;

public abstract class choiceEditor extends FieldEditor
{
  static String[]   params = {"default", "empty", "type", "size", "labelSeparator", "elementSeparator" };
  static String[][] paramValues = {null, null, {"hidden", "radio", "checkbox", "tickbox" }, null, null, null};
  public String[]   getAcceptedParams() { return params; }
  public String[][] getAcceptedValue()  { return paramValues; }
	
  
  /** Get the available options. */
  public abstract Object getOptions();

  /** Gets the number of available options. */
  public abstract int getOptionsLength(Object opts);

  /** Gets the value of option 'i'. */
  public abstract Object getOptionValue(Object options, int i);
  
  /** Gets the title/label of option 'i'. */
  public abstract String formatOptionTitle(Object options, int i);

  /** Formats an option value, in the sequence of options. */
  public abstract String formatOptionValue(Object opts, int i, Object val);

  /** Formats an option value. */
  public abstract String formatOptionValue(Object val);

  /** Returns blank string, or " multiple " if multiple selections possible. */
  // FIXME, would be better with "boolean isMultiple()"
  public abstract String getMultiple();
  public abstract boolean isMultiple();

  /** Gets the default size of the select HTML box. */
  public abstract int getDefaultSize();


  // height? orderBy? where?
  public String format(Object o, Dictionary formatParams) 
  {
    String type = (String)formatParams.get("type");
    boolean hidden = "hidden".equals(type);
    boolean yn_radio    = "radio".equals(type);
    boolean yn_checkbox = "checkbox".equals(type);
    boolean yn_tickbox  = "tickbox".equals(type);
    if (yn_tickbox) {
       if (isMultiple()) yn_checkbox = true;
       else yn_radio = true;
    }
  
    Vector value;
    o = getValueOrDefault(o, formatParams);
    if (o instanceof Vector) { 
        value=(Vector)o;
    } else {
	value=new Vector(1);
	if(o!=null)
	   value.addElement(o);
    }

    if (!hidden) {
        HtmlChoiceWriter hcw = new HtmlChoiceWriter(getInputName(formatParams));
        int size=getIntParam(formatParams,"size");
        if (size==-1)
            size=getDefaultSize();
        hcw.setSize(size);
        hcw.setMultiple(isMultiple());
        hcw.setLiteralHtml(getExtraFormatting(formatParams));
    
	Object opt=getOptions(); 
	List values = new ArrayList(getOptionsLength(opt));
	List labels = new ArrayList(getOptionsLength(opt));
	String[] valueFormattedList = new String[value.size()];
	
        for(int i=0; i<getOptionsLength(opt); i++) {
            Object val=getOptionValue(opt, i);
            values.add(formatOptionValue(opt, i, val));
            labels.add(formatOptionTitle(opt, i));
//          System.out.println(formatOptionTitle(opt, i)+"="+formatOptionValue(opt, i, val));
        }
        hcw.setValues(values);
        hcw.setLabels(labels);

	try{ // set deprecated values if data type supports it
	   Vector dv=getDeprecatedValues();
//	   System.out.println("setting deprecated:"+dv);
           if (dv != null && !dv.isEmpty() ) {
  	       String[] dvs=new String[dv.size()];
	       for(int i=0; i< dv.size(); i++) {
	          dvs[i]=(String)dv.elementAt(i).toString();
	       }
               hcw.setDeprecatedValues(dvs);
           }
	} catch(ClassCastException cce) { }

        for(int i=0; i < value.size(); i++ ) {
	    valueFormattedList[i] = formatOptionValue( value.get(i) );
	}
        hcw.setSelectedValues(valueFormattedList);
        
        if (yn_radio || yn_checkbox) {
            String sep = (String)formatParams.get("elementSeparator");
            if (sep!=null) hcw.setOptionSeparator(sep);
            sep = (String)formatParams.get("labelSeparator");
            if (sep!=null) hcw.setTickLabelSeparator(sep);

            if (yn_radio) return hcw.getRadioSelect();
            else return hcw.getCheckboxSelect();
        }
        
        return hcw.getSelect();
        
    } else {   // hidden
    	
        StringBuffer sb= new StringBuffer();
	for(Enumeration f= value.elements(); f.hasMoreElements(); ) {
	    Object val=f.nextElement();
	    sb.append("<input type=\"hidden\" name=\"").append(getInputName(formatParams)).append("\" value=\"").append(formatOptionValue(val)).append("\">");
	}
	return sb.toString();
    }
  }


  /** Return value if not null, or finds the default option and returns it as a Vector. */
  public Object getValueOrDefault (Object o, Dictionary formatParams) {
     if (o == null || ( o instanceof Vector && ((Vector)o).size()==0)  ) {
         String nullReplacer = (String) formatParams.get("default");
         if (nullReplacer != null) { 
             Vector v = new Vector(); 
             v.add(nullReplacer);
             return v; 
         }
     }
     return o;
  }
  
}
