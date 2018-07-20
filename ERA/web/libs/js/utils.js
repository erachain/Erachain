// select view format 
function fformat(text){
var pref = text.substring(0,2);
if (pref =="<\n"){
// return HTML
return text.substring(2);
}
if (pref=="#\n"){
// return MarkDown
return marked(text.substring(2));
}

var pref = text.substring(0,1);
if (pref =="<"){
// return HTML
return text;
}
if (pref=="#"){
// return MarkDown
return marked(text);
}



//  return text
return htmlFilter(wordwrap(text, 100, '\n', true));

}