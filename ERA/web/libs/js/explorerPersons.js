function person_asset(data) {

    var output = lastBlock(data.lastBlock);

    if (data.error != null) {
        return data.error;
    }

    output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1180">';
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

    var output = lastBlock(data.lastBlock);

    if (data.error != null) {
        return data.error;
    }

    output += '<table id=last BORDER=0 cellpadding=15 cellspacing=0 width="1180">';
    output += '<tr><td align=left>';
    output += '<table><tr><td>';

    output += '<img src="data:image/gif;base64,' + data.person_img + '" width = "350" /></td><td style ="padding-left:20px">';

    if (data.hasOwnProperty('last')) {
        output += '<h3>' + data.Label_current_state + '</h3>';
        output += '<span style="font-size:2em;">' + data.last.text + '</span><br>';
        if (data.last.hasOwnProperty('endTimestamp')) {
            output += '<img src="img/check-no.png" style="height:4em; margin-bottom:20px;">'
            output += '<b><span style="font-size:3em; color:crimson"> &nbsp' + convertTimestamp(data.last.endTimestamp, true) + '</span></b><br>';
        }
        if (data.last.hasOwnProperty('beginTimestamp')) {
            output += '<img src="img/check-yes.png" style="height:4em; margin-bottom:20px;">'
            output += '<b><span style="font-size:3em; color:#0cb70c"> &nbsp' + convertTimestamp(data.last.beginTimestamp, true) + '</span></b><br>';
        }
        output += data.Label_creator + ': <a href ="?address=' +
            data.last.creator + get_lang() + '">' + data.last.creator_name + '</a><br>';
        output += data.Label_transaction + ': <a href ="?tx=' + data.last.txBlock + '-' + data.last.txSeqNo + get_lang()
        + '">' + data.last.txBlock + '-' + data.last.txSeqNo + '</a><br>';
        output += '<br>';
    }

    output += data.Label_person + ': <a href ="?person=' +
        data.person_key + get_lang() + '">[' + data.person_key + ']' + data.person_name + '</a><br>';
    output += data.Label_status + ': <a href ="?status=' +
        data.status_key + get_lang() + '">[' + data.status_key + ']' + data.status_name + '</a><br>';

    output += '<br>';
    output += '<br>';

    output += '</table>';
    output += '</table>';

    output += '<hl>';
    if (data.hasOwnProperty('last')) {
        output += '<h3>' + data.Label_status_history + '</h3>';
    } else {
        output += '<h3>' + data.Label_statuses_list + '</h3>';
    }

    if (data.hasOwnProperty('history')) {
        output += '<table id=history BORDER=0 cellpadding=15 cellspacing=0 width="1180">';
        output += '<tr><td width="40%"><b>' + data.Label_result + '<td><b>' + data.Label_from
            + '<td><b>' + data.Label_to + '<td><b>' + data.Label_creator + '<td><b>' + data.Label_transaction;
        for (key in data.history) {
            var item = data.history[key];

            output += '<tr><td>'
            if (data.hasOwnProperty('last')) {
                output += '<span style="font-size:1.2em;">' + item.text + '</span>';
            } else {
                output += '<span style="font-size:1.3em;">' + item.text + '</span>';
            }
            output += '<td>';
            if (item.hasOwnProperty('beginTimestamp')) {
                output += convertTimestamp(item.beginTimestamp, true);
            }
            output += '<td>';
            if (item.hasOwnProperty('endTimestamp')) {
                output += convertTimestamp(item.endTimestamp, true);
            }
            output += '<td> <a href ="?address=' +
                item.creator + get_lang() + '">' + cutBlank(item.creator_name, 16) + '...</a>';
            output += '<td> <a href ="?tx=' +
                item.txBlock + '-' + item.txSeqNo + get_lang() + '">' + item.txBlock + '-' + item.txSeqNo + '</a>';

        }
    }

    output += '</table>';

    return output;
}

