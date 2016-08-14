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

package org.makumba.forms.responder;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.makumba.CompositeValidationException;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.ProgrammerError;
import org.makumba.Transaction;
import org.makumba.commons.DbConnectionProvider;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.commons.tags.MakumbaJspConfiguration;
import org.makumba.forms.html.FieldEditor;
import org.makumba.forms.html.RecordEditor;
import org.makumba.forms.validation.ClientsideValidationProvider;
import org.makumba.providers.DataDefinitionProvider;

public class FormResponder extends Responder {

    private static final long serialVersionUID = 1L;

    RecordEditor editor;

    /**
     * reads the data submitted to the controller by http, also sets the values in the request so they can be retrieved
     * as attributes
     */
    @Override
    public Dictionary<String, Object> getHttpData(HttpServletRequest req, String suffix) {
        if (editor != null) {
            if (operation.equals("search")) {
                // search forms are more basic in their form data treatment, and don't have unresolved inputs
                return editor.readFromSearchForm(req, suffix, lazyEvaluatedInputs);
            }
            // first read data from the form itself
            Dictionary<String, Object> data = editor.readFrom(req, suffix, lazyEvaluatedInputs);

            // then, fill in values from unresolved inputs (i.e. from nested forms)
            @SuppressWarnings("unchecked")
            HashMap<String, Object> results = (HashMap<String, Object>) req.getAttribute(Responder.FORM_RESULTS);
            if (lazyEvaluatedInputs != null) { // check for != null to be on the safe side
                for (String key : lazyEvaluatedInputs.keySet()) {
                    if (results.get(key) != null) {
                        data.put(lazyEvaluatedInputs.get(key), results.get(key));
                    }
                }
            }

            return data;
        } else {
            return new Hashtable<String, Object>(1);
        }
    }

    @Override
    public ArrayList<InvalidValueException> getUnassignedExceptions(CompositeValidationException e,
            ArrayList<InvalidValueException> unassignedExceptions, String suffix) {
        if (editor != null) {
            return editor.getUnassignedExceptions(e, unassignedExceptions, suffix);
        } else {
            return null;
        }
    }

    Hashtable<String, Integer> indexes = new Hashtable<String, Integer>();

    // TODO: more precise name
    DataDefinition dd = DataDefinitionProvider.getInstance().getVirtualDataDefinition("Form responder");

    int max = 0;

    Hashtable<String, Dictionary<String, Object>> fieldParameters = new Hashtable<String, Dictionary<String, Object>>();

    Hashtable<String, String> fieldNames = new Hashtable<String, String>();

    /** Format a field using the editor, and grow the editor as needed */
    public String format(String fname, FieldDefinition ftype, Object fval, Hashtable<String, Object> formatParams,
            String extraFormatting, Object formIdentifier) {
        Dictionary<String, Object> paramCopy = toDictionary(formatParams.clone());

        // appending the ID to the extra formatting params seems like a bit of a hack here.. but it also the fastest..
        // don't do it for dates (a date is several inputs, need _0, _1, _2, ..) and radio / checkbox / tickbox
        if (!ftype.isDateType()
                && !org.makumba.commons.StringUtils.equalsAny(formatParams.get("type"), new String[] { "radio",
                        "checkbox", "tickbox" })) {
            extraFormatting += "id=\"" + fname + formIdentifier + "\" ";
        }

        FieldEditor.setSuffix(paramCopy, storedSuffix);
        FieldEditor.setExtraFormatting(paramCopy, extraFormatting);
        FieldEditor.setFormName(paramCopy, formName);

        boolean display = formatParams.get("org.makumba.noDisplay") == null;
        Integer i = indexes.get(fname);
        if (i != null) {
            throw new ProgrammerError("duplicate field: " + fname);
            // return display ? editor.format(i, fval, paramCopy) : "";
        }

        indexes.put(fname, new Integer(max));
        String colName = "col" + max;
        fieldNames.put(colName, fname);
        fieldParameters.put(colName, formatParams);
        dd.addField(DataDefinitionProvider.getInstance().makeFieldWithName(colName, ftype));

        // FIXME: the form responder value is needed to passed to the editor, so it can generate unique auto id's for
        // tick-boxes. the auto-ids are use so we can create a <label for="id">, that allows clicking on the text next
        // to the tickbox as well
        // we could also use the field name & the form suffix, but that would be not unique if we have twice the same
        // root-form in the page
        // getResponderValue() changes on each iteration, as it takes the hashcode of the form, and that changes when we
        // add more fields. thus, we create too many new responders
        // it seems we can't set the responder value later, when the editors are complete, as the format method needs
        // it, and is called in this current method
        editor = new RecordEditor(dd, fieldNames, database, operation.equals("search"), formIdentifier);

        editor.config(formatParams);
        // add client side validation, but only for edit operations (not search), and for fields that are visible
        if (!operation.equals("search") && display
                && org.makumba.commons.StringUtils.equalsAny(clientSideValidation, new String[] { "true", "live" })) {
            provider.initField(fname, formIdentifier.toString(), ftype, clientSideValidation.equals("live"));
        }
        max++;
        return display ? editor.format(max - 1, fval, paramCopy) : "";
    }

