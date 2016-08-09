function EditInPlace() {}

EditInPlace.prototype.AIM = 
{
    frame : function(c, nr) {
        var n = 'f' + Math.floor(Math.random() * 99999);
        var d = document.createElement('DIV');
        d.innerHTML = '<iframe style="display:none" src="about:blank" id="'+n+'" name="'+n+'" onload="eip.AIM.loaded(\''+n+'\', \''+nr+'\')"></iframe>';
        document.body.appendChild(d);

        var i = document.getElementById(n);
        if (c && typeof(c.onComplete) == 'function') {
            i.onComplete = c.onComplete;
        }

        return n;
    },

    form : function(f, name) {
        f.setAttribute('target', name);
    },

    submit : function(f, c, nr) {
        this.form(f, this.frame(c, nr));
        if (c && typeof(c.onStart) == 'function') {
            return c.onStart();
        } else {
            return true;
        }
    },

    loaded : function(id, nr) {
        var i = document.getElementById(id);
        if (i.contentDocument) {
            var d = i.contentDocument;
        } else if (i.contentWindow) {
            var d = i.contentWindow.document;
        } else {
            var d = window.frames[id].document;
        }
        if (d.location.href == "about:blank") {
            return;
        }

        if (typeof(i.onComplete) == 'function') {
            i.onComplete(d.body.innerHTML, nr);
        }
    }

}
	
EditInPlace.prototype.hide = function(id)
{
	if(document.getElementById(id)) document.getElementById(id).style.display = 'none';
}
	
EditInPlace.prototype.show_inline = function(id)
{
	if(document.getElementById(id)) document.getElementById(id).style.display = 'inline';
}
	
EditInPlace.prototype.turnit = function(id)
{
	a = document.getElementById("mak_edittype_"+id); 
	if(a)
	{
		if(a.value == "text" || a.value == "char")
		{
			document.getElementById("mak_onedit_"+id+"_text").value = 
				this.unhtmlspecialchars(document.getElementById('mak_onview_'+id).innerHTML);
		}
		
		this.hide("mak_onview_"+id);
		this.show_inline("mak_onedit_"+id);

		document.getElementById("mak_onedit_"+id+"_text").focus();
	}
}
	
EditInPlace.prototype.completeCallback = function(response, id) {
	response = response.replace(/^<pre>/, "");
	response = response.replace(/[\n]*<\/pre>\n?$/, "");
	//if(response == "success")
	{
		a = document.getElementById('mak_edittype_'+id);
		if(a)
		{
			if(document.getElementById('mak_onedit_'+id))
			{
				if(a.value == 'select')
				{
					document.getElementById('mak_onview_'+id).innerHTML = eip.htmlspecialchars(document.getElementById('mak_onedit_'+id+'_text').options[document.getElementById('mak_onedit_'+id+'_text').selectedIndex].text);
				}
				else
				{
					document.getElementById('mak_onview_'+id).innerHTML = eip.htmlspecialchars(document.getElementById('mak_onedit_'+id+'_text').value);
				}
			}
		}
		eip.show_inline("mak_onview_"+id);
		eip.hide("mak_onedit_"+id);
	}
	/*
	else
	{
		alert("There was some error.");
	}
	*/
}


EditInPlace.prototype.htmlspecialchars = function(txt)
{
	if(typeof(txt) != "string")
	{
		return txt;
	}
	
	txt = txt.split('&').join('&amp;').
		split('"').join('&quot;').
		split('<').join('&lt;').
		split('>').join('&gt;');
		
	txt = txt.split("\n").join('<br/>');
	
	return txt;
}

EditInPlace.prototype.unhtmlspecialchars = function(txt)
{
	if(typeof(txt) != "string")
	{
		return txt;
	}
	
	txt = txt.split('&amp;').join('&').
		split('&quot;').join('"').
		split('&lt;').join('<').
		split('&gt;').join('>');
		
	txt = txt.split("<br/>").join('\n')
			.split("<br>").join('\n');
	
	return txt;
}

eip = new EditInPlace();

