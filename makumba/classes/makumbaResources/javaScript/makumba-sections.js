makEvent = function(name) {

	var eventToId = $H(_mak_event_to_id_);
	var idEventToType = $H(_mak_idevent_to_type_);
	var pageParameters = $H(_mak_page_params_);
	var toReload = new Array();
	
	var eventParam = $H({__mak_event__: name});
	
	//eventParam.each(function(pair) {alert(pair.key + ' ' + pair.value)});
	//pageParameters.each(function(pair) {alert(pair.key + ' ' + pair.value)});
	
	pageParameters = pageParameters.merge(eventParam);
	
	// fetch the sections we have to process
	var sections = eventToId.get(name);
	
	// for each section, hide it, show it, or schedule it for refresh
	sections.each(function(id) {
		//alert(id);
		var action = idEventToType.get(id + '___'+ name);
		//alert("key: " + id + '___'+ name + " action:" + action);
		if(action == 'show') {
			$(id).show();
		} else if(action == 'hide') {
			$(id).hide();
		} else if(action == 'reload') {
			toReload.push(id);
		}
	});
	
	// for each section we have to reload, display the waiting widget
	toReload.each(function(id) {
		// TODO this should be a class in the makumba.css and hence made configurable
		$(id).update('<img src="_CONTEXT_PATH__RESOURCE_PATH_/image/ajax-loader.gif"></img>');
	});
	
	
	// AJAX callback on the page to fetch the new data
	new Ajax.Request(_mak_page_url_, {
		  method:'get',
		  requestHeaders: {Accept: 'application/json'},
		  parameters: pageParameters,
		  onSuccess: function(transport) {
		    var newContent = $H(transport.responseText.evalJSON());
		 	// now go over the data and update the sections
		    newContent.each(function(pair) {
		    	if(sections.indexOf(pair.key) != -1) {
			 		$(pair.key).update(pair.value);
		    	}
		 	});
		   }
	});	
}