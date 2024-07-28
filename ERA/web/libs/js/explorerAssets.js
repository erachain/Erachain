function assets(data) {
    var output = '';
    var notDisplayPages = data.notDisplayPages;
    var numberShiftDelta = data.pageSize;
    output += lastBlock(data.lastBlock);
    output += `<p>${data.assets_list_tip} <a class="button ll-blue-bgc" href="https://wiki.erachain.org/ru/Assets" target="_blank">?</a></p>`;
    var start = data.start;

    output += '<table width="1280" border=0><tr><td align=left><br>';
    output += '<a href="?assets"' + get_lang() + '><h3 style="display:inline;">' + data.Label_Title + '</h3></a>';
    output += '<br>';
    for (var key in data.types_abbrevs) {
        output += ' &nbsp&nbsp<a href=?q=:' + data.types_abbrevs[key] + get_lang() + '&search=assets class="button ll-blue-bgc"<b>' + data.types_abbrevs[key] + '</b></a>';
    }
    output += '</table>';

    if (!notDisplayPages) {
        output += pagesComponent2(data);
    }

    output += '<table BORDER=0 cellpadding=10 cellspacing=0 ' +
        'class="tiny table-striped" style="max-width:1380px; font-size:1.2em; border: 1px solid #ddd;"><tr>';
    output += '<td width="50%"><b>' + data.Label_table_asset_name + ' / ' + data.Label_table_asset_key +
        '<td><b>' + data.Label_table_asset_type + ' / <b>' + data.Label_table_asset_maker;
    output += '<td><b>' + data.Label_table_asset_quantity + '<td><b>' + data.Label_table_asset_released
         + '<td><b>' + data.Label_table_asset_lastPrice
         + '<td><b>' + data.Label_table_asset_changePrice
         + '<td><b>' + data.Label_table_asset_marketCap;

    //Отображение таблицы элементов активов
    for (var i in data.pageItems) {
        var item = data.pageItems[i];
        output += '<tr>';
        output += '<td>';
        //output += makeMediaIcon(item, '', 'width:2em')
        output += '<div class="row" style="white-space:normal;"><div class="col-lg-2"><a href=?asset=' + item.key + get_lang() + '>'
            + makeMediaIcon(item, '', 'width: 4em; padding: 10px;') + '</a></div><div class="col-lg-10">';
        output += '<div class="row"><a href=?asset=' + item.key + get_lang() + '>' + cutBlank(escapeHtml(item.nameOrig), 70) + '</a></div>';
        output += '<div class="row" style="font-size:0.8em">' + item.key;

        if (item.tags) {
            for (var i in item.tags) {
                output += ' <a href=?q=' + item.tags[i] + '&lang=ru&search=assets>' + item.tags[i] + '</a> ';
            }
        }

        output += '</div></div></div>';
        output += '<td style="font-size:0.8em">' + item.assetTypeNameFull;

        output += '<br><a href=?address=' + item.maker + get_lang() + '>';
        if (item.hasOwnProperty('person'))
            output += '[' + item.person_key + ']' + cutBlank(escapeHtml(item.person), 25);
        else
            output += item.maker;
        output += '</a></td>';

        output += '<td>' + item.quantity;
        output += '<td>' + item.released;
        output += '<td>' + (item.price == 0? "--" : item.price.toPrecision(6));
        output += '<td>' + (item.changePrice == 0? "--" : item.changePrice.toPrecision(2));
        output += '<td>' + (item.marketCap == 0? "--" : item.marketCap.toPrecision(6));

    }

    if (!notDisplayPages) {
        output += '</table>';
        output += pagesComponent2(data);
    }
    return output;
}

