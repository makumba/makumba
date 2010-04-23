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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.makumba.CompositeValidationException;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.Transaction;
import org.makumba.ValidationRule;
import org.makumba.commons.DbConnectionProvider;
import org.makumba.commons.attributes.HttpParameters;
import org.makumba.commons.attributes.RequestAttributes;
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.RecordFormatter;
import org.makumba.providers.datadefinition.mdd.validation.ComparisonValidationRule;
import org.makumba.providers.datadefinition.mdd.validation.MultiUniquenessValidationRule;


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

    /**
     * This is a simple version of {@link #readFrom(HttpServletRequest, String, HashMap)}, as search forms don't need to
     * apply validation rules, and have relaxed validity constraints on type checks. E.g., they accept multiple values
     * for ptr or enum types.
     */
    public Dictionary<String, Object> readFromSearchForm(HttpServletRequest req, String suffix, HashMap<String, String> lazyEvaluatedInputs) {
        Dictionary<String, Object> data = new Hashtable<String, Object>();
        // will collect all exceptions from the field validity checks
        Vector<InvalidValueException> exceptions = new Vector<InvalidValueException>();

        for (int i = 0; i < dd.getFieldNames().size(); i++) {
            String inputName = FieldEditor.getInputName(this, i, suffix);
            if (inputName == null) {
                continue;
            }
            Object o = null;
            try {
                FieldDefinition fd = dd.getFieldDefinition(i);
                o = ((FieldEditor) formatterArray[i]).readFrom(this, i, RequestAttributes.getParameters(req), suffix);
                if (o != null) {
                    o = fd.checkValue(o);
                } else {
                    o = fd.getNull();
                }
                RequestAttributes.setAttribute(req, FieldEditor.getInputName(this, i, suffix) + "_type", fd);


            } catch (InvalidValueException e) {
                // if there is an exception in this field
                // we store it in the hash, together with the field definition where it occurred
                exceptions.add(e);
            }
            data.put(inputName, o);
            RequestAttributes.setAttribute(req, FieldEditor.getInputName(this, i, suffix), o);
        }
        if (exceptions.size() > 0) {
            throw new CompositeValidationException(exceptions);
        }
        return data;
    }
    
    public Dictionary<String, Object> readFrom(HttpServletRequest req, String suffix, HashMap<String, String> lazyEvaluatedInputs) {
        Dictionary<String, Object> data = new Hashtable<String, Object>();
        // will collect all exceptions from the field validity checks
        Vector<InvalidValueException> exceptions = new Vector<InvalidValueException>();

        Hashtable<Integer, Object> validatedFields = new Hashtable<Integer, Object>();
        Hashtable<String, Object> validatedFieldsNameCache = new Hashtable<String, Object>();
        Hashtable<FieldDefinition, Object> validatedFieldsFdCache = new Hashtable<FieldDefinition, Object>();
        

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
                o = fe.readFrom(this, i, RequestAttributes.getParameters(req), suffix);
                if (o != null) {
                    o = fd.checkValue(o);
                } else {
                    // check for not-null fields
                    // we don't check if the field is going to be lazily evaluated
                    // TODO maybe find a more robust way to make sure wether the field is to be lazily evaluated
                    boolean lazyEvaluation = lazyEvaluatedInputs.containsValue(inputName.substring(0, inputName.indexOf(suffix)));
                    
                    if (fd.isNotNull() && !lazyEvaluation) {
                        String error = fd.getNotNullErrorMessage();
                        if(error == null)
                            error = FieldDefinition.ERROR_NOT_NULL;
                        throw new InvalidValueException(inputName, error);
                    }
                    o = fd.getNull();
                }
                // for string types (text, char) check not empty
                if (fd.isNotEmpty() && fd.isStringType() && StringUtils.isEmpty(o.toString())) {
                    String error = fd.getNotEmptyErrorMessage();
                    if(error == null)
                        error = FieldDefinition.ERROR_NOT_EMPTY;

                    throw new InvalidValueException(inputName, error);
                }

                validatedFields.put(new Integer(i), o);
                validatedFieldsNameCache.put(inputName, o);
                
                // FIXME caching the original FDs is not the most efficient thing to do
                FieldDefinition originalFd = fd.getOriginalFieldDefinition();
                
                if(originalFd != null) {
                    validatedFieldsFdCache.put(originalFd, o);
                }

            } catch (InvalidValueException e) {
                // if there is an exception in this field
                // we store it in the hash, together with the field definition where it occurred
                exceptions.add(e);
            }
        }

        ArrayList<Integer> validatedFieldsOrdered = new ArrayList<Integer>(validatedFields.keySet());
        Collections.sort(validatedFieldsOrdered);

        // in the second validation pass, we only validate those fields that passed the first check
        // on those, we apply the user-defined checks from the validation definition
        
        // TODO once we have more than one multi-field validation rule type, abstract this to ValidationRule
        LinkedHashMap<ValidationRule, FieldDefinition> multiFieldValidationRules = new LinkedHashMap<ValidationRule, FieldDefinition>();
        
        DbConnectionProvider prov = (DbConnectionProvider) req.getAttribute(RequestAttributes.PROVIDER_ATTRIBUTE);
        Transaction t = prov.getConnectionTo(prov.getTransactionProvider().getDefaultDataSourceName());
        
        // STEP 1: go over all the fields and fetch validation rules
        for (int index = 0; index < validatedFieldsOrdered.size(); index++) {
            int i = (validatedFieldsOrdered.get(index)).intValue();
            FieldDefinition fieldDefinition = dd.getFieldDefinition(i);
            Object o = validatedFields.get(validatedFieldsOrdered.get(index));
            Collection<ValidationRule> validationRules = fieldDefinition.getValidationRules();

            if (validationRules != null) {
                for (ValidationRule validationRule : validationRules) {
                    ValidationRule rule = validationRule;
                    
                    try { // evaluate each rule separately

                        // STEP 1-a: treat or fetch multi-field validation rules

                        // FIXME this is an old validation rule, once we switch, remove this
                        if (rule instanceof org.makumba.providers.datadefinition.makumba.validation.ComparisonValidationRule
                                && !((org.makumba.providers.datadefinition.makumba.validation.ComparisonValidationRule) rule).isCompareToExpression()) {
                            FieldDefinition otherFd = ((org.makumba.providers.datadefinition.makumba.validation.ComparisonValidationRule) rule).getOtherFd();
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
                                rule.validate(new Object[] { o, otherValue }, t);
                            }
                        } else if (rule instanceof ComparisonValidationRule || rule instanceof MultiUniquenessValidationRule) {
                            // we just fetch the multi-field validation rules, do not treat them yet
                            multiFieldValidationRules.put(rule, fieldDefinition);

                        // STEP 1-b: treat single-field validation rules
                        } else {
                            rule.validate(o, t);
                        }
                    } catch (InvalidValueException e) {
                        exceptions.add(e);
                    }
                }
            }

            RequestAttributes.setAttribute(req, FieldEditor.getInputName(this, i, suffix) + "_type", fieldDefinition);

            String inputName = FieldEditor.getInputName(this, i, "");
            if (fieldDefinition.isFileType() && o != null) {
                // if we have a file type data-definition, put all fields in the sub-record
                HttpParameters parameters = RequestAttributes.getParameters(req);
                Integer length = (Integer) parameters.getParameter(inputName + "_contentLength");
                if(length > 0) {
                    data.put(inputName + ".content", o);
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
                }
                    
            } else {
                // the data is written in the dictionary without the suffix
                data.put(inputName, o);
            }
            
            RequestAttributes.setAttribute(req, FieldEditor.getInputName(this, i, suffix), o);
        }
        
        
        // STEP 2 - process multi-field validation rules
        for(ValidationRule r : multiFieldValidationRules.keySet()) {

            LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
            boolean validate = true;
            // fetch the fields of the rule
            for(String fieldName : r.getValidationRuleArguments()) {
                // we have to append the suffix to the field name of the rule in order to find back our field
                if(validatedFieldsNameCache.containsKey(fieldName + suffix)) {
                    values.put(fieldName, validatedFieldsNameCache.get(fieldName + suffix));
                } else {
                    // check if this field is maybe a pointed type
                    // do this by checking if any of the original field definitions the form is made of
                    // for this we use the data definition the validation rule applies to and the field that is pointed
                    DataDefinition ruleDD = r.getDataDefinition();
                    FieldDefinition ruleFd = ruleDD.getFieldOrPointedFieldDefinition(fieldName);
                    Object o = validatedFieldsFdCache.get(ruleFd);
                    if(o != null) {
                        values.put(fieldName, o);
                    } else {
                        // FIXME what to do in this case? we don't have all the values for the validation rule, so we can't evaluate it.
                        // we could maybe fetch the value of the field from the DB in some cases, pretty advanced stuff though.
                        // see also the comment at getUnassignedExceptions()
                        // basically now that we have a transaction at our disposal, we can fetch the baseObject (__makumba__base__)
                        // from the request and the type of the object from the responder (__makumba__responder__)
                        // then, do a query and fetch the other value...
                        validate = false;
                    }
                }
            }
            if(validate) {
                try {
                    r.validate(values, t);
                } catch(InvalidValueException e) {
                    exceptions.add(e);
                }
            }
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