    @SuppressWarnings("unchecked")
    private Dictionary<String, Object> toDictionary(Object o) {
        return (Dictionary<String, Object>) o;
    }

    @Override
    public String responderKey() {
        return "" + fieldNames + fieldParameters + super.responderKey();
    }

    protected String action;

    protected boolean methodDefault = true;

    protected String method = "POST";

    protected boolean multipart;

    Map<String, String> extraFormattingParams;

    private ClientsideValidationProvider provider = MakumbaJspConfiguration.getClientsideValidationProvider();

    /**
     * Values of inputs that could not be resolved (yet), e.g. from nested form operations. Stores a formName->fieldName
     * mapping.
     */
    private HashMap<String, String> lazyEvaluatedInputs = new HashMap<String, String>();

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setMultipart(boolean multipart) {
        this.multipart = multipart;
    }

    public void setMethod(String method) {
        this.methodDefault = false;
        this.method = method;
    }

    public void setExtraFormattingParams(Map<String, String> extraFormattingParams) {
    	this.extraFormattingParams = extraFormattingParams;
    }
    
    private StringBuffer stringifyExtraFormattingParams(Map<String, String> extraFormattingParams2) {
        StringBuffer extraFormatting = new StringBuffer();

        for (Entry<String, String> me : extraFormattingParams2.entrySet()) {
            extraFormatting.append(" ").append(me.getKey()).append("=\"").append(me.getValue()).append("\" ");
        }
        
        return extraFormatting;
    }

    public void writeFormPreamble(StringBuffer sb, String basePointer, HttpServletRequest request) {
        if (!storedSuffix.equals("")) {
            // no preamble for non-root forms (forms included in other forms)
            return;
        }
        String targetPage = "";
        String actionBase = "";
        String actionAnchor = "";
        String sep = "";

        // if we have an action defined, we process it to handle anchors
        // if we are in a partial postback, we don't have an action page
        // however we want an URL in order to do stuff
        // TODO: not sure if this is good, fix me later
        if (triggerEvent != null && action == null) {
            targetPage = request.getContextPath() + request.getServletPath();
            actionBase = targetPage;
            String styleClass = extraFormattingParams.get("class");
            
            // add the mak-ajax-form class to the form if it's ajax enabled
            if(styleClass == null) {
            	styleClass = "mak-ajax-form";
            } else {
            	styleClass += " mak-ajax-form";
            }
            
            extraFormattingParams.put("data-mak-trigger-event", triggerEvent);
            
            extraFormattingParams.put("class", styleClass);

            sep = "?";
        } else {
            targetPage = action;
            actionBase = targetPage;
            sep = targetPage.indexOf('?') >= 0 ? "&" : "?";
            // handle anchors in actions (bla.jsp?person=hg34bw#employment)
            int actionHashPos = targetPage.indexOf('#');
            if (actionHashPos > -1) {
                actionBase = targetPage.substring(0, actionHashPos);
                actionAnchor = targetPage.substring(actionHashPos);
            }
        }

        StringBuffer extraFormatting = stringifyExtraFormattingParams(extraFormattingParams);
        if (operation.equals("deleteLink")) {

            // a root deleteLink

            sb.append("<a href=\"").append(actionBase).append(sep).append(basePointerName).append("=").append(
                basePointer).append('&').append(responderName).append("=").append(getPrototype()).append(actionAnchor).append(
                "\" ").append(extraFormatting).append(">");

        }

        else if (operation.equals("deleteForm")) {

            sb.append("<form");
            if (triggerEvent == null) {
                sb.append(" action=").append("\"").append(actionBase).append(actionAnchor).append("\"");
            }
            sb.append(" id=").append("\"").append(getFormId()).append("\"");
            sb.append(" method=").append("\"").append(method).append("\"");
            if (multipart) {
                sb.append(" enctype=\"multipart/form-data\" ");
            }
            sb.append("style = \"display: inline; \"");
            sb.append(">");

            sb.append("<input type=\"submit\" ");

            // formatting - we add it to the button instead of the form as per #986
            // this makes sense as a deleteForm renders as only visible element a button and hence any formatting should
            // be applied to it
            sb.append(extraFormatting + " ");

            sb.append("value=\"");

        } else {
            // a root form, translates into an HTML form

            // if we have a search form we by default give it a GET method because the search string is useful to have
            if (operation.equals("search") && methodDefault) {
                method = "GET";
            }
            sb.append("<form");
            if (triggerEvent == null) {
                // also allowing anchors and query parameters in the actions of common forms (bug 1143)
                sb.append(" action=").append("\"").append(actionBase).append(actionAnchor).append("\"");
            }
            sb.append(" id=").append("\"").append(getFormId()).append("\"");
            sb.append(" method=");
            sb.append("\"" + method + "\"");
            if (multipart) {
                sb.append(" enctype=\"multipart/form-data\" ");
            }
            // if we do client side validation, we need to put an extra formatting parameter for onSubmit
            // but, do it only for edit operations (not search)
            if (!operation.equals("search")
                    && org.makumba.commons.StringUtils.equalsAny(clientSideValidation, new String[] { "true", "live" })) {
                StringBuffer onSubmitValidation = provider.getOnSubmitValidation();
                // we append it only if we actually have data
                if (onSubmitValidation != null && onSubmitValidation.length() > 0) {
                    sb.append(" onsubmit=\"return ");
                    sb.append(onSubmitValidation);
                    sb.append("\"");
                }
            }

            sb.append(extraFormatting);
            sb.append(">");
        }
    }

