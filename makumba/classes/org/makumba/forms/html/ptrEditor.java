// /////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003 http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
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

package org.makumba.forms.html;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.makumba.FieldDefinition;
import org.makumba.HtmlUtils;
import org.makumba.Pointer;
import org.makumba.commons.StringUtils;
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.RecordFormatter;
import org.makumba.providers.Configuration;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryProvider;

public class ptrEditor extends choiceEditor {

    private static final class SingletonHolder {
        static final FieldEditor singleton = new ptrEditor();
    }
          
    /** Don't use this, use getInstance() */
    protected ptrEditor() {
    }

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    public void onStartup(RecordFormatter rf, int fieldIndex) {
        ((RecordEditor) rf).db[fieldIndex] = ((RecordEditor) rf).database;
        Map<String, String> m= new HashMap<String, String>();
        
        ((RecordEditor) rf).query[fieldIndex] = m;
        String titleField = rf.dd.getFieldDefinition(fieldIndex).getTitleField();
        String titleExpr = "choice."+titleField;
        
        String choiceType = rf.dd.getFieldDefinition(fieldIndex).getPointedType().getName();

        m.put("oql",
            "SELECT choice as choice, "
                + titleExpr + " as title FROM "
                + choiceType + " choice "
                +"ORDER BY title"
                );
        FieldDefinition titleFieldDef = rf.dd.getFieldDefinition(fieldIndex).getPointedType().getFieldOrPointedFieldDefinition(
            titleField);
        if (titleFieldDef != null && titleFieldDef.getType().equals("ptr")) { // null if we have functions for title fields
            titleExpr += ".id";
        }
        m.put("hql",
            "SELECT choice.id as choice, "
                + titleExpr +" as title FROM "
                + choiceType + " choice "
                +"ORDER BY " +titleExpr
                );
 
    }

    public Object getOptions(RecordFormatter rf, int fieldIndex, Dictionary<String, Object> formatParams) {

        ChoiceSet c = (ChoiceSet) formatParams.get(ChoiceSet.PARAMNAME);
        if (c != null)
            return c;

        Vector v = null;
        String queryLang = (String)formatParams.get("org.makumba.forms.queryLanguage");
        QueryProvider qp= QueryProvider.makeQueryRunner(((RecordEditor) rf).db[fieldIndex], queryLang);
        
        //Transaction dbc = TransactionProvider.getInstance().getConnectionTo(((RecordEditor) rf).db[fieldIndex]);
        try {
           // v = dbc.executeQuery(((RecordEditor) rf).query[fieldIndex], null);
            v=qp.execute(((RecordEditor) rf).query[fieldIndex].get(queryLang), null, 0, -1);
        } 
        finally {
            qp.close();
        }
        c = new ChoiceSet();
        if (nullOption != null) {
            c.add("", nullOption, false, false);
        }
        for (Iterator i = v.iterator(); i.hasNext();) {
            Dictionary d = (Dictionary) i.next();
            Object ttl = d.get("title");
            if (ttl == null) {
                Pointer ptr = (Pointer) d.get("choice");
                // FIXME ? maybe just displayed the field as untitled?
                // ttl = "untitled [" + ptr.toExternalForm() + "]";
                throw new org.makumba.ProgrammerError(
                        "Object "
                                + ptr
                                + " (external ID: "
                                + ptr.toExternalForm()
                                + ") has a null value for the title-field ("
                                + ptr.getType()
                                + "."
                                + DataDefinitionProvider.getInstance().getDataDefinition(ptr.getType()).getTitleFieldName()
                                + "), and can't be displayed in the drop-down.\nEither make sure you have no null values in this field, or use a different field for the title display, using the '!title=' directive in the MDD.");
            }
            c.add(d.get("choice"), ttl.toString(), false, false);
        }
        return c;
    }

    public int getOptionsLength(RecordFormatter rf, int fieldIndex, Object opts) {
        return ((ChoiceSet) opts).size();
    }

    public Object getOptionValue(RecordFormatter rf, int fieldIndex, Object options, int i) {
        return ((ChoiceSet.Choice) ((ChoiceSet) options).get(i)).getValue();
    }

