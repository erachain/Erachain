function assets(data) {
    var output = '';
    var notDisplayPages = data.notDisplayPages;
    var numberShiftDelta = data.pageSize;
    output += lastBlock(data.lastBlock);
    var start = data.start;
    if (!notDisplayPages) {
        //output += pagesComponentBeauty(start, data.label_Assets, data.lastNumber, data.pageSize, 'start');
        output += pagesComponent2(data);
    }


    output += '<table width="1280" border=0><tr><td align=left><br>';
    output += '<a href="?assets"' + get_lang() + '><h3 style="display:inline;">' + data.label_Title + '</h3></a>';
    output += '<br><br>';

    output += '<table width=80% BORDER=0 cellpadding=10 cellspacing=0 ' +
        'class="tiny table table-striped" style="border: 1px solid #ddd;"><tr>';
    output += '<td><b>' + data.label_table_asset_key + ': <b>' + data.label_table_asset_name +
        '<td><b>' + data.label_table_asset_type + '<td><b>' + data.label_table_asset_owner;
    output += '<td><b>' + data.label_table_asset_orders + '<td><b>' + data.label_table_asset_amount
         + '<td><b>' + data.label_table_asset_scale;

    //Отображение таблицы элементов активов
    //var length = Object.keys(data.pageItems).length;
    //for (var i = 0; i < length - 1; i++) {
    for (var i in data.pageItems) {
        var item = data.pageItems[i];
        output += '<tr>';
        output += '<td> <a href=?asset=' + item.key + get_lang() + '>';
        output += '<b>' + item.key + '</b>: ';
        if (item.icon.length > 0)
            output += '<img src="data:image/gif;base64,' + item.icon + '"  style="width:2em;" /> ';

        output += escapeHtml(item.name);
        output += '</a>';
        output += '<td>' + item.assetTypeNameFull;
        ////output += '<td>' + escapeHtml(item.description.substr(0, 60));

        output += '<td><a href=?address=' + item.owner + get_lang() + '>';
        if (item.hasOwnProperty('person'))
            output += '[' + item.person_key + ']' + escapeHtml(item.person);
        else
            output += item.owner;
        output += '</a></td>';

        output += '<td>' + item.orders;
        output += '<td>' + item.quantity;
        output += '<td>' + item.scale;

    }
    if (!notDisplayPages) {
        output += '</table></td></tr></table>';
        output += pagesComponent2(data);
    }
    return output;
}

