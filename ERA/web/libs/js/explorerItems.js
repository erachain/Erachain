function itemHead(item, forPrint) {

    var output = '';
    var type = item.item_type;

    if (item.image) {
        output += '<td><a href="#" ><img src="data:image/gif;base64,' + item.image + '" width = "350" /></a></td><td style ="width: 70%; padding-left:20px">';
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

    output += '</h3>';
    if (item.hasOwnProperty('exLink')) {
        output += '<h3>'
            + '<img src="img/parentTx.png" style="height:1.5em"> ' + item.exLink_Name + ' '
            + item.Label_Parent + ' <a href=?tx=' + item.exLink.ref + get_lang() + '><b>' + item.exLink.ref + '</b></a></h3>';
    }

    output += '<h4>';

    if (forPrint)
        output += item.Label_Number + ':<b> ' + item.key + '</b>';
    else
        output += '[ <input id="key1" name="' + type + '" size="8" type="text" value="' + item.key + '" class="" style="font-size: 1em;"'
                       + ' onkeydown="if (event.keyCode == 13) buttonSearch(this)"> ]';

    output += ', &nbsp' + item.Label_DateIssue + ':<b> ' + convertTimestamp(item.block_timestamp, true) + '</b></h4>';

    output += '<h4>' + item.Label_Owner + ': ';
    if (item.owner_person) {
        if (forPrint)
            output += '<b>' + item.owner_person + ' (' + item.creator + ')</b></h4>';
        else
            output += '<a href ="?address=' + item.owner + get_lang() + '"><b> ' + item.owner_person + '</b></a></h4>';
    } else {
        if (forPrint)
            output += '<b>' + item.owner + '</b></h4>';
        else
            output += '<a href ="?address=' + item.owner + get_lang() + '"><b> ' + item.owner + '</b></a></h4>';
    }

    if (item.tx_seqNo) {
        output +=  '<h4>' + item.Label_TXIssue;
        var creator;
        if (item.tx_creator_person) {
            if (forPrint)
                creator = '<b>' + item.tx_creator_person + ' (' + item.tx_creator + ')</b></h4>';
            else
                creator = '<a href ="?address=' + item.tx_creator + get_lang() + '"><b> ' + item.tx_creator_person + '</b></a></h4>';
        } else {
            if (forPrint)
                creator = '<b>' + item.tx_creator + '</b></h4>';
            else
                creator = '<a href ="?address=' + item.tx_creator + get_lang() + '"><b> ' + item.tx_creator + '</b></a></h4>';
        }
        if (forPrint) {
            output += ': <b> ' + item.tx_seqNo + '</b></h4>';
            output += '&nbsp&nbsp&nbsp&nbsp' + item.Label_Pubkey + ':<b> ' + item.tx_creator_pubkey + '</b><br>';
            output += '&nbsp&nbsp&nbsp&nbsp' + item.Label_Signature + ':<b> ' + item.tx_signature + '</b><br>';
            output += '&nbsp&nbsp&nbsp&nbsp' + item.Label_TXCreator + ':<b> ' + creator + '</b><br>';
        } else {
            output += ': <a href=?tx=' + item.tx_seqNo + get_lang() + ' class="button ll-blue-bgc"><b>' + item.tx_seqNo + '</b></a></h4>';
            output += '&nbsp&nbsp&nbsp&nbsp' + item.Label_Pubkey + ':<b> ' + item.tx_creator_pubkey + '</b><br>';
            output += '&nbsp&nbsp&nbsp&nbsp' + item.Label_Signature + ':<b> ' + item.reference + '</b><br>';
            output += '&nbsp&nbsp&nbsp&nbsp' + item.Label_TXCreator + ':<b> ' + creator + '</b><br>';

            output += '<a href=?q=' + item.charKey + get_lang() + '&search=transactions class="button ll-blue-bgc"><b>' + item.Label_Actions + '</b></a>';
        }
    }
    if (!forPrint) {
        output += ' &nbsp&nbsp<a href=../api'+ type + '/raw/' + item.key + ' class="button ll-blue-bgc"><b>' + item.Label_RAW + '</b></a>';
        output += ' &nbsp&nbsp<a href=?'+ type + '=' + item.key + get_lang() + '&print class="button ll-blue-bgc"><b>' + item.Label_Print + '</b></a></h4>';
        output += ' &nbsp&nbsp<a href=../api'+ type + '/text/' + item.key + ' class="button ll-blue-bgc"><b>' + item.Label_SourceText + '</b></a></h4>';
    }

    return output;

}

function itemFoot(item, forPrint) {
    var type = item.item_type;

    var output = '';
    if (item.description)
        output += '<h3>' + item.Label_Description + '</h3><br>' + fformat(item.description);

    if (item.hasOwnProperty('vouches')) {
        output += '<hr>' + item.vouches;
    }

    if (item.hasOwnProperty('links')) {
        output += '<hr>' + item.links;
    }

    return output;
}