function person(data) {

    var output = lastBlock(data.lastBlock);
    output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1180">';
    output += '<tr><td align=left>';
    output += '<table><tr><td>';


    output += '<img src="data:image/gif;base64,' + data.img + '" width = "350" /></td><td style ="padding-left:20px">';
    output += data.Label_key + ': ' +'<a href=?person=' + data.key + get_lang() + '><b>' + data.key + '</b></a>, &nbsp&nbsp';
    output += data.Label_seqNo + ': ' +'<a href=?tx=' + data.seqNo + get_lang() + '><b>' + data.seqNo + '</b></a><br>';

    output += data.Label_name + ': &nbsp&nbsp <b>' + data.name + '</b><br>';
    output += data.Label_born + ': &nbsp&nbsp<b> ' + data.birthday + '</b>';
    if ('deathday' in data) {
        output += ', &nbsp&nbsp ' + data.Label_dead + ': &nbsp&nbsp<b> ' + data.deathday + '</b><br>'
    } else {
        output += '<br>';
    }
    output += data.Label_gender + ': &nbsp&nbsp<b> ' + data.gender + '</b><br>';

    if (data.era_balance_a) {
        output += '<h4>ERA: &nbsp&nbsp<u>A</u>:' + data.era_balance_a + '&nbsp&nbsp<u>B</u>:' + data.era_balance_b + '&nbsp&nbsp<u>C</u>:' + data.era_balance_c + '</h4>';
    }
    if (data.compu_balance) {
        output += '<h4>COMPU: &nbsp&nbsp <b>' + data.compu_balance + '</b></h4>';
    }

    if (data.lia_balance_a) {
        output += '<h5>' + data.Label_total_registered + ': <b>' + data.lia_balance_a + '</b>, ' + data.Label_total_certified + ': <b>' + data.lia_balance_b + '</b></h5></br>';
    }

    if (data.creator_name != "") {
        output += data.Label_creator + ': &nbsp&nbsp<a href ="?address=' + data.creator + get_lang() + '"><b> ' + data.creator_name + '</b></a><br>';
    } else {
        output += data.Label_creator + ': &nbsp&nbsp<a href ="?address=' + data.creator + get_lang() + '"><b> ' + data.creator + '</b></a><br>';
    }

    if (data.registrar_name != "") {
        output += data.Label_registrar + ': &nbsp&nbsp<a href ="?address=' + data.registrar + get_lang() + '"><b> ' + data.registrar_name + '</b></a><br>';
    } else {
        output += data.Label_registrar + ': &nbsp&nbsp<a href ="?address=' + data.registrar + get_lang() + '"><b> ' + data.registrar + '</b></a><br>';
    }

    output += data.Label_description + ':<br>' + fformat(data.description) + '<br>';

    output += '</td>';
    output += '</tr>';
    output += '</table>';

    //statuses
    if (data.Label_statuses) {
        output += '<br>' + data.Label_statuses + ':';
        output += '<table id=statuses BORDER=0 cellpadding=15 cellspacing=0 width="800"  class="table table-striped" style="border: 1px solid #ddd; word-wrap: break-word;" ><tr bgcolor="f1f1f1"><td><b>'
           + data.Label_Status_table_status + '<td><b>' + data.Label_Status_table_period + '<td><b>' + data.Label_Status_table_appointing + '<tr>';

        for (key in data.statuses) {
            output += '<tr ><td ><a href ="?person=' + data.key + '&status=' + data.statuses[key].status_key + get_lang() + '">' + data.statuses[key].status_name
                + '<td>' + data.statuses[key].status_period
                + '<td><a href ="?address=' + data.statuses[key].status_creator + get_lang() + '">' + data.statuses[key].status_creator_name + '</a><tr>';
        }
        output += '</table>';
    }

    // accounts
    if (data.Label_accounts) {
        output += '<br>' + data.Label_accounts + ':';
        output += '<table id=accounts BORDER=0 cellpadding=15 cellspacing=0 width="800"  class="table table-striped" style="border: 1px solid #ddd; word-wrap: break-word;" ><tr bgcolor="f1f1f1"><td><b>'
            + data.Label_accounts_table_address + '<td><b>' + data.Label_accounts_table_to_date + '<td><b>' + data.Label_accounts_table_verifier + '<tr>';

        for (key in data.accounts) {
            output += '<tr><td><a href = "?address=' + data.accounts[key].address + get_lang() + '">' + data.accounts[key].address + '</a><td>'
                + convertTimestamp(data.accounts[key].to_date, true) + '<td>';
             if (data.accounts[key].verifier_name != "") {
                 output += '<a href ="?address=' + data.accounts[key].verifier + get_lang() + '">' + data.accounts[key].verifier_name + '</a><br>';
             } else {
                 output += '<a href ="?address=' + data.accounts[key].verifier + get_lang() + '">' + data.accounts[key].verifier + '</a><tr>';
             }
        }
        output += '</table>';
    }

    //my persons
    if (data.Label_My_Persons) {
        output += '<br>' + data.Label_My_Persons + ':';
        output += '<table id=accounts BORDER=0 cellpadding=15 cellspacing=0 width="800"  class="table table-striped" style="border: 1px solid #ddd; word-wrap: break-word;" ><tr bgcolor="f1f1f1"><td><b>'
           + data.Label_accounts_table_date + '<td><b>' + data.Label_My_Person_key + '<td><b>' + data.Label_My_Persons_Name + '<tr>';

        key = 0;
        for (key in data.My_Persons) {

            output += '<tr><td><a href ="?tx=' + data.My_Persons[key].seqNo + get_lang() + '">' + convertTimestamp(data.My_Persons[key].timestamp, true) + '</a>'
             + '<td>' + data.My_Persons[key].key + '<td><a href ="?person=' + data.My_Persons[key].key + get_lang() + '">' + data.My_Persons[key].name + '</a><tr>';
        }
        output += '</table>';
    }

    output += '</table>';
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
            item.key + get_lang() + '>' + escapeHtml(item.name) + '</a>';
        output += '<td>' + escapeHtml(item.description.substr(0, 100)) + '</td>';
        output += '<td><a href=?address=' + item.owner + get_lang() + '>';
        if (item.hasOwnProperty('person'))
            output += '[' + item.person_key + ']' + escapeHtml(item.person);
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
