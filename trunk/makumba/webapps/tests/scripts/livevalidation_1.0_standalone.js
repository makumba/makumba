// Copyright (c) 2007 Alec Hill (www.livevalidation.com) - LiveValidation is licensed under the terms of the MIT License

/**
 *	validates a form field in real-time based on validations you assign to it
 *	
 *	@var element {mixed} - either a dom element reference or the string id of the element to validate
 *	@var optionsObj {Object} - general options, see below for details
 *
 *	optionsObj properties:
 *							validMessage {String} 	- the message to show when the field passes validation
 *													  (DEFAULT: "Thankyou!")
 *							onValid {Function} 		- function to execute when field passes validation
 *													  (DEFAULT: function(){ this.insertMessage(this.createMessageSpan()); this.addFieldClass(); } )	
 *							onInvalid {Function} 	- function to execute when field fails validation
 *													  (DEFAULT: function(){ this.insertMessage(this.createMessageSpan()); this.addFieldClass(); })
 *							insertAfterWhatNode {Int} 	- position to insert default message
 *													  (DEFAULT: the field that is being validated)							
 */
var LiveValidation = function(element, optionsObj){
  	this.initialize(element, optionsObj);
}

/*************************************** element types constants *************************************************/

LiveValidation.TEXTAREA 		= 1;
LiveValidation.TEXT 			    = 2;
LiveValidation.PASSWORD 		= 3;
LiveValidation.CHECKBOX 		= 4;

/*************************************** Static methods *********************************************/

/**
 *	pass an array of LiveValidation objects and it will validate all of them
 *	
 *	@var validations {Array} - an array of LiveValidation objects
 *	@return {Bool} - true if all passed validation, false if any fail						
 */
LiveValidation.massValidate = function(validations){
  	var returnValue = true;
	for(var i = 0, len = validations.length; i < len; ++i ){
		var valid = validations[i].validate();
		if(returnValue) returnValue = valid;
	}
	return returnValue;
}

/*********************************************** LiveValidation class ***********************************/

/**
 *	initialises all of the properties and events
 *
 * @var - Same as constructor above
 */
LiveValidation.prototype.initialize = function(element, optionsObj){
  	var self = this;
  	if(!element) throw new Error("LiveValidation::initialize - No element reference or element id has been provided!");
	this.element = element.nodeName ? element : document.getElementById(element);
	if(!this.element) throw new Error("LiveValidation::initialize - No element with reference or id of '" + element + "' exists!");
	var options = optionsObj || {};
	this.validMessage = options.validMessage || 'Thankyou!';
	this.insertAfterWhatNode = options.insertAfterWhatNode || this.element;
  	this.onValid = options.onValid || function(){ this.insertMessage(this.createMessageSpan()); this.addFieldClass(); };
  	this.onInvalid = options.onInvalid || function(){ this.insertMessage(this.createMessageSpan()); this.addFieldClass(); };	
	this.message;
	this.validations = [];
	this.validClass = 'LV_valid';
	this.invalidClass = 'LV_invalid';
    this.validFieldClass = 'LV_valid_field';
	this.invalidFieldClass = 'LV_invalid_field';
    this.messageClass = 'LV_validation_message';
  	this.elementType = this.getElementType();
  	this.displayMessageWhenEmpty = false;
  	this.validationFailed = false;
  	if(this.elementType == LiveValidation.CHECKBOX){
	    this.element.onchange = function(e){ self.validate(); }
		this.element.onclick = function(e){ self.validate(); }
	}else{
	  	this.element.onkeyup = function(e){ self.validate(); }
	  	this.element.onblur = function(e){ self.validate(); }
	}
}

/**
 *	adds a validation to perform to a LiveValidation object
 *
 *	@var validationFunction {Function} - validation function to be used (ie Validate.Presence )
 *	@var validationParamsObj {Object} - parameters for doing the validation, if wanted or necessary
 * @return {Object} - the LiveValidation object itself so that calls can be chained
 */
LiveValidation.prototype.add = function(validationFunction, validationParamsObj){
  	this.validations.push( {type: validationFunction, params: validationParamsObj || {} } );
    return this;
}