function asset(data, forPrint) {

    var output = '';

    if (!forPrint) {
        output += lastBlock(data.lastBlock);
        output += `<p>${data.assets_list_tip} <a class="button ll-blue-bgc" href="https://wiki.erachain.org/ru/Assets" target="_blank">?</a></p>`;
    }

    if (!data.item) {
        output += '<h2>Not found</h2>';
        return output;
    }

    if (data.hasOwnProperty('error')) {
        output += '<br><h5>' + data.error + '</h5>';

        return output;
    }

    output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1300">';
    output += '<tr><td align=left>';
    output += '<table><tr style="vertical-align:top">';

    var item = data.item;
    ////// HEAD
    if (item.imageTypeName == 'audio') {
        output += itemHead(item, forPrint, item.maker_person_image_url, item.maker_person_image_media_type);
    } else {
        output += itemHead(item, forPrint);
    }

    //////// BODY
    output += '<p style="font-size:1.3em; margin-top:0.5em; margin-bottom:0px">';
    if (item.original) {
        output += '<b>' + item.Label_Original_Asset + ':</b> <a href="?asset=' + item.original.key + get_lang() + '">' + item.original.key + '</a><br>';
    }

    if (item.isUnique) {
        output += '<b>' + item.Label_Unique + '</b>';
        if (item.index) {
            output += ', <b>' + item.Label_Series + ' #' + item.total + '</b>';
        }
    } else {
        if (item.isUnlimited) {
            output += '<b>' + item.Label_Unlimited + '</b>';
        } else {
            output += item.Label_Quantity + ': <b>' + addCommas(item.quantity) + '</b>';
        }
        output += ', &nbsp&nbsp' + item.Label_Scale + ': <b>' + item.scale + '</b>';
    }
    output += ', &nbsp&nbsp' + item.Label_Released + ': <b>' + addCommas(item.released) + '</b>';

    if (!forPrint)
        output += ', &nbsp&nbsp<a href=?owners&asset=' + item.key + get_lang() + ' class="button ll-blue-bgc"><b>' + item.Label_Holders + '</b></a>';

    output += '<br>' + item.Label_AssetType + ': ';
    if (forPrint) {
        output += '<b>' + item.assetTypeNameFull + '</b><br>';
    } else {
        output += '<a href=?q=%3A' + item.type_abbrev + get_lang() + '&search=assets ><b>' + item.assetTypeNameFull + '</b></a><br>';
    }

    if (item.properties) {
        output += '</p><p style="margin-bottom:0px">';
        output += '<b>' + item.Label_Properties + '</b>: ' + item.properties + '</p>';
    }

    output += '<p style="margin-bottom:0px"><b>' + item.Label_AssetType_Desc + '</b>: ' + item.assetTypeDesc + '</p>';

    if (item.DEXAwards) {
        output += '<p style="margin-bottom:0px"><b>' + item.Label_DEX_Awards + '</b>:<br>';
        for (key in item.DEXAwards) {
            output += '&nbsp;&nbsp;&nbsp;&nbsp;';
            if (forPrint) {
                output += item.DEXAwards[key].address;
            } else {
                output += '<a href ="?address=' + item.DEXAwards[key].address + get_lang() + '">' + item.DEXAwards[key].address + '</a>';
            }
            output += ' <b>x' + item.DEXAwards[key].value1 * 0.001 + '%</b>';
            if (item.DEXAwards[key].memo) {
                output += ' - ' + item.DEXAwards[key].memo;
            }
            output += '<br>';
        }
        output += '</p>';
    }

    output += itemFoot(item, forPrint);

    if (forPrint)
        return output;

    output += '<br>';

    output += '</td>';
    output += '</tr>';
    output += '</table>';

    output += '<h3>' + item.Label_Available_pairs + '</h3>';

    output += '<table border="0" cellspacing="10" class="tiny table table-striped" style="border: 1px solid #ddd;"><tr>';
    output += '<td><b>' + item.Label_Asset + '<td><b>' + data.Label_Last_Price + '</b></td>';
    output += '<td><b>' + data.Label_Price_Change + '<br>' + data.Label_Trades_Count;
    output += '<td><b>' + data.Label_Bit_Ask;
    output += '<td><b>' + data.Label_Volume24;
    output += '<td><b>' + data.Label_Price_Low_High + '</b></td></tr>';

    var totalOpenOrdersCount = 0;
    var totalTradeVolume = 0.0;
    for (key in data.pairs) {
        var pair = data.pairs[key];
        totalOpenOrdersCount += pair.count_24h;
        totalTradeVolume += pair.base_volume;

        output += '<tr>';

        output += '<td><b>';
        output += '<a href="?asset=' + pair.quote_id + get_lang() + '">';
        output += getItemName2(1000, pair.quote_id, pair.quote_name);


        output += '<td><a href="?asset=' + pair.base_id + '&asset=' + pair.quote_id  + get_lang() + '"><b>'
                + addCommas(pair.last_price.toPrecision(6)) + '</a><br>';
        output += '<a href="?asset=' + pair.quote_id + '&asset=' + pair.base_id  + get_lang() + '"><b>'
                + addCommas((1.0 / pair.last_price).toPrecision(6));

        output += '<td>';
        if (pair.price_change_percent_24h > 0) {
            output += '<span style="color:green"><b>+' + pair.price_change_percent_24h.toPrecision(3) + '</b></span>';
        } else if (pair.price_change_percent_24h < 0) {
            output += '<span style="color:red"><b>' + pair.price_change_percent_24h.toPrecision(3) + '</b></span>';
        } else {
            output += '0';
        }
        output += '<br>' + pair.count_24h;

        output += '<td>';
        output += addCommas(pair.highest_bid.toPrecision(6)) + ' / ' + addCommas(pair.lowest_ask.toPrecision(6));
        output += '<br>' + addCommas((1.0 / pair.lowest_ask).toPrecision(6)) + ' / ' + addCommas((1.0 / pair.highest_bid).toPrecision(6));

        output += '<td nowrap>';
        output += addCommas(pair.quote_volume.toPrecision(6)) + '<br>' + addCommas(pair.base_volume.toPrecision(6));

        output += '<td>';
        output += addCommas(pair.lowest_price_24h.toPrecision(6)) + ' / ' + addCommas(pair.highest_price_24h.toPrecision(6));
        output += '<br>' + addCommas((1.0 / pair.highest_price_24h).toPrecision(6)) + ' / ' + addCommas((1.0 / pair.lowest_price_24h).toPrecision(6));

    }
    output += '<tr><td><b>' + data.Label_Total + ':';
    output += '<td><td><b>' + totalOpenOrdersCount;
    output += '<td><td><b>' + totalTradeVolume;
    output += '<td></td></tr></table>';


    return output;
}

