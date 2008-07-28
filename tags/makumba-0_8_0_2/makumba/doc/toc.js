// Automatic TOC generation, http://www.evolt.org/article/Automatic_TOC_Generation/17/45809/index.html
// Implemented by vilius, 2004 03 25
// Usage: in document: <body onload="generate_TOC('toc');"> or
//                     <script type="text/javascript"> generate_TOC('toc'); </script> (end of doc)
// To print TOC: <div id="toc"></div> 

function H_getText(el) {
  var text = "";
  for (var i = el.firstChild; i != null; i = i.nextSibling) {
    if (i.nodeType == 3 /* Node.TEXT_NODE, IE doesn't speak constants */)
      text += i.data;
    else if (i.firstChild != null)
      text += H_getText(i);
  }
  return text;
}

function TOC_EL(el, text, level) {
  this.element = el;
  this.text = text;
  this.level = level;
}

function getHeadlines(el) {
  var l = new Array;
  var rx = /[hH]([1-6])/;
  // internal recursive function that scans the DOM tree
  var rec = function (el) {
    for (var i = el.firstChild; i != null; i = i.nextSibling) {
      if (i.nodeType == 1 /* Node.ELEMENT_NODE */) {
        if (rx.exec(i.tagName))
          l[l.length] = new TOC_EL(i, H_getText(i), parseInt(RegExp.$1));
        rec(i);
      }
    }
  }
  rec(el);
  return l;
}

function generate_TOC(parent_id) {
  var parent = document.getElementById(parent_id);
  var hs = getHeadlines(document.getElementsByTagName("body")[0]);
  for (var i = 0; i < hs.length; ++i) {
    var hi = hs[i];
    var d = document.createElement("div");
    if (hi.element.id == "")
      hi.element.id = "gen" + i;
    var a = document.createElement("a");
    a.href = "#" + hi.element.id;
    a.appendChild(document.createTextNode(hi.text));
    d.appendChild(a);
    d.className = "level" + hi.level;
    parent.appendChild(d);
  }
  parent.style.display = "block";
}