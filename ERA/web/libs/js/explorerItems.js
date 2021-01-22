function itemHead(item, forPrint, type) {

    var output = '';

    if (item.image && item.image.length > 0) {
        output += '<td><img src="data:image/gif;base64,' + item.image + '" width = "350" /></td><td style ="padding-left:20px">';
        output += '<br>';
    }

    output += '<h3 style="display:inline;">';

    if (!print)
        output += '<a href="?' + type + '=' + item.key + get_lang() + '">';

    if (item.icon && item.icon.length > 0)
        output += ' <img src="data:image/gif;base64,' + item.icon + '" style="width:50px;" /> ';

    output += item.name;

    if (!print)
        output += '</a>';

    output += '</h3><h4>';

    output += '[ <input id="key1" name="' + type + '" size="8" type="text" value="' + item.key + '" class="" style="font-size: 1em;"'
                   + ' onkeydown="if (event.keyCode == 13) buttonSearch(this)"> ] ';


    if (item.hasOwnProperty('seqNo')) {
        if (print)
            output += item.Label_key + ':<b> ' + item.key + '</b><br>' + item.Label_TXIssue + ':<b> ' + item.seqNo + '</b>';
        else {
            output += '<a href=?tx=' + item.seqNo + get_lang() + ' class="button ll-blue-bgc"><b>' + item.seqNo + '</b></a>';
            output += ' ' +'<a href=?q=' + item.charKey + get_lang() + '&search=transactions class="button ll-blue-bgc"><b>' + item.label_Actions + '</b></a>';
            output += ' ' +'<a href=../api'+ type + '/raw/' + item.key + ' class="button ll-blue-bgc"><b>' + item.label_RAW + '</b></a>';
            output += ' ' + '<a href=?'+ type + '=' + item.key + get_lang() + '&print class="button ll-blue-bgc"><b>' + item.label_Print + '</b></a></h4>';

            output += '<br>';

            output += '<b>' + item.label_Creator + ':</b> <a href=?address=' + item.owner + get_lang() + '>' + item.owner + '</a>';

            output += '<br>';
        }
    } else {
        output += '</h4>';
    }

    return output;

}

function itemFoot(item, forPrint, type) {
    var output = '<b>' + item.label_Description + ':</b> ' + fformat(item.description);
    return output;
}