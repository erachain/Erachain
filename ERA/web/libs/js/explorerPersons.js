function person_asset(data) {
    var output = '';
    if (data.error != null) {
        return data.error;
    }

    output = '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1180">';
    output += '<tr><td align=left>';
    output += '<table><tr><td>';

    output += '<img src="data:image/gif;base64,' + data.person_img + '" width = "350" /></td><td style ="padding-left:20px">';

    if (data.sum > 0) {
        output += '<img src="img/check-yes.png" style="height:110px">'
        output += '<b><span style="font-size:4em; color:#0cb70c"> &nbsp&nbsp +' + data.sum + '</span></b><br>';
        output += '<br>';
    } else if (data.sum == 0) {
        output += '<img src="img/check-no.png" style="height:120px">'
        output += '<br><br>';
    } else {
        output += '<img src="img/check-no.png" style="height:120px">'
        output += '<b><span style="font-size:3em; color:crimson"> &nbsp&nbsp ' + data.sum + '</span></b><br>';
        output += '<br>';
    }

    output += data.Label_asset + ': <fon_t size=10> &nbsp&nbsp' + data.asset_name + '</fon_t><br>';
    output += data.Label_person + ': <a href ="?person=' +
        data.person_key + get_lang() + '">[' + data.person_key + ']' + data.person_name + '</a>';

    output += '<br>';
    output += '<br>';

    output += '</td>';
    output += '</tr>';
    output += '</table>';

    return output;
}

function person_status(data) {
    var output = '';
    if (data.error != null) {
        return data.error;
    }

    output = '<table id=last BORDER=0 cellpadding=15 cellspacing=0 width="1180">';
    output += '<tr><td align=left>';
    output += '<table><tr><td>';

    output += '<img src="data:image/gif;base64,' + data.person_img + '" width = "350" /></td><td style ="padding-left:20px">';

    output += data.last.text + ': <br>';
    if (data.last.hasOwnProperty('beginTimestamp')) {
        output += '<img src="img/check-yes.png" style="height:4em; margin-bottom:20px;">'
        output += '<b><span style="font-size:3em; color:#0cb70c"> &nbsp' + convertTimestamp(data.last.beginTimestamp) + '</span></b><br>';
    }
    if (data.last.hasOwnProperty('endTimestamp')) {
        output += '<img src="img/check-no.png" style="height:4em; margin-bottom:20px;">'
        output += '<b><span style="font-size:3em; color:crimson"> &nbsp' + convertTimestamp(data.last.endTimestamp) + '</span></b><br>';
    }

    output += data.Label_person + ': <a href ="?person=' +
        data.person_key + get_lang() + '">[' + data.person_key + ']' + data.person_name + '</a><br>';
    output += data.Label_status + ': <a href ="?status=' +
        data.status_key + get_lang() + '">[' + data.status_key + ']' + data.status_name + '</a><br>';
    output += data.Label_creator + ': <a href ="?address=' +
        data.last.creator + get_lang() + '">' + data.last.creator + '</a><br>';
    output += data.Label_transaction + ': <a href ="?tx=' + data.last.txBlock + '-' + data.last.txSeqNo + get_lang()
        + '">' + data.last.txBlock + '-' + data.last.txSeqNo + '</a><br>';

    output += '<br>';
    output += '<br>';

    output += '</table>';
    output += '</table>';

    output += '<hl>';
    output += '<h3>' + data.Label_history + '</h3>';


    if (data.hasOwnProperty('history')) {
        output += '<table id=history BORDER=0 cellpadding=15 cellspacing=0 width="1180">';
        output += '<tr><td width="30%"><b>' + data.Label_result + '<td><b>' + data.Label_from
            + '<td><b>' + data.Label_to + '<td><b>' + data.Label_creator + '<td><b>' + data.Label_transaction;
        for (key in data.history) {
            var item = data.history[key];

            output += '<tr><td>' + item.text;
            output += '<td>';
            if (item.hasOwnProperty('beginTimestamp')) {
                output += convertTimestamp(item.beginTimestamp);
            }
            output += '<td>';
            if (item.hasOwnProperty('endTimestamp')) {
                output += convertTimestamp(item.endTimestamp);
            }
            output += '<td> <a href ="?address=' +
                item.creator + get_lang() + '">' + item.creator + '</a>';
            output += '<td> <a href ="?tx=' +
                item.txBlock + '-' + item.txSeqNo + get_lang() + '">' + item.txBlock + '-' + item.txSeqNo + '</a>';

        }
    }

    output += '</table>';

    return output;
}