/**
 *	gets the type of element, to check whether it is compatible
 *
 *	@var validationFunction {Function} - validation function to be used (ie Validate.Presence )
 *	@var validationParamsObj {Object} - parameters for doing the validation, if wanted or necessary
 */
LiveValidation.prototype.getElementType = function(){
	switch(true){
	  	case (this.element.nodeName == 'TEXTAREA'):
	  		return LiveValidation.TEXTAREA;
	  	case (this.element.nodeName == 'INPUT' && this.element.type == 'text'):
	  		return LiveValidation.TEXT;
	  	case (this.element.nodeName == 'INPUT' && this.element.type == 'password'):
	  		return LiveValidation.PASSWORD;
	  	case (this.element.nodeName == 'INPUT' && this.element.type == 'checkbox'):
	  		return LiveValidation.CHECKBOX;
	  	case (this.element.nodeName == 'INPUT'):
    	  	throw new Error('LiveValidation::getElementType - Cannot use LiveValidation on a ' + this.element.type + ' input!');
	  	default:
	  		throw new Error('LiveValidation::getElementType - Element must be an input or textarea!');
	}
}

/**
 *	loops through all the validations added to the LiveValidation object and checks them one by one
 *
 *	@var validationFunction {Function} - validation function to be used (ie Validate.Presence )
 *	@var validationParamsObj {Object} - parameters for doing the validation, if wanted or necessary
 * @return {Boolean} - whether the all the validations passed or if one failed
 */
LiveValidation.prototype.doValidations = function(){
  	this.validationFailed = false;
  	for(var i = 0, len = this.validations.length; i < len; ++i){
	 	var validation = this.validations[i];
		switch(validation.type){
		   	case Validate.Presence:
            case Validate.Confirmation:
            case Validate.Acceptance:
		   		this.displayMessageWhenEmpty = true;
		   		this.validationFailed = !this.validateElement(validation.type, validation.params); 
				break;
		   	default:
		   		this.validationFailed = !this.validateElement(validation.type, validation.params);
		   		break;
		}
		if(this.validationFailed) return false;	
	}
	this.message = this.validMessage;
	return true;
}

/**
 *	performs validation on the element and handles any error (validation or otherwise) it throws up
 *
 *	@var validationFunction {Function} - validation function to be used (ie Validate.Presence )
 *	@var validationParamsObj {Object} - parameters for doing the validation, if wanted or necessary
 * @return {Boolean} - whether the validation has passed or failed
 */
LiveValidation.prototype.validateElement = function(validationFunction, validationParamsObj){
  	var value = this.element.value;
  	if(validationFunction == Validate.Acceptance){
	    if(this.elementType != LiveValidation.CHECKBOX) throw new Error('LiveValidation::validateElement - Element to validate acceptance must be a checkbox!');
		value = this.element.checked;
	}
    var isValid = true;
  	try{    
		validationFunction(value, validationParamsObj);
	} catch(error) {
	  	if(error instanceof Validate.Error){
			if( value !== '' || (value === '' && this.displayMessageWhenEmpty) ){
				this.validationFailed = true;
				this.message = error.message;
				isValid = false;
			}
		}else{
		  	throw error;
		}
	}finally{
	    return isValid;
    }
}

/**
 *	makes it do the all the validations and fires off the onValid or onInvalid callbacks
 *
 * @return {Boolean} - whether the all the validations passed or if one failed
 */
LiveValidation.prototype.validate = function(){
  	var isValid = this.doValidations();
	if(isValid){
		this.onValid();
		return true;
	}else{
	  	this.onInvalid();
	  	return false;
	}
}

/** Message insertion methods ****************************
 * 
 * These are only used in the onValid and onInvalid callback functions and so if you overide the default callbacks,
 * you must either impliment your own functions to do whatever you want, or call some of these from them if you 
 * want to keep some of the functionality
 */

/**
 *	makes a span containg the passed or failed message
 *
 * @return {HTMLSpanObject} - a span element with the message in it
 */
LiveValidation.prototype.createMessageSpan = function(){
    var span = document.createElement('span');
	var textNode = document.createTextNode(this.message);
  	span.appendChild(textNode);
    return span;
}

/**
 *	removes the message element if it exists, so that the new message will replace it
 */
