function statements(data) {

    var output = '';

    if (data.hasOwnProperty('error')) {
        return '<h2>' + data.error + '</h2>';
    }

    output += lastBlock(data.lastBlock);

    output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="">';
    output += '<tr><td align=left>';
//	output += '<a href=?unconfirmed'+get_lang()+'>' + data.Label_Unconfirmed_transactions+': '+data.unconfirmedTxs+'</a>.';

    output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1180"  class="table table-striped" style="border: 1px solid #ddd;">';
    output += '<tr><td>' + data.Label_No + '</td><td><b>' + data.Label_Timestamp + '<td><b>' + data.Label_block + ' <td><b>' + data.Label_Template + '<td><b>' + data.Label_Statement + '<td><b>' + data.Label_Creator;

    minHeight = (data.maxHeight <= 20) ? 1 : data.maxHeight - 20;

    for (var i in data.Statements) {
        output += '<tr><td>' + i;
        output += '<td>' + data.Statements[i].Timestamp;
        output += '<td><a href =?block=' + data.Statements[i].Block + get_lang() + '>' + data.Statements[i].Block + '</a>';
        output += '<td>' + data.Statements[i].Template;
        output += '<td><a href = ?statement=' + data.Statements[i].Block + '&seqNo=' + data.Statements[i].seqNo + get_lang() + '>';
        output += '<div style="word-wrap: break-word;  width: 300px;">' + data.Statements[i].Statement + '</div></a>';
        if (data.Statements[i].person_key != "") {
            output += '<td><a href =?person=' + data.Statements[i].person_key + get_lang() + '>' + data.Statements[i].Creator + '</a>';
        }
        else {
            output += '<td>' + data.Statements[i].Creator
        }
    }

    output += '<tr><td colspan=4>';
//	if(data.rowCount != data.lastBlock.height)
//	{
//		if(data.rowCount < data.start + 20)
//		{
//			output += '<a href=?startBlock='+(data.lastBlock.height)+get_lang()+'>'+data.Label_Later;
//		}
//		else
//		{
//			output += '<a href=?startBlock='+(data.maxHeight+21)+get_lang()+'>'+data.Label_Later;
//		}
//	}

//	output += '<td colspan=4 align=right>';
//	if(minHeight > 1) {
//		output += '<a href=?startBlock='+(data.maxHeight-21)+get_lang()+'>'+data.Label_Previous;
//	}

    return output;

}

function statements2(data) {

    var output = lastBlock(data.lastBlock);

    if (!data.hasOwnProperty('person_key')) {
        output += '<h2>Not found</h2>';
        return output;
    }

    if (data.hasOwnProperty('error')) {
        return '<h2>' + data.error + '</h2>';
    }

    output += '<div class="navbar-form">';
    output += '<label>' + data.Label_Statement + '</label>&nbsp;&nbsp;';
    output += '<div class="input-group">';
    output += '<label for="name" class="sr-only">Search</label>';
    output += '<input id="statement_search_q" size="55" type="text" value="" class="form-control" onkeydown="if (event.keyCode == 13) openStatement()">';
    output += '<span class="input-group-btn"> <button id="stbutton" type="button" class="btn btn-link" onclick="openStatement()"><span class="glyphicon glyphicon-search"></span><span class="sr-only">Search</span></button></span>';
    output += '</div>';
    output += '</div>';

    return output;
}

function openStatement() {
    var params = document.getElementById('statement_search_q').value.split('-');
    if (params.length > 1) {
        document.location.href = '?statement=' + params[0] + '&seqNo=' + params[1] + get_lang();
    }
}

