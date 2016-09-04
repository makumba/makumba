/**
 * Makumba-autocomplete
 * This file handles the mak:section postback logic
 */
(function($){
	$(document).ready(function() {
		// Bind event listener
		$('[data-mak-hide-event]').each(function() {
			var eventName = $(this).data('makHideEvent');
			$(this).on(eventName, function(){
				$(this).hide();
			});
		});

		$('[data-mak-show-event]').each(function() {
			var eventName = $(this).data('makShowEvent');
			$(this).on(eventName, function(){
				$(this).show();
			});
		});

		// Bind event triggerers
		$('[data-mak-on-click]').each(function() {
			$(this).click(function() {
				var eventName = $(this).data('makOnClick');
				var eventList = [];
				$.each(['show','hide'], function(i, ev){
					eventList.push('[data-mak-' + ev +'-event=' + eventName + ']');
				});

				$(eventList.join(',')).trigger(eventName);
			});
		});

		// Bind the ajax call to the forms and prevent the actual POST call
		$('.mak-ajax-form').each(function() {
			$form = $(this);
			$form.submit(function(e) {
				e.preventDefault();
				var response = $.ajax({
					method: 'POST',
					data: $form.serializeArray(),
				}).done(function(){
					responseHash = $.parseJSON(response.responseText);
					updateSections($form.data('makTriggerEvent'), responseHash);
				});
			});
		});
	});

	/**
	 * Update mak:section elements with the response of the AJAX request
	 * @param {String} event    the event which triggered the refresh
	 * @param {Object} response a hash with the responses for all sections, organized by form_id
	 */
	function updateSections(event, response) {
		$('div[data-mak-reload-event=' + event + ']').each(function(){
			var form_id = $(this).attr('id');
			$(this).html(response[form_id]);
		});
	}
})(jQuery);