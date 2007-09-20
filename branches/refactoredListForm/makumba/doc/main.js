/* $Id$ */


//"global" variables, for the page
var queryStr="";

function makeNavBar(pathToBase) 
{
	var bDir=pathToBase;

	document.write('<div class="navbar">');
	//document.write(bDir);
	
	document.write('<a href="'+bDir+'index.html"><img src="'+bDir+'makumba-logo-small.gif" border="0"></a><br>');

        document.write('<br>');
	document.write('<a href="'+bDir+'index.html"><b>Home</b></a><br>');	
	document.write('<br>');
	document.write('<a href="'+bDir+'news.html"><b>News</b></a><br>');	

	document.write('<br>');
	document.write('<a href="'+bDir+'documentation.html"><b>Documentation</b></a><br>');
	document.write('- <a href="'+bDir+'makumba-install.html">Installation</a><br/>');
	document.write('- <a href="'+bDir+'makumba-spec.html">Specification</a><br>');
	document.write('- <a href="'+bDir+'makumba-example.html">Example</a><br>');
	document.write('- <a href="'+bDir+'SQL-drivers.html"> Config DB</a><br>');
	document.write('- <a href="'+bDir+'api/org/makumba/package-summary.html">API docs</a><br>');
	document.write('- <a href="'+bDir+'faq.html">FAQ</a><br>');
	
	document.write('<br>');
	document.write('<a href="'+bDir+'download.html"><b>Download</b></a><br>');

	document.write('<br>');
	document.write('<a href="http://bugzilla.makumba.org/cgi-bin/bugzilla/query.cgi?product=Makumba">Bugzilla</a><br>');
	document.write('<a href="'+bDir+'documentation.html#devel"><b>Developer</b></a><br>');
	document.write('- <a href="'+bDir+'hacking/index.html">documentation</a><br>');
	document.write('- <a href="'+bDir+'issues/index.html">issues</a><br>');
	document.write('- <a href="http://makumba.svn.sourceforge.net/">SVN repository</a><br>');
	document.write('<br>');
	document.write('<FORM method=GET action=http://www.google.com/custom class="search" id="searchForm">');
	 document.write('<INPUT TYPE=text name=q size=10 maxlength=255 value="Search" onFocus="if(this.value==\'Search\') this.value=queryStr;" onBlur="queryStr=this.value; this.value=\'Search\';" title="Google Search makumba.org" class="search" id="searchBox" accesskey="s" onDblClick="location.href=\''+bDir+'search.html\'">');
	 document.write('<INPUT type=hidden name=sa VALUE="Google Search">');
	 document.write('<INPUT type=hidden name=cof VALUE="S:http://www.makumba.org;VLC:#044a2c;AH:center;BGC:white;LH:100;LC:#044a2c;GFNT:#999999;L:http://www.makumba.org/makumba-logo.gif;ALC:red;LW:329;T:black;GIMP:red;AWFID:ae27560a86a9a04e;">');
	 document.write('<input type=hidden name=domains value="makumba.org">');
	 document.write('<input type=hidden name=sitesearch value="makumba.org">');
	document.write('</FORM>');
        
	document.write('</div>');
}


/* Takes svn "$Id$" */
/*   or "$Header$" */
/*   tag and breaks it down to basic parts */
function getPageInfo(svnID)
{
	//trim it
	svnID=svnID.substring(svnID.indexOf(": ")+2,svnID.lastIndexOf(" $"));
	//split it at spaces
	var svnIds=svnID.split(" ");
	//take needed parts
	var filename=svnIds[0];
	var revision=svnIds[1];
	var date=svnIds[2].replace("/","-").replace("/","-");
	var time=svnIds[3];
	var author=svnIds[4];

	return(''+filename+', revision '+revision+', last modified on '+date+' at '+time+' by '+author);

}

function makeFooter(svnID)
{
	document.write('<div class="pageFooter">');
	document.write(getPageInfo(svnID));
	document.write('</div>');

}