LiveValidation.prototype.removeMessageIfExists = function(){
	var nextEl;
	var el = this.insertAfterWhatNode;
	while(el.nextSibling){
	    if(el.nextSibling.nodeType === 1){
		  	nextEl = el.nextSibling;
		  	break;
		}
		el = el.nextSibling;
	}
  	if(nextEl && nextEl.className.indexOf(this.messageClass) != -1) this.insertAfterWhatNode.parentNode.removeChild(nextEl);
}

/**
 *	inserts the element containing the message in place of the element that already exists (if it does)
 *
 * @var elementToIsert {HTMLElementObject} - an element node to insert
 */
LiveValidation.prototype.insertMessage = function(elementToInsert){
  	this.removeMessageIfExists();
  	if( (this.displayMessageWhenEmpty && (this.elementType == LiveValidation.CHECKBOX || this.element.value == ''))
	  || this.element.value != '' ){
        var className = this.validationFailed ? this.invalidClass : this.validClass;
	  	elementToInsert.className += ' ' + this.messageClass + ' ' + className;
        if(this.insertAfterWhatNode.nextSibling){
		  		this.insertAfterWhatNode.parentNode.insertBefore(elementToInsert, this.insertAfterWhatNode.nextSibling);
		}else{
			    this.insertAfterWhatNode.parentNode.appendChild(elementToInsert);
	    }
	}
}


/**
 *	changes the class of the field based on whether it is valid or not
 */
LiveValidation.prototype.addFieldClass = function(){
    if(!this.validationFailed){
        if(this.element.className.indexOf(this.invalidFieldClass) != -1) this.element.className = this.element.className.split(this.invalidFieldClass).join('');
        if(!this.displayMessageWhenEmpty && this.element.value == ''){
           if(this.element.className.indexOf(this.validFieldClass) != -1) this.element.className = this.element.className.split(this.validFieldClass).join('');
        }else{
            if(this.element.className.indexOf(this.validFieldClass) == -1) this.element.className += ' ' + this.validFieldClass;
        }
    }else{
        if(this.element.className.indexOf(this.validFieldClass) != -1) this.element.className = this.element.className.split(this.validFieldClass).join(' ');
        if(this.element.className.indexOf(this.invalidFieldClass) == -1) this.element.className += ' ' + this.invalidFieldClass;
    }
}

/*************************************** Validate class ****************************************/
/**
 * This class contains all the methods needed for doing the actual validation itself
 *
 * All methods are static so that they can be used outside the context of a form field
 * as they could be useful for validating stuff anywhere you want really
 *
 * All of them will return true if the validation is successful, but will raise a ValidationError if
 * they fail, so that this can be caught and the message explaining the error can be accessed ( as just 
 * returning false would leave you a bit in the dark as to why it failed )
 *
 * Can use validation methods alone and wrap in a try..catch statement yourself if you want to access the failure
 * message and handle the error, or use the Validate::now method if you just want true or false
 */

var Validate = {};

/**
 *	validates that the field has been filled in
 *
 *	@var value {mixed} - value to be checked
 *	@var paramsObj {Object} - parameters for this particular validation, see below for details
 *
 *	paramsObj properties:
 *							failureMessage {String} - the message to show when the field fails validation 
 *													  (DEFAULT: "Can't be empty!")
 */
Validate.Presence = function(value, paramsObj){
  	var paramsObj = paramsObj || {};
	var message = paramsObj.failureMessage || "Can't be empty!";
	if(value === '' || value === null || value === undefined){ 
	  	Validate.fail(message);
	}
	return true;
}

/**
 *	validates that the value is numeric, does not fall within a given range of numbers
 *	
 *	@var value {mixed} - value to be checked
 *	@var paramsObj {Object} - parameters for this particular validation, see below for details
 *
 *	paramsObj properties:
 *							notANumberMessage {String} - the message to show when the validation fails when value is not a number
 *													  	  (DEFAULT: "Must be a number!")
 *							notAnIntegerMessage {String} - the message to show when the validation fails when value is not an integer
 *													  	  (DEFAULT: "Must be a number!")
 *							wrongNumberMessage {String} - the message to show when the validation fails when is param is used
 *													  	  (DEFAULT: "Must be {is}!")
 *							tooLowMessage {String} 		- the message to show when the validation fails when minimum param is used
 *													  	  (DEFAULT: "Must not be less than {minimum}!")
 *							tooHighMessage {String} 	- the message to show when the validation fails when maximum param is used
 *													  	  (DEFAULT: "Must not be more than {maximum}!")
 *							is {Int} 					- the length must be this long 
 *							minimum {Int} 				- the minimum length allowed
 *							maximum {Int} 				- the maximum length allowed
 *                         onlyInteger {Boolean} - if true will only allow integers to be valid
 *                                                             (DEFAULT: false)
 *
 *  NB. can be checked if it is within a range by specifying both a minimum and a maximum
 *  NB. will evaluate numbers represented in scientific form (ie 2e10) correctly as numbers				
 */
