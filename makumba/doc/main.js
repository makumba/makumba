/* $Id$ */

function makeNavBar(pathToBase) 
{
	var bDir=pathToBase;

	document.write('<div class="navbar">');
	//document.write(bDir);
	
	document.write('<a href="'+bDir+'index.html"><img src="'+bDir+'makumba-logo-small.gif" border="0"></a><br>');

        document.write('<br>');
	document.write('<a href="'+bDir+'index.html"><b>Home</b></a><br>');	

	document.write('<br>');
	document.write('<a href="'+bDir+'documentation.html"><b>Documentation</b></a><br>');
	document.write('- <a href="'+bDir+'makumba-spec.html">Specification</a><br>');
	document.write('- <a href="'+bDir+'makumba-example.html">Example</a><br>');
	document.write('- <a href="'+bDir+'api/org/makumba/package-summary.html">API docs</a><br>');
	document.write('- <a href="'+bDir+'faq.html">FAQ</a><br>');
	
	document.write('<br>');
	document.write('<a href="'+bDir+'download.html"><b>Download</b></a><br>');

	//document.write('<img src="'+bDir+'gears.gif" align="right">');
	document.write('<br>');
	document.write('<a href="http://bugzilla.makumba.org/query.cgi?product=Makumba">Bugzilla</a><br>');
	document.write('<br>');
	document.write('<a href="http://cvs.makumba.org/">CVS repository</a><br>');

	document.write('</div>');
}


/* Takes cvs "$Id$" */
/*   or "$Header$" */
/*   tag and breaks it down to basic parts */
function getPageInfo(cvsID)
{
	//trim it
	cvsID=cvsID.substring(cvsID.indexOf(": ")+2,cvsID.lastIndexOf(" $"));
	//split it at spaces
	var cvsIds=cvsID.split(" ");
	//take needed parts
	var filename=cvsIds[0].substring(0,cvsIds[0].indexOf(","));
	var revision=cvsIds[1];
	var date=cvsIds[2].replace("/","-").replace("/","-");
	var time=cvsIds[3];
	var author=cvsIds[4];

	return(''+filename+', revision '+revision+', last modified on '+date+' at '+time+' by '+author);

}

function makeFooter(cvsID)
{
	document.write('<div class="pageFooter">');
	document.write(getPageInfo(cvsID));
	document.write('</div>');

}
