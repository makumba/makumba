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
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.makumba.CompositeValidationException;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.ValidationRule;
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.RecordFormatter;
import org.makumba.controller.validation.ComparisonValidationRule;
import org.makumba.forms.validation.ClientsideValidationProvider;

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

    String[] query;

    protected RecordEditor() {
    }

    public RecordEditor(DataDefinition ri, Hashtable h, String database) {
        super(ri, h);
        this.database = database;
        db = new String[ri.getFieldNames().size()];
        query = new String[ri.getFieldNames().size()];
    }

    public ArrayList getUnassignedExceptions(CompositeValidationException e, ArrayList unassignedExceptions,
            HttpServletRequest req, String suffix) {
        for (int i = 0; i < dd.getFieldNames().size(); i++) {
            FieldEditor fe = (FieldEditor) formatterArray[i];
            Collection exceptions = e.getExceptions(fe.getInputName(this, i, suffix));
            if (exceptions != null) {
                for (Iterator iter = exceptions.iterator(); iter.hasNext();) {
                    unassignedExceptions.remove(iter.next());
                }
            }
        }
        return unassignedExceptions;
    }

    public void initClientSideValidation(ClientsideValidationProvider provider, boolean liveValidation, String suffix) {
        for (int i = 0; i < dd.getFieldNames().size(); i++) {
            FieldEditor fe = (FieldEditor) formatterArray[i];
            FieldDefinition fieldDefinition = dd.getFieldDefinition(i);
            String inputName = fe.getInputName(this, i, suffix);
            if (inputName == null) {
                continue;
            }
            provider.initField(inputName, fieldDefinition, /* validationDefinition, */liveValidation);
        }
    }

    public Dictionary readFrom(HttpServletRequest req, String suffix) {
        Dictionary<String, Object> data = new Hashtable<String, Object>();
        Vector<Exception> exceptions = new Vector<Exception>(); // will collect all exceptions from the field validity checks

        Hashtable<Integer, Object> validatedFields = new Hashtable<Integer, Object>();
        Hashtable<String, Object> validatedFieldsNameCache = new Hashtable<String, Object>();

        // we validate all fields in two passes - first we validate data type integrity, i.e. we let makumba check if
        // the declared types in the MDD match with what we have in the form
        for (int i = 0; i < dd.getFieldNames().size(); i++) {
            FieldEditor fe = (FieldEditor) formatterArray[i];
            FieldDefinition fieldDefinition = dd.getFieldDefinition(i);
            String inputName = fe.getInputName(this, i, suffix);
            if (inputName == null) {
                continue;
            }
            Object o = null;
            try {
                o = fe.readFrom(this, i, org.makumba.controller.http.RequestAttributes.getParameters(req), suffix);
                if (o != null) {
                    o = dd.getFieldDefinition(i).checkValue(o);
                } else {
                    o = dd.getFieldDefinition(i).getNull();
                }

                validatedFields.put(new Integer(i), o);
                validatedFieldsNameCache.put(inputName, o);

            } catch (InvalidValueException e) {
                // if there is an exception in this field
                // we store it in the hash, together with the field definition where it occured
                exceptions.add(e);
            }
        }

        ArrayList validatedFieldsOrdered = new ArrayList(validatedFields.keySet());
        Collections.sort(validatedFieldsOrdered);

        // in the second validation pass, we only validate those fields that passed the first check
        // on those, we apply the user-defined checks from the validation definition
        for (int index = 0; index < validatedFieldsOrdered.size(); index++) {
            int i = ((Integer) validatedFieldsOrdered.get(index)).intValue();
            FieldEditor fe = (FieldEditor) formatterArray[i];
            FieldDefinition fieldDefinition = dd.getFieldDefinition(i);
            Object o = validatedFields.get(validatedFieldsOrdered.get(index));
            Collection validationRules = fieldDefinition.getValidationRules();// validationDefinition.getValidationRules(fe.getInputName(this,
                                                                                // i, ""));
            if (validationRules != null) {
                for (Iterator iter = validationRules.iterator(); iter.hasNext();) {
                    ValidationRule rule = (ValidationRule) iter.next();
                    try { // evaluate each rule separately
                        if (rule instanceof ComparisonValidationRule
                                && !((ComparisonValidationRule) rule).isCompareToExpression()) {
                            FieldDefinition otherFd = ((ComparisonValidationRule) rule).getOtherFd();
                            Object otherValue = validatedFieldsNameCache.get(otherFd.getName());
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

            org.makumba.controller.http.RequestAttributes.setAttribute(req, fe.getInputName(this, i, suffix) + "_type",
                fieldDefinition);

            if (o != null) {
                // the data is written in the dictionary without the suffix
                data.put(fe.getInputName(this, i, ""), o);
            }
            org.makumba.controller.http.RequestAttributes.setAttribute(req, fe.getInputName(this, i, suffix), o);
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

    protected void initFormatters() {
        formatterArray = new FieldFormatter[dd.getFieldNames().size()];
        for (int i = 0; i < dd.getFieldNames().size(); i++) {
            FieldDefinition fd = dd.getFieldDefinition(i);
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
