// select view format 
function fformat(text){

if (text == null || text.lenght == 0) return "";
text = text.toString();
if (text.lenght <5) return text;

var pref1 = text.substring(0,1);
var pref2 = text.substring(1,2);

if (pref1 =="<"){
// return HTML
if (pref2 =="\n"){
return text.substring(2);
}
return text;
}

if (pref1=="#"){
// return MarkDown
if (pref2=="\n"){
// return MarkDown
return marked(text.substring(2));
}
return marked(text);
}

//  return plain text
return htmlFilter(wordwrap(text, 0, '\n', true));

}