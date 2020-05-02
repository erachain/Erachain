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
    output += '<thead><tr><td><b>'+ data.label_table_key  +': ' +
        data.label_table_name + '</b></td><td><b>' + data.label_table_description +
        '</b></td><td><b>' + data.label_table_total_votes +
        '</b></td><td><b>' + data.label_table_options_count +
        '</b></td><td><b>' + data.label_table_creator + '</b></td></tr></thead>';

    //Отображение таблицы элементов статусов
    for (var i in data.pageItems) {
        var item = data.pageItems[i];
        output += '<tr><td><a href="?poll=' + item.key + get_lang() + '">' + item.key + ': ';
        if (item.icon.length > 0)
            output += '<img src="data:image/gif;base64,' + item.icon + '" style="width:2em;" /> ';
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

    var output = lastBlock(data.lastBlock);

    if (!data.hasOwnProperty('poll')) {
        output += '<h2>Not found</h2>';
        return output;
    }

    if (data.hasOwnProperty('error')) {
        output += '<br><h5>' + data.error + '</h5>';

        return output;
    }

    output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1180">';
    output += '<tr><td align=left>';
    output += '<table><tr style="vertical-align:top">';

    if (data.poll.image.length > 0) {
        output += '<td><img src="data:image/gif;base64,' + data.poll.image + '" width = "350" /></td><td style ="padding-left:20px">';
        output += '<br>';
    }

    output += '<h3 style="display:inline;"><a href="?poll=' + data.poll.key + get_lang() + '">';
    if (data.poll.icon.length > 0) output += ' <img src="data:image/gif;base64,' + data.poll.icon + '" style="width:50px;" />';
    output += data.poll.name + '</a></h3>';

    output += '<br>';

    //output += '<b>' + data.poll.label_Key + ':</b> ' + data.poll.key;
    //output += data.label_Key + ': ' +'<a href=?poll=' + data.poll.key + get_lang() + '><b>' + data.poll.key + '</b></a>, &nbsp&nbsp';


    output += '<h3 style="display:inline;"><b>' + data.label_Poll + ':</b>';

    output += ' [ <input id="key1" name="poll" size="4" type="text" value="' + data.poll.key + '" class="" style="font-size: 1em;"'
                   + ' onkeydown="if (event.keyCode == 13) buttonSearch(this)"> ] ';

    output += '<a href=?tx=' + data.poll.seqNo + get_lang() + ' class="button ll-blue-bgc"><b>' + data.poll.seqNo + '</b></a>';
    output += ' ' +'<a href=?q=' + data.charKey + get_lang() + '&search=transactions class="button ll-blue-bgc"><b>' + data.label_Actions + '</b></a></h4>';

    output += '<h4 style="display:inline;"><b>' + data.label_Asset + ':</b>';
    output += ' [ <input id="key2" name="asset" size="4" type="text" value="' + data.assetKey + '" class="" style="font-size: 1em;"'
                   + ' onkeydown="if (event.keyCode == 13) buttonSearch(this)"> ] ';
    output += '<a href ="?asset=' +  data.assetKey + get_lang() + '">' + data.assetName + '</a></h4>';

    output += '<br>';

    output += '<b>' + data.label_Owner + ':</b> <a href=?address=' + data.poll.owner + get_lang() + '>' + data.poll.owner + '</a>';

    output += '<br>';

    output += '<b>' + data.label_Description + ':</b><br>';
    output += fformat(data.poll.description);

    output += '<hl>';

    output += '</td>';
    output += '</tr>';
    output += '</table>';

    output += '<table width="1280" border=0><tr><td align=left><br>';
    output += '<table width=80% BORDER=0 cellpadding=10 cellspacing=0 class="table table-striped" style="border: 1px solid #ddd;">';
    output += '<thead><tr><td><b>'+ data.label_table_key  +' - ' +
        data.label_table_option_name +
        '</b></td><td><b>' + data.label_table_person_votes +
        '</b></td><td><b>%%' +
        '</b></td><td><b>' + data.label_table_option_votes +
        '</b></td><td><b>%%</b></td></tr></thead>';

    var number = 1;
    for (var i in data.poll.votes) {
        var item = data.poll.votes[i];
        output += '<tr><td><b>' + number++ + ' - ' + item.name + ':</b></td>';
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
