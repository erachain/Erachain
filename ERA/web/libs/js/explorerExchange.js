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
        output += '<tr><td>' + getShortAssetURL(item.have.key, item.have.name, item.have.icon, 30);
        output += '<td>' + getShortAssetURL(item.want.key, item.want.name, item.want.icon, 30);;
        output += '<td><a href="?asset=' + item.have.key
            + '&asset=' + item.want.key + get_lang() + '"><b>' + item.orders + '</b></a>';
        output += '<td><a href="?asset=' + item.have.key
            + '&asset=' + item.want.key + get_lang() + '"><b>' + (item.last > 100? item.last.toPrecision(10) : item.last > 1? item.last.toPrecision(8) : item.last.toPrecision(6)) + '</b></a>';
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
            output += '<span style="font-size:1.1em">' + addCommas(trade.realReversePrice.toPrecision(10)) + '</span>';

        } else {
            output += '<td align=right>' + addCommas(trade.amountHave);

            output += '<td align=left>';
            if (trade.unchecked == true) {}
            else {
                output += '<span class="glyphicon glyphicon-arrow-up" style="color:limegreen; font-size:1.2em"></span>';
            }
            output += '<span style="font-size:1.1em">' + addCommas(trade.realPrice.toPrecision(10)) + '</span>';

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
        if (data.canceled) {
            status = data.Label_Canceled;
        } else {
            status = data.Label_Completed;
        }
    } else {
        status = data.Label_Active;
    }

    output += '<h3 style="text-align: center;"><img src=img/13_order_creation.png> ' + data.Label_Head + '</h3>';
    output += '<h5 style="text-align: center;">(' + status + ')</h5>';

    output += '<br><p style="text-align: left; font-size:1.4em">';
    output += data.Label_Order + ': <a href="?tx=' + data.txSeqNo + get_lang() + '"><b>' + data.txSeqNo + '</b></a><br>';
    output += data.Label_table_have + '</b>: <a href=?asset=' + data.assetHaveKey + get_lang() + '><b>' + data.assetHaveName + '</b></a><br>';
    output += data.Label_Volume + ': <b>' + addCommas(data.order.amountHave) + '</b><br>';
    output += data.Label_Price + ': <b>' + addCommas(data.order.price) + '</b><br>';
    output += data.Label_Total_Cost + ': <b>' + addCommas(data.order.amountWant) + '</b><br>';
    output += data.Label_table_want + ': <a href=?asset=' + data.assetWantKey + get_lang() + '><b>' + data.assetWantName + '</b></a><br>';
    output += data.Label_Creator + ': <a href="?address=' + data.creator + '">' + data.creator_person + '</a><br>';
    //output += data.Label_Fulfilled + ': <b>' + addCommas(data.order.fulfilledHave) + '</b><br>';
    output += data.Label_LeftHave + ': <b>' + addCommas(data.order.leftHave) + '</b><br>';
    output += data.Label_LeftPrice + ': <b>' + addCommas(data.order.leftPrice) + '</b><br>';

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

        output += '<td align=center><a href=?trade=' + trade.initiatorTx + '/' + trade.targetTx + get_lang()
        output += '>' + convertTimestamp( trade.timestamp, false);

        output += '<td><a href=?asset=' + trade.assetHaveKey + '&asset=' + trade.assetWantKey + '>' + getShortNameBlanked(trade.assetHaveName) + '/' + getShortNameBlanked(trade.assetWantName) + '</a>';

        output += '<td><a href=?address=' + trade.initiatorCreator_addr + '>' + cutBlank(trade.initiatorCreator, 20) + '</a>';

        // отобрадает что это создатель актива действует
        if (trade.initiatorCreator_addr == data.assetWantMaker) {
            if (trade.type != 'sell') {
                output += '<span class="glyphicon glyphicon-arrow-up" style="color:limegreen"></span>';
            } else {
                output += '<span class="glyphicon glyphicon-arrow-down" style="color:crimson"></span>';
            }
        }

        if (trade.type == 'sell') {
            output += '<td align=right>' + addCommas(trade.amountHave);

            output += '<td align=left>';
            if (trade.unchecked == true) {}
            else {
                output += '<span class="glyphicon glyphicon-arrow-down" style="color:crimson; font-size:1.2em"></span>';
            }
            output += '<span style="font-size:1.1em">' + addCommas(trade.realReversePrice) + '</span>';

        } else {
            output += '<td align=right>' + addCommas(trade.amountHave);

            output += '<td align=left>';
            if (trade.unchecked == true) {}
            else {
                output += '<span class="glyphicon glyphicon-arrow-up" style="color:limegreen; font-size:1.2em"></span>';
            }
            output += '<span style="font-size:1.1em">' + addCommas(trade.realPrice) + '</span>';

        }

        // отобрадает что это создатель актива действует
        if (trade.targetCreator_addr == data.assetWantMaker) {
            if (trade.type == 'sell') {
                output += '<span class="glyphicon glyphicon-arrow-up" style="color:limegreen"></span>';
            } else {
                output += '<span class="glyphicon glyphicon-arrow-down" style="color:crimson"></span>';
            }
        }

        output += '<td><a href=?address=' + trade.targetCreator_addr + '>' + cutBlank(trade.targetCreator, 20) + '</a>';

    }

    output += '</table>';

    return output;
}
