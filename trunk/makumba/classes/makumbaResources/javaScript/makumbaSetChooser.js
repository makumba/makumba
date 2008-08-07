/** $Id: AllowedException.java 1049 2005-06-25 13:16:52Z rosso_nero $ */
<!-- //Begin

// Original:  Bob Rockers (brockers@subdimension.com)
// http://javascript.internet.com/forms/list-organizer.html
// This script and many more are available free online at
// The JavaScript Source!! http://javascript.internet.com

function move(fbox, tbox) {
   var arrFbox = new Array();
   var arrTbox = new Array();
   var arrLookup = new Array();
   var i;

   for (i = 0; i < tbox.options.length; i++) {
      arrLookup[tbox.options[i].text] = tbox.options[i].value;
        arrTbox[i] = tbox.options[i].text;
   }

   var fLength = 0;
   var tLength = arrTbox.length;
   for(i = 0; i < fbox.options.length; i++) {
      arrLookup[fbox.options[i].text] = fbox.options[i].value;
      if (fbox.options[i].selected && fbox.options[i].value != "") {
         arrTbox[tLength] = fbox.options[i].text;
         tLength++;
      }
      else {
         arrFbox[fLength] = fbox.options[i].text;
         fLength++;
      }
   }

   arrFbox.sort();
   arrTbox.sort();
   fbox.length = 0;
   tbox.length = 0;
   var c;
   for(c = 0; c < arrFbox.length; c++) {
      var no = new Option();
      no.value = arrLookup[arrFbox[c]];
      no.text = arrFbox[c];
      fbox[c] = no;
   }

   for(c = 0; c < arrTbox.length; c++) {
      var no = new Option();
      no.value = arrLookup[arrTbox[c]];
      no.text = arrTbox[c];
      tbox[c] = no;
   }
}//end: move(fbox, tbox)

function Moveup(dbox) {
   for(var i = 0; i < dbox.options.length; i++) {
      if (dbox.options[i].selected && dbox.options[i] != "" && dbox.options[i] != dbox.options[0]) {
         var tmpval = dbox.options[i].value;
         var tmpval2 = dbox.options[i].text;
         dbox.options[i].value = dbox.options[i - 1].value;
         dbox.options[i].text = dbox.options[i - 1].text
         dbox.options[i-1].value = tmpval;
         dbox.options[i-1].text = tmpval2;
	 dbox.selectedIndex = i-1;
	 break;
      }
   }
}//end: Moveup(dbox)

function Movedown(ebox) {
   for(var i = 0; i < ebox.options.length; i++) {
      if (ebox.options[i].selected && ebox.options[i] != "" && ebox.options[i+1] != 
         ebox.options[ebox.options.length]) {

         var tmpval = ebox.options[i].value;
         var tmpval2 = ebox.options[i].text;
         ebox.options[i].value = ebox.options[i+1].value;
         ebox.options[i].text = ebox.options[i+1].text
         ebox.options[i+1].value = tmpval;
         ebox.options[i+1].text = tmpval2;
	 ebox.selectedIndex = i+1;
	 break;
      }
   }
}//end: Movedown(ebox)


// -- end code by Bob Rockers

// -- functions changed/added for the Makumba chooser

function moveNotSelected(fbox, tbox) {
   var arrFbox = new Array();
   var arrTbox = new Array();
   var arrLookup = new Array();
   var i;

   for (i = 0; i < tbox.options.length; i++) {
      arrLookup[tbox.options[i].text] = tbox.options[i].value;
        arrTbox[i] = tbox.options[i].text;
   }

   var fLength = 0;
   var tLength = arrTbox.length;
   for(i = 0; i < fbox.options.length; i++) {
      arrLookup[fbox.options[i].text] = fbox.options[i].value;
      if ( (fbox.options[i].selected==false) && fbox.options[i].value != "") {
         arrTbox[tLength] = fbox.options[i].text;
         tLength++;
      }
      else {
         arrFbox[fLength] = fbox.options[i].text;
         fLength++;
      }
   }

   arrFbox.sort();
   arrTbox.sort();
   fbox.length = 0;
   tbox.length = 0;
   var c;
   for(c = 0; c < arrFbox.length; c++) {
      var no = new Option();
      no.value = arrLookup[arrFbox[c]];
      no.text = arrFbox[c];
      fbox[c] = no;
   }

   for(c = 0; c < arrTbox.length; c++) {
      var no = new Option();
      no.value = arrLookup[arrTbox[c]];
      no.text = arrTbox[c];
      tbox[c] = no;
   }
}//end: moveNotSelected(fbox, tbox)


function highlightAll(box) {
   for(var i = 0; i < box.options.length; i++) {
      box.options[i].selected=true;
   }
}

//  End -->
