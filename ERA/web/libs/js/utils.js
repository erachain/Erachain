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

function convertTimestamp(timestamp, withYear) {
    if (timestamp == null) return '';
    var date = new Date(timestamp);
    var month = date.getMonth() + 1;
    if (month < 10) month = '0' + month;
    var day = date.getDate();
    if (day < 10) day = '0' + day;
    var hours = date.getHours();
    if (hours < 10) hours = '0' + hours;
    var minutes = date.getMinutes();
    if (minutes < 10) minutes = '0' + minutes;
    var seconds = date.getSeconds();
    if (seconds < 10) seconds = '0' + seconds;

    if (withYear) {
        var year = date.getFullYear();
        return year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds;
    }
    return month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds;

}

var entityMap = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#39;',
  '/': '&#x2F;',
  '`': '&#x60;',
  '=': '&#x3D;'
};

function escapeHtml(string) {
  return String(string).replace(/[&<>"'`=\/]/g, function (s) {
    return entityMap[s];
  });
}