function polls(data){

    var output = lastBlock(data.lastBlock);

    if(data.hasOwnProperty('error'))
    {
        return '<h2>' + data.error + '</h2>';
    }

    var notDisplayPages = data.notDisplayPages;
    var numberShiftDelta = data.numberOfRepresentsItemsOnPage;
    //Отображение последнего блока
    output += lastBlock(data.lastBlock);
    var start = data.start;

    if (!notDisplayPages) {
        output += pagesComponent2(data);
    }
    output += '<table width="1280" border=0><tr><td align=left><br>';
    output += '<table width=80% BORDER=0 cellpadding=10 cellspacing=0 class="table table-striped" style="border: 1px solid #ddd;">';
    output += '<thead><tr><td><b>'+ data.Label_table_key  +': ' +
        data.Label_table_name + '</b></td><td><b>' + data.Label_table_description +
        '</b></td><td><b>' + data.Label_table_total_votes +
        '</b></td><td><b>' + data.Label_table_options_count +
        '</b></td><td><b>' + data.Label_table_creator + '</b></td></tr></thead>';

    //Отображение таблицы элементов статусов
    for (var i in data.pageItems) {
        var item = data.pageItems[i];
        output += '<tr><td><a href="?poll=' + item.key + get_lang() + '">' + item.key + ': ';
        output += makeMediaIcon(item, '', 'width:2em')

        output += '<b>' + escapeHtml(item.name) + '</b></a></td>';
        output += '<td>' + escapeHtml(item.description.substr(0, 100)) + '</td>';
        output += '<td>' + item.totalVotes + '</td>';
        output += '<td>' + item.optionsCount + '</td>';

        output += '<td><a href=?address=' + item.maker + get_lang() + '>';
        if (item.hasOwnProperty('person'))
            output += '[' + item.person_key + ']' + escapeHtml(item.person);
        else
            output += item.maker;
        output += '</a></td></tr>';
    }
    if (!notDisplayPages) {
        //Отображение ссылки предыдущая
        output += '</table></td></tr></table>';
        output += pagesComponent2(data);
    }

    return output;
}

function poll(data, forPrint) {

    var output = '';

    if (!forPrint)
        output += lastBlock(data.lastBlock);

    if (!data.hasOwnProperty('item')) {
        output += '<h2>Not found</h2>';
        return output;
    }

    if (data.hasOwnProperty('error')) {
        output += '<br><h5>' + data.error + '</h5>';

        return output;
    }

    output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1300">';
    output += '<tr><td align=left>';
    output += '<table><tr style="vertical-align:top">';

    var item = data.item;
    ////// HEAD
    output += itemHead(item, forPrint);

    //////// BODY
    output += '<p style="font-size:1.3em">';


    output += '<h4 style="display:inline;"><b>' + data.Label_Asset + ':</b>';
    output += ' [ <input id="key2" name="asset" size="8" type="text" value="' + data.assetKey + '" class="" style="font-size: 1em;"'
                   + ' onkeydown="if (event.keyCode == 13) buttonSearch(this)"> ] ';
    output += '<a href ="?asset=' +  item.assetKey + get_lang() + '">' + data.assetName + '</a></h4>';

    output += '<br>';

    output += '</p>';

    //// FOOT
    output += itemFoot(item, forPrint);

    output += '<hl>';

    output += '</td>';
    output += '</tr>';
    output += '</table>';

    output += '<table width="1280" border=0><tr><td align=left><br>';
    output += '<table width=80% BORDER=0 cellpadding=10 cellspacing=0 class="table table-striped" style="border: 1px solid #ddd;">';
    output += '<thead><tr><td><b>'+ data.Label_table_key  +' - ' +
        data.Label_table_option_name +
        '</b></td><td><b>' + data.Label_table_person_votes +
        '</b></td><td><b>%%' +
        '</b></td><td><b>' + data.Label_table_option_votes +
        '</b></td><td><b>%%</b></td></tr></thead>';

    var number = 1;
    var votes = data.votes;
    for (var i in votes) {
        var item = votes[i];
        var voteNo = i * 1 + 1;
        output += '<tr><td><b>' + number++ + ' - ' + item.name + ':</b></td>';
        output += '<td>' + item.persons + '</td>';
        output += '<td>' + (data.personsTotal > 0? (100.0 * item.persons / data.personsTotal).toPrecision(6) : '--')  + '</td>';
        output += '<td><a href=?q=' + data.charKey + '%20%23%23' + voteNo + get_lang() + '&search=transactions class="button ll-blue-bgc"><b>' + item.votes + '</b></a></td>';
        output += '<td>' + (data.votesTotal? (100.0 * item.votes / data.votesTotal).toPrecision(6) : '--') + '</td>';

        output += '</td></tr>';
    }

    output += '<tr><td><b>' + data.Label_Total + '</b></td>';
    output += '<td>' + data.personsTotal + '</td>';
    output += '<td><td>' + data.votesTotal + '</td><td>';

    output += '</td></tr>';

    return output;
}
