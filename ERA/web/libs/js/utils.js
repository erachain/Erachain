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
//  return text
return text;

}