Validate.Numericality = function(value, paramsObj){
    var suppliedValue = value;
    var value = Number(value);
	var paramsObj = paramsObj || {};
    var minimum = ((paramsObj.minimum) || (paramsObj.minimum == 0)) ? paramsObj.minimum : null;;
    var maximum = ((paramsObj.maximum) || (paramsObj.maximum == 0)) ? paramsObj.maximum : null;
	var is = ((paramsObj.is) || (paramsObj.is == 0)) ? paramsObj.is : null;
    var notANumberMessage = paramsObj.notANumberMessage || "Must be a number!";
    var notAnIntegerMessage = paramsObj.notAnIntegerMessage || "Must be an integer!";
	var wrongNumberMessage = paramsObj.wrongNumberMessage || "Must be " + is + "!";
	var tooLowMessage = paramsObj.tooLowMessage || "Must not be less than " + minimum + "!";
	var tooHighMessage = paramsObj.tooHighMessage || "Must not be more than " + maximum + "!";
    if (!isFinite(value)) Validate.fail(notANumberMessage);
    if (paramsObj.onlyInteger && (/\.0+$|\.$/.test(String(suppliedValue))  || value != parseInt(value)) ) Validate.fail(notAnIntegerMessage);
	switch(true){
	  	case (is !== null):
	  		if( value != Number(is) ) Validate.fail(wrongNumberMessage);
			break;
	  	case (minimum !== null && maximum !== null):
	  		Validate.Numericality(value, {tooLowMessage: tooLowMessage, minimum: minimum});
	  		Validate.Numericality(value, {tooHighMessage: tooHighMessage, maximum: maximum});
	  		break;
	  	case (minimum !== null):
	  		if( value < Number(minimum) ) Validate.fail(tooLowMessage);
			break;
	  	case (maximum !== null):
	  		if( value > Number(maximum) ) Validate.fail(tooHighMessage);
			break;
	}
	return true;
}

/**
 *	validates against a RegExp pattern
 *	
 *	@var value {mixed} - value to be checked
 *	@var paramsObj {Object} - parameters for this particular validation, see below for details
 *
 *	paramsObj properties:
 *							failureMessage {String} - the message to show when the field fails validation
 *													  (DEFAULT: "Not valid!")
 *							pattern {RegExp} 		- the regular expression pattern
 *													  (DEFAULT: /./)
 *
 *  NB. will return true for an empty string, to allow for non-required, empty fields to validate.
 *		If you do not want this to be the case then you must either add a LiveValidation.PRESENCE validation
 *		or build it into the regular expression pattern
 */
Validate.Format = function(value, paramsObj){
  	var value = String(value);
	var paramsObj = paramsObj || {};
	var message = paramsObj.failureMessage || "Not valid!";
  	var pattern = paramsObj.pattern || /./;
	if(!pattern.test(value) /* && value != ''*/ ){ 
	  	Validate.fail(message);
	}
	return true;
}
/*
Validate.FormatFunction = function(value, paramsObj){
    var value = String(value);
    var paramsObj = paramsObj || {};
    var message = paramsObj.failureMessage || "Not valid!";
    var pattern = paramsObj.pattern || /./;
    var func = paramsObj.applyFunction || "";
    if (func == 'lower') {
      value = 
    }
  return true;
}*/

/**
 *	validates that the field contains a valid email address
 *	
 *	@var value {mixed} - value to be checked
 *	@var paramsObj {Object} - parameters for this particular validation, see below for details
 *
 *	paramsObj properties:
 *							failureMessage {String} - the message to show when the field fails validation
 *													  (DEFAULT: "Must be a number!" or "Must be an integer!")
 */
