function top100(data) {

    var output = "";

    output += lastBlock(data.lastBlock);

    output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="900">';
    output += '<tr><td align=center>';

    output += data.Label_Total_coins_in_the_system + ': <b>' + addCommas(data.total) + '</b>, '
        + data.Label_in_order + ': <b>' + addCommas(data.allinOrders) + '</b>, '
        + data.Label_Released + ': <b>' + addCommas(data.released) + '</b><br>';


    if (data.limit == -1) {
        buf = ''
        output += '<h3>' + data.Label_All_non + '</h3>';
    } else if (data.limit == -2) {
        output += '<h3>' + data.Label_All_accounts + '</h3>';
    } else {
        output += '<h3>' + data.Label_Title + '</h3>';
    }


    var table = '<table id=top100 BORDER=0  cellpadding=10 cellspacing=0 class="tiny table table-striped" style="border: 1px solid #ddd; width: auto;"><tr><td><b>#<td><b>' + data.Label_Table_Account + '<td><b>' + data.Label_Table_person + '<td><b>' + data.label_Balance_1 + '<td><b>' + data.label_Balance_2 + '<td><b>' + data.label_Balance_3 + '<td><b>' + data.label_Balance_4 + '<td><b>' + data.Label_Table_Prop;

    for (key in data.top) {
        var item = data.top[key];
        table += '<tr>';
        table += '<td>' + key + '<td>' + '<a href="?address=' + item.address + get_lang() + '">' + item.address + '</a>';

        if (item.hasOwnProperty('person')) {
            table += '<td><a href="?person=' + item.person_key + get_lang() + '">' + item.person + '</a>';
        } else {
            table += '<td>';
        }
        table += '<td>' + addCommas(item.OWN);
        table += '<td>' + addCommas(item.DEBT);
        table += '<td>' + addCommas(item.HOLD);
        table += '<td>' + addCommas(item.SPEND);
        table += '<td>' + ((item.OWN / data.total) * 100).toFixed(2) + "%";
    }
    table += '</table>';

    output += table;
    return output;
}
