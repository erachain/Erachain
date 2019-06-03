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

    output += '<table width="600" border=0><tr><td align=left><br>';
    output += '<table width=80% BORDER=0 cellpadding=10 cellspacing=0 class="table table-striped" style="border: 1px solid #ddd;">';
    output += '<thead><tr><td><b>'+ data.label_table_have + '</b></td><td><b>' + data.label_table_want +
        '</b></td><td><b>' + data.label_table_orders + '</b></td><td><b>' + data.label_table_last_price + '</b></td></tr></thead>';

    //Отображение таблицы элементов статусов
    for (var i in data.pairs) {
        var item = data.pairs[i];
        output += '<tr><td><a href="?asset=' + item.have.key + get_lang() + '">';
        output += '<b>' + getAssetName2(item.have.key, item.have.name) + '</b></a>';
        output += '<td><a href="?asset=' + item.want.key + get_lang() + '">';
        output += '<b>' + getAssetName2(item.want.key, item.want.name) + '</b></a></td>';
        output += '<td><a href="?asset=' + item.have.key
            + '&asset=' + item.want.key + get_lang() + '"><b>' + item.orders + '</b></a>';
        output += '<td><a href="?asset=' + item.have.key
            + '&asset=' + item.want.key + get_lang() + '"><b>' + item.last + '</b></a>';

        output += '</tr>';
    }

    return output;
}