Validate.Email = function(value, paramsObj){
	var paramsObj = paramsObj || {};
	var message = paramsObj.failureMessage || "Must be a valid email address!";
	Validate.Format(value, { failureMessage: message, pattern: /^([^@\s]+)@((?:[-a-z0-9]+\.)+[a-z]{2,})$/i } );
	return true;
}

/**
 *	validates the length of the value
 *	
 *	@var value {mixed} - value to be checked
 *	@var paramsObj {Object} - parameters for this particular validation, see below for details
 *
 *	paramsObj properties:
 *							wrongLengthMessage {String} - the message to show when the fails when is param is used
 *													  	  (DEFAULT: "Must be {is} characters long!")
 *							tooShortMessage {String} 	- the message to show when the fails when minimum param is used
 *													  	  (DEFAULT: "Must not be less than {minimum} characters long!")
 *							tooLongMessage {String} 	- the message to show when the fails when maximum param is used
 *													  	  (DEFAULT: "Must not be more than {maximum} characters long!")
 *							is {Int} 					- the length must be this long 
 *							minimum {Int} 				- the minimum length allowed
 *							maximum {Int} 				- the maximum length allowed
 *
 *  NB. can be checked if it is within a range by specifying both a minimum and a maximum				
 */
Validate.Length = function(value, paramsObj){
	var value = String(value);
	var paramsObj = paramsObj || {};
    var minimum = ((paramsObj.minimum) || (paramsObj.minimum == 0)) ? paramsObj.minimum : null;
	var maximum = ((paramsObj.maximum) || (paramsObj.maximum == 0)) ? paramsObj.maximum : null;
	var is = ((paramsObj.is) || (paramsObj.is == 0)) ? paramsObj.is : null;
    var wrongLengthMessage = paramsObj.wrongLengthMessage || "Must be " + is + " characters long!";
	var tooShortMessage = paramsObj.tooShortMessage || "Must not be less than " + minimum + " characters long!";
	var tooLongMessage = paramsObj.tooLongMessage || "Must not be more than " + maximum + " characters long!";
	switch(true){
	  	case (is !== null):
	  		if( value.length != Number(is) ) Validate.fail(wrongLengthMessage);
			break;
	  	case (minimum !== null && maximum !== null):
	  		Validate.Length(value, {tooShortMessage: tooShortMessage, minimum: minimum});
	  		Validate.Length(value, {tooLongMessage: tooLongMessage, maximum: maximum});
	  		break;
	  	case (minimum !== null):
	  		if( value.length < Number(minimum) ) Validate.fail(tooShortMessage);
			break;
	  	case (maximum !== null):
	  		if( value.length > Number(maximum) ) Validate.fail(tooLongMessage);
			break;
		default:
			throw new Error("Validate::Length - Length(s) to validate against must be provided!");
	}
	return true;
}

/**
 *	validates that the value falls within a given set of values
 *	
 *	@var value {mixed} - value to be checked
 *	@var paramsObj {Object} - parameters for this particular validation, see below for details
 *
 *	paramsObj properties:
 *							failureMessage {String} - the message to show when the field fails validation
 *													  (DEFAULT: "Must be included in the list!")
 *							within {Array} 			- an array of values that the value should fall in 
 *													  (DEFAULT: [])	
 *							allowNull {Bool} 		- if true, and a null value is passed in, validates as true
 *													  (DEFAULT: false)
 *                         partialMatch {Bool} 	- if true, will not only validate against the whole value to check but also if it is a substring of the value 
 *													  (DEFAULT: false)
 *                         exclusion {Bool} 		- if true, will validate that the value is not within the given set of values
 *													  (DEFAULT: false)			
 */
Validate.Inclusion = function(value, paramsObj){
	var paramsObj = paramsObj || {};
	var message = paramsObj.failureMessage || "Must be included in the list!";
	if(paramsObj.allowNull && value == null) return true;
    if(!paramsObj.allowNull && value == null) Validate.fail(message)
	var list = paramsObj.within || [];
	var found = false;
	for(var i = 0, length = list.length; i < length; ++i){
	  	if(list[i] == value) found = true;
        if(paramsObj.partialMatch){ 
            if(value.indexOf(list[i]) != -1) found = true;
        }
	}
	if( (!paramsObj.exclusion && !found) || (paramsObj.exclusion && found) ) Validate.fail(message);
	return true;
}

