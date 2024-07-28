function exchange(data){
    var output = '';

    if(data.hasOwnProperty('error'))
    {
        return '<h2>' + data.error + '</h2>';
    }

    var notDisplayPages = data.notDisplayPages;
    var numberShiftDelta = data.numberOfRepresentsItemsOnPage;
    //Отображение последнего блока
    output += lastBlock(data.lastBlock);
    var start = data.start;

    output += '<div class="col-lg-1 col-md-0"></div><div class="col-lg-5 col-md-6">';

    output += '<h4 style="text-align: center;">' + data.Label_table_PopularPairs + '</h4>';

    output += '<table border="0" cellspacing="3" cellpadding="5" class="tiny table table-striped" style="width:100%; vertical-align: baseline; border: 1px solid #ddd; font-size:1.0em">';
    output += '<tr bgcolor="#e0e0e0" style="background:#e0e0e0"><td align=center><b>' + data.Label_table_have;
    output += '<td><b>' + data.Label_table_want + '<td><b>' + data.Label_table_orders + '<td><b>' +
         data.Label_table_last_price + '<td><b>' + data.Label_table_volume24 + '</tr>';

    //Отображение таблицы элементов статусов
    for (var i in data.popularPairs) {
        var item = data.popularPairs[i];
        output += '<tr><td>' + getShortItemURL(item.have, '', 'width: 30px');
        output += '<td>' + getShortItemURL(item.want, '', 'width: 30px');
        output += '<td><a href="?asset=' + item.have.key
            + '&asset=' + item.want.key + get_lang() + '"><b>' + item.orders + '</b></a>';
        output += '<td><a href="?asset=' + item.have.key
            + '&asset=' + item.want.key + get_lang() + '"><b>' + (item.last > 100? item.last.toPrecision(6) : item.last > 1? item.last.toPrecision(6) : item.last.toPrecision(6)) + '</b></a>';
        output += '<td><a href="?asset=' + item.have.key
            + '&asset=' + item.want.key + get_lang() + '"><b>' + item.volume24 + '</b></a>';

        output += '</tr>';
    }
    output += '</table></div><div class="col-lg-6 col-md-5">';

    output += '<h4 style="text-align: center;">' + data.Label_table_LastTrades + '</h4>';

    output += '<table border="0" cellspacing="3" cellpadding="5" class="tiny table table-striped" style="width:100%; vertical-align: baseline; border: 1px solid #ddd; font-size:0.9em; line-height: 0.8em">';
    output += '<tr bgcolor="#e0e0e0" style="background:#e0e0e0"><td align=center><b>' + data.Label_Date;
    output += '<td align=center><b>' + data.Label_Pair + '<td align=center><b>' + data.Label_Trade_Initiator;
    output += '<td align=center><b>' + data.Label_Amount;
    output += '<td align=center><b>' + data.Label_Price;
    output += '<td align=center><b>' + data.Label_Position_Holder + '<tr>'
    //output += data.Label_Total_Cost + '</b></td></tr>';

    for (key in data.lastTrades) {

        var trade = data.lastTrades[key];
        output += '<tr>';

        output += '<td align=center><a href=?trade=' + trade.initiatorTx + '/' + trade.targetTx + get_lang()
        output += '>' + convertTimestamp( trade.timestamp, false);

        output += '<td><a href=?asset=' + trade.assetHaveKey + '&asset=' + trade.assetWantKey + '>' + getShortNameBlanked(trade.assetHaveName) + '/' + getShortNameBlanked(trade.assetWantName) + '</a>';

        output += '<td>';

        // отобрадает что это создатель актива действует
        if (trade.initiatorCreator_addr == trade.assetWantMaker) {
            if (trade.type != 'sell') {
                output += '<span class="glyphicon glyphicon-arrow-up" style="color:limegreen"></span>';
            } else {
                output += '<span class="glyphicon glyphicon-arrow-down" style="color:crimson"></span>';
            }
        }

        output += '<a href=?address=' + trade.initiatorCreator_addr + '>' + cutBlank(trade.initiatorCreator, 20) + '</a>';

        if (trade.type == 'sell') {
            output += '<td align=right>' + addCommas(trade.amountHave);
            //output += ' ' + getItemNameMini('asset', data.assetHave, data.assetHaveName);

            output += '<td align=left>';
            if (trade.unchecked == true) {}
            else {
                output += '<span class="glyphicon glyphicon-arrow-down" style="color:crimson; font-size:1.2em"></span>';
            }
            output += '<span style="font-size:1.1em">' + addCommas(trade.realReversePrice.toPrecision(6)) + '</span>';

        } else {
            output += '<td align=right>' + addCommas(trade.amountHave);

            output += '<td align=left>';
            if (trade.unchecked == true) {}
            else {
                output += '<span class="glyphicon glyphicon-arrow-up" style="color:limegreen; font-size:1.2em"></span>';
            }
            output += '<span style="font-size:1.1em">' + addCommas(trade.realPrice.toPrecision(6)) + '</span>';

        }

        output += '<td>';

        // отображает что это создатель актива действует
        if (trade.targetCreator_addr == trade.assetHaveMaker) {
            if (trade.type == 'sell') {
                output += '<span class="glyphicon glyphicon-arrow-up" style="color:limegreen"></span>';
            } else {
                output += '<span class="glyphicon glyphicon-arrow-down" style="color:crimson"></span>';
            }
        }

        output += '<a href=?address=' + trade.targetCreator_addr + '>' + cutBlank(trade.targetCreator, 20) + '</a>';

    }

    output += '</table></div>';

    return output;
}

