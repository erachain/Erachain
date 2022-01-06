function makePageUri(page, linkName) {
    // parse url
    var urlParams;
    var match,
        pl = /\+/g,  // Regex for replacing addition symbol with a space
        search = /([^&=]+)=?([^&]*)/g,
        decode = function (s) {
            return decodeURIComponent(s.replace(pl, " "));
        },
        query = window.location.search.substring(1);

    urlParams = {};
    while (match = search.exec(query))
        urlParams[decode(match[1])] = decode(match[2]);

    urlParams[linkName] = page;
    var uri = '';

    for (var paramKey in urlParams) {
        if (uri === '') {
            uri += '?';
        } else {
            uri += '&';
        }

        uri += paramKey + '=' + encodeURIComponent(urlParams[paramKey]);
    }

    return uri;
}

function makePageUri2(pageKey, offset, parsAdds) {
    // parse url
    var urlParams;
    var match,
        pl = /\+/g,  // Regex for replacing addition symbol with a space
        search = /([^&=]+)=?([^&]*)/g,
        decode = function (s) {
            return decodeURIComponent(s.replace(pl, " "));
        },
        query = window.location.search.substring(1);

    urlParams = {};
    while (match = search.exec(query))
        urlParams[decode(match[1])] = decode(match[2]);

    if (pageKey == 0)
        urlParams['pageKey'] = null;
    else
        urlParams['pageKey'] = pageKey;

    if (offset == 0)
        urlParams['offset'] = null;
    else
        urlParams['offset'] = offset;

    if (parsAdds)
        for (var paramKey in parsAdds) {
            if (parsAdds[paramKey])
                urlParams[paramKey] = parsAdds[paramKey];
            else
                delete urlParams[paramKey];
        }

    var uri = '';

    for (var paramKey in urlParams) {
        if (urlParams[paramKey] == null)
            continue;

        if (uri === '') {
            uri += '?';
        } else {
            uri += '&';
        }

        uri += paramKey + '=' + encodeURIComponent(urlParams[paramKey]);

    }

    return uri;
}

function pagesComponent(data) {
    var output = '';

    if (data.pageCount > 1) {
        output += 'Pages: ';
        for (var page = 1; page <= data.pageCount; page++) {
            if (page == data.pageNumber) {
                output += '<b>' + page + '</b>&nbsp;';
            } else {
                output += '<a href="' + makePageUri(page, 'page') + '">' + page + '</a>&nbsp;';
            }
        }
    }

    return output;
}

function pagesComponent2(data, uriAdds) {
    var output = '';

    var listSize = 0 + data.listSize;
    var pageSize = 0 + data.pageSize;

    if (data.hasOwnProperty('useoffset')) {
        // в начало прыгнуть

        var uriAddsEmpty = {};
        for (var paramKey in uriAdds) {
                uriAddsEmpty[paramKey] = null;
        }
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri2(0, 0, uriAddsEmpty) + '"><b><span class="glyphicon glyphicon-fast-backward"></span></b></a>';

        var pageFromKey = data.pageFromKey;
        if (pageFromKey != null) {
            // это не самое начало значит можно скакать вверх
            output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri2(pageFromKey, -pageSize - 1, uriAdds) + '"><b><span class="glyphicon glyphicon-triangle-left"></span></b></a>';
        }

        output += '&emsp; [ <input size="10" type="text" value="' + (pageFromKey == null? '-' : pageFromKey) + '" class="" style="font-size: 1em;"'
                   + ' onkeydown="if (event.keyCode == 13) document.location = makePageUri2(this.value.trim(), 0)"> ] ';

        var pageToKey = data.pageToKey;
        if (pageToKey != null) {
            // листнуть ниже
            output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri2(pageFromKey, pageSize, uriAdds) + '"><b><span class="glyphicon glyphicon-triangle-right"></span></b></a>';
        }

        // в конец прыгнуть
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri2(0, -pageSize, uriAddsEmpty) + '"><b><span class="glyphicon glyphicon-fast-forward"></span></b></a>';

        return output;
    }

    if (data.hasOwnProperty('start')) {
        var start = data.start;
    } else {
        var start = listSize;
    }

    if (start == 0)
        start = listSize;

    if (start != listSize)
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(listSize, 'start') + '"><b><span class="glyphicon glyphicon-fast-backward"></span></b></a>';

    if (start + pageSize * 10 < listSize) {
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(start + pageSize * 10, 'start') + '"><b><span class="glyphicon glyphicon-backward"></span></b></a>';
    }

    if (start + pageSize < listSize) {
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(start + pageSize, 'start') + '"><b><span class="glyphicon glyphicon-triangle-left"></span></b></a>';
    }

    output += '&emsp; <a class="button ll-blue-bgc active" href="' + makePageUri(start, 'start') + '"><b> ' + start + ' </b></a>';

    if (start > pageSize + 1) {
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(start - pageSize, 'start') + '"><b><span class="glyphicon glyphicon-triangle-right"></span></b></a>';
        //output += (start - pageSize) + ' - ';
    }

    if (start > pageSize * 10) {

        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(start - pageSize * 10, 'start') + '"><b><span class="glyphicon glyphicon-forward"></span></b></a>';
        //output += (start - pageSize * 10) + ' --- ';
    }

    if (start > pageSize)
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(pageSize, 'start') + '"><b><span class="glyphicon glyphicon-fast-forward"></span></b></a>';

    return output;
}

