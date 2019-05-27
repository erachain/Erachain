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
        output += '<b>' + item.name + '</b></a></td>';
        output += '<td>' + item.description.substr(0, 100) + '</td>';

        output += '<td><a href=?address=' + item.owner + get_lang() + '>';
        if (item.hasOwnProperty('person'))
            output += '[' + item.person_key + ']' + htmlFilter(item.person);
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

    var output = '';

    if (data.hasOwnProperty('error')) {
        return '<h2>' + data.error + '</h2>';
    }

    output += lastBlock(data.lastBlock);

    output += '<table width="1280" border=0><tr><td align=left><br>';

    output += '<h3 style="display:inline;">' + data.label_Template + ':</h3>';

    //output += '<h3 style="display:inline;"> | </h3>';

    output += '<a href="?template=' + data.template.key + get_lang() + '"><h3 style="display:inline;">';
    output += getAssetName2(data.template.key, data.template.name) + '</h3></a>';

    output += '<br><br>';

    output += '<b>' + data.label_Key + ':</b> ' + data.template.key;

    output += '<br><br>';


    output += '<b>' + data.label_Creator + ':</b> <a href=?address=' + data.template.owner + get_lang() + '>' + data.template.owner + '</a>';

    output += '<br><br>';


    output += '<b>' + data.label_Description + ':</b><br>';

    output += fformat(data.template.description); //wordwrap(data.template.description, 80, '\n', true);

    return output;
}
