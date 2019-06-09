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

    output += '<div class = "row"><div class="col-lg-6">';
    //output += '<table width="100%" border=0><tr><td align=left><br>';
    //output += '<center>';
    output += '<table BORDER=0 cellpadding=10 cellspacing=0 class="table table-striped" style="width:90%; border: 1px solid #ddd;">';
    output += '<thead><tr><td><b>'+ data.label_table_have + '</b></td><td><b>' + data.label_table_want +
        '</b></td><td><b>' + data.label_table_orders + '</b></td><td><b>' +
         data.label_table_last_price + '</b></td><td><b>' + data.label_table_volume24 + '</b></td></tr></thead>';

    //Отображение таблицы элементов статусов
    for (var i in data.pairs) {
        var item = data.pairs[i];
        output += '<tr><td>' + getAssetURL(item.have.key, item.have.name, item.have.icon, 30);
        output += '<td>' + getAssetURL(item.want.key, item.want.name, item.want.icon, 30);;
        output += '<td><a href="?asset=' + item.have.key
            + '&asset=' + item.want.key + get_lang() + '"><b>' + item.orders + '</b></a>';
        output += '<td><a href="?asset=' + item.have.key
            + '&asset=' + item.want.key + get_lang() + '"><b>' + item.last + '</b></a>';
        output += '<td><a href="?asset=' + item.have.key
            + '&asset=' + item.want.key + get_lang() + '"><b>' + item.volume24 + '</b></a>';

        output += '</tr>';
    }
    output += '</table>';
    //output += '</center>';
    output +=  '</div><div class="col-lg-6">';
    //output += '<table width="100%" border=0><tr><td align=left><br>';
    output += '<table BORDER=0 cellpadding=10 cellspacing=0 class="table table-striped" style="width:90%; border: 1px solid #ddd;">';
    output += '<thead><tr><td><b>'+ data.label_table_have + '</b></td><td><b>' + data.label_table_want +
        '</b></td><td><b>' + data.label_table_orders + '</b></td><td><b>' +
         data.label_table_last_price + '</b></td><td><b>' + data.label_table_volume24 + '</b></td></tr></thead>';

    //Отображение таблицы элементов статусов
    for (var i in data.trades) {
        var trade = data.trades[i];
        output += '<tr><td>' + convertTimestamp(trade.timestamp, false);
        output += '<td>' + addCommas(trade.amountHave);
        output += '<td>' + getAssetNameMini(trade.have, '');

        output += '</tr>';
    }

    output += '</div></div>';

    return output;
}
