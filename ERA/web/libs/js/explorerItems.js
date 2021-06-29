function itemHead(item, forPrint, imageFaceURL, imageFaceType) {

    var output = '<td>';
    var type = item.item_type;

    var source;

    // FACE - for AUDIO
    if (imageFaceURL) {
        if (imageFaceType == 'video') {
            output += '<video autoplay muted playsinline loop width="350"><source src="' + imageFaceURL + '"></video>';

        } else {
            output += '<img width="350" src="' + imageFaceURL + '" />';
        }


    }

    if (item.image) {
        source = 'data:image/gif;base64,' + item.image;
    } else if (item.imageURL) {
        source = item.imageURL;
    } else if (item.imageTypeName == 'video') {
        source = '/api' + item.item_type + '/image/' + item.key;
    }

    if (source) {
        if (item.imageTypeName == 'video') {
            output += '<video style="display:none;" onclick="style.display=\'none\';this.stop()" id="video-holder" loop controls ></video>';
            output += '<video autoplay muted playsinline loop width="350" onclick="this.pause();showWindowVideo(\'' + source + '\')"><source src="' + source + '"></video>';

        } else if (item.imageTypeName == 'audio') {
            //output += '<video controls="" autoplay="" name="media"><source src="' + source + '" type="audio/mp3">';
            //output += '<source src="' + item.maker_person_image_url + '" type="' + item.maker_person_image_media_type + '"></video>';
            output += '<audio controls autoplay loop><source src="' + source + '" type="audio/mp3"></audio>';

        } else {
            output += '<img id="image-holder" onclick="style.display=\'none\'">';
            output += '<a href="#" onclick="showWindowImage(\'' + source + '\')" ><img width="350" src="' + source + '" /></a>';
        }

        output += '</td><td style ="width: 70%; padding-left:20px"><br>';

    }


    output += '<h3 style="display:inline;">';

    if (!forPrint)
        output += '<a href="?' + type + '=' + item.key + get_lang() + '">';

    output += makeMediaIcon(item, '', 'width:50px');

    output += item.name;

    if (!forPrint)
        output += '</a>';

    output += '</h3>';
    if (item.hasOwnProperty('tags')) {
        output += '<br>' + item.tags;
    }

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

    output += '<h4>' + item.Label_Maker + ': ';
    if (item.maker_person) {
        if (forPrint)
            output += '<b>' + item.maker_person + ' (' + item.creator + ')</b></h4>';
        else
            output += '<a href ="?address=' + item.maker + get_lang() + '"><b> ' + item.maker_person + '</b></a></h4>';
    } else {
        if (forPrint)
            output += '<b>' + item.maker + '</b></h4>';
        else
            output += '<a href ="?address=' + item.maker + get_lang() + '"><b> ' + item.maker + '</b></a></h4>';
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

            output += '&nbsp&nbsp&nbsp&nbsp' + item.Label_Pubkey + ': '
             + '<a href ="?address=' + item.tx_creator_pubkey + get_lang() + '"><b> ' + item.tx_creator_pubkey + '</b></a><br>';

            output += '&nbsp&nbsp&nbsp&nbsp' + item.Label_Signature + ': '
             + '<a href ="?tx=' + item.tx_signature + get_lang() + '"><b>' + item.tx_signature + '</b></a><br>';

            output += '&nbsp&nbsp&nbsp&nbsp' + item.Label_TXCreator + ':<b> ' + creator + '</b><br>';

            output += '<a href=?q=' + item.charKey + get_lang() + '&search=transactions class="button ll-blue-bgc"><b>' + item.Label_Actions + '</b></a>';
        }
    }
    if (!forPrint) {
        output += ' &nbsp&nbsp<a href=../api'+ type + '/raw/' + item.key + ' class="button ll-blue-bgc"><b>' + item.Label_RAW + '</b></a>';
        output += ' &nbsp&nbsp<a href=?'+ type + '=' + item.key + get_lang() + '&print class="button ll-blue-bgc"><b>' + item.Label_Print + '</b></a></h4>';
    }

    return output;

}

function itemFoot(item, forPrint) {
    var type = item.item_type;

    var output = '';
    if (item.description) {
        output += '<h3>' + item.Label_Description;
        if (!forPrint)
            output += ' &nbsp&nbsp<a href=../api'+ type + '/text/' + item.key + ' class="tiny button ll-blue-bgc" style="font-size:0.7em"><b>' + item.Label_SourceText + '</b></a>';
        output += '</h3><br>' + fformat(item.description);
    }

    if (item.hasOwnProperty('vouches')) {
        output += '<hr>' + item.vouches;
    }

    if (item.hasOwnProperty('links')) {
        output += '<hr>' + item.links;
    }

    return output;
}

function getItemNameMini(item) {
    return getItemNameMini(item.itemType, item.key, item.name);
}

function getItemNameMini(itemType, itemKey, itemName) {
    return '<abbr title="' + '[' + itemKey + '] ' + itemName + '"><a  href=?' + itemType + '=' + itemKey + get_lang() + ' ><font size=-2 color=black>' + itemName + '</font></a></abbr>';
}

function getItemNameMiniGrey(item) {
    return '<abbr title="' + '[' + item.key + '] ' + item.name + '"><a class=without href=?' + item.itemType + '=' + item.key + get_lang() + '><font size=-2 color=#e0e0e0>' + item.name + '</font></a></abbr>';
}

function getItemURL(item, imgSize) {
    var output = '<a href="?' + item.itemType + '=' + item.key + get_lang() + '">';
    if (item.key > item.startKey)
        output += '<b>[' + item.key + ']</b>';

    output += makeMediaIcon(item, '', 'size:' + imgSize);

    output += '<b>' +  escapeHtml(name) + '</b></a>';

    return output;
}

function getShortNameBlanked(name) {

    var shortName = escapeHtml(name).split(' ')[0];
    if (shortName.length > 12)
        shortName = shortName.substr(0, 12) + '.';

    return shortName;
}

function getShortItemURL(item, class1, style1) {
    var output = '<a href="?' + item.itemType + '=' + item.key + get_lang() + '">';
    if (item.key > item.startKey)
        output += '<b>[' + item.key + ']</b>';

    output += makeMediaIcon(item, class1, style1);

    var shortName = escapeHtml(item.name).split(' ')[0];
    if (shortName.length > 10)
        shortName = shortName.substr(0, 10);

    output += '<b>' +  shortName + '</b></a>';

    return output;
}

function getItemName(item) {
    return getItemName(item.startKey, item.key, item.name);
}
function getItemName(startKey, key, name) {
    if (key < startKey)
        return escapeHtml(name);

    return '[' + key + '] ' + escapeHtml(name);
}

function getItemName2(item) {
    return getItemName2(item.startKey, item.key, item.name);
}
function getItemName2(startKey, key, name) {
    if (key < startKey)
        return escapeHtml(name);

    return '[' + key + '] ' + escapeHtml(name);
}