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

function toggleFileRelationsDisplay() {
    if (document.getElementById('fileRelations').style.display == 'none') {
        document.getElementById('fileRelations').style.display = 'block';
    } else {
        document.getElementById('fileRelations').style.display = 'none';
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
