function tx(data) {

    var output = lastBlock(data.lastBlock);

    if (!data.hasOwnProperty('tx')) {
        output += '<h2>Not found</h2>';
        return output;
    }
    var tx = data.tx;

    output += '<table width="1280" border=0><tr><td align=left>';
    if (tx.hasOwnProperty('head')) {
        output += tx.head + '<br>';
        output += '<b>' + tx.timestampLabel + '</b>: ' + convertTimestamp(tx.timestamp, true) + ' / ' + tx.timestamp + '<br>';
        output += '<hr>';
    }

    if (tx.hasOwnProperty('body')) {
        var body = tx.body;
        if (body.hasOwnProperty("type_name")) {
            // тут просто JSON от calculated
            if (body.hasOwnProperty("message")) {
                output += '<h4>' + body.message + '</h4>';
            }
            if (body.hasOwnProperty("recipient")) {
                output += 'recipient: ' + body.recipient + '<br>';
            }
        } else {
            output += tx.body + '<br>';
        }
    }

    if (tx.hasOwnProperty('message')) {
        output += fformat(tx.message) + '<br>';
    }
    if (tx.hasOwnProperty('foot')) {
        output += fformat(tx.foot) + '<br>';
    }
    if (tx.hasOwnProperty('signs')) {
        output += tx.signs + '<br>';
    }
    if (tx.hasOwnProperty('links')) {
        output += tx.links + '<br>';
    }

    output += '<br><a href ="/api/recordrawbynumber/' + data.heightSeqNo + '"> RAW </a>';

    //	output += data.Json ;

    return output;
}