/**
 *	validates that the value does not fall within a given set of values
 *	
 *	@var value {mixed} - value to be checked
 *	@var paramsObj {Object} - parameters for this particular validation, see below for details
 *
 *	paramsObj properties:
 *							failureMessage {String} - the message to show when the field fails validation
 *													  (DEFAULT: "Must not be included in the list!")
 *							within {Array} 			- an array of values that the value should not fall in 
 *													  (DEFAULT: [])
 *							allowNull {Bool} 		- if true, and a null value is passed in, validates as true
 *													  (DEFAULT: false)
 *                         partialMatch {Bool} 	- if true, will not only validate against the whole value to check but also if it is a substring of the value 
 *													  (DEFAULT: false)			
 */
Validate.Exclusion = function(value, paramsObj){
    var paramsObj = paramsObj || {};
	paramsObj.failureMessage = paramsObj.failureMessage || "Must not be included in the list!";
    paramsObj.exclusion = true;
	Validate.Inclusion(value, paramsObj);
    return true;
}

/**
 *	validates that the value matches that in another field
 *	
 *	@var value {mixed} - value to be checked
 *	@var paramsObj {Object} - parameters for this particular validation, see below for details
 *
 *	paramsObj properties:
 *							failureMessage {String} - the message to show when the field fails validation
 *													  (DEFAULT: "Does not match!")
 *							match {String} 			- id of the field that this one should match						
 */
Validate.Confirmation = function(value, paramsObj){
  	if(!paramsObj.match) throw new Error("Validate::Confirmation - Error validating confirmation: Id of element to match must be provided!");
	var paramsObj = paramsObj || {};
	var message = paramsObj.failureMessage || "Does not match!";
	var match = paramsObj.match.nodeName ? paramsObj.match : document.getElementById(paramsObj.match);
	if(!match) throw new Error("Validate::Confirmation - There is no reference with name of, or element with id of '" + paramsObj.match + "'!");
	if(value != match.value){ 
	  	Validate.fail(message);
	}
	return true;
}

/**
 *	validates that the value is true (for use primarily in detemining if a checkbox has been checked)
 *	
 *	@var value {mixed} - value to be checked if true or not (usually a boolean from the checked value of a checkbox)
 *	@var paramsObj {Object} - parameters for this particular validation, see below for details
 *
 *	paramsObj properties:
 *							failureMessage {String} - the message to show when the field fails validation 
 *													  (DEFAULT: "Must be accepted!")
 */
Validate.Acceptance = function(value, paramsObj){
  	var paramsObj = paramsObj || {};
	var message = paramsObj.failureMessage || "Must be accepted!";
	if(!value){ 
	  	Validate.fail(message);
	}
	return true;
}



/************************************** Validate::now method ******************************************/

/**
 *	validates whatever it is you pass in, and handles the validation error for you so it gives a nice true or false reply
 *
 *	@var validationFunction {Function} - validation function to be used (ie Validation.validatePresence )
 *	@var value {mixed} - value to be checked if true or not (usually a boolean from the checked value of a checkbox)
 *	@var validationParamsObj {Object} - parameters for doing the validation, if wanted or necessary
 */
Validate.now = function(validationFunction, value, validationParamsObj){
  	if(!validationFunction) throw new Error("Validate::now - Validation function must be provided!");
	var isValid = true;
    try{    
		validationFunction(value, validationParamsObj || {});
	} catch(error) {
		if(error instanceof Validate.Error){
			isValid =  false;
		}else{
		 	throw error;
		}
	}finally{ 
        return isValid 
    }
}


/************************************** Validate::fail method **********************************/

/**
 * shortcut for failing throwing a validation error
 *
 *	@var errorMessage {String} - message to display
 */
Validate.fail = function(errorMessage){
        throw new Validate.Error(errorMessage);
}

/*************************************** Validate.Error class ***********************************/

Validate.Error = function(errorMessage){
	this.message = errorMessage;
	this.name = 'ValidationError';
}