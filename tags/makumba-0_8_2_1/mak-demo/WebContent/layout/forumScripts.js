 var quoter = "&gt; ";


function quoteReplacer() {
	var contents = document.getElementsByName("forumContents");
	var quoteElement = "<div class=\"forumQuoter\">&nbsp;</div>";
	for(var i=0;i<contents.length;i++) {
	    contents[i].innerHTML = contents[i].innerHTML.replace(/&gt; /g,quoteElement);
	    
    //Next lines are a hack for some makumba bug
	//    contents[i].innerHTML = contents[i].innerHTML.replace("<pre style=\"margin:0px\">","");
	//    contents[i].innerHTML = contents[i].innerHTML.replace("</pre>","");
	//    contents[i].innerHTML = contents[i].innerHTML.replace("<PRE style=\"margin:0px\">","");
	//    contents[i].innerHTML = contents[i].innerHTML.replace("</PRE>","");
	}
}

function insertQuote() {

  var linelength = 70;
  var parPost = document.getElementById('parentPostContents').innerHTML;
  var paragraphs = parPost.split("\n");
  for (var i = 0; i<paragraphs.length; i++) {
    var j = paragraphs[i].indexOf(" ", linelength);
    while (j<paragraphs[i].length && j > 0) {
      j = paragraphs[i].indexOf(" ", j);
      paragraphs[i] = paragraphs[i].substring(0,j) + "\n" + paragraphs[i].substring(j+1); 
      j = j + linelength;
    }
  }
  parPost = paragraphs.join("\n");
  parPost = quoter + parPost.replace(/\n/g,"\n" + quoter) + "\n";
  document.getElementById('contents_area').innerHTML = parPost + document.getElementById('contents_area').innerHTML; 
}