function asset(data, print) {

    var output = '';
    if (!print)
        output += lastBlock(data.lastBlock);

    if (!data.asset.hasOwnProperty('this')) {
        output += '<h2>Not found</h2>';
        return output;
    }

    if (data.hasOwnProperty('error')) {
        output += '<br><h5>' + data.error + '</h5>';

        return output;
    }

    output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1180">';
    output += '<tr><td align=left>';
    output += '<table><tr style="vertical-align:top">';


    if (data.asset.this.image && data.asset.this.image.length > 0) {
        output += '<td><img src="data:image/gif;base64,' + data.asset.this.image + '" width = "350" /></td><td style ="padding-left:20px">';
        output += '<br>';
    }

    output += '<h3 style="display:inline;">';

    if (!print)
        output += '<a href="?asset=' + data.asset.this.key + get_lang() + '">';

    if (data.asset.this.icon && data.asset.this.icon.length > 0)
        output += ' <img src="data:image/gif;base64,' + data.asset.this.icon + '" style="width:50px;" /> ';

    output += data.asset.this.name;

    if (!print)
        output += '</a>';

    output += '</h3><h4>';

    output += '[ <input id="key1" name="asset" size="8" type="text" value="' + data.asset.this.key + '" class="" style="font-size: 1em;"'
                   + ' onkeydown="if (event.keyCode == 13) buttonSearch(this)"> ] ';


    if (print)
        output += data.Label_key + ':<b> ' + data.asset.this.key + '</b><br>' + data.Label_TXIssue + ':<b> ' + data.asset.this.seqNo + '</b>';
    else {
        if (data.asset.this.hasOwnProperty('seqNo')) {
            output += '<a href=?tx=' + data.asset.this.seqNo + get_lang() + ' class="button ll-blue-bgc"><b>' + data.asset.this.seqNo + '</b></a>';
            output += ' ' +'<a href=?q=' + data.charKey + get_lang() + '&search=transactions class="button ll-blue-bgc"><b>' + data.label_Actions + '</b></a>';
            output += ' ' +'<a href=../apiasset/raw/' + data.asset.this.key + ' class="button ll-blue-bgc"><b>' + data.label_RAW + '</b></a>';
            output += ' ' + '<a href=?asset=' + data.asset.this.key + get_lang() + '&print class="button ll-blue-bgc"><b>' + data.label_Print + '</b></a></h4>';

            output += '<br>';

            output += '<b>' + data.asset.label_Creator + ':</b> <a href=?address=' + data.asset.this.owner + get_lang() + '>' + data.asset.this.owner + '</a>';

            output += '<br>';
        } else {
            output += '</h4>';
        }
    }

    output += data.asset.label_AssetType + ': <b>' + data.asset.this.assetTypeChar + '</b>: ' + data.asset.this.assetTypeFull + '<br>';
    output += data.asset.label_AssetType_Desc + ': ' + fformat(data.asset.this.assetTypeDesc) + '<br>';

    output += data.asset.label_Quantity + ': <b>' + addCommas(data.asset.this.quantity) + '</b>';
    output += ', ' + data.asset.label_Scale + ': <b>' + data.asset.this.scale + '</b>';
    output += ', ' + data.asset.label_Released + ': <b>' + addCommas(data.asset.this.released) + '</b>';

    if (print)
        return output;

    if (data.asset.this.key != 0) {
        assetUrl = '&asset=' + data.asset.this.key;
    } else {
        assetUrl = '';
    }

    output += ' , <a href=?top=all' + assetUrl + get_lang() + ' class="button ll-blue-bgc"><b>' + data.asset.label_Holders + '</b></a>';
    output += '<br>';

    output += '<b>' + data.asset.label_Description + ':</b> ' + fformat(data.asset.this.description);

    output += '</td>';
    output += '</tr>';
    output += '</table>';

    output += '<h3>' + data.asset.label_Available_pairs + '</h3>';

    output += '<table border="0" cellspacing="10" class="tiny table table-striped" style="border: 1px solid #ddd;"><tr>';
    output += '<td><b>' + data.asset.label_Pair + '</b></td><td><b>' + data.asset.label_Orders_Count + '</b></td>';
    output += '<td><b>' + data.asset.label_Open_Orders_Volume + '</b></td>';
    output += '<td><b>' + data.asset.label_Trades_Count + '</b></td><td><b>' + data.asset.label_Trades_Volume + '</b></td>';
    output += '<td><b>' + data.asset.label_Description + '</b></td></tr>';

    for (key in data.asset.pairs) {
        output += '<tr>';

        output += '<td><b>';
        output += '<a href="?asset=' + key + get_lang() + '">';
        output += getAssetName2(key, data.asset.pairs[key].assetName);

        output += '<td>' + data.asset.pairs[key].openOrdersCount;

        output += '<td nowrap>';
        output += '<a href="?asset=' + data.asset.this.key + '&asset=' + key + get_lang() + '"><b>'
        output += addCommas(data.asset.pairs[key].last);
        output += '</b></a> / <a href="?asset=' + key + '&asset=' + data.asset.this.key + get_lang() + '"><b>'
        output += addCommas(data.asset.pairs[key].lastReverse) + '</b></a><br>';

        output += addCommas(data.asset.pairs[key].ordersPriceVolume) + ' / ' + addCommas(data.asset.pairs[key].ordersAmountVolume);

        output += '<td>' + data.asset.pairs[key].tradesCount;

        output += '<td nowrap>';
        output += addCommas(data.asset.pairs[key].tradeAmountVolume) + ' ' + getAssetNameMini(key, data.asset.pairs[key].assetName);
        output += '<br>' + addCommas(data.asset.pairs[key].tradesPriceVolume) + ' ' + getAssetNameMini(data.asset.this.key, data.asset.this.name);

        output += '<td>' + escapeHtml(data.asset.pairs[key].description.substr(0, 100));
    }
    output += '<tr><td><b>' + data.asset.label_Total + ':';
    output += '<td>' + data.asset.totalOpenOrdersCount;
    output += '<td>' + addCommas(data.asset.totalOrdersVolume) + ' ' + getAssetNameMini(data.asset.this.key, data.asset.this.name);
    output += '<td>' + data.asset.totalTradesCount;
    output += '<td>' + addCommas(data.asset.totalTradesVolume) + ' ' + getAssetNameMini(data.asset.this.key, data.asset.this.name);
    output += '<td></td></tr></table>';


    return output;
}

