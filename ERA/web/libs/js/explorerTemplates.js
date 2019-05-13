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
        output += pagesComponentBeauty(start, data.Label_Templates, data.numberLast, numberShiftDelta, 'start');
    }

    output += '<table width="1280" border=0><tr><td align=left><br>';

    output += '<table width=80% BORDER=0 cellpadding=10 cellspacing=0 class="table table-striped" style="border: 1px solid #ddd;">';
    output += '<thead><tr><td><b>' + data.label_table_key + ': ' + data.label_table_name + '</b></td><td><b>' + data.label_table_description + '</b></td><td><b>' + data.label_table_creator + '</b></td></tr></thead>';

    //Отображение таблицы элементов шаблонов
    var length = Object.keys(data.Templates).length;
    for (var i = length - 1; i >= 0; i--) {
        output += '<tr><td><a href="?template=' + data.Templates[i].key + get_lang() + '">' + data.Templates[i].key + ': ';
        output += '<b>' + data.Templates[i].name + '</b></a></td>';
        output += '<td>' + data.Templates[i].description.substr(0, 100) + '</td>';
        output += '<td><a href=?addr=' + data.Templates[i].owner + get_lang() + '>' + htmlFilter(data.Templates[i].owner) + '</a></td>';
        output += '</tr>';
    }

    if (!notDisplayPages) {
        //Отображение ссылки предыдущая
        output += '<tr><td colspan=4>';
        if (start > 1) {
            output += '<a href=?templates&start=' +
                (start - numberShiftDelta) + get_lang() + '>' + data.Label_Previous;
        }
        //Отображение ссылки следующая
        output += '<td colspan=4 align=right>';
        if (data.numberLast > start) {
            output += '<a href=?templates&start=' +
                (start + numberShiftDelta) + get_lang() + '>' + data.Label_Later;
        }
        output += '</table></td></tr></table>';
        //Отображение компонента страниц(снизу)
        output += pagesComponentBeauty(start, data.Label_Statuses, data.numberLast, numberShiftDelta, 'start');
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


    output += '<b>' + data.label_Creator + ':</b> <a href=?addr=' + data.template.owner + get_lang() + '>' + data.template.owner + '</a>';

    output += '<br><br>';


    output += '<b>' + data.label_Description + ':</b><br>';

    output += fformat(data.template.description); //wordwrap(data.template.description, 80, '\n', true);

    return output;
}