function person(data) {

    var output = '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1180">';
    output += '<tr><td align=left>';
    output += '<table><tr><td>';


    output += '<img src="data:image/gif;base64,' + data.img + '" width = "350" /></td><td style ="padding-left:20px">';
    output += data.Label_key + ':  &nbsp&nbsp<b>' + data.key + '</b><br>';
    output += data.Label_name + ': &nbsp&nbsp <b>' + data.name + '</b><br>';
    if (data.creator_key != "") {
        output += data.Label_creator + ': &nbsp&nbsp<a href ="?person=' + data.creator_key + get_lang() + '"><b> ' + data.creator + '</b></a><br>';
    } else {
        output += data.Label_creator + ': &nbsp&nbsp<b> ' + data.creator + '</b><br>';
    }
    output += data.Label_born + ': &nbsp&nbsp<b> ' + data.birthday + '</b>';
    if ('deathday' in data) {
        output += ', &nbsp&nbsp ' + data.Label_dead + ': &nbsp&nbsp<b> ' + data.deathday + '</b><br>'
    } else {
        output += '<br>';
    }
    output += data.Label_gender + ': &nbsp&nbsp<b> ' + data.gender + '</b><br>';
    output += data.Label_description + ': &nbsp&nbsp' + fformat(data.description) + '<br>';
    if (data.era_balance_a) {
        output += '<h4>ERA: &nbsp&nbsp<u>A</u>:' + data.era_balance_a + '&nbsp&nbsp<u>B</u>:' + data.era_balance_b + '&nbsp&nbsp<u>C</u>:' + data.era_balance_c + '</h4>';
    }
    if (data.compu_balance) {
        output += '<h4>COMPU: &nbsp&nbsp <b>' + data.compu_balance + '</b></h4>';
    }
    if (data.lia_balance_a) {
        output += '<h5>' + data.label_registered + ': <b>' + data.lia_balance_a + '</b>, ' + data.label_certified + ': <b>' + data.lia_balance_b + '</b></h5></br>';
    }
    output += '</td>';
    output += '</tr>';
    output += '</table>';

//statuses
    output += '<br>' + data.Label_statuses + ':';
    output += '<table id=statuses BORDER=0 cellpadding=15 cellspacing=0 width="800"  class="table table-striped" style="border: 1px solid #ddd; word-wrap: break-word;" ><tr bgcolor="f1f1f1"><td><b>' + data.Label_Status_table_status + '<td><b>' + data.Label_Status_table_period + '<td><b>' + data.Label_accounts_table_creator + '<tr>';

    for (key in data.statuses) {
        output += '<tr ><td ><a href ="?person=' + data.key + '&status=' + data.statuses[key].status_key + get_lang() + '">' + data.statuses[key].status_name
            + '<td>' + data.statuses[key].status_period
            + '<td><a href ="?address=' + data.statuses[key].status_creator_address + get_lang() + '">' + data.statuses[key].status_creator + '</a><tr>';
    }
    output += '</table><br>';

// accounts

    output += '<br>' + data.Label_accounts + ':';
    output += '<table id=accounts BORDER=0 cellpadding=15 cellspacing=0 width="800"  class="table table-striped" style="border: 1px solid #ddd; word-wrap: break-word;" ><tr bgcolor="f1f1f1"><td><b>' + data.Label_accounts_table_adress + '<td><b>' + data.Label_accounts_table_to_date + '<td><b>' + data.Label_accounts_table_creator + '<tr>';

    for (key in data.accounts) {
        output += '<tr><td><a href = "?address=' + data.accounts[key].address + get_lang() + '">' + data.accounts[key].address + '</a><td>'
            + data.accounts[key].to_date + '<td><a href ="?address=' + data.accounts[key].creator_address + get_lang() + '">' + data.accounts[key].creator + '</a><tr>';
    }
    output += '</table><br>';

//my p3ersons
    output += '<br>' + data.Label_My_Persons + ':';
    output += '<table id=accounts BORDER=0 cellpadding=15 cellspacing=0 width="800"  class="table table-striped" style="border: 1px solid #ddd; word-wrap: break-word;" ><tr bgcolor="f1f1f1"><td><b>' + data.Label_accounts_table_date + '<td><b>' + data.Label_My_Person_key + '<td><b>' + data.Label_My_Persons_Name + '<tr>';

    key = 0;
    for (key in data.My_Persons) {

        output += '<tr><td>' + data.My_Persons[key].date + '<td>' + data.My_Persons[key].key + '<td><a href ="?person=' + data.My_Persons[key].key + get_lang() + '">' + data.My_Persons[key].name + '</a><tr>';
    }

    output += '</table><br>';
    output += '</table><br>';
    return output;
}

function persons(data) {
    var output = '';
    if (data.hasOwnProperty('error')) {
        return '<h2>' + data.error + '</h2>';
    }
    var notDisplayPages = data.notDisplayPages;
    var numberShiftDelta = data.pageSize;
    //Отображение последнего блока
    output += lastBlock(data.lastBlock);
    var start = data.start;
    if (!notDisplayPages) {
        //Отображение компонента страниц(вверху)
        //output += pagesComponentBeauty(start, data.Label_Persons, data.lastNumber, data.pageSize, 'start');
        output += pagesComponent2(data);
    }
    //Отображение шапки таблицы
    output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="">';
    output += '<tr><td align=left>';
    output += '<a href=?unconfirmed' + get_lang() + '>' +
        data.Label_Unconfirmed_transactions + ': ' + data.unconfirmedTxs + '</a>.';
    output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1180"  ' +
        'class="table table-striped" style="border: 1px solid #ddd;">';
    output += '<tr><td><td><b>' + data.Label_key + ' <td><b>' +
        data.Label_name + '<td><b>'+ data.Label_description + ' <td><b>' + data.Label_creator;
    //Отображение таблицы элементов персон
    for (var i in data.pageItems) {
    //var length = Object.keys(data.pageItems).length;
    //for (var i = 0; i < length; i++) {
        var item = data.pageItems[i];


        output += '<tr>';
        output += ' <td><img src="personimage?key=' + item.key + '" width="100"/></td>';
        output += '<td>' + item.key + '<td><a href=?person=' +
            item.key + get_lang() + '>' + item.name + '</a>';
        output += '<td>' + item.description.substr(0, 100) + '</td>';
        output += '<td><a href=?address=' + item.owner + get_lang() + '>';
        if (item.hasOwnProperty('person'))
            output += '[' + item.person_key + ']' + htmlFilter(item.person);
        else
            output += item.owner;
        output += '</a>';
    }
    if (!notDisplayPages) {
        output += '</table></td></tr></table>';
        //Отображение компонента страниц(снизу)
        //output += pagesComponentBeauty(start, data.Label_Persons, data.lastNumber, data.pageSize, 'start');
        output += pagesComponent2(data);
    }
    return output;
}