function transactionLite(urlstart, data, i, item) {

    var output = '';

    if (item.transaction.type == 1) // GENESIS_ISSUE_ASSET_TRANSACTION
    {

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Sender</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Recipient</b></td>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/01_genesis.jpg>';
        output += '<br><font size="-2">Genesis</font></td>';

        output += '<td>Genesis</td>';

        output += '<td>' + addCommas(item.transaction.amount) + ' <font size="-2">ERA</font>';
        output += '<br>fee: ' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font></td>';

        if (item.transaction.recipient == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.recipient + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.recipient + get_lang() + '>' + item.transaction.recipient + '</font></td>';
        }

        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }

        output += '</tr></table>';

        output += '</table>';

    } else if (item.transaction.type == 6) // GENESIS_SEND_ASSET_TRANSACTION
    {

    } else if (item.transaction.type == 2) // Payment
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Sender</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Recipient</b></td>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        if (item.transaction.creator == data.address) {
            output += '<img src=img/02_payment_out.png>';
        } else {
            output += '<img src=img/02_payment_in.png>';
        }
        output += '<br><font size="-2">Payment</font></td>';

        if (item.transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.creator + get_lang() + '>' + item.transaction.creator + '</a></td>';
        }
        output += '<td>' + addCommas(item.transaction.amount) + ' <font size="-2">ERA</font>';
        output += '<br>fee: ' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font></td>';

        if (item.transaction.recipient == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.recipient + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.recipient + get_lang() + '>' + item.transaction.recipient + '</font></td>';
        }
        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table>';

    } else if (item.transaction.type == 3) // Name registration
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Maker</b></td><td width=100><b>Fee</b></td>';
        output += '<td><b>Name:</b> <a href="?name=' + encodeURIComponent(item.transaction.name) + get_lang() + '">' + escapeHtml(item.transaction.name) + '</a></td>';
        output += '<td><b>Site:</b> <a href=/' + encodeURIComponent(item.transaction.name) + get_lang() + '>http://' + document.location.host + '/' + escapeHtml(item.transaction.name) + '</a></td>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/03_name_registration.png>';
        output += '<br><font size="-2">Name Registration</font></td>';

        if (item.transaction.maker == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.maker + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.maker + get_lang() + '>' + item.transaction.maker + '</a></td>';
        }
        output += '<td>' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font>';

        output += '<td colspan=2><b>';
        if (item.transaction.сompressed) {
            output += 'Compressed';
        }
        output += ' Value:</b><pre style="width: 95%;">' + htmlFilter(wordwrap(item.transaction.value, 80, '\n', true)) + '</pre>';

        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (item.transaction.type == 4) // Name Update
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a> ';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';

        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>New Maker</b></td><td width=100><b>Fee</b></td>';
        output += '<td><b>Name:</b> <a href="?name=' + encodeURIComponent(item.transaction.name) + get_lang() + '">' + htmlFilter(item.transaction.name) + '</a></td>';
        output += '<td><b>Site:</b> <a href=/' + encodeURIComponent(item.transaction.name) + get_lang() + '>http://' + document.location.host + '/' + htmlFilter(item.transaction.name) + '</a></td>';

        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }

        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/04_name_update.png>';
        output += '<br><font size="-2">Name Update</font></td>';

        if (item.transaction.newMaker == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.newMaker + '</font>';
        } else {
            output += '<td><a href=?address=' + item.transaction.newMaker + get_lang() + '>' + item.transaction.newMaker + '</a>';
        }
        output += '<br><font color="e5e5e5">' + item.transaction.maker + '</font>';

        output += '<td>' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font>';

        output += '<td colspan=2><b>New ';
        if (item.transaction.сompressed) {
            output += 'Compressed';
        }
        output += ' Value:</b><pre style="width: 95%;">' + htmlFilter(wordwrap(item.transaction.newValue, 80, '\n', true)) + '</pre>';

        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (item.transaction.type == 5) // Name to Sale
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Maker</b></td><td width=100><b>Fee</b></td>';
        output += '<td width=200><b>Price</b></td><td><b>Name</b></td>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/05_name_sale.png>';
        output += '<br><font size="-2">Name to Sale</font></td>';

        if (item.transaction.maker == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.maker + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.maker + get_lang() + '>' + item.transaction.maker + '</a></td>';
        }
        output += '<td>' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td>' + addCommas(item.transaction.amount) + ' <font size="-2">ERA</font></td>';

        output += '<td><a href="?name=' + encodeURIComponent(item.transaction.name) + get_lang() + '">' + htmlFilter(item.transaction.name) + '</td>';

        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (item.transaction.type == 6) // Cancel Name Sale
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Maker</b></td><td width=200><b>Fee</b></td>';
        output += '<td><b>Name</b></td>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/06_cancel_name_sale.png>';
        output += '<br><font size="-2">Cancel Name Sale</font></td>';

        if (item.transaction.maker == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.maker + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.maker + get_lang() + '>' + item.transaction.maker + '</a></td>';
        }

        output += '<td>' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td><a href="?name=' + encodeURIComponent(item.transaction.name) + get_lang() + '">' + htmlFilter(item.transaction.name) + '</td>';

        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (item.transaction.type == 7) // Name Purchase
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Seller</b></td><td><b>Name</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Buyer</b></td>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        if (item.transaction.seller == data.address) {
            output += '<img src=img/07_name_purchase_in.png>';
            output += '<br><font size="-2">Name Selling</font></td>';
        } else {
            output += '<img src=img/07_name_purchase_out.png>';
            output += '<br><font size="-2">Name Buying</font></td>';
        }

        if (item.transaction.seller == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.seller + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.seller + get_lang() + '>' + item.transaction.seller + '</a></td>';
        }

        output += '<td><a href="?name=' + encodeURIComponent(item.transaction.name) + get_lang() + '">' + htmlFilter(item.transaction.name) + '</a></td>';

        output += '<td>' + addCommas(item.transaction.amount) + ' <font size="-2">ERA</font>';
        output += '<br>fee: ' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font></td>';

        if (item.transaction.buyer == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.buyer + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.buyer + get_lang() + '>' + item.transaction.buyer + '</font></td>';
        }

        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (item.transaction.type == 8) // Poll Creation
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Creator</b></td><td width=100><b>Fee</b></td>';
        output += '<td><b>Poll</b></td><td><b>Options</b></td>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top" rowspan=2>';

        output += '<img src=img/08_poll_creation.jpg>';
        output += '<br><font size="-2">Poll Creation</font></td>';

        if (item.transaction.creator == data.address) {
            output += '<td rowspan=2><font color="dimgray">' + item.transaction.creator + '</font></td>';
        } else {
            output += '<td rowspan=2><a href=?address=' + item.transaction.creator + get_lang() + '>' + item.transaction.creator + '</a></td>';
        }
        output += '<td rowspan=2>' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td><a href="?poll=' + encodeURIComponent(item.transaction.name) + get_lang() + '">' + htmlFilter(item.transaction.name) + '</a></td>';

        output += '<td><ul>';

        for (key in item.transaction.options) {
            output += '<li>' + escapeHtml(item.transaction.options[key]) + '</li>';
        }
        output += '</ul></td>';

        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr>';
        output += '<tr><td colspan=2><b>Description:</b> ' + fformat(item.transaction.description);
        output += '</table>';

        output += '</table><br>';

    } else if (item.transaction.type == 9) // Poll Vote
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';

        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Creator</b></td><td width=100><b>Fee</b></td>';
        output += '<td><b>Poll</b></td><td><b>Option</b></td>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/09_poll_vote.jpg>';
        output += '<br><font size="-2">Poll Vote</font></td>';

        if (item.transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.creator + get_lang() + '>' + item.transaction.creator + '</a></td>';
        }
        output += '<td>' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td><a href="?poll=' + encodeURIComponent(item.transaction.poll) + get_lang() + '">' + escapeHtml(item.transaction.poll) + '</a></td>';

        output += '<td>' + escapeHtml(item.transaction.optionString) + '</td>';

        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (item.transaction.type == 10) // Arbitrary Transaction
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Creator</b></td><td width=150><b>Fee:</b> ' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font>' + '</td>';
        output += '<td><b>Data Base58</b></td>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td rowspan=3 align="center" valign="top">';

        output += '<img src=img/10_arbitrary_transaction.png>';
        output += '<br><font size="-2">Arbitrary Transaction</font></td>';

        if (item.transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.creator + get_lang() + '>' + item.transaction.creator + '</a></td>';
        }

        output += '<td><b>Service:</b> ' + item.transaction.service + '</td>';


        dataText = uintToString(Base58.decode(item.transaction.data));

        dataText = dataText.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

        output += '<td rowspan=3><abbr title="' + dataText + '"><pre>' + wordwrap(item.transaction.data, 70, '\n', true) + '</pre></abbr>';

        if (item.hasOwnProperty('balance')) {
            output += '<td rowspan=3>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr>';

        if (item.transaction.hasOwnProperty('payments')) {
            assetsAmounts = item.transaction.assetsAmounts;

            output += '<tr><td colspan=2><b>Payments:</b><br>';
            payments = item.transaction.payments;
            for (key in payments) {
                output += addCommas(payments[key].amount) + ' ' + getItemNameMini('asset', payments[key].asset,
                    assetsAmounts[payments[key].asset].name) + ' -> ';
                if (payments[key].recipient == data.address) {
                    output += '<font color="dimgray">' + payments[key].recipient + '</font>';
                } else {
                    output += '<a href=?address=' + payments[key].recipient + get_lang() + '>' + payments[key].recipient + '</font></a>';
                }
                output += '<br>';
            }

            output += '<tr><td colspan=2 height=99%><b>Amount:</b><br>';

            for (key in assetsAmounts) {
                output += addCommas(assetsAmounts[key].amount) + ' ' + getItemNameMini('asset', key, assetsAmounts[key].name) + '<br>';
            }
        }

        output += '</table>';

        output += '</table><br>';

    } else if (item.transaction.type == 21) // ISSUE_ASSET_TRANSACTION
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';

        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Creator</b></td>';
        output += '<td width="100"><b>Fee</b></td><td><b>Name:</b> ';

        if (item.transaction.asset != '0') {
            output += '<a href=?asset=' + item.transaction.asset + get_lang() + '>' + escapeHtml(item.transaction.assetName) + '</a>'
        } else {
            output += escapeHtml(item.transaction.assetName);
        }

        output += '</td><td><b>Key</b><td align=center><b>Quantity</b><td><b>Divisible</b>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/11_asset_issue.png>';
        output += '<br><font size="-2">Asset Issue</font></td>';

        if (item.transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.creator + get_lang() + '>' + item.transaction.creator + '</a></td>';
        }
        output += '<td>' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td><b>Description:</b> ' + fformat(item.transaction.description) + '</td>';

        output += '<td align=center>' + item.transaction.asset + '</td>';
        output += '<td align=center>' + addCommas(item.transaction.quantity) + '</td>';
        output += '<td align=center>' + item.transaction.divisible + '</td>';

        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (item.transaction.type == 31) // SEND_ASSET_TRANSACTION
    {

        ////////////////////// MESSAGE ////////////////
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Sender</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Asset</b></td><td><b>Recipient</b></td>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        if (item.transaction.creator == data.address) {
            output += '<img src=img/12_asset_transfer_out.png>';
        } else {
            output += '<img src=img/12_asset_transfer_in.png>';
        }

        output += '<br><font size="-2">Asset Transfer</font></td>';

        if (item.transaction.creator == data.address) {
            //output += '<td><font color="dimgray">'+item.transaction.creator+'</font></td>';
            output += '<td><a href=?address=' + item.transaction.creator + get_lang() + '>' + item.transaction.recipient + '</a></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.creator + get_lang() + '>' + item.transaction.creator + '</a></td>';
        }
        output += '<td>' + addCommas(item.transaction.amount);
        //output += ' ' + getItemNameMini('asset', item.transaction.asset, item.transaction.assetName);

        output += '<br>fee: ' + addCommas(item.transaction.fee) + '</td>';

        output += '<td>' + ' <a href=?asset=' + item.transaction.asset + get_lang() + '>' + getItemName('asset', 1000, item.transaction.asset, item.transaction.assetName) + '</a>';

        if (item.transaction.recipient == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.recipient + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.recipient + get_lang() + '>' + item.transaction.recipient + '</font></td>';
        }

        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }

        output += '<td>' + item.transaction.isText + '</td>';
        output += '<td>' + item.transaction.encrypted + '</td>';

        if (item.hasOwnProperty('balance')) {
            output += '<td rowspan=2>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr>';

        output += '<tr><td colspan=5><b>Message:</b> <pre>';

        if (!item.transaction.isText) {
            output += '<b>base58:</b> ';
        }

        if (item.transaction.encrypted) {
            output += '<font color=red>encrypted</font>';
        } else {
            output += fformat(item.transaction.data); // wordwrap(item.transaction.data, 120, '\n', true);
        }

        output += '</pre></td>';

        output += '</table>';

        output += '</table><br>';


    } else if (item.transaction.type == 41) // HASHES_RECORD
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Sender</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Asset</b></td><td><b>Recipient</b></td>';

        if (item.hasOwnProperty('data')) {
            output += '<td width=180><b>Description</b></td>';
        }
        output += '</tr>';


        output += '<tr><td align="center" valign="top">';

        if (item.transaction.creator == data.address) {
            output += '<img src=img/12_asset_transfer_out.png>';
        } else {
            output += '<img src=img/12_asset_transfer_in.png>';
        }

        output += '<br><font size="-2">Hashes Record</font></td>';

        if (item.transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.creator + get_lang() + '>' + item.transaction.creator + '</a></td>';
        }

        output += '<td> fee: ' + addCommas(item.transaction.fee) + '</td>';

        output += '</tr>';
        output += '</table>';

        if (item.transaction.hasOwnProperty("url") && item.transaction.url.length > 0) {
            output += '<tr><td width="100%"><b>';
            output += item.transaction.url;
            output += '</b></td></tr>';
        }
        output += '<tr><td width="100%">';
        output += item.transaction.data;
        output += '</td></tr>';

        output += '<tr><td width="100%">';

        for (key in item.transaction.hashes) {
            if (item.transaction.hasOwnProperty("url") && item.transaction.url.length > 10) {
                output += ' <b>' + key + '</b>:<a target="blank" href="' + item.transaction.url + '/' + item.transaction.hashes[key] + get_lang() + '">' + item.transaction.hashes[key] + '</a>';
            } else {
                output += ' <b>' + key + '</b>:' + item.transaction.hashes[key];
            }
        }

        output += '</td></tr>';

        output += '</table><br>';

    } else if (item.transaction.type == 13) // Create Order
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Сreator</b></td><td width=100><b>Fee</b></td>';
        output += '<td width=135><b>Have</b></td><td width=135><b>Want</b></td>';
        output += '<td width=135><b>Price</b></td><td width=135><b>Amount</b></td>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/13_order_creation.png>';
        output += '<br><font size="-2">Order Creation</font></td>';

        if (item.transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.creator + get_lang() + '>' + item.transaction.creator + '</a></td>';
        }

        output += '<td>' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td>' + ' <a href=?asset=' + item.transaction.order.have + get_lang() + '>' + getItemName('asset', 1000, item.transaction.order.have, item.transaction.haveName) + '</a>';

        output += '<td>' + ' <a href=?asset=' + item.transaction.order.want + get_lang() + '>' + getItemName('asset', 1000, item.transaction.order.want, item.transaction.wantName) + '</a>';

        output += '<td>' + addCommas(item.transaction.order.price);

        output += '<br>' + getItemNameMini('asset', item.transaction.order.want, item.transaction.wantName);
        output += '/' + getItemNameMini('asset', item.transaction.order.have, item.transaction.haveName);

        output += '<td>' + addCommas(item.transaction.order.amount);

        output += ' ' + getItemNameMini('asset', item.transaction.order.have, item.transaction.haveName);

        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (item.transaction.type == 14) // Asset cancel order
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Сreator</b></td><td width=100><b>Fee</b></td>';
        output += '<td width=135><b>Have</b></td><td width=135><b>Want</b></td>';
        output += '<td width=135><b>Price</b></td><td width=135><b>Amount<font size=-2> left from <a href=?order=' + item.transaction.order + get_lang() + '>order</a></b></td>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/14_cancel_order.png>';
        output += '<br><font size="-2">Cancel Order</font></td>';

        if (item.transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.creator + get_lang() + '>' + item.transaction.creator + '</a></td>';
        }

        output += '<td>' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td>' + ' <a href=?asset=' + item.transaction.orderSource.have + get_lang() + '>' + getItemName('asset', 1000, item.transaction.orderSource.have, item.transaction.orderSource.haveName) + '</a>';

        output += '<td>' + ' <a href=?asset=' + item.transaction.orderSource.want + get_lang() + '>' + getItemName('asset', 1000, item.transaction.orderSource.want, item.transaction.orderSource.wantName) + '</a>';

        output += '<td>' + addCommas(item.transaction.orderSource.price);

        output += '<br>' + getItemNameMini('asset', item.transaction.orderSource.want, item.transaction.orderSource.wantName);
        output += '/' + getItemNameMini('asset', item.transaction.orderSource.have, item.transaction.orderSource.haveName);

        output += '<td>' + addCommas(item.transaction.orderSource.amountLeft);

        output += ' ' + getItemNameMini('asset', item.transaction.orderSource.have, item.transaction.orderSource.haveName);

        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (item.transaction.type == 15) // Multi Payment
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Sender</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Recipient</b></td>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        if (item.transaction.creator == data.address) {
            output += '<img src=img/15_multi_payment_out.png>';
        } else {
            output += '<img src=img/15_multi_payment_in.png>';
        }
        output += '<br><font size="-2">Multi Payment</font></td>';

        if (item.transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.creator + get_lang() + '>' + item.transaction.creator + '</a></td>';
        }

        output += '<td>';

        assetsAmounts = item.transaction.assetsAmounts;
        for (key in assetsAmounts) {
            output += addCommas(assetsAmounts[key].amount) + ' ' + getItemNameMini('asset', key, assetsAmounts[key].name) + '<br>';
        }

        output += 'fee: ' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td>';
        payments = item.transaction.payments;
        for (key in payments) {
            output += addCommas(payments[key].amount) + ' ' + getItemNameMini('asset', payments[key].asset,
                assetsAmounts[payments[key].asset].assetName) + ' -> ';
            if (payments[key].recipient == data.address) {
                output += '<font color="dimgray">' + payments[key].recipient + '</font>';
            } else {
                output += '<a href=?address=' + payments[key].recipient + get_lang() + '>' + payments[key].recipient + '</font></a>';
            }
            output += '<br>';
        }
        output += '</td>';

        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (item.transaction.type == 16) // Deploy AT
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Creator</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Values';

        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top" rowspan=2>';

        output += '<img src=img/16_deploy_at.png>';
        output += '<br><font size="-2">Deploy AT</font></td>';

        if (item.transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + item.transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + item.transaction.creator + get_lang() + '>' + item.transaction.creator + '</a></td>';
        }
        output += '<td>' + addCommas(item.transaction.amount) + ' <font size="-2">ERA</font>';
        output += '<br>fee: ' + addCommas(item.transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td><b>Name: </b>' + item.transaction.name;
        if (item.transaction.atAddress == data.address) {
            output += '<br><b>AT Address: </b> <font color="dimgray">' + item.transaction.atAddress + '</font>';
        } else {
            output += '<br><b>AT Address: </b><a href=?address=' + item.transaction.atAddress + get_lang() + '>' + item.transaction.atAddress + '</a>';
        }
        output += '<br><b>AT Type:</b> ' + item.transaction.atType;
        output += '<br><b>Description:</b> ' + fformat(item.transaction.description);
        output += '<br><b>Tags:</b> ' + item.transaction.tags;
        output += '</td>';


        if (item.hasOwnProperty('balance')) {
            output += '<td rowspan=2>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr>';


        output += '<tr><td colspan=3><b>Creation Bytes:</b><pre>' + wordwrap(item.transaction.creationBytes, 138, '\n', true);

        output += '</tr></table>';

        output += '</table><br>';

    } else {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + item.transaction.signature + get_lang() + '>' + item.transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (item.transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + item.transaction.blockHeight + get_lang() + '>' + item.transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + item.transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + convertTimestamp(item.transaction.timestamp, true) + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Sender</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Asset</b></td><td><b>Recipient</b></td>';
        if (item.hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        if (item.transaction.creator == data.address) {
            output += '<img src=img/12_asset_transfer_out.png>';
        } else {
            output += '<img src=img/12_asset_transfer_in.png>';
        }

        output += '<td>';
        if (item.transaction.creator == data.address) {
            output += '<font color="dimgray">' + item.transaction.creator + '</font>';
        } else {
            output += '<a href=?address=' + item.transaction.creator + get_lang() + '>' + item.transaction.creator + '</a>';
        }
        output += '<br><font>' + item.transaction.record_type + '</font>';
        output += '<br>fee: ' + addCommas(item.transaction.fee) + '';
        output += '</td>';

        if (item.hasOwnProperty('balance')) {
            output += '<td>' + printBalance(item.balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';
    }

    return output;
}