function owners(data) {

    var output = "";

    output += lastBlock(data.lastBlock);

    output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="900">';
    output += '<tr><td align=center>';

    output += '<h3>' + data.Label_Title + '</h3>';

    if (data.pageFromAddressKey) {
        var parsAdd = {'pageFromAddressKey': data.pageFromAddressKey};
        output += pagesComponent2(data, parsAdd);
    } else {
        output += pagesComponent2(data);
    }

    var table = '<table id=owners BORDER=0  cellpadding=10 cellspacing=0 class="tiny table table-striped" style="border: 1px solid #ddd; width: auto;"><tr><td><b>' + data.Label_Table_Account + '<td><b>' + data.Label_Table_person + '<td><b>' + data.Label_Balance_1 + '<td><b>' + data.Label_Balance_2 + '<td><b>' + data.Label_Balance_3 + '<td><b>' + data.Label_Balance_4
      + '<td><b>' + data.Label_Balance_5 + '<td><b>%';

    var totalReleased = data.assetRealeased;
    for (key in data.page) {
        var item = data.page[key];
        table += '<tr>';
        table += '<td>' + '<a href="?address=' + item[0] + get_lang() + '">' + item[0] + '</a>';

        if (item.length > 6) {
            table += '<td><a href="?person=' + item[6] + get_lang() + '">' + item[7] + '</a>';
        } else {
            table += '<td>';
        }
        table += '<td>' + addCommas(item[1]);
        table += '<td>' + addCommas(item[2]);
        table += '<td>' + addCommas(item[3]);
        table += '<td>' + addCommas(item[4]);
        table += '<td>' + addCommas(item[5]);

        table += '<td>' + ((item[1] / totalReleased) * 100).toFixed(5) + "%";

    }
    table += '</table>';

    output += table;

    if (data.pageFromAddressKey) {
        output += pagesComponent2(data, parsAdd);
    } else {
        output += pagesComponent2(data);
    }

    return output;
}
