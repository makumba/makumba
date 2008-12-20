// simple call to scriptaculous for AutoComplete. This is needed in order to have the context path figured out by the resource servlet

var MakumbaAutoComplete = {};

MakumbaAutoComplete.AutoComplete = function(id, location, type, field){

	new Ajax.Autocompleter(id, 'autocomplete_choices_'+id, '_CONTEXT_PATH_'+location+'?type='+type+'&field='+field, { minChars: 2, paramName: 'value'});
	
}