function pagesComponent2forward(data) {
    var output = '';

    var listSize = data.listSize;
    var pageSize = data.pageSize;
    var start = data.start;

    if (data.hasOwnProperty('start')) {
        start = data.start;
    } else {
        var start = listSize;
    }

    if (start == 0)
        start = 1;

    if (1 != start)
        output += '<a class="button ll-blue-bgc" href="' + makePageUri(1, 'start') + '"><b>' + '1</b></a>';

    if (start > pageSize * 10) {

        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(start - pageSize * 10, 'start') + '"><b>' + (start - pageSize * 10) + '</b></a>';
        //output += (start - pageSize * 10) + ' --- ';
    }
    if (start > pageSize + 1) {
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(start - pageSize, 'start') + '"><b>' + (start - pageSize) + '</b></a>';
        //output += (start - pageSize) + ' - ';
    }

    output += '&emsp; <a class="button ll-blue-bgc active" href="' + makePageUri(start, 'start') + '"><b> ' + start + ' </b></a>';

    if (start + pageSize < listSize) {
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(start + pageSize, 'start') + '"><b>' + (start + pageSize) + '</b></a>';
    }
    if (start + pageSize * 10 < listSize) {
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(start + pageSize * 10, 'start') + '"><b>' + (start + pageSize * 10) + '</b></a>';
    }

    if (listSize != start)
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(listSize, 'start') + '"><b>' + listSize + '</b></a>';

    return output;
}

function pagesComponent3(data) {
    var output = '';

    var listSize = data.listSize;
    var pageSize = data.pageSize;
    var start = data.start;

    if (data.hasOwnProperty('start')) {
        start = data.start;
    } else {
        var start = listSize;
    }

    if (start == 0)
        start = 1;

    if (start == 1) {
        output += '<a class="button ll-blue-bgc active" href="' + makePageUri(1, 'start') + '"><b>' + 'last</b></a>';
    } else {
        output += '<a class="button ll-blue-bgc" href="' + makePageUri(1, 'start') + '"><b>' + 'last</b></a>';
    }

    if (start > pageSize * 10) {
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(start - pageSize * 10, 'start') + '"><b>' + '&lt;&lt;' + '</b></a>';
    }

    if (start > pageSize + 1) {
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(start - pageSize, 'start') + '"><b>' + '&lt;' + '</b></a>';
    }

    if (start != 1 && start < listSize - pageSize) {
        output += '&emsp; <a class="button ll-blue-bgc active" href="' + makePageUri(start, 'start') + '"><b> -' + start + ' </b></a>';
    }

    if (start + pageSize < listSize) {
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(start + pageSize, 'start') + '"><b>' + '&gt;' + '</b></a>';
    }
    if (start + pageSize * 10 < listSize) {
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(start + pageSize * 10, 'start') + '"><b>' + '&gt;&gt;' + '</b></a>';
    }

    if (start >= listSize - pageSize) {
        if (start < listSize) {
            output += '&emsp; <a class="button ll-blue-bgc active" href="' + makePageUri(listSize - pageSize, 'start') + '"><b>' + '-' + (listSize - pageSize) + '</b></a>';
        }
    } else {
        output += '&emsp; <a class="button ll-blue-bgc" href="' + makePageUri(listSize - pageSize, 'start') + '"><b>' + '-' + (listSize - pageSize) + '</b></a>';
    }

    return output;
}


function pagesComponentMixed(data) {
    if (data.pageCount < 10) {
        return pagesComponent(data);
    } else if (data.pageCount >= 10) {
        return pagesComplexComponent(data);
    }
}