function order(data){
    var output = '';

    if(data.hasOwnProperty('error'))
    {
        return '<h2>' + data.error + '</h2>';
    }

    var notDisplayPages = data.notDisplayPages;
    var numberShiftDelta = data.numberOfRepresentsItemsOnPage;
    //Отображение последнего блока
    output += lastBlock(data.lastBlock);
    var start = data.start;

    output += '<div><div class="col-lg-1 col-md-0"></div>"<div class="col-lg-4 col-md-5" style="padding-left: 5em;">';

    var status;
    if (data.completed) {
        status = data.Label_Completed;
    } else if (data.canceled) {
        status = data.Label_Canceled;
    } else {
        status = data.Label_Active;
    }

    output += '<h3 style="text-align: center;"><img src=img/13_order_creation.png> ' + data.Label_Head + '</h3>';
    output += '<h5 style="text-align: center;">(' + status + ')</h5>';

    output += '<br><p style="text-align: left; font-size:1.4em">';
    output += data.Label_Order + ': <a href="?tx=' + data.txSeqNo + get_lang() + '"><b>' + data.txSeqNo + '</b></a><br>';
    output += data.Label_table_have + '</b>: <a href=?asset=' + data.assetHaveKey + get_lang() + '><b>' + data.assetHaveName + '</b></a><br>';
    output += data.Label_Volume + ': <b>' + addCommas(data.order.amountHave) + '</b><br>';
    output += data.Label_Price + ': <b>' + addCommas(data.order.price) + ' / ' + addCommas(data.order.priceReverse) + '</b><br>';
    output += data.Label_Total_Cost + ': <b>' + addCommas(data.order.amountWant) + '</b><br>';
    output += data.Label_table_want + ': <a href=?asset=' + data.assetWantKey + get_lang() + '><b>' + data.assetWantName + '</b></a><br>';
    output += data.Label_Creator + ': <a href="?address=' + data.creator + '">' + data.creator_person + '</a><br>';
    //output += data.Label_Fulfilled + ': <b>' + addCommas(data.order.fulfilledHave) + '</b><br>';
    if (data.order.leftHave != 0 && data.order.leftWant != 0) {
        output += data.Label_LeftHave + ': ' + addCommas(data.order.leftHave) + ' / ' + addCommas(data.order.leftWant) + '<br>';
        output += data.Label_LeftPrice + ': ' + (100.0 * (1 - data.order.leftPrice / data.order.price)).toPrecision(4) + '%<br>';
    }

    output += data.Label_Pair + ': <a href="?asset=' + data.assetHaveKey
        + '&asset=' + data.assetWantKey + get_lang() + '"><b>' + data.assetHaveName + ' / ' + data.assetWantName + '</b></a>';

    output += '</p>';

    output += '</div><div class="col-lg-7" style="padding-right: 5em;">';

    output += '<h4 style="text-align: center;">' + data.Label_table_LastTrades + '</h4>';

    output += '<table border="0" cellspacing="3" cellpadding="5" class="tiny table table-striped" style="width:100%; vertical-align: baseline; border: 1px solid #ddd; fonf-size:0.8em">';
    output += '<tr bgcolor="#e0e0e0" style="background:#e0e0e0"><td align=center><b>' + data.Label_Date;
    output += '<td align=center><b>' + data.Label_Pair + '<td align=center><b>' + data.Label_Trade_Initiator;
    output += '<td align=center><b>' + data.Label_Amount;
    output += '<td align=center><b>' + data.Label_Price;
    output += '<td align=center><b>' + data.Label_Position_Holder + '<tr>'
    //output += data.Label_Total_Cost + '</b></td></tr>';

    for (key in data.lastTrades) {

        var trade = data.lastTrades[key];
        output += '<tr>';

        if (trade.type == 'cancel' || trade.type == 'change') {
            output += '<td align=center><a href=?tx=' + trade.initiatorTx + get_lang()
        } else {
            output += '<td align=center><a href=?trade=' + trade.initiatorTx + '/' + trade.targetTx + get_lang()
        }
        output += '>' + convertTimestamp(trade.timestamp, false);

        output += '<td><a href=?asset=' + trade.assetHaveKey + '&asset=' + trade.assetWantKey + '>' + getShortNameBlanked(trade.assetHaveName) + '/' + getShortNameBlanked(trade.assetWantName) + '</a>';

        output += '<td><a href=?address=' + trade.initiatorCreator_addr + '>' + cutBlank(trade.initiatorCreator, 20) + '</a>';

        // отображает что это создатель актива действует
        if (trade.initiatorCreator_addr == data.assetWantMaker) {
            if (trade.type == 'buy') {
                output += '<span class="glyphicon glyphicon-arrow-up" style="color:limegreen"></span>';
            } else if (trade.type == 'sell') {
                output += '<span class="glyphicon glyphicon-arrow-down" style="color:crimson"></span>';
            }
        }

        if (trade.type == 'sell') {
            output += '<td align=right>' + addCommas(trade.amountHave);

            output += '<td align=right>';
            if (trade.unchecked == true) {}
            else {
                output += '<span class="glyphicon glyphicon-arrow-down" style="color:crimson; font-size:1.2em"></span>';
            }
            output += '<span style="font-size:1.1em">' + addCommas(trade.realReversePrice) + '</span>';

        } else if (trade.type == 'buy') {
            output += '<td align=right>' + addCommas(trade.amountHave);

            output += '<td align=right>';
            if (trade.unchecked == true) {}
            else {
                output += '<span class="glyphicon glyphicon-arrow-up" style="color:limegreen; font-size:1.2em"></span>';
            }
            output += '<span style="font-size:1.1em">' + addCommas(trade.realPrice) + '</span>';

        } else if (trade.type == 'change') {
            output += '<td align=right><span class="glyphicon glyphicon-edit" style="color:blue; font-size:1.0em"></span>';

            output += '<td align=right>';
            output += '<span style="font-size:1.1em">' + addCommas(trade.realPrice) + '</span>';

        } else if (trade.type == 'cancel') {
            output += '<td align=right>' + addCommas(trade.amountHave);

            output += '<td align=left>';
            output += '<span class="glyphicon glyphicon-remove" style="color:crimson; font-size:1.0em"></span>';
            output += '<span style="color:crimson">' + data.Label_Cancel + '</span>';

        } else if (trade.type == 'order-cancel') {
            output += '<td align=left>';
            output += '<span class="glyphicon glyphicon-remove" style="color:crimson; font-size:1.0em"></span>';
            output += '<span style="color:crimson">' + data.Label_Cancel + '</span>';

            output += '<td align=right>' + addCommas(trade.targetAmount);


        }

        // отображает что это создатель актива действует
        if (trade.targetCreator_addr == data.assetWantMaker) {
            if (trade.type == 'sell') {
                output += '<span class="glyphicon glyphicon-arrow-up" style="color:limegreen"></span>';
            } else if (trade.type == 'buy') {
                output += '<span class="glyphicon glyphicon-arrow-down" style="color:crimson"></span>';
            }
        }

        output += '<td><a href=?address=' + trade.targetCreator_addr + '>' + cutBlank(trade.targetCreator, 20) + '</a>';

    }

    output += '</table>';

    return output;
}