    public void writeFormPostamble(StringBuffer sb, String basePointer, HttpServletRequest request) {
        String session = request.getSession().getId();
        Random random = new Random();
        factory.setResponderWorkingDir(request);
        if (storedSuffix.equals("") && operation.equals("deleteLink")) {
            // a root deleteLink
            sb.append("</a>");
            return;
        } else if (storedSuffix.equals("") && operation.equals("deleteForm")) {
            sb.append("\"/>");
        }

        // writes the hidden fields

        if (basePointer != null) {
            writeInput(sb, basePointerName, basePointer, storedSuffix);
        }

        String responderValue = getResponderValue();
        String formSessionValue = responderValue + session + Integer.toString(random.nextInt()); // gets the formSession value

        writeInput(sb, responderName, responderValue, "");
        if (multipleSubmitErrorMsg != null && !multipleSubmitErrorMsg.equals("")) {
            sb.append('\n');
            writeInput(sb, formSessionName, formSessionValue, "");

            // insert the formSession into the database
            Transaction db = ((DbConnectionProvider) request.getAttribute(RequestAttributes.PROVIDER_ATTRIBUTE)).getTransactionProvider().getConnectionTo(
                database);
            try {
                Dictionary<String, Object> p = new Hashtable<String, Object>();
                p.put("formSession", formSessionValue);
                db.insert("org.makumba.controller.MultipleSubmit", p);
            } finally {
                db.close();
            }
        }

        if (storedSuffix.equals("")) {
            // a root form
            sb.append("\n</form>");
        }
    }

    public String getResponderValue() {
        return getPrototype() + storedSuffix + storedParentSuffix;
    }

    void writeInput(StringBuffer sb, String name, String value, String suffix) {
        sb.append("<input type=\"hidden\" name=\"").append(name).append(suffix).append("\" value=\"").append(value).append(
            "\">");
    }

    @Override
    protected void postDeserializaton() {
        if (editor != null) {
            editor.initFormatters();
        }
    }

    public void writeClientsideValidation(StringBuffer sb) {
        sb.append(provider.getClientValidation(clientSideValidation.equals("live")));
    }

    public void setLazyEvaluatedInputs(HashMap<String, String> unresolvedInputValues) {
        this.lazyEvaluatedInputs = unresolvedInputValues;
    }

    public HashMap<String, String> getLazyEvaluatedInputs() {
        return lazyEvaluatedInputs;
    }

}
