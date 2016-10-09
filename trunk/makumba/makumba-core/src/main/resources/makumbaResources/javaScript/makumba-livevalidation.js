/**
 * Makumba liveValidation
 * This file handles the makumba live validation
 * @TODO: implement date comparison
 */
(function($){
	'use strict';

	$(document).ready(function() {
		$('.mak-live-validation').each(function(){
			var $form = $(this);
			$('input,textarea').each(function(){
				if(!$(this).attr('data-mak-validation'))
					return;

				$(this).focusout(function(){
					$(this).next('.mak-validation').remove();
					var ruleset = $(this).data('makValidation');
					var errors = [];
					for( var rule in ruleset) {
						var error = $(this).checkRule(rule, ruleset[rule], $form);
						if(error) {
							errors.push(error);
						}
					}
					$(this).displayMessage(errors);
				});
			});
		});
	});

	$.fn.displayMessage = function(messages) {
		var html = '<span class="mak-validation mak-validation-error">' + messages.join('; ') + '</span>';
		$(this).after(html);
	}

	$.fn.checkRule = function(rule, ruleAttributes, $form) {
		var $input = $(this);
		switch(rule) {
			case 'presence':
					var value = $input.val();
					if(!value) return(ruleAttributes.failureMessage);
				break;
			case 'length':
				var value = $input.val();
				if(value.length > ruleAttributes.maximum || value.length < ruleAttributes.minimum) {
					return ruleAttributes.failureMessage;
				}
				break;
			case 'uniqueness':
				var response = $.ajax({
					url: Mak.CONTEXT_PATH + Mak.UNIQUENESS_LOCATION,
					method: 'GET',
					data: {
						field: ruleAttributes.field,
						table: ruleAttributes.table,
						value: $input.val(),
					},
				}).done(function(){
					var responseHash = $.parseJSON(response.responseText);
					if(responseHash.success && responseHash.success == 'not unique') {
						return ruleAttributes.failureMessage;
					}
				});
				break;
			case 'numberComparison':
				var thisValue = $input.val();
				var otherValue;
				if(ruleAttributes.value) {
					otherValue = ruleAttributes.value;
				} else {
					otherValue = $form.children('[name=' + ruleAttributes.field + ']').val();
				}

				if(thisValue == '' || otherValue == '') return;

				if(_compare(ruleAttributes.comparisonOperator, Number(thisValue), Number(otherValue))) {
					return ruleAttributes.failureMessage;
				}
				break;
			case 'stringComparison':
				var thisValue = $input.val();
				var otherValue;

				if(ruleAttributes.value) {
					otherValue = ruleAttributes.value;
				} else {
					otherValue = $form.children('[name=' + ruleAttributes.field + ']').val();
				}

				if(thisValue == '' || otherValue == '') return;

				if(ruleAttributes.functionToApply && ruleAttributes.functionToApply == 'lower') {
					thisValue = thisValue.toLowerCase();
				} else if(ruleAttributes.functionToApply && ruleAttributes.functionToApply == 'upper') {
					thisValue = thisValue.toUpperCase();
				}

				if(_compare(ruleAttributes.comparisonOperator, thisValue, otherValue)) {
					return ruleAttributes.failureMessage;
				}
				break;
			case 'numericality':
				var value = $input.val();
				if(!value) return;

				if(("minimum" in ruleAttributes && ruleAttributes.minimum > value)
					|| ("maximum" in ruleAttributes && ruleAttributes.maximum < value)) {
					return ruleAttributes.failureMessage;
				}
				break;
			case 'format':
				var value = $input.val();
				if(!value) return;

				var regex = ruleAttributes.pattern.split('/');
				var re    = new RegExp(regex[1], regex[2]);

				if(!value.match(re)) {
					return ruleAttributes.failureMessage;
				}
				break;
			default:
				return;
		}
	}

	function _compare(operator, value1, value2) {
		if (operator == "=") {
			return value1 == value2;
		} else if (operator == ">") {
			return value1 > value2;
		} else if (operator == ">=") {
			return value1 > value2;
		} else if (operator == "<") {
			return value1 < value2;
		} else if (operator == "<=") {
			return value1 <= value2;
		} else if (operator == "!=") {
			return value1 != value2;
		} else {
			throw new Error("Mak-validate - comparison operator must be present and valid! (given: '" + comparisonOperater + "')");
		}
	}
})(jQuery);