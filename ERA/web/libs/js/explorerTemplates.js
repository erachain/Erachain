function templates(data) {

    var output = '';

    if (data.hasOwnProperty('error')) {
        return '<h2>' + data.error + '</h2>';
    }

    console.log('data=')
    console.log(data)


    var notDisplayPages = data.notDisplayPages;
    var numberShiftDelta = data.numberOfRepresentsItemsOnPage;
    //Отображение последнего блока
    output += lastBlock(data.lastBlock);
    var start = data.start;

    if (!notDisplayPages) {
        //Отображение компонента страниц(вверху)
        //output += pagesComponentBeauty(start, data.Label_Templates, data.numberLast, numberShiftDelta, 'start');
        output += pagesComponent2(data);

    }

    output += '<table width="1280" border=0><tr><td align=left><br>';

    output += '<table width=80% BORDER=0 cellpadding=10 cellspacing=0 class="table table-striped" style="border: 1px solid #ddd;">';
    output += '<thead><tr><td><b>' + data.label_table_key + ': ' + data.label_table_name + '</b></td><td><b>' + data.label_table_description + '</b></td><td><b>' + data.label_table_creator + '</b></td></tr></thead>';

    //Отображение таблицы элементов шаблонов
    for (var i in data.pageItems) {
        var item = data.pageItems[i];
        output += '<tr><td><a href="?template=' + item.key + get_lang() + '">' + item.key + ': ';
        if (item.icon.length > 0)
            output += '<img src="data:image/gif;base64,' + item.icon + '" style="width:2em;" /> ';
        output += '<b>' + escapeHtml(item.name) + '</b></a></td>';
        output += '<td>' + escapeHtml(item.description.substr(0, 100)) + '</td>';

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
        //Отображение компонента страниц(снизу)
        //output += pagesComponentBeauty(start, data.Label_Statuses, data.numberLast, numberShiftDelta, 'start');
        output += pagesComponent2(data);

    }

    return output;
}


function template(data) {

    var output = lastBlock(data.lastBlock);

    if (!data.hasOwnProperty('template')) {
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

    if (data.template.image.length > 0) {
        output += '<td><img src="data:image/gif;base64,' + data.template.image + '" width = "350" /></td><td style ="padding-left:20px">';
        output += '<br>';
    }

    output += '<h3 style="display:inline;"><a href="?template=' + data.template.key + get_lang() + '">';
    if (data.template.icon.length > 0) output += '<img src="data:image/gif;base64,' + data.template.icon + '" style="width:50px;"/> ';
    output += data.template.name + '</a></h3>';

    output += '<h4> [ <input id="key1" name="template" size="4" type="text" value="' + data.template.key + '" class="" style="font-size: 1em;"'
                   + ' onkeydown="if (event.keyCode == 13) buttonSearch(this)"> ] ';
    //output += data.template.Label_seqNo + ': ' +'<a href=?tx=' + data.template.seqNo + get_lang() + '><b>' + data.template.seqNo + '</b></a></h4>';
    output += '<a href=?tx=' + data.template.seqNo + get_lang() + ' class="button ll-blue-bgc"><b>' + data.template.seqNo + '</b></a>';
    output += ' ' +'<a href=?q=' + data.charKey + get_lang() + '&search=transactions class="button ll-blue-bgc"><b>' + data.label_Actions + '</b></a></h4>';

    output += '<br><br>';

    output += '<b>' + data.label_Creator + ':</b> <a href=?address=' + data.template.owner + get_lang() + '>' + data.template.owner + '</a>';

    output += '<br><br>';


    output += '<b>' + data.label_Description + ':</b><br>';

    output += fformat(data.template.description); //wordwrap(data.template.description, 80, '\n', true);

    return output;
}
