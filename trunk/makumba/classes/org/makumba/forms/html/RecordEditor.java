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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.makumba.CompositeValidationException;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.ValidationRule;
import org.makumba.commons.attributes.HttpParameters;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.RecordFormatter;
import org.makumba.providers.datadefinition.makumba.validation.ComparisonValidationRule;

/**
 * Editor of Makumba data. Each subclass knows how to format HTML <input> and <select> tags for each type of Makumba
 * data, and how to read their data from HTTP query strings in form responses.
 * 
 * @author Cristian Bogdan
 * @author Rudolf Mayer
 * @version $Id$
 */
public class RecordEditor extends RecordFormatter {
    private static final long serialVersionUID = 1L;

    String database;

    String[] db;

    Map<String, String>[] query;

    protected RecordEditor() {
    }

    public RecordEditor(DataDefinition ri, Hashtable<String, String> h, String database, boolean isSearchForm,
            Object formIdentifier) {
        super(ri, h, isSearchForm, formIdentifier);
        this.database = database;
        db = new String[ri.getFieldNames().size()];
        query = new Map[ri.getFieldNames().size()];
    }

    public ArrayList<InvalidValueException> getUnassignedExceptions(CompositeValidationException e,
            ArrayList<InvalidValueException> unassignedExceptions, String suffix) {
        for (int i = 0; i < dd.getFieldNames().size(); i++) {
            // FIXME: validation definitions that span multiple fields (compare, ..) or multi-field uniqueness
            // definitions are only added to one field. If that field is not in the form, the exception can't be
            // annotated next to the form field. However, the secondary field might be present in the form, then it
            // might make sense to annotate it there.. ?
            // In any case, if only one of the fields is present in the form, it would make sense to display the value
            // of the other field somewhere in the form, to give the user a hint
            Collection<InvalidValueException> exceptions = e.getExceptions(FieldEditor.getInputName(this, i, suffix));
            if (exceptions != null) {
                for (InvalidValueException invalidValueException : exceptions) {
                    unassignedExceptions.remove(invalidValueException);
                }
            }
        }
        return unassignedExceptions;
    }

    public Dictionary<String, Object> readFrom(HttpServletRequest req, String suffix, boolean applyValidationRules) {
        Dictionary<String, Object> data = new Hashtable<String, Object>();
        // will collect all exceptions from the field validity checks
        Vector<InvalidValueException> exceptions = new Vector<InvalidValueException>();

        Hashtable<Integer, Object> validatedFields = new Hashtable<Integer, Object>();
        Hashtable<String, Object> validatedFieldsNameCache = new Hashtable<String, Object>();

        // we validate all fields in two passes - first we validate data type integrity, i.e. we let makumba check if
        // the declared types in the MDD match with what we have in the form
        for (int i = 0; i < dd.getFieldNames().size(); i++) {
            FieldEditor fe = (FieldEditor) formatterArray[i];
            String inputName = FieldEditor.getInputName(this, i, suffix);
            if (inputName == null) {
                continue;
            }
            Object o = null;
            try {
                FieldDefinition fd = dd.getFieldDefinition(i);
                o = fe.readFrom(this, i, org.makumba.commons.attributes.RequestAttributes.getParameters(req), suffix);
                if (o != null) {
                    o = fd.checkValue(o);
                } else {
                    // check for not-null fields
                    if (applyValidationRules && fd.isNotNull()) {
                        throw new InvalidValueException(inputName, FieldDefinition.ERROR_NOT_NULL);
                    }
                    o = fd.getNull();
                }
                // for string types (text, char) check not empty
                if (applyValidationRules && fd.isNotEmpty() && fd.isStringType() && StringUtils.isEmpty(o.toString())) {
                    throw new InvalidValueException(inputName, FieldDefinition.ERROR_NOT_EMPTY);
                }

                validatedFields.put(new Integer(i), o);
                validatedFieldsNameCache.put(inputName, o);

            } catch (InvalidValueException e) {
                // if there is an exception in this field
                // we store it in the hash, together with the field definition where it occured
                exceptions.add(e);
            }
        }

        ArrayList<Integer> validatedFieldsOrdered = new ArrayList<Integer>(validatedFields.keySet());
        Collections.sort(validatedFieldsOrdered);

        // in the second validation pass, we only validate those fields that passed the first check
        // on those, we apply the user-defined checks from the validation definition
        for (int index = 0; index < validatedFieldsOrdered.size(); index++) {
            int i = (validatedFieldsOrdered.get(index)).intValue();
            FieldDefinition fieldDefinition = dd.getFieldDefinition(i);
            Object o = validatedFields.get(validatedFieldsOrdered.get(index));
            Collection<ValidationRule> validationRules = fieldDefinition.getValidationRules();

            if (validationRules != null && applyValidationRules) {
                for (ValidationRule validationRule : validationRules) {
                    ValidationRule rule = validationRule;
                    try { // evaluate each rule separately
                        if (rule instanceof ComparisonValidationRule
                                && !((ComparisonValidationRule) rule).isCompareToExpression()) {
                            FieldDefinition otherFd = ((ComparisonValidationRule) rule).getOtherFd();
                            Object otherValue = validatedFieldsNameCache.get(otherFd.getName());
                            if (otherValue == null) { // check if the other field definition is maybe in a pointed type
                                // do this by checking if it equals any of the original field definitions the form field
                                // definitions are made of
                                // FIXME: this seems like a hack. maybe on making the new field definition, the
                                // validation rules should be adapted too?
                                for (String field : dd.getFieldNames()) {
                                    FieldDefinition fd = dd.getFieldDefinition(field).getOriginalFieldDefinition();
                                    if (otherFd == fd) {
                                        otherValue = o;
                                        break;
                                    }
                                }
                            }
                            if (otherValue != null) {
                                rule.validate(new Object[] { o, otherValue });
                            }
                        } else {
                            rule.validate(o);
                        }
                    } catch (InvalidValueException e) {
                        exceptions.add(e);
                    }
                }
            }

            org.makumba.commons.attributes.RequestAttributes.setAttribute(req,
                FieldEditor.getInputName(this, i, suffix) + "_type", fieldDefinition);

            if (o != null) {
                // if we have a file type data-definition, put all fields in the sub-record
                String inputName = FieldEditor.getInputName(this, i, "");
                if (fieldDefinition.isFileType()) {
                    data.put(inputName + ".content", o);
                    HttpParameters parameters = RequestAttributes.getParameters(req);
                    data.put(inputName + ".contentType", parameters.getParameter(inputName + "_contentType"));
                    data.put(inputName + ".contentLength", parameters.getParameter(inputName + "_contentLength"));
                    data.put(inputName + ".originalName", parameters.getParameter(inputName + "_filename"));
                    data.put(inputName + ".name", parameters.getParameter(inputName + "_filename"));
                    if (parameters.getParameter(inputName + "_imageWidth") != null) {
                        data.put(inputName + ".imageWidth", parameters.getParameter(inputName + "_imageWidth"));
                    }
                    if (parameters.getParameter(inputName + "_imageHeight") != null) {
                        data.put(inputName + ".imageHeight", parameters.getParameter(inputName + "_imageHeight"));
                    }
                } else {
                    // the data is written in the dictionary without the suffix
                    data.put(inputName, o);
                }
            }
            org.makumba.commons.attributes.RequestAttributes.setAttribute(req,
                FieldEditor.getInputName(this, i, suffix), o);
        }

        if (exceptions.size() > 0) {
            throw new CompositeValidationException(exceptions);
        }
        return data;
    }

