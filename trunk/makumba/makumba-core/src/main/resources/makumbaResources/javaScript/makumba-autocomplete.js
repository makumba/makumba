// simple call to scriptaculous for AutoComplete. This is needed in order to have the context path figured out by the resource servlet

var MakumbaAutoComplete = {};

MakumbaAutoComplete.AutoComplete = function(id, location, type, field, fieldType, queryLang) {
	
	if(fieldType == 'char') {
		
		new Ajax.Autocompleter(id, 'autocomplete_choices_'+id, '_CONTEXT_PATH_'+location+'?type='+type+'&field='+field+'&fieldType='+fieldType+'&queryLang='+queryLang, { minChars: 2, paramName: 'value', autoSelect: true});
		
	} else if (fieldType == 'ptr') {
		
		new Ajax.Autocompleter(id + '_visible', 'autocomplete_choices_'+id, '_CONTEXT_PATH_'+location+'?type='+type+'&field='+field+'&fieldType='+fieldType+'&queryLang='+queryLang, { minChars: 2, paramName: 'value', autoSelect: true, afterUpdateElement: MakumbaAutoComplete.getSelectionId});
		
	}
}

MakumbaAutoComplete.getSelectionId = function(div, li) {
    var hidden_div_id = div.id.substring(0, div.id.length - '_visible'.length);
    document.getElementById(hidden_div_id).value = li.id;
}
