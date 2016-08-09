function showhide(id){
if (document.getElementById){
obj = document.getElementById(id);
if (obj.style.display == "none"){
obj.style.display = "";
} else {
obj.style.display = "none";
}
}
}

function getPage(url,element)
{		
	var pars = '';
	
	var myAjax = new Ajax.Updater(
		element, 
		url, 
		{
			method: 'get', 
			parameters: pars
		});
	
}