    public void config() {
        for (int i = 0; i < dd.getFieldNames().size(); i++) {
            ((FieldEditor) formatterArray[i]).onStartup(this, i);
        }
    }

    @Override
    public void initFormatters() {
        formatterArray = new FieldFormatter[dd.getFieldNames().size()];
        for (int i = 0; i < dd.getFieldNames().size(); i++) {
            FieldDefinition fd = dd.getFieldDefinition(i);
            if (fd.isFileType()) {
                formatterArray[i] = binaryEditor.getInstance();
                continue;
            }
            switch (fd.getIntegerType()) {
                case FieldDefinition._ptr:
                    formatterArray[i] = ptrEditor.getInstance();
                    break;
                case FieldDefinition._ptrOne:
                case FieldDefinition._setComplex:
                    formatterArray[i] = FieldEditor.getInstance();
                    break;
                case FieldDefinition._int:
                    formatterArray[i] = intEditor.getInstance();
                    break;
                case FieldDefinition._intEnum:
                    formatterArray[i] = intEnumEditor.getInstance();
                    break;
                case FieldDefinition._char:
                    formatterArray[i] = charEditor.getInstance();
                    break;
                case FieldDefinition._charEnum:
                    formatterArray[i] = charEnumEditor.getInstance();
                    break;
                case FieldDefinition._text:
                    formatterArray[i] = textEditor.getInstance();
                    break;
                case FieldDefinition._binary:
                    formatterArray[i] = binaryEditor.getInstance();
                    break;
                case FieldDefinition._boolean:
                    formatterArray[i] = booleanEditor.getInstance();
                    break;
                case FieldDefinition._date:
                    formatterArray[i] = dateEditor.getInstance();
                    break;
                case FieldDefinition._set:
                    formatterArray[i] = setEditor.getInstance();
                    break;
                // case FieldDefinition._nil:
                // formatterArray[i] = nilEditor.getInstance();
                // break;
                case FieldDefinition._real:
                    formatterArray[i] = realEditor.getInstance();
                    break;
                case FieldDefinition._setCharEnum:
                    formatterArray[i] = setcharEnumEditor.getInstance();
                    break;
                case FieldDefinition._setIntEnum:
                    formatterArray[i] = setintEnumEditor.getInstance();
                    break;
                case FieldDefinition._dateCreate:
                case FieldDefinition._dateModify:
                    if (isSearchForm) { // in search forms, we allow to have inputs on TS_create/modify
                        formatterArray[i] = dateEditor.getInstance();
                    } else {
                        formatterArray[i] = errorEditor.getInstance();
                    }
                    break;
                case FieldDefinition._ptrIndex:
                case FieldDefinition._ptrRel:
                    formatterArray[i] = errorEditor.getInstance();
                    break;
                default:
                    throw new RuntimeException(
                            "Internal Makumba error: Unknown FieldDefinition type lead to invalid formatter content. Please report to developers.");
            }
        }
    }

}
