/**
 * Makumba autocomplete
 * This file handles the makumba
 */
(function($){
  'use strict';
	var MIN_LENGTH = 2; // minimum length of search string

	$(document).ready(function() {
		$('input[data-mak-ac]').each(function() {
			Mak.autocomplete($(this));
		});
	});

	Mak.fn.autocomplete = function($inputElement) {
		var $targetElement = $('#' + $inputElement.data('makAcTarget'));

		$inputElement.on('input', function() {
			if ($inputElement.val().length < MIN_LENGTH) {
				$targetElement.hide();
				return;
			}

			var response = $.ajax({
				url: Mak.CONTEXT_PATH + Mak.AUTOCOMPLETE_LOCATION,
				method: 'GET',
				data: {
					type:      $inputElement.data("makAcType"),
					field:     $inputElement.data("makAcField"),
					fieldType: $inputElement.data("makAcFieldType"),
					queryLang: $inputElement.data("makAcQueryLang"),
					value:     $inputElement.val(),
				},
			}).done(function(){
				var responseHash = $.parseJSON(response.responseText);
				switch($inputElement.data("makAcFieldType")) {
					case "char":
						_updateCharSuggestions(responseHash.result, $inputElement, $targetElement);
					case "ptr":
						_updatePtrSuggestions(responseHash.result, $inputElement, $targetElement);
				}
				if($.isEmptyObject(responseHash.result)) {
					$targetElement.hide();
				} else {
					$targetElement.show();
				}

			});
		});

		function _updateCharSuggestions(result, $inputElement, $targetElement) {
			$targetElement.html('<ul></ul>');
			$.each(result, function(index, suggestion) {
				$('<li>' + suggestion + '</li>').appendTo($targetElement.find('ul')).click(function(){
					$inputElement.val($(this).text());
					$targetElement.hide();
				});
			});
		}

		function _updatePtrSuggestions(result, $inputElement, $targetElement) {
			var $hiddenElement = $('[name=' + $inputElement.data('makAcHiddenField') + ']');
			$targetElement.html('<ul></ul>');
			$.each(result, function(ptrValue, suggestion) {
				$('<li>' + suggestion + '</li>').appendTo($targetElement.find('ul')).data('ptrValue', ptrValue).click(function(){
					$hiddenElement.val($(this).data('ptrValue'));
					$inputElement.val($(this).text());
					$targetElement.hide();
				});
			});
		}
	};

})(jQuery);