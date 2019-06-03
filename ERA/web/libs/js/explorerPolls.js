function polls(data){

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

    if (!notDisplayPages) {
        output += pagesComponent2(data);
    }
    output += '<table width="1280" border=0><tr><td align=left><br>';
    output += '<table width=80% BORDER=0 cellpadding=10 cellspacing=0 class="table table-striped" style="border: 1px solid #ddd;">';
    output += '<thead><tr><td><b>'+ data.label_table_key  +': ' +
        data.label_table_name + '</b></td><td><b>' + data.label_table_description +
        '</b></td><td><b>' + data.label_table_total_votes +
        '</b></td><td><b>' + data.label_table_options_count +
        '</b></td><td><b>' + data.label_table_creator + '</b></td></tr></thead>';

    //Отображение таблицы элементов статусов
    for (var i in data.pageItems) {
        var item = data.pageItems[i];
        output += '<tr><td><a href="?poll=' + item.key + get_lang() + '">' + item.key + ': ';
        output += '<b>' + escapeHtml(item.name) + '</b></a></td>';
        output += '<td>' + escapeHtml(item.description.substr(0, 100)) + '</td>';
        output += '<td>' + item.totalVotes + '</td>';
        output += '<td>' + item.optionsCount + '</td>';

        output += '<td><a href=?address=' + item.owner + get_lang() + '>';
        if (item.hasOwnProperty('person'))
            output += '[' + item.person_key + ']' + escapeHtml(item.person);
        else
            output += item.owner;
        output += '</a></td></tr>';
    }
    if (!notDisplayPages) {
        //Отображение ссылки предыдущая
        output += '</table></td></tr></table>';
        output += pagesComponent2(data);
    }

    return output;
}

function poll(data) {

    var output = '';

    if (data.hasOwnProperty('error')) {
        return '<h2>' + data.error + '</h2>';
    }

    output += lastBlock(data.lastBlock);


    output += '<table width="1280" border=0><tr><td align=left><br>';

    output += '<h3 style="display:inline;"><b>' + data.label_Poll + ':</b>';

    output += ' [ <input id="key1" name="poll" size="4" type="text" value="' + data.poll.key + '" class="" style="font-size: 1em;"'
                   + ' onkeydown="if (event.keyCode == 13) buttonSearch()"> ] ';

    output += '<a href="?poll=' + data.poll.key + get_lang() + '">';
    //output += getAssetName2(data.poll.key, data.poll.name) + '</a></h3>';
    output += data.poll.name + '</a></h3>';

    output += '<br><br>';

    output += '<h4 style="display:inline;"><b>' + data.label_Asset + ':</b>';
    output += ' [ <input id="key2" name="asset" size="4" type="text" value="' + data.assetKey + '" class="" style="font-size: 1em;"'
                   + ' onkeydown="if (event.keyCode == 13) buttonSearch()"> ] ';
    output += '<a href ="?asset=' +  data.assetKey + get_lang() + '">' + data.assetName + '</a></h4>';

    output += '<br>';

    output += '<b>' + data.label_Owner + ':</b> <a href=?address=' + data.poll.owner + get_lang() + '>' + data.poll.owner + '</a>';

    output += '<br>';

    output += '<b>' + data.label_Description + ':</b><br>';
    output += fformat(data.poll.description);

    output += '<hl>';

    output += '<table width="1280" border=0><tr><td align=left><br>';
    output += '<table width=80% BORDER=0 cellpadding=10 cellspacing=0 class="table table-striped" style="border: 1px solid #ddd;">';
    output += '<thead><tr><td><b>'+ data.label_table_key  +' - ' +
        data.label_table_option_name +
        '</b></td><td><b>' + data.label_table_person_votes +
        '</b></td><td><b>%%' +
        '</b></td><td><b>' + data.label_table_option_votes +
        '</b></td><td><b>%%</b></td></tr></thead>';

    for (var i in data.poll.votes) {
        var item = data.poll.votes[i];
        output += '<tr><td><b>' + i + ' - ' + item.name + ':</b></td>';
        output += '<td>' + item.persons + '</td>';
        output += '<td>' + (100.0 * item.persons / data.poll.personsTotal).toPrecision(6)  + '</td>';
        output += '<td>' + item.votes + '</td>';
        output += '<td>' + (100.0 * item.votes / data.poll.votesTotal).toPrecision(6) + '</td>';

        output += '</td></tr>';
    }

    output += '<tr><td><b>' + data.label_Total + '</b></td>';
    output += '<td>' + data.poll.personsTotal + '</td>';
    output += '<td><td>' + data.poll.votesTotal + '</td><td>';

    output += '</td></tr>';

    return output;
}
