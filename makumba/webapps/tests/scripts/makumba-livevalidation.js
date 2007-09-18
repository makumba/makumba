/**
adds functions to the livevalidation framework which are not defined there, e.g. comparison of strings and numbers. 

Issues to fix:

- If we compare field1 with field2, then we add a validation only on field1 for the moment ==> there should be a way to trigger this validation when field2 is edited

- comparison of dates should be implemented

*/

var MakumbaValidate = {};

// define a textual description of the comparison operators
this.comparisonOperatorTextual = new Array();
comparisonOperatorTextual["="] = "equal to";
comparisonOperatorTextual[">"] = "greater than";
comparisonOperatorTextual[">="] = "equal or greater than";
comparisonOperatorTextual["<"] = "less than";
comparisonOperatorTextual["<="] = "equal or less than";
comparisonOperatorTextual["!="] = "not equal to";


// compare two values, using the given operator
MakumbaValidate.compare = function(comparisonOperator, value1, value2) {
    if (comparisonOperator == "=") {
        return value1 == value2;
    } else if (comparisonOperator == ">") {
        return value1 > value2;
    } else if (comparisonOperator == ">=") {
        return value1 > value2;
    } else if (comparisonOperator == "<") {
        return value1 < value2;
    } else if (comparisonOperator == "<=") {
        return value1 <= value2;
    } else if (comparisonOperator == "!=") {
        return value1 != value2;
    } else {
        throw new Error("MakumbaValidate::Compare - comparison operator must be present and valid! (given: '" + comparisonOperater + "')");
    }
}

// perform a number comparison
MakumbaValidate.NumberComparison = function(value1, paramsObj){
    var paramsObj = paramsObj || {};
    var value1 = Number(value1) || null;
    var value2 = Number(document.getElementById(paramsObj.element2).value) || null;
    var comparisonOperator = (paramsObj.comparisonOperator) || null;
    if (value1 != null && value2 != null) {
        var comparissionPassed = MakumbaValidate.compare(comparisonOperator, value1, value2);
        var failureMessage = paramsObj.failureMessage || paramsObj.element1 + " must be " + comparisonOperatorTextual[comparisonOperator] + " " + paramsObj.element2 + "!";
        if (!comparissionPassed) {
            Validate.fail(failureMessage);
        }
    }
    return true;
}


MakumbaValidate.StringComparison = function(value1, paramsObj){
    var paramsObj = paramsObj || {};
    var value2 = (document.getElementById(paramsObj.element2).value) || null;
    var comparisonOperator = (paramsObj.comparisonOperator) || null;

    var functionToApply = (paramsObj.functionToApply) || null;
    // apply functions
    if (functionToApply == "lower") {
        value1 = value1.toLowerCase();
    } else if (functionToApply == "upper") {
        value1 = value1.toUpperCase();
    }
    
    if (value1 != null && value2 != null) {
        var comparissionPassed = MakumbaValidate.compare(comparisonOperator, value1, value2);
        var field1 = paramsObj.element1;
        if (functionToApply != null) {
          field1 = functionToApply + "(" + field1 + ")";
        }
        var failureMessage = paramsObj.failureMessage || field1 + " must be " + comparisonOperatorTextual[comparisonOperator] + " " + paramsObj.element2 + "!";
        if (!comparissionPassed) {
            Validate.fail(failureMessage);
        }
    }
    return true;
}