    public String formatOptionValue(RecordFormatter rf, int fieldIndex, Object val) {
        if (val == Pointer.Null)
            return "";
        if (val instanceof String) {
            // for reloaded forms (via ControllerFilter) check needed
            // val is String when a null option is selected in mak:input (with mak:option)
            return (String) val;
        }
        return ((Pointer) val).toExternalForm();
    }

    public String formatOptionValue(RecordFormatter rf, int fieldIndex, Object opts, int i, Object val) {
        return formatOptionValue(rf, fieldIndex, val);
    }

    public String formatOptionTitle(RecordFormatter rf, int fieldIndex, Object options, int i) {
        return ((ChoiceSet.Choice) ((ChoiceSet) options).get(i)).getTitle();
    }

    public Object readFrom(RecordFormatter rf, int fieldIndex, org.makumba.commons.attributes.HttpParameters p,
            String suffix) {
        Object o = super.readFrom(rf, fieldIndex, p, suffix);
        if ("".equals(o))
            return null;
        return o;
    }

    public boolean isMultiple(RecordFormatter rf, int fieldIndex) {
        return false;
    }

    public int getDefaultSize(RecordFormatter rf, int fieldIndex) {
        return 1;
    }
    
    @Override
    public String format(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
        boolean autoComplete = formatParams.get("autoComplete") != null && formatParams.get("autoComplete").equals("true");
        
        // we have to check whether we are not a setEditor
        if(autoComplete && this instanceof ptrEditor) {
            return formatAutoComplete(rf, fieldIndex, o, formatParams);
        } else {
            return super.format(rf, fieldIndex, o, formatParams);
        }
    }

    private String formatAutoComplete(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
        
        String res = "", id="", inputName = "";
        
        inputName = getInputName(rf, fieldIndex, formatParams);
        
        // TODO: add a hidden input with the right name and id
        // extend the JS method for autocomplete to write the selected value in the hidden input

        id = StringUtils.getParam("id", getExtraFormatting(rf, fieldIndex, formatParams));

        // we need to have a different id for the visible input field
        // dirty hack, because we get the id hardcoded in the extra formatting params
        String extraFormattingVisible = getExtraFormatting(rf, fieldIndex, formatParams);
        int cutIndex = extraFormattingVisible.indexOf("id=") + 4 + id.length();
        extraFormattingVisible = extraFormattingVisible.substring(0, cutIndex) + "_visible" + extraFormattingVisible.substring(cutIndex);
        
        res += "<input name=\"" + inputName + "_visible\" type=\"text\" value=\""
                + formatValue(rf, fieldIndex, o, formatParams) + "\" "
                + extraFormattingVisible
                + "autocomplete=\"off\""
                + ">";

        res += "<input name=\"" + inputName + "\" type=\"hidden\" value=\""
        + formatValue(rf, fieldIndex, o, formatParams) + "\" "
        + getExtraFormatting(rf, fieldIndex, formatParams)
        + ">";
                
        // the second part of the auto-complete, i.e. the dropdown that appears
        
        res += "<div id=\"autocomplete_choices_"+id+"\" class=\"autocomplete\"></div>";
        
        res += "<script type=\"text/javascript\">MakumbaAutoComplete.AutoComplete(\""+id+"\", \""+Configuration.getMakumbaAutoCompleteLocation()+"\", \""+ rf.dd.getFieldDefinition(fieldIndex).getOriginalFieldDefinition().getDataDefinition().getName()+"\", \""+rf.dd.getFieldDefinition(fieldIndex).getOriginalFieldDefinition().getName()+"\", \"ptr\", \""+(String)formatParams.get("org.makumba.forms.queryLanguage")+"\");</script>";
    
        return res;
    }
    
    /** Formats the value to appear in an input statement. */
    @Override
    public String formatValue(RecordFormatter rf, int fieldIndex, Object o, Dictionary<String, Object> formatParams) {
        String s = (o == null) ? null : HtmlUtils.string2html(o.toString());
        return resetValueFormat(rf, fieldIndex, s, formatParams);
    }
}
