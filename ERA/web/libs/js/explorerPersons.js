function person_asset(data) {

    var output = lastBlock(data.lastBlock);

    if (!data.hasOwnProperty('person_key')) {
        output += '<h2>Not found</h2>';
        return output;
    }

    if (data.error != null) {
        return data.error;
    }

    output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1180">';
    output += '<tr><td align=left>';
    output += '<table><tr><td>';

    output += '<img src="data:image/gif;base64,' + data.person_img + '" width = "350" /></td><td style ="padding-left:20px">';

    output += '<h2><center>' + data.label_Balance_Pos + '</center></h2>';

    var sideName;
    if (data.side == 1)
        sideName = data.Label_TotalDebit;
    if (data.side == 2)
        sideName = data.Label_Left;
    if (data.side == 3)
        sideName = data.Label_TotalCredit;
    if (data.side == 4)
        sideName = data.Label_TotalForged;

    if (data.sum > 0) {
        output += '<b><span style="font-size:2em; color:#0cb70c"> &nbsp&nbsp ' + sideName + '</span></b><br>';
        output += '<img src="img/check-yes.png" style="height:110px">'
        output += '<b><span style="font-size:4em; color:#0cb70c"> &nbsp&nbsp +' + data.sum + '</span></b><br>';
        output += '<br>';
    } else if (data.sum == 0) {
        output += '<b><span style="font-size:2em; color:crimson"> &nbsp&nbsp ' + sideName + '</span></b><br>';
        output += '<img src="img/check-no.png" style="height:120px">'
        output += '<br><br>';
    } else {
        output += '<b><span style="font-size:2em; color:crimson"> &nbsp&nbsp ' + sideName + '</span></b><br>';
        output += '<img src="img/check-no.png" style="height:120px">'
        output += '<b><span style="font-size:3em; color:crimson"> &nbsp&nbsp ' + data.sum + '</span></b><br>';
        output += '<br>';
    }

    output += data.Label_person + ': <a href ="?person=' +
        data.person_key + get_lang() + '">[' + data.person_key + ']' + data.person_name + '</a><br>';

    output += data.Label_asset + ': <a href ="?asset=' +
        data.asset_key + get_lang() + '">[' + data.asset_key + ']' + data.asset_name + '</a><br>';

    output += '<span style="font-size:1.2em">' + data.Label_Sides + ': &nbsp&nbsp ';

    if (data.side == '1')
        output +=  ': &nbsp&nbsp <span style="font-size:1.2em; color:#0cb70c"> &nbsp&nbsp ' + data.Label_TotalDebit + '</span></b>';
    else
        output +=  ': &nbsp&nbsp <a href ="?person=' + data.person_key + '&asset=' + data.asset_key + '&position=' + data.position + '&side=1' + get_lang()
                                 + '">' + data.Label_TotalDebit + '</a>';

    if (data.side == '2')
        output +=  ' &nbsp&nbsp <span style="font-size:1.2em; color:#0cb70c"> &nbsp&nbsp ' + data.Label_Left + '</span></b>';
    else
        output +=  ' &nbsp&nbsp <a href ="?person=' + data.person_key + '&asset=' + data.asset_key + '&position=' + data.position + '&side=2' + get_lang()
                                        + '">' + data.Label_Left + '</a>';

    if (data.side == '3')
        output +=  ' &nbsp&nbsp <span style="font-size:1.2em; color:#0cb70c"> &nbsp&nbsp ' + data.Label_TotalCredit + '</span></b>';
    else
        output +=  ' &nbsp&nbsp <a href ="?person=' + data.person_key + '&asset=' + data.asset_key + '&position=' + data.position + '&side=3' + get_lang()
                                        + '">' + data.Label_TotalCredit + '</a>';

    if (data.hasOwnProperty('Label_TotalForged')) {
        if (data.side == '4')
            output +=  ' &nbsp&nbsp <span style="font-size:1.2em; color:#0cb70c"> &nbsp&nbsp ' + data.Label_TotalForged + '</span></b>';
        else
            output +=  ' &nbsp&nbsp <a href ="?person=' + data.person_key + '&asset=' + data.asset_key + '&position=' + data.position + '&side=4' + get_lang()
                                           + '">' + data.Label_TotalForged + '</a>';
   }


    output +=  ' &nbsp&nbsp <span id="side-help" style="display:none;"><br>' + data.Side_Help + '</span>';
    output +=  ' &nbsp&nbsp <a href ="#" onclick="$(\'#side-help\').toggle();"><span class="glyphicon glyphicon-question-sign"></span></a>';
    output += '</span>';

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

    if (data.hasOwnProperty('error')) {
        output += '<br><h5>' + data.error + '</h5>';

        return output;
    }

    output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1180">';
    output += '<tr><td align=left>';
    output += '<table><tr style="vertical-align:top">';

    if (data.image.length > 0) {
        output += '<td><img src="data:image/gif;base64,' + data.image + '" width = "350" /></td><td style ="padding-left:20px">';
        output += '<br>';
    }

    output += '<h3><a href="?person=' + data.key + get_lang() + '"><h3 style="display:inline;">';
    if (false && data.icon.length > 0) output += ' <img src="data:image/gif;base64,' + data.icon + '" style="width:50px;" /> ';
    output += data.name + '</a></h3>';

    output += '<h4> [ <input id="key1" name="person" size="4" type="text" value="' + data.key + '" class="" style="font-size: 1em;"'
                   + ' onkeydown="if (event.keyCode == 13) buttonSearch(this)"> ] ';
    output += data.Label_seqNo + ': ' +'<a href=?tx=' + data.seqNo + get_lang() + '><b>' + data.seqNo + '</b></a></h4>';

    output += '<br>';

    output += '<h5>' + data.Label_born + ': &nbsp&nbsp<b> ' + data.birthday + '</b>';
    if ('deathday' in data) {
        output += ', &nbsp&nbsp ' + data.Label_dead + ': &nbsp&nbsp<b> ' + data.deathday + '</b>'
    } else {
        output += '</h5>';
    }
    output += '<h6>' + data.Label_gender + ': &nbsp&nbsp<b> ' + data.gender + '</b></h6>';

    if (data.era_balance_a) {
        output += '<h5>ERA: &nbsp&nbsp<u>A</u>:<b>' + data.era_balance_a + '</b>&nbsp&nbsp<u>B</u>:<b>'
            + data.era_balance_b + '</b>&nbsp&nbsp<u>C</u>:<b>' + data.era_balance_c + '</b></h5>';
    }
    if (data.compu_balance) {
        output += '<h5>COMPU: &nbsp&nbsp <b>' + data.compu_balance + '</b></h5>';
    }

    if (data.lia_balance_a) {
        output += '<h5>' + data.Label_total_registered + ': <b>' + data.lia_balance_a + '</b>, ' + data.Label_total_certified + ': <b>' + data.lia_balance_b + '</b></h5>';
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

    output += '<h5>' + data.Label_description + ':</h5>' + fformat(data.description) + '<br>';

    output += '</td>';
    output += '</tr>';
    output += '</table>';

    //statuses
    if (data.Label_statuses) {
        output += '<br>' + data.Label_statuses + ':';
        output += '<table id=statuses BORDER=0 cellpadding=15 cellspacing=0 width="800"  class="table table-striped" style="border: 1px solid #ddd; word-wrap: break-word;" ><tr bgcolor="f1f1f1"><td><b>'
           + data.Label_Status_table_status + '<td><b>' + data.Label_Status_table_period + '<td><b>' + data.Label_Status_table_appointing + '<tr>';

        for (key in data.statuses) {
            output += '<tr ><td ><a href ="?person=' + data.key + '&status=' + data.statuses[key].status_key + get_lang() + '">'
                + '<img src="data:image/gif;base64,' + data.statuses[key].status_icon + '" style="width:3em;"/> '
                + data.statuses[key].status_name
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