function trades(data) {
    var output = "";

    output += lastBlock(data.lastBlock);

    output += '';

    output += '<h3 style="display:inline;"><a href="?asset=' + data.assetWant + '&asset=' + data.assetHave + get_lang()
        + '"><img src="img/exchange.png" style="width:1em"></a> '
        + data.label_Trades + '</h3> ';

    output += '<a href="?asset=' + data.assetHave + '&asset=' + data.assetWant + get_lang() + '"><h3 style="display:inline;">';
    output += getAssetName2(data.assetHave, data.assetHaveName) + ' / ';
    output += getAssetName2(data.assetWant, data.assetWantName) + '</h3></a>';

    output += '<br>';

    output +='<div><div class="col-lg-5" style="padding-left:5em;">';
    output += '<h4>' + data.label_Orders + '</h4>';

    output += '<table border="0" cellspacing="3" cellpadding="5" class="tiny table table-striped"'
        + 'style="width:100%; border: 1px solid #ddd; margin-bottom: 0px;">';

    var averageVolume = 1 * data.sellsSumAmountGood;
    if (1 * data.buysSumAmountGood > averageVolume)
        averageVolume = 1 * data.buysSumAmountGood;

    var width = data.sellsSumAmountGood;

    for (key in data.sells) {

        output += '<tr style="background-color: transparent">';

        var widthLocal = width;
        if (widthLocal > averageVolume) {
            widthLocal = averageVolume;
        }

        output += '<td style="position: relative">';
        output += '<span style="z-index: -1; width:' + 250 * widthLocal / averageVolume
                + '%; position:absolute; background-color:#ffe4e4; top: 0; bottom: 0; left: 0;"></span>';

        width -= data.sells[key].amount;

        output += '<span><a href ="?address=' + data.sells[key].creator_addr + get_lang() + '">' + cutBlank(data.sells[key].creator, 20) + '</a></span>';
        output += '<td><a href=?tx=' + key + get_lang() + '><b>' + addCommas(data.sells[key].price) + '</b</a>';
        output += '<td align=right><a href=?tx=' + key + get_lang() + '>' + addCommas(data.sells[key].amount) + '</a></tr>';

    }

    output += '<tr bgcolor="#f9f9f9">';
    output += '<td><td>' + data.label_Total_For_Sell;
    ///output += '<td><b>' + addCommas(data.sellsSumTotalGood) + ' ' + getAssetNameMini(data.assetWant, data.assetWantName);
    output += ':<td><b>' + addCommas(data.sellsSumAmountGood) + ' ' + getAssetNameMini(data.assetHave, data.assetHaveName);

    output += '<tr bgcolor="#e0e0e0" style="background:#e0e0e0"><td width=40%><b>'
        + data.label_Creator + ' / ' + data.label_Amount + '<td width=30% style="font-size:1.4em"><b>' + data.label_Price
        + '</b></td><td width=40%><b>' + data.label_Amount + ' / ' + data.label_Creator + '</b></td></tr>';

    output += '<tr bgcolor="#f9f9f9">';
    ///output += '<td><b>' + addCommas(data.buysSumTotalGood) + ' ' + getAssetNameMini(data.assetWant, data.assetWantName);
    output += '<td><b>' +  addCommas(data.buysSumAmountGood) + ' ' + getAssetNameMini(data.assetHave, data.assetHaveName);
    output += '<td>- ' + data.label_Total_For_Buy + '<td>';

    width = 0;
    for (key in data.buys) {

        output += '<tr style="background-color: transparent">';
        output += '<td style="position: relative" align=right>';

        width += 1 * data.buys[key].buyingAmount; // преобразование строки в число

        var widthLocal = width;
        if (widthLocal > averageVolume) {
            widthLocal = averageVolume;
        }

        output += '<span style="position:absolute; z-index: -1; background-color:#cdfdcc; width:'
            + 250 * widthLocal / averageVolume + '%; top: 0; bottom: 0; left: 0;"></span>';

        output += '<span><a href=?tx=' + key + get_lang() + '>' + addCommas(data.buys[key].buyingAmount) + '</a></span>';
        output += '<td><a href=?tx=' + key + get_lang() + ' ><b>' + addCommas(data.buys[key].buyingPrice) + '</b></a>';
        output += '<td><a href ="?address=' + data.buys[key].creator_addr + get_lang() + '">' +
                cutBlank(data.buys[key].creator, 20) + '</a>';

    }

    output += '</table>';

    output += '</div><div class="col-lg-7" style="padding-right: 5em;">';

    output += '<h4 style="text-align: center;">' + data.label_Trade_History + '</h4>';

    output += '<table border="0" cellspacing="3" cellpadding="5" class="tiny table table-striped" style="width:100%; vertical-align: baseline; border: 1px solid #ddd; fonf-size:0.8em">';
    output += '<tr bgcolor="#e0e0e0" style="background:#e0e0e0"><td align=center><b>' + data.label_Date; // + '<td align=center><b>' + data.label_Type + '</b></td>';
    output += '<td align=center><b>' + data.label_Trade_Initiator
    output += '<td align=center><b>' + data.label_Amount;
    output += '<td align=center><b>' + data.label_Price;
    output += '<td align=center><b>' + data.label_Total_Cost;
    output += '<td align=center><b>' + data.label_Position_Holder
    output += '</tr>';

    for (key in data.trades) {

        var trade = data.trades[key];
        output += '<tr>';

        output += '<td align=center><a href=?trade=' + trade.initiatorTx + '/' + trade.targetTx + get_lang()
        output += '>' + convertTimestamp( trade.timestamp, false);

        output += '<td align=right style="line-height: 150%;">';

        if (trade.initiatorCreator_addr == data.assetWantOwner) {
            if (trade.type != 'sell') {
                output += '<span class="glyphicon glyphicon-arrow-up" style="color:limegreen"></span>';
            } else {
                output += '<span class="glyphicon glyphicon-arrow-down" style="color:crimson"></span>';
            }
        }

        output += '<a href=?address=' + trade.initiatorCreator_addr + '>' + cutBlank(trade.initiatorCreator, 20) + '</a>';

        if (trade.type == 'sell') {

            output += '<td align=right>' + addCommas(trade.amountHave);

            output += '<td align=left><span class="glyphicon glyphicon-arrow-down" style="color:crimson; font-size:1.2em"></span>'
                + '<b>' + addCommas(trade.realReversePrice) + '</b>';

            output += '<td align=right>' + addCommas(trade.amountWant);

        } else {

            output += '<td align=right>' + addCommas(trade.amountHave);

            output += '<td align=left><span class="glyphicon glyphicon-arrow-up" style="color:limegreen; font-size:1.2em"></span>'
                + '<b>' + addCommas(trade.realPrice) + '</b>';

            output += '<td align=right>' + addCommas(trade.amountWant);

        }

        output += '<td style="line-height: 150%;">';
        output += '<a href=?address=' + trade.targetCreator_addr + '>' + cutBlank(trade.targetCreator, 20) + '</a>';

        if (trade.targetCreator_addr == data.assetHaveOwner) {
            if (trade.type == 'sell') {
                output += '<span class="glyphicon glyphicon-arrow-up" style="color:limegreen"></span>';
            } else {
                output += '<span class="glyphicon glyphicon-arrow-down" style="color:crimson"></span>';
            }
        }

    }

    output += '</table>';

    output += '</div></div>';

    //output += '<b>' + data.label_Trade_Volume + ':</b>&nbsp;&nbsp;&nbsp;&nbsp;' + addCommas(data.tradeHaveAmount) + ' ' + getAssetNameMini(data.assetHave, data.assetHaveName);
    //output += '&nbsp;&nbsp;&nbsp;&nbsp;' + addCommas(data.tradeWantAmount) + ' ' + getAssetNameMini(data.assetWant, data.assetWantName);

    output += '<br><br><b>' + data.label_Go_To + ': <a href=?asset=' + data.assetHave + get_lang() + '>' + getAssetName2(data.assetHave, data.assetHaveName) + '</a>';
    output += '&nbsp;&nbsp;<a href=?asset=' + data.assetWant + get_lang() + '>' + getAssetName2(data.assetWant, data.assetWantName) + '</a>';
    output += '&nbsp;&nbsp;<a href=?asset=' + data.assetWant + '&asset=' + data.assetHave + get_lang() + '>' + getAssetName2(data.assetWant, data.assetWantName) + '/' + getAssetName2(data.assetHave, data.assetHaveName);
    output += '</b>';

    return output;
}
