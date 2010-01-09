Mak = function() {
  addMethod(this, "event", function(name) {
    makEvent(name, null);
  });
  addMethod(this, "event", function(name, exprValue) {
    makEvent(name, exprValue);
  });
  addMethod(this, "submit", function(formName) {
	    makSubmit(formName, null, null);
  });
  addMethod(this, "submit", function(formName, annotation, annotationSeparator) {
	    makSubmit(formName, annotation, annotationSeparator);
});
}

/**
 * makumba event - client-to-server-side event firing
 * - shows / hides / reloads sections depending on the event type
 * - when necessary makes an ajax request to the server with the given event and the page parameters,
 *   then updates the affected sections with the data
 */
makEvent = function(name, exprValue) {

	var eventToId = $H(_mak_event_to_id_);
	var idEventToType = $H(_mak_idevent_to_type_);
	var pageParameters = $H(_mak_page_params_);
	var toReload = new Array();
	
	var eventName;
	
	if(exprValue == null) {
		eventName = name;
	} else {
		eventName = name + "---" + exprValue;
	}
	
	var eventParam = $H({__mak_event__: eventName});
	
	//eventParam.each(function(pair) {alert(pair.key + ' ' + pair.value)});
	//pageParameters.each(function(pair) {alert(pair.key + ' ' + pair.value)});
	
	pageParameters = pageParameters.merge(eventParam);
	

	// fetch the sections we have to process
	var sections = eventToId.get(eventName);
	

	
	// for each section, hide it, show it, or schedule it for refresh
	sections.each(function(id) {
		//alert(id);
		var action = idEventToType.get(id + '___'+ eventName);
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
		$(id).update('<span class="sectionReload" ></span>');
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

/**
 * form submit via partial postback
 * - submits the form in an ajax request
 * - "fires" the returned event if everything went ok
 * - displays the form errors if there were errors (annotations and message)
 */
makSubmit = function(formName, annotation, annotationSeparator) {
	$(formName).request({
		  requestHeaders: {Accept: 'application/json'},
		  onComplete: function(transport) {
			  var response = transport.responseText.evalJSON();

			  // clear previous error messages
			  $(formName).select('span.makumba_field_annotation').each(function(e) {$(e).remove()});
			  $(formName).select('span.makumba_form_message').each(function(e) {$(e).remove()});

			  if(response.event != undefined) {
				  // TODO support for forms inside of a list that have a projection expression
				  makEvent(response.event, null);
				  $(formName).reset();
			  } else {
				  var message = new String(response.message);
				  var fieldErrors = $H(response.fieldErrors);
				  
				  if(!message.blank()) {
					  var messageSpan = new Element('span', {'class':'makumba_form_message'});
					  $(messageSpan).update(message);
					  $(formName).insert({top: messageSpan});
				  }
				  
				  fieldErrors.each(function(pair) {
					  var key = pair.key;
					  var errors = pair.value;
					  var sep = '<br>';
					  if(annotationSeparator != undefined) {
						  sep = annotationSeparator;
					  }
					  var inputSpan = new Element('span', {'class':'makumba_field_annotation'});
					  for(var index = 0; index < errors.length; ++index) {
						  var message = errors[index];
						  var span = new Element('span', {'class':'LV_validation_message LV_invalid'}).update(message);
						  $(inputSpan).insert({ bottom: span});
						  if(index + 1 < errors.length) {
							  $(span).insert({after: sep});
						  }
					  }
					  
					  var annotationPosition;
					  if(annotation != undefined) {
						  annotationPosition = annotation;
					  } else {
						  annotationPosition = 'after';
					  }
					  if(annotationPosition == 'before') {
						  $(key).insert({before: inputSpan});
					  } else if(annotationPosition == 'after') {
						  $(key).insert({after: inputSpan});
					  } else if(annotationPosition == 'both') {
						  $(key).insert({before: inputSpan});
						  $(key).insert({after: inputSpan});
					  }
					  
				  });
			  }
		  }
	});	
}

/**
 * addMethod - By John Resig (MIT Licensed)
 */
function addMethod(object, name, fn) {
    var old = object[ name ];
    object[ name ] = function(){
        if ( fn.length == arguments.length )
            return fn.apply( this, arguments );
        else if ( typeof old == 'function' )
            return old.apply( this, arguments );
    };
}