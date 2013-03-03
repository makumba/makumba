/** $Header: /usr/local/cvsroot/karamba/public_html/layout/style/styles.css,v 1.8 2007/03/19 17:11:50 manu Exp $ */

function toggleStackTrace() {
    if (document.getElementById('stackTrace').style.display == 'none') {
        document.getElementById('stackTrace').style.display = 'block';
        document.getElementById('hideStackTrace').style.display = 'inline';
        document.getElementById('showStackTrace').style.display = 'none';
    } else {
        document.getElementById('stackTrace').style.display = 'none';
        document.getElementById('hideStackTrace').style.display = 'none';
        document.getElementById('showStackTrace').style.display = 'inline';
    }
}

function toggleElementDisplay(element) {
    if (element.style.display == 'none') {
    	element.style.display = 'block';
    } else {
    	element.style.display = 'none';
    }
}

function toggleLineNumbers() {
    var elements = document.getElementsByClassName("lineNo");
    for (i=0; i<elements.length; i++) {
        if (elements[i].style.display == 'none') {
            elements[i].style.display = 'inline';
        } else {
            elements[i].style.display = 'none';
        }
    }
}

/** toggles reference SQL details visibility on and off */
function toggleSQLDisplay(element, link) {
    if (element.style.display == 'none') {
        element.style.display = 'block';
        link.innerHTML='[-]';
    } else {
        element.style.display = 'none';
        link.innerHTML='[+]';
    }
}

/** toggles date function evaluation visibility on and off */
function toggleDateFunctionDisplay(element) {
    if (element.style.display == 'none') {
        element.style.display = 'inline';
    } else {
        element.style.display = 'none';
    }
}

/** toggles validation rule visibility on and off */
function toggleValidtionRuleDisplay() {
    var elements = document.getElementsByName('validationRule');
    for (i=0; i<elements.length; i++) {
        if (elements[i].style.display == 'none') {
            elements[i].style.display = 'inline';
        } else {
            elements[i].style.display = 'none';
        }
    }
}

/** toggles mdd function visibility on and off */
function toggleFunctionDisplay() {
    var elements = document.getElementsByName('mddFunction');
    for (i=0; i<elements.length; i++) {
        if (elements[i].style.display == 'none') {
            elements[i].style.display = 'inline';
        } else {
            elements[i].style.display = 'none';
        }
    }
}

/** this is executed at the page load **/
$(document).ready(function () {

    // Creates a popover on the <a> elements that have
    // rel='popover' and data-popover-id='id to element that holds the popover html'
    $('[rel=popover]').each(function(index,value) {
        var popoverId = $(value).data("popover-id");
        $(value).popover({
            html: true,
            placement: 'bottom',
            content: function() {
                return $("#" + popoverId).html();
            }
        }).click(function(e) {
            e.preventDefault();
        });
    });

    // Runs the highlighter
    if(typeof hljs !== 'undefined') {
        hljs.initHighlighting();
    }

});
