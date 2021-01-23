function itemHead(item, forPrint, type) {

    var output = '';

    if (item.image) {
        output += '<td><img src="data:image/gif;base64,' + item.image + '" width = "350" /></td><td style ="width: 70%; padding-left:20px">';
        output += '<br>';
    }

    output += '<h3 style="display:inline;">';

    if (!forPrint)
        output += '<a href="?' + type + '=' + item.key + get_lang() + '">';

    if (item.icon)
        output += ' <img src="data:image/gif;base64,' + item.icon + '" style="width:50px;" /> ';

    output += item.name;

    if (!forPrint)
        output += '</a>';

    output += '</h3><h4>';

    if (!forPrint)
        output += '[ <input id="key1" name="' + type + '" size="8" type="text" value="' + item.key + '" class="" style="font-size: 1em;"'
                       + ' onkeydown="if (event.keyCode == 13) buttonSearch(this)"> ] ';

    if (item.hasOwnProperty('seqNo')) {
        if (forPrint) {
            output += item.Label_Number + ':<b> ' + item.key + '</b><br>' + item.Label_TXIssue + ':<b> '
             + item.seqNo + '</b>, ' + item.Label_DateIssue + ':<b> ' + convertTimestamp(item.blk_timestamp, true);
            output += '</h4>' + item.Label_IssueReference + ':<b> ' + item.reference + '</b>';
        } else {
            output += ', ' + item.Label_DateIssue + ':<b> ' + convertTimestamp(item.blk_timestamp, true);
            output += '</h4>' + item.Label_IssueReference + ':<b> ' + item.reference + '</b>';
            output += '<h4><a href=?tx=' + item.seqNo + get_lang() + ' class="button ll-blue-bgc"><b>' + item.seqNo + '</b></a>';
            output += ' <a href=?q=' + item.charKey + get_lang() + '&search=transactions class="button ll-blue-bgc"><b>' + item.label_Actions + '</b></a>';
            output += ' <a href=../api'+ type + '/raw/' + item.key + ' class="button ll-blue-bgc"><b>' + item.label_RAW + '</b></a>';
            output += ' <a href=?top=all&'+ type + '=' + item.key + get_lang() + ' class="button ll-blue-bgc"><b>' + item.label_Holders + '</b></a>';
            output += ' <a href=?'+ type + '=' + item.key + get_lang() + '&print class="button ll-blue-bgc"><b>' + item.Label_Print + '</b></a></h4>';
            output += '</h4><br>';
        }
    }


    output += '<h4>' + item.Label_Creator + ': &nbsp&nbsp<b> ';
    if (item.owner_person) {
        if (forPrint)
            output += item.owner_person + ' (' + item.creator + ')</b></h4>';
        else
            output += '<a href ="?address=' + item.owner + get_lang() + '"><b> ' + item.owner_person + '</b></a></h4>';
    } else {
        if (forPrint)
            output += item.owner + '</b></h4>';
        else
            output += '<a href ="?address=' + item.owner + get_lang() + '"><b> ' + item.owner + '</b></a></h4>';
    }

    return output;

}

function itemFoot(item, forPrint, type) {
    var output = '';
    if (item.description)
        output += '<br><b>' + item.Label_Description + ':<br>' + fformat(item.description);

    return output;
}