function pagesComplexComponent(data) {
    var output = '';
    if (data.pageCount > 1) {
        output += 'Pages: ';
        //В начале пагинация первых 3ех
        for (var page = 1; page <= 3; page++) {
            if (page == data.pageNumber) {
                output += '<b>' + page + '</b>&nbsp;';
            } else {
                output += '<a href="' + makePageUri(page, 'page') + '">' + page + '</a>&nbsp;';
            }
        }
        output += '...';
        //пагинация от текущего

        for (var page = data.pageNumber - 2; page <= data.pageNumber + 2; page++) {
            if (page > 2 && page < data.pageCount - 2) {
                if (page == data.pageNumber) {
                    output += '<b>' + page + '</b>&nbsp;';
                } else {
                    output += '<a href="' + makePageUri(page, 'page') + '">' + page + '</a>&nbsp;';
                }
            }
        }
        output += '...';
        //В конце пагинация последних 3ех
        for (var page = data.pageCount - 2; page <= data.pageCount; page++) {
            if (page == data.pageNumber) {
                output += '<b>' + page + '</b>&nbsp;';
            } else {
                output += '<a href="' + makePageUri(page, 'page') + '">' + page + '</a>&nbsp;';
            }
        }
    }
    return output;
}


function pageCreation(from, to, step, restriction, start, linkName) {
    var output = '';
    for (var page = from; page > to; page -= step) {
        if (page >= step) {
            if (page >= 1 && page <= restriction) {
                if (page === start) {
                    output += '<b>' + page + '</b>&nbsp;';
                    continue;
                }
                output += '<a href="' + makePageUri(page, linkName) + '">' + page + '</a>&nbsp;';

            }
        }
    }
    return output;
}

function pagesComponentBeauty(start, label, numberLast, step, linkName) {
    var output = '';
    var delta = 5;
    var numberPages = 3;
    if (start >= 1) {
        output += label + ':';
        output += pageCreation(numberLast, numberLast - (numberPages - 2) * step + 1, step, numberLast, start, linkName);
        output += '...';
        output += pageCreation(start + step * delta, (start - step * delta), step, numberLast - 1, start, linkName);
        output += '...';
        output += pageCreation(numberPages * step, 1, step, numberLast, start, linkName);
    }
    return output;
}

function filterTX() {
    if ($('#useForge').is(':checked')) {
        window.location = updateURLParameter(window.location.href, 'forge', 'yes')
    } else {
        window.location = updateURLParameter(window.location.href, 'forge', 'no')
    }

    //window.location.reload(true);

}

function transactions_Table(data) {

    var output = '';
    //console.log("data=")
    //console.log(data)
    output += '<h4>' + data.Transactions.Label_transactions_table + '</h4>';

    var useForgeChecked = getQueryParams('forge') == 'yes'? 'checked' : '';

    output += '<input id="useForge" type="checkbox" name="option1" value="useForge" ' + useForgeChecked + ' onClick="filterTX()">' + data.Transactions.Label_useForge + '<br>';

    output += pagesComponent2(data);

    output += '<table id="transactions" id=accounts BORDER=0 cellpadding=15 cellspacing=0 width="800" ' +
        ' class="tiny table table-striped" style="border: 1px solid #ddd; word-wrap: break-word;" >';

    output += '<tr bgcolor="f1f1f1"><td><b>' + data.Transactions.Label_seqNo + '<td><b>' +
        data.Transactions.Label_title + '<td><b>' + data.Transactions.Label_type_transaction + '<td><b>' +
        data.Transactions.Label_amount_key + '<td><b>' + data.Transactions.Label_date + '<td><b>' +
        data.Transactions.Label_atside + '<td><b>' + data.Transactions.Label_size + '<td><b>' +
        data.Transactions.Label_fee + '</tr>';
         //+ '<td><b>' + data.Transactions.Label_confirmations + '</tr>';

    for (key in data.Transactions.transactions) {
        var item = data.Transactions.transactions[key];
        output += '<tr><td><a href ="?tx=' + item.block + '-'
            + item.seqNo + get_lang() + '">' + item.block + '-' + item.seqNo + '</a><td>';

        if (item.title != null) {
            output += '<a href="?tx=' + item.signature + get_lang() + '">'
                + escapeHtml(cutBlank(item.title, 40)) + '</a><td>';
        } else {
            output += '<td>';
        }

        output += item.type + item.type_name + '<td>';

        if (item.hasOwnProperty('amount')) {
            output += item.amount + ' ';
        }

        if (item.hasOwnProperty('itemKey')) {

            if (item.hasOwnProperty('itemName')) {
                output += '<a href ="?' + item.itemType + '=' + item.itemKey + get_lang() + '">' +
                    cut(cutBlank(item.itemName, 40), 35) + '</a>';
            } else {
                output += '[' + item.itemKey + ']';
            }
        }

        output += '<td>' + convertTimestamp(item.timestamp, true);
        output += '<td><a href ="?address=' + item.creator_addr + get_lang() + '">' +
            cut(cutBlank(item.creator, 40), 35) + '</a><td>';
        output += item.size
            + '<td>'+ item.fee
            //+ '<td>' + item.confirmations + '</td>'
            + '</tr>';

    }
    output += '</table></td></tr></table>';
    output += pagesComponent2(data);


    return output;

}