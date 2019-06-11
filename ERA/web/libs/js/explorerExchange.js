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

    output += '<div class = "row"><div class="col-lg-5" style="padding-left: 5em;">';

    output += '<h4 style="text-align: center;">' + data.label_table_PopularPairs + '</h4>';

    output += '<table border="0" cellspacing="3" cellpadding="5" class="table table-striped" style="width:100%; vertical-align: baseline; border: 1px solid #ddd; fonf-size:0.8em">';
    output += '<tr bgcolor="#e0e0e0" style="background:#e0e0e0"><td align=center><b>' + data.label_table_have;
    output += '<td><b>' + data.label_table_want + '<td><b>' + data.label_table_orders + '<td><b>' +
         data.label_table_last_price + '<td><b>' + data.label_table_volume24 + '</tr>';

    //Отображение таблицы элементов статусов
    for (var i in data.popularPairs) {
        var item = data.popularPairs[i];
        output += '<tr><td>' + getShortAssetURL(item.have.key, item.have.name, item.have.icon, 30);
        output += '<td>' + getShortAssetURL(item.want.key, item.want.name, item.want.icon, 30);;
        output += '<td><a href="?asset=' + item.have.key
            + '&asset=' + item.want.key + get_lang() + '"><b>' + item.orders + '</b></a>';
        output += '<td><a href="?asset=' + item.have.key
            + '&asset=' + item.want.key + get_lang() + '"><b>' + item.last + '</b></a>';
        output += '<td><a href="?asset=' + item.have.key
            + '&asset=' + item.want.key + get_lang() + '"><b>' + item.volume24 + '</b></a>';

        output += '</tr>';
    }
    output += '</table></div><div class="col-lg-7" style="padding-right: 5em;">';

    output += '<h4 style="text-align: center;">' + data.label_table_LastTrades + '</h4>';

    output += '<table border="0" cellspacing="3" cellpadding="5" class="table table-striped" style="width:100%; vertical-align: baseline; border: 1px solid #ddd; fonf-size:0.8em">';
    output += '<tr bgcolor="#e0e0e0" style="background:#e0e0e0"><td align=center><b>' + data.label_Date;
    output += '<td align=center><b>' + data.label_Pair + '<td align=center><b>' + data.label_Trade_Initiator;
    output += '<td align=center><b>' + data.label_Amount;
    output += '<td align=center><b>' + data.label_Price;
    output += '<td align=center><b>' + data.label_Position_Holder + '<tr>'
    //output += data.label_Total_Cost + '</b></td></tr>';

    for (key in data.lastTrades) {

        var trade = data.lastTrades[key];
        output += '<tr>';

        output += '<td align=center><a href=?trade=' + trade.initiatorTx + '/' + trade.targetTx + get_lang()
        output += '>' + convertTimestamp( trade.timestamp, false);

        output += '<td><a href=?asset=' + trade.assetHaveKey + '&asset=' + trade.assetWantKey + '>' + getShortNameBlanked(trade.assetHaveName) + '/' + getShortNameBlanked(trade.assetWantName) + '</a>';

        output += '<td><a href=?address=' + trade.initiatorCreator_addr + '>' + cutBlank(trade.initiatorCreator, 20) + '</a>';

        // отобрадает что это создатель актива действует
        if (trade.initiatorCreator_addr == data.assetHaveOwner) {
            output += ' <b>&#9654;</b> ';
        } else if (trade.initiatorCreator_addr == data.assetWantOwner) {
            output += ' <b>&#9655;</b> ';
        }

        if (trade.type == 'sell') {
                    output += '<td align=right>' + addCommas(trade.amountHave);
                    //output += ' ' + getAssetNameMini(data.assetHave, data.assetHaveName);

                    if (trade.unchecked == true) {}
                    else {
                        output += '<td align=left><span class="glyphicon glyphicon-arrow-down" style="color:crimson; font-size:1.2em"></span>'
                            + '<span style="font-size:1.4em">' + addCommas(trade.realReversePrice) + '</span>';
                        ///output += ' ' + getAssetNameMini(data.assetWant, data.assetWantName);
                    }

                    //output += '<td>' + addCommas(trade.amountWant);
                    //output += ' ' + getAssetNameMini(data.assetWant, data.assetWantName);
            } else {
                    output += '<td align=right>' + addCommas(trade.amountHave);
                    //output += ' ' + getAssetNameMini(data.assetHave, data.assetHaveName);

                    if (trade.unchecked == true) {}
                    else {
                        output += '<td align=left><span class="glyphicon glyphicon-arrow-up" style="color:limegreen; font-size:1.2em"></span>'
                            + '<span style="font-size:1.4em">' + addCommas(trade.realPrice) + '</span>';
                        //output += ' ' + getAssetNameMini(data.assetWant, data.assetWantName) + '';
                    }

                    //output += '<td>' + addCommas(trade.amountWant);
                    //output += ' ' + getAssetNameMini(data.assetWant, data.assetWantName);
            }

        // отобрадает что это создатель актива действует
        if (trade.targetCreator_addr == data.assetHaveOwner) {
            output += ' <b>&#9664;</span></b> ';
        } else if (trade.targetCreator_addr == data.assetWantOwner) {
            output += ' <b>&#9665;</b> ';
        }

        output += '<td><a href=?address=' + trade.targetCreator_addr + '>' + cutBlank(trade.targetCreator, 20) + '</a>';

        //output += '<td>' + addCommas(trade.amountWant);

    }

    output += '</table>';

    return output;
}