function statement(data) {

    var output = lastBlock(data.lastBlock);

    if (!data.hasOwnProperty('type')) {
        output += '<h2>Not found</h2>';
        return output;
    }

    if (data.hasOwnProperty('error')) {
        return '<h2>' + data.error + '</h2>';
    }

    output += '<table><tr><td>';
    output += '<div style="word-wrap: break-word;  width: 1000px;">';
    output += data.Label_type + ':<b>' + data.type + '</b> &nbsp&nbsp';

    if (data.tx.hasOwnProperty("height")) {
        output += data.Label_block + ': <a href=?block=' + data.tx.height + get_lang() + '><b>' + data.tx.height + '</b></a>';
        output += ' &nbsp&nbsp' + data.Label_seqNo + ': <a href=?tx=' + data.tx.seqNo + get_lang() + '><b>' + data.tx.seqNo + '</b></a>';
    }
    output += ' &nbsp&nbsp' + data.Label_date + ': <b>' + convertTimestamp(data.tx.timestamp, true) + '</b>';
    output += ' &nbsp&nbsp' + data.Label_size + ': <b>' + data.tx.size + '</b>';
    output += ' &nbsp&nbsp' + data.Label_fee + ': <b>' + data.tx.fee + '</b>';
    output += '<br>' + data.Label_creator + ': <a href=?address=' + data.tx.creator + get_lang() + '><b>' + data.creator_name + '</b></a>';
    output += '<br>' + data.Label_pubKey + ': <b>' + data.tx.publickey + '</b>';

    output += '<br>' + data.Label_signature + ': <b>' + data.tx.signature + '</b>';

    if (data.hasOwnProperty('title')) {
        output += '<h3>' + data.Label_title + ': ' + escapeHtml(data.title) + '</h3>';
    }

    if (data.hasOwnProperty('exLink')) {
        output += '<h3>'
            + '<img src="img/parentTx.png" style="height:1.5em"> ' + data.exLink_Name + ' '
            + data.Label_Parent + ' <a href=?tx=' + data.exLink.ref + get_lang() + '><b>' + data.exLink.ref + '</b></a></h3>';
    }

    if (data.hasOwnProperty('exAction')) {
        var exAction = data.exAction;
        output += '<h3>'
            //+ '<img src="img/parentTx.png" style="height:1.5em"> '
            + data.Label_Action + '</h3>';

        if (exAction.typeName == 'Accruals') {
            if (exAction.hasOwnProperty('filteredAccrualsCount')) {
                output += exAction.Label_Counter + ': <b>' + exAction.filteredAccrualsCount + '</b><br>';
                output += exAction.Label_Total_Amount + ': <b>' + exAction.totalPay + '</b><br>';
                output += exAction.Label_Additional_Fee + ': <b>' + exAction.totalFee + '</b><br>';

            }

            output += '<h4>' + exAction.Label_Action_for_Asset + '</h4>';
            output += exAction.Label_assetKey + ': <b>' + exAction.assetKey + '</b><br>';
            output += exAction.Label_balancePos + ': <b>[' + exAction.balancePos + "]" + exAction.balancePosName + '</b><br>';
            output += exAction.Label_backward + ': <b>' + exAction.backward + '</b><br>';
            output += exAction.Label_payMethod + ': <b>[' + exAction.payMethod + "]" + exAction.payMethodName + '</b><br>';
            output += exAction.Label_payMethodValue + ': <b>' + exAction.payMethodValue + '</b><br>';
            if (exAction.hasOwnProperty('amountMin'))
                output += exAction.Label_amountMin + ': <b>' + exAction.amountMin + '</b><br>';
            if (exAction.hasOwnProperty('amountMax'))
                output += exAction.Label_amountMax + ': <b>' + exAction.amountMax + '</b><br>';

            output += '<h4>' + exAction.Label_Filter_By_Asset_and_Balance + '</h4>';
            output += exAction.Label_assetKey + ': <b>' + exAction.filterAssetKey + '</b><br>';
            output += exAction.Label_balancePos + ': <b>[' + exAction.filterBalancePos + "]" + exAction.filterBalancePosName + '</b><br>';
            output += exAction.Label_balanceSide + ': <b>[' + exAction.filterBalanceSide + "]" + exAction.filterBalanceSideName + '</b><br>';

            if (exAction.hasOwnProperty('filterBalanceMIN'))
                output += exAction.Label_filterBalanceMIN + ': <b>' + exAction.filterBalanceMIN + '</b><br>';
            if (exAction.hasOwnProperty('filterBalanceMAX'))
                output += exAction.Label_filterBalanceMAX + ': <b>' + exAction.filterBalanceMAX + '</b><br>';

            output += '<h4>' + exAction.Label_Filter_by_Actions_and_Period + '</h4>';
            if (exAction.filterTXType)
                output += exAction.Label_filterTXType + ': <b>[' + exAction.filterTXType + "]" + exAction.filterTXTypeName + '</b><br>';
            if (exAction.hasOwnProperty('filterTimeStart'))
                output += exAction.Label_filterTimeStart + ': <b>' + convertTimestamp(exAction.filterTimeStart, true) + '</b><br>';
            if (exAction.hasOwnProperty('filterTimeEnd'))
                output += exAction.Label_filterTimeEnd + ': <b>' + convertTimestamp(exAction.filterTimeEnd, true) + '</b><br>';

            output += '<h4>' + exAction.Label_Filter_by_Persons + '</h4>';
            if (exAction.filterByGender)
            output += exAction.Label_filterByGender + ': <b>' + exAction.filterByGenderName + '</b><br>';
            output += exAction.Label_selfUse + ': <b>' + exAction.useSelfBalance + '</b><br>';
        }

        if (exAction.hasOwnProperty('results')) {
            output += exAction.results;
        }

    }

    output += '<hr>';

    if (data.hasOwnProperty('Label_CanSignOnlyRecipients')) {
        output += '<b>' + data.Label_CanSignOnlyRecipients + '</b><br>';
    }

    if (data.hasOwnProperty('recipients')) {
        output += '<b>' + data.Label_recipients + '</b>:';
        for (i in data.recipients) {
            output += '<br><a href=?address=' + data.recipients[i][0] + get_lang() + '><b>' + data.recipients[i][1] + '</b></a>';
        }
        output += '<hr>';
    }

    if (data.hasOwnProperty('authors')) {
        output += '<h4>' + data.Label_Authors + '</h4>';
        var index = 1;
        for (i in data.authors) {
            output += index++ + '. ' + data.authors[i].share + ' x ';
            output += '<a href=?person=' + data.authors[i].ref + get_lang() + '><b>' + data.authors[i].name + '</b></a>';
            if (data.authors[i].hasOwnProperty('memo')) {
                output += ' - ' + data.authors[i].memo;
            }
            output += '<br>';
        }
        output += '<hr>';
    }

    if (data.hasOwnProperty('encrypted')) {

        output += '<h4>' + data.encrypted + '</h4>';

    } else {

        if (data.hasOwnProperty('templateKey')) {
            output += data.Label_Used_Template + ': <a href="?template=' + data.templateKey + get_lang() + '">['
             + data.templateKey + '] ' + data.templateName + '</a></br>';

            output += data.Label_template_hash + ': ';
            if (data.hasOwnProperty('templateUnique')) {
                output += '<a href="?search=transactions&q=' + data.templateHash + get_lang() + '"><b>'
                 + data.templateHash + '</b></a><br>';
            } else {
                output += data.templateHash + '<br>';
            }

            if (data.hasOwnProperty('body')) {
                output += '<hr>';
                output += fformat(data.body);
            }
            output += '<hr>';

        }

        if (data.hasOwnProperty('message')) {
            output += '<br>' + data.Label_mess_hash + ': ';
            if (data.hasOwnProperty('messageUnique')) {
                output += '<a href="?search=transactions&q=' + data.messageHash + get_lang() + '"><b>'
                 + data.messageHash + '</b></a>';
            } else {
                output += data.messageHash;
            }
            output += ' <a class="button ll-blue-bgc" href="../apidocuments/message/' + data.tx.seqNo
                + '"><b>' + data.Label_Source_Mess + '</b></a><br>';

            output += fformat(data.message);
        }

        if (data.hasOwnProperty('hashes')) {
            output += '<br><hr><b>' + data.Label_hashes + '</b>:<br>' + data.hashes;
        }

        if (data.hasOwnProperty('files')) {
            output += '<br><hr><b>' + data.Label_files + '</b>:<br>' + data.files;
        }

    }

    if (data.hasOwnProperty('sources')) {
        output += '<h4>' + data.Label_Sources + '</h4>';
        var index = 1;
        for (i in data.sources) {
            output += index++ + '. ' + data.sources[i].weight + ' x ';
            output += '<a href=?tx=' + data.sources[i].ref + get_lang() + '><b>' + data.sources[i].name + '</b></a>';
            if (data.sources[i].hasOwnProperty('memo')) {
                output += ' - ' + data.sources[i].memo;
            }
            output += '<br>';

        }
        output += '<hr>';
    }

    if (data.hasOwnProperty('tags')) {
        output += '<br><b>' + data.Label_Tags + '</b>: ';
        for (tag of data.tags.split(',')) {
            output += ' <a href=?q=' + encodeURIComponent(tag) + get_lang() + '&search=transactions><b>' + tag + ',</b></a>';
        }
    }

    output += '</div>';

    if (data.hasOwnProperty('signs')) {
        output += '<hr>' + data.signs;
    }

    if (data.hasOwnProperty('links')) {
        output += '<hr>' + data.links;
    }

    output += '<hr><a href ="/api/tx/raw/' + data.tx.signature + '">{ ' + data.Label_RAW + ' }</a>';

    return output;

}




