// /* $Header$ */
// Tabs Menu scripts, by Vilius (starting 2004 03 25)
// Usage is defined in /itc/PAguidelines.jsp

// to test script: <span id="printout"></span>
//   var el = document.getElementById("printout");
//   var text = document.createTextNode("This is : " + var1 +" ["+var2+"] ");
//   el.appendChild(text);

function clickTab(event) {
// wrapper for event
	activateTab(this);
	return false;  // does not allow to change URL in browser
	// it would be nice to do that, but then we need to find out how to schrool to top of the page.
	// window.scrollTo(0,0); should do that, but it executed before browser moves to #
	// onLoad does not work as page is not reloaded, just scrooled.
}

function initiateTab(url) {
	
	// assign link handlers for menu
	var tabList = document.getElementsByName("tabmenu");
	for (var i = 0; i < tabList.length; i++) {
		tabList[i].onclick=clickTab;
	}

	var tabList = document.getElementsByName("tabmenu_expand");
	for (var i = 0; i < tabList.length; i++) {
		tabList[i].onclick=showTabs;
	}
	
	// activate Tab
	activateTab(url);
} // function initiateTab


function activateTab(url) {
// load indicated tab

	if (!url) { // no object provided, this means that we load not via click
		var url_rx =  /.*#tab_([^\?&]*)/;
		url_rx.exec(document.URL);
		var tab=RegExp.$1;
		if (!tab) {
			// I need to search first tab and place it in url
			var divList = document.getElementsByTagName("DIV");
			var rx = /tab_([^\?&]*)/;
			for (var i = 0; i < divList.length; i++) {
	
			if (rx.exec(divList[i].id)) {
					url = "#"+divList[i].id;
					break;  // leave on the first match
				}
			}
			if (!url) { // 
				// find tab in which #id is located. go to parent until find one.
				// otherwise, just show all (?)
			}
		} else url=document.URL;   // url has #tab_doc structure
		
	} // if (!url)


	var rx = /.*#([^\?&]*)/;
	rx.exec(url);
	tab=RegExp.$1;
	// Hide current active tab (via hiding all) and deactivate menu
	hideTabs();

	// show tab
	document.getElementById(tab).style.display = "block";

	// 
	// activate menu
	var tabList = document.getElementsByName("tabmenu");

	for (var i = 0; i < tabList.length; i++) {
			rx.exec(tabList[i].getAttribute("href"));
			tab_href=RegExp.$1;
			if (tab_href == tab) {
				// changing parent of <a> class (usually this should be span)
				tabList[i].parentNode.className="active_menu_item";
			}
	}
}
	

// Hide all the tabs
// used before activating a new tab.
function hideTabs() {
	var divList = document.getElementsByTagName("DIV");
	var text;
	var rx = /tab_(.*[^\?&]*)/;
	for (var i = 0; i < divList.length; i++) {
		if (rx.exec(divList[i].id)) {
			divList[i].style.display = "none";
		}
	}
	
	// deactivate meniu
	var tabList = document.getElementsByName("tabmenu");
	
	for (var i = 0; i < tabList.length; i++) {
		tabList[i].parentNode.className="menu_item";
	}
	var tabList = document.getElementsByName("tabmenu_expand");
	
	for (var i = 0; i < tabList.length; i++) {
		tabList[i].parentNode.className="menu_item";
	}
	
}

// Hide all the tabs
// used before activating a new tab.
function showTabs(event) {
	var divList = document.getElementsByTagName("DIV");
	var rx = /tab_([^\?&]*)/;
	
	var el = document.getElementById("printout");
	for (var i = 0; i < divList.length; i++) {
	
		if (rx.exec(divList[i].id)) {
			divList[i].style.display = "block";
		}
	}
	
	// deactivate meniu
	var tabList = document.getElementsByName("tabmenu");
	
	for (var i = 0; i < tabList.length; i++) {
		tabList[i].parentNode.className="menu_item";
	}

	// activate menu
	var tabList = document.getElementsByName("tabmenu_expand");

	var rx = /.*#([^\?&]*)/;
	rx.exec(this);
	tab=RegExp.$1;
	for (var i = 0; i < tabList.length; i++) {
			rx.exec(tabList[i].getAttribute("href"));
			tab_href=RegExp.$1;
			if (tab_href == tab) {
				// changing parent of <a> class
				tabList[i].parentNode.className="active_menu_item";
			}
	}

	return false; // don't allow to change URL
}