function trades(data) {
    var output = "";

    output += lastBlock(data.lastBlock);

    output += '';

    output += '<h3 style="display:inline;"><a href="?asset=' + data.assetWant + '&asset=' + data.assetHave + get_lang()
        + '"><img src="img/exchange.png" style="width:1em"></a> '
        + data.Label_Trades + '</h3> ';

    output += '<a href="?asset=' + data.assetHave + '&asset=' + data.assetWant + get_lang() + '"><h3 style="display:inline;">';
    output += getItemName2(1000, data.assetHave, data.assetHaveName) + ' / ';
    output += getItemName2(1000, data.assetWant, data.assetWantName) + '</h3></a>';

    output += '<br>';

    output +='<div><div class="col-lg-5" style="padding-left:5em;">';
    output += '<h4>' + data.Label_Orders + '</h4>';

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

        output += '<a href=?tx=' + key + get_lang() + '>' + addCommas(data.sells[key].amount) + '</a>';
        output += '<td><a href=?tx=' + key + get_lang() + '><b>' + addCommas(data.sells[key].price) + '</b</a>';
        output += '<td><span><a href ="?address=' + data.sells[key].creator_addr + get_lang() + '">' +
                cutBlank(data.sells[key].creator, 60) + '</a></span>';

    }

    output += '<tr bgcolor="#f9f9f9">';
    output += ':<td><b>' + addCommas(data.sellsSumAmountGood);
    output += '<td>' + data.Label_Total_For_Sell + '<td>' + getItemNameMini('asset', data.assetHave, data.assetHaveName);

    output += '<tr bgcolor="#e0e0e0" style="background:#e0e0e0"><td width=20% style="font-size:1.4em"><b>'
        + data.Label_Amount + '<td width=20% style="font-size:1.4em"><b>' + data.Label_Price
        + '</b></td><td width=60% style="font-size:1.4em"><b>' + data.Label_Creator + '</b></td></tr>';

    output += '<tr bgcolor="#f9f9f9">';
    output += '<td><b>' +  addCommas(data.buysSumAmountGood);
    output += '<td>' + data.Label_Total_For_Buy + '<td>' + getItemNameMini('asset', data.assetHave, data.assetHaveName);;

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
                cutBlank(data.buys[key].creator, 60) + '</a>';

    }

    output += '</table>';

    output += '</div><div class="col-lg-7" style="padding-right: 5em;">';

    output += '<h4 style="text-align: center;">' + data.Label_Trade_History + '</h4>';

    output += '<table border="0" cellspacing="3" cellpadding="5" class="tiny table table-striped" style="width:100%; vertical-align: baseline; border: 1px solid #ddd; fonf-size:0.8em">';
    output += '<tr bgcolor="#e0e0e0" style="background:#e0e0e0"><td align=center><b>' + data.Label_Date; // + '<td align=center><b>' + data.Label_Type + '</b></td>';
    output += '<td align=center><b>' + data.Label_Trade_Initiator
    output += '<td align=center><b>' + data.Label_Amount;
    output += '<td align=center><b>' + data.Label_Price;
    output += '<td align=center><b>' + data.Label_Total_Cost;
    output += '<td align=center><b>' + data.Label_Position_Holder
    output += '</tr>';

    for (key in data.trades) {

        var trade = data.trades[key];
        output += '<tr>';

        output += '<td align=center><a href=?trade=' + trade.initiatorTx + '/' + trade.targetTx + get_lang()
        output += '>' + convertTimestamp( trade.timestamp, false);

        output += '<td align=right style="line-height: 150%;">';

        if (trade.initiatorCreator_addr == data.assetWantMaker) {
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

        if (trade.targetCreator_addr == data.assetHaveMaker) {
            if (trade.type == 'sell') {
                output += '<span class="glyphicon glyphicon-arrow-up" style="color:limegreen"></span>';
            } else {
                output += '<span class="glyphicon glyphicon-arrow-down" style="color:crimson"></span>';
            }
        }

    }

    output += '</table>';

    output += '</div></div>';

    //output += '<b>' + data.Label_Trade_Volume + ':</b>&nbsp;&nbsp;&nbsp;&nbsp;' + addCommas(data.tradeHaveAmount) + ' ' + getItemNameMini('asset', data.assetHave, data.assetHaveName);
    //output += '&nbsp;&nbsp;&nbsp;&nbsp;' + addCommas(data.tradeWantAmount) + ' ' + getItemNameMini('asset', data.assetWant, data.assetWantName);

    output += '<br><br><b>' + data.Label_Go_To + ': <a href=?asset=' + data.assetHave + get_lang() + '>' + getItemName2(1000, data.assetHave, data.assetHaveName) + '</a>';
    output += '&nbsp;&nbsp;<a href=?asset=' + data.assetWant + get_lang() + '>' + getItemName2(1000, data.assetWant, data.assetWantName) + '</a>';
    output += '&nbsp;&nbsp;<a href=?asset=' + data.assetWant + '&asset=' + data.assetHave + get_lang() + '>' + getItemName2(1000, data.assetWant, data.assetWantName) + '/' + getItemName2(1000, data.assetHave, data.assetHaveName);
    output += '</b>';

    return output;
}
