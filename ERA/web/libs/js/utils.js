// select view format 
function preFormat(text){

if (text == null || text.length == 0) return "";
text = text.toString();
if (text.length <5) return text;

var pref1 = text.substring(0,1);
var pref2 = text.substring(1,2);

if (pref1 =="<"){
// return HTML
if (pref2 =="\n"){
return text.substring(2);
}
return text;
} else if (pref1=="#"){
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

function fformat(text_in) {
    if (text_in.startsWith('@TGM{')) {
        return telegramView(text_in, JSON.parse(text_in.substring(4)));
    }

    var preText = preFormat(text_in)
    preText = preText.replace(/@@(\d+-\d+)/g, '<a href=?tx=$1>@@$1</a>')
    preText = preText.replace(/@@A(\d+)/g, '<a href=?asset=$1>@@A$1</a>')
    preText = preText.replace(/@@I(\d+)/g, '<a href=?imprint=$1>@@I$1</a>')
    preText = preText.replace(/@@P(\d+)/g, '<a href=?person=$1>@@P$1</a>')
    preText = preText.replace(/@@S(\d+)/g, '<a href=?status=$1>@@S$1</a>')
    preText = preText.replace(/@@T(\d+)/g, '<a href=?template=$1>@@T$1</a>')
    preText = preText.replace(/@@V(\d+)/g, '<a href=?poll=$1>@@V$1</a>')

    return preText
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

function cut(string, max) {
    if (string.length > max)
        return string.substring(0,max) + '.';

    return string;
}

function cutBlank(string, max) {
    if (string.length > max) {
        var words = string.split(' ');
        var result = "";
        for (index in words) {
            if (index == 0)
                result += words[0];
            else
                if (result.length > max / 3)
                    if (words[index].length > 4)
                        result += words[index].substring(0,3) + '.';
                    else
                        result += words[index];
                else
                    result += words[index];

            if (result.length > max) break;

            result += ' ';
        }

        return result;
    }

    return string;
}

/**
 * http://stackoverflow.com/a/10997390/11236
 */
function updateURLParameter(url, param, paramVal){
    var newAdditionalURL = "";
    var tempArray = url.split("?");
    var baseURL = tempArray[0];
    var additionalURL = tempArray[1];
    var temp = "";
    if (additionalURL) {
        tempArray = additionalURL.split("&");
        for (var i=0; i<tempArray.length; i++){
            if(tempArray[i].split('=')[0] != param){
                newAdditionalURL += temp + tempArray[i];
                temp = "&";
            }
        }
    }

    var rows_txt = temp + "" + param + "=" + paramVal;
    return baseURL + "?" + newAdditionalURL + rows_txt;
}

function showWindowHtml() {
  var holder = document.getElementById('image-holder');
  //holder.src = source;
  holder.style.display = 'block';
  holder.style.resizable = 1;
}

function showWindowImage(source) {
  var img = document.getElementById('image-holder');
  img.src = source;
  img.style.display = 'block';
  img.style.resizable = 1;
}

function showWindowVideo(source) {
  var video = document.getElementById('video-holder');
  video.style.display = 'block';
  video.style.resizable = 1;
  video.src = source;
  video.autoplay = 1;
  video.playsinline = 1;
  video.loop = 1;
}


function makeMediaIcon(item, class1, style1) {

    var source;
    if (item.iconURL) {
        source = item.iconURL;
        if (item.iconMediaType.startsWith('video')) {
            return '<video muted src="' + source + '" autoplay autoplay loop class="' + class1 + '" style="' + style1 + '"></video>';
        }
        return '<img src="' + source + '" class="' + class1 + '" style="' + style1+ '" /> ';
    }

    return '';

}

function makeMediaImage(item, class1, style1) {

    var source;
    if (item.imageURL) {
        source = item.imageURL;
        if (item.imageMediaType.startsWith('video')) {
            return '<video muted src="' + source + '" autoplay playsinline loop class="' + class1 + '" style="' + style1 + '"></video>';
        }
        return '<img src="' + source + '" class="' + class1 + '" style="' + style1 + '" /> ';
    }

    return '';

}