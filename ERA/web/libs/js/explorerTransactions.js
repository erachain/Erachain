function transaction_old(data) {
    var output = '';

    if (data[i].transaction.type == 1) // GENESIS_ISSUE_ASSET_TRANSACTION
    {

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Sender</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Recipient</b></td>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/01_genesis.jpg>';
        output += '<br><font size="-2">Genesis</font></td>';

        output += '<td>Genesis</td>';

        output += '<td>' + addCommas(data[i].transaction.amount) + ' <font size="-2">ERA</font>';
        output += '<br>fee: ' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font></td>';

        if (data[i].transaction.recipient == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.recipient + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.recipient + get_lang() + '>' + data[i].transaction.recipient + '</font></td>';
        }

        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }

        output += '</tr></table>';

        output += '</table>';

    } else if (data[i].transaction.type == 6) // GENESIS_SEND_ASSET_TRANSACTION
    {

    } else if (data[i].transaction.type == 2) // Payment
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Sender</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Recipient</b></td>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        if (data[i].transaction.creator == data.address) {
            output += '<img src=img/02_payment_out.png>';
        } else {
            output += '<img src=img/02_payment_in.png>';
        }
        output += '<br><font size="-2">Payment</font></td>';

        if (data[i].transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.creator + get_lang() + '>' + data[i].transaction.creator + '</a></td>';
        }
        output += '<td>' + addCommas(data[i].transaction.amount) + ' <font size="-2">ERA</font>';
        output += '<br>fee: ' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font></td>';

        if (data[i].transaction.recipient == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.recipient + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.recipient + get_lang() + '>' + data[i].transaction.recipient + '</font></td>';
        }
        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table>';

    } else if (data[i].transaction.type == 3) // Name registration
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Owner</b></td><td width=100><b>Fee</b></td>';
        output += '<td><b>Name:</b> <a href="?name=' + encodeURIComponent(data[i].transaction.name) + get_lang() + '">' + htmlFilter(data[i].transaction.name) + '</a></td>';
        output += '<td><b>Site:</b> <a href=/' + encodeURIComponent(data[i].transaction.name) + get_lang() + '>http://' + document.location.host + '/' + htmlFilter(data[i].transaction.name) + '</a></td>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/03_name_registration.png>';
        output += '<br><font size="-2">Name Registration</font></td>';

        if (data[i].transaction.owner == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.owner + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.owner + get_lang() + '>' + data[i].transaction.owner + '</a></td>';
        }
        output += '<td>' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font>';

        output += '<td colspan=2><b>';
        if (data[i].transaction.сompressed) {
            output += 'Compressed';
        }
        output += ' Value:</b><pre style="width: 95%;">' + htmlFilter(wordwrap(data[i].transaction.value, 80, '\n', true)) + '</pre>';

        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (data[i].transaction.type == 4) // Name Update
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a> ';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';

        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>New Owner</b></td><td width=100><b>Fee</b></td>';
        output += '<td><b>Name:</b> <a href="?name=' + encodeURIComponent(data[i].transaction.name) + get_lang() + '">' + htmlFilter(data[i].transaction.name) + '</a></td>';
        output += '<td><b>Site:</b> <a href=/' + encodeURIComponent(data[i].transaction.name) + get_lang() + '>http://' + document.location.host + '/' + htmlFilter(data[i].transaction.name) + '</a></td>';

        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }

        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/04_name_update.png>';
        output += '<br><font size="-2">Name Update</font></td>';

        if (data[i].transaction.newOwner == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.newOwner + '</font>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.newOwner + get_lang() + '>' + data[i].transaction.newOwner + '</a>';
        }
        output += '<br><font color="e5e5e5">' + data[i].transaction.owner + '</font>';

        output += '<td>' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font>';

        output += '<td colspan=2><b>New ';
        if (data[i].transaction.сompressed) {
            output += 'Compressed';
        }
        output += ' Value:</b><pre style="width: 95%;">' + htmlFilter(wordwrap(data[i].transaction.newValue, 80, '\n', true)) + '</pre>';

        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (data[i].transaction.type == 5) // Name to Sale
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Owner</b></td><td width=100><b>Fee</b></td>';
        output += '<td width=200><b>Price</b></td><td><b>Name</b></td>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/05_name_sale.png>';
        output += '<br><font size="-2">Name to Sale</font></td>';

        if (data[i].transaction.owner == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.owner + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.owner + get_lang() + '>' + data[i].transaction.owner + '</a></td>';
        }
        output += '<td>' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td>' + addCommas(data[i].transaction.amount) + ' <font size="-2">ERA</font></td>';

        output += '<td><a href="?name=' + encodeURIComponent(data[i].transaction.name) + get_lang() + '">' + htmlFilter(data[i].transaction.name) + '</td>';

        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (data[i].transaction.type == 6) // Cancel Name Sale
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Owner</b></td><td width=200><b>Fee</b></td>';
        output += '<td><b>Name</b></td>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/06_cancel_name_sale.png>';
        output += '<br><font size="-2">Cancel Name Sale</font></td>';

        if (data[i].transaction.owner == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.owner + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.owner + get_lang() + '>' + data[i].transaction.owner + '</a></td>';
        }

        output += '<td>' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td><a href="?name=' + encodeURIComponent(data[i].transaction.name) + get_lang() + '">' + htmlFilter(data[i].transaction.name) + '</td>';

        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (data[i].transaction.type == 7) // Name Purchase
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Seller</b></td><td><b>Name</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Buyer</b></td>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        if (data[i].transaction.seller == data.address) {
            output += '<img src=img/07_name_purchase_in.png>';
            output += '<br><font size="-2">Name Selling</font></td>';
        } else {
            output += '<img src=img/07_name_purchase_out.png>';
            output += '<br><font size="-2">Name Buying</font></td>';
        }

        if (data[i].transaction.seller == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.seller + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.seller + get_lang() + '>' + data[i].transaction.seller + '</a></td>';
        }

        output += '<td><a href="?name=' + encodeURIComponent(data[i].transaction.name) + get_lang() + '">' + htmlFilter(data[i].transaction.name) + '</a></td>';

        output += '<td>' + addCommas(data[i].transaction.amount) + ' <font size="-2">ERA</font>';
        output += '<br>fee: ' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font></td>';

        if (data[i].transaction.buyer == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.buyer + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.buyer + get_lang() + '>' + data[i].transaction.buyer + '</font></td>';
        }

        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (data[i].transaction.type == 8) // Poll Creation
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Creator</b></td><td width=100><b>Fee</b></td>';
        output += '<td><b>Poll</b></td><td><b>Options</b></td>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top" rowspan=2>';

        output += '<img src=img/08_poll_creation.jpg>';
        output += '<br><font size="-2">Poll Creation</font></td>';

        if (data[i].transaction.creator == data.address) {
            output += '<td rowspan=2><font color="dimgray">' + data[i].transaction.creator + '</font></td>';
        } else {
            output += '<td rowspan=2><a href=?address=' + data[i].transaction.creator + get_lang() + '>' + data[i].transaction.creator + '</a></td>';
        }
        output += '<td rowspan=2>' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td><a href="?poll=' + encodeURIComponent(data[i].transaction.name) + get_lang() + '">' + htmlFilter(data[i].transaction.name) + '</a></td>';

        output += '<td><ul>';

        for (key in data[i].transaction.options) {
            output += '<li>' + htmlFilter(data[i].transaction.options[key]) + '</li>';
        }
        output += '</ul></td>';

        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr>';
        output += '<tr><td colspan=2><b>Description:</b> ' + fformat(data[i].transaction.description);
        output += '</table>';

        output += '</table><br>';

    } else if (data[i].transaction.type == 9) // Poll Vote
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';

        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Creator</b></td><td width=100><b>Fee</b></td>';
        output += '<td><b>Poll</b></td><td><b>Option</b></td>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/09_poll_vote.jpg>';
        output += '<br><font size="-2">Poll Vote</font></td>';

        if (data[i].transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.creator + get_lang() + '>' + data[i].transaction.creator + '</a></td>';
        }
        output += '<td>' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td><a href="?poll=' + encodeURIComponent(data[i].transaction.poll) + get_lang() + '">' + htmlFilter(data[i].transaction.poll) + '</a></td>';

        output += '<td>' + htmlFilter(data[i].transaction.optionString) + '</td>';

        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (data[i].transaction.type == 10) // Arbitrary Transaction
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Creator</b></td><td width=150><b>Fee:</b> ' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font>' + '</td>';
        output += '<td><b>Data Base58</b></td>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td rowspan=3 align="center" valign="top">';

        output += '<img src=img/10_arbitrary_transaction.png>';
        output += '<br><font size="-2">Arbitrary Transaction</font></td>';

        if (data[i].transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.creator + get_lang() + '>' + data[i].transaction.creator + '</a></td>';
        }

        output += '<td><b>Service:</b> ' + data[i].transaction.service + '</td>';


        dataText = uintToString(Base58.decode(data[i].transaction.data));

        dataText = dataText.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

        output += '<td rowspan=3><abbr title="' + dataText + '"><pre>' + wordwrap(data[i].transaction.data, 70, '\n', true) + '</pre></abbr>';

        if (data[i].hasOwnProperty('balance')) {
            output += '<td rowspan=3>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr>';

        if (data[i].transaction.hasOwnProperty('payments')) {
            assetsAmounts = data[i].transaction.assetsAmounts;

            output += '<tr><td colspan=2><b>Payments:</b><br>';
            payments = data[i].transaction.payments;
            for (key in payments) {
                output += addCommas(payments[key].amount) + ' ' + getAssetNameMini(payments[key].asset,
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
                output += addCommas(assetsAmounts[key].amount) + ' ' + getAssetNameMini(key, assetsAmounts[key].name) + '<br>';
            }
        }

        output += '</table>';

        output += '</table><br>';

    } else if (data[i].transaction.type == 21) // ISSUE_ASSET_TRANSACTION
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';

        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Creator</b></td>';
        output += '<td width="100"><b>Fee</b></td><td><b>Name:</b> ';

        if (data[i].transaction.asset != '0') {
            output += '<a href=?asset=' + data[i].transaction.asset + get_lang() + '>' + htmlFilter(data[i].transaction.assetName) + '</a>'
        } else {
            output += htmlFilter(data[i].transaction.assetName);
        }

        output += '</td><td><b>Key</b><td align=center><b>Quantity</b><td><b>Divisible</b>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/11_asset_issue.png>';
        output += '<br><font size="-2">Asset Issue</font></td>';

        if (data[i].transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.creator + get_lang() + '>' + data[i].transaction.creator + '</a></td>';
        }
        output += '<td>' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td><b>Description:</b> ' + fformat(data[i].transaction.description) + '</td>';

        output += '<td align=center>' + data[i].transaction.asset + '</td>';
        output += '<td align=center>' + addCommas(data[i].transaction.quantity) + '</td>';
        output += '<td align=center>' + data[i].transaction.divisible + '</td>';

        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (data[i].transaction.type == 31) // SEND_ASSET_TRANSACTION
    {

        ////////////////////// MESSAGE ////////////////
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Sender</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Asset</b></td><td><b>Recipient</b></td>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        if (data[i].transaction.creator == data.address) {
            output += '<img src=img/12_asset_transfer_out.png>';
        } else {
            output += '<img src=img/12_asset_transfer_in.png>';
        }

        output += '<br><font size="-2">Asset Transfer</font></td>';

        if (data[i].transaction.creator == data.address) {
            //output += '<td><font color="dimgray">'+data[i].transaction.creator+'</font></td>';
            output += '<td><a href=?address=' + data[i].transaction.creator + get_lang() + '>' + data[i].transaction.recipient + '</a></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.creator + get_lang() + '>' + data[i].transaction.creator + '</a></td>';
        }
        output += '<td>' + addCommas(data[i].transaction.amount);
        //output += ' ' + getAssetNameMini(data[i].transaction.asset, data[i].transaction.assetName);

        output += '<br>fee: ' + addCommas(data[i].transaction.fee) + ' <font size="-2">COMPU</font></td>';

        output += '<td>' + ' <a href=?asset=' + data[i].transaction.asset + get_lang() + '>' + getAssetName(data[i].transaction.asset, data[i].transaction.assetName) + '</a>';

        if (data[i].transaction.recipient == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.recipient + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.recipient + get_lang() + '>' + data[i].transaction.recipient + '</font></td>';
        }

        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }

        output += '<td>' + data[i].transaction.isText + '</td>';
        output += '<td>' + data[i].transaction.encrypted + '</td>';

        if (data[i].hasOwnProperty('balance')) {
            output += '<td rowspan=2>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr>';

        output += '<tr><td colspan=5><b>Message:</b> <pre>';

        if (!data[i].transaction.isText) {
            output += '<b>base58:</b> ';
        }

        if (data[i].transaction.encrypted) {
            output += '<font color=red>encrypted</font>';
        } else {
            output += fformat(data[i].transaction.data); // wordwrap(data[i].transaction.data, 120, '\n', true);
        }

        output += '</pre></td>';

        output += '</table>';

        output += '</table><br>';


    } else if (data[i].transaction.type == 41) // HASHES_RECORD
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Sender</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Asset</b></td><td><b>Recipient</b></td>';

        if (data[i].hasOwnProperty('data')) {
            output += '<td width=180><b>Description</b></td>';
        }
        output += '</tr>';


        output += '<tr><td align="center" valign="top">';

        if (data[i].transaction.creator == data.address) {
            output += '<img src=img/12_asset_transfer_out.png>';
        } else {
            output += '<img src=img/12_asset_transfer_in.png>';
        }

        output += '<br><font size="-2">Hashes Record</font></td>';

        if (data[i].transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.creator + get_lang() + '>' + data[i].transaction.creator + '</a></td>';
        }

        output += '<td> fee: ' + addCommas(data[i].transaction.fee) + ' <font size="-2">COMPU</font></td>';

        output += '</tr>';
        output += '</table>';

        if (data[i].transaction.hasOwnProperty("url") && data[i].transaction.url.length > 0) {
            output += '<tr><td width="100%"><b>';
            output += data[i].transaction.url;
            output += '</b></td></tr>';
        }
        output += '<tr><td width="100%">';
        output += data[i].transaction.data;
        output += '</td></tr>';

        output += '<tr><td width="100%">';

        for (key in data[i].transaction.hashes) {
            if (data[i].transaction.hasOwnProperty("url") && data[i].transaction.url.length > 10) {
                output += ' <b>' + key + '</b>:<a target="blank" href="' + data[i].transaction.url + '/' + data[i].transaction.hashes[key] + get_lang() + '">' + data[i].transaction.hashes[key] + '</a>';
            } else {
                output += ' <b>' + key + '</b>:' + data[i].transaction.hashes[key];
            }
        }

        output += '</td></tr>';

        output += '</table><br>';

    } else if (data[i].transaction.type == 13) // Create Order
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Сreator</b></td><td width=100><b>Fee</b></td>';
        output += '<td width=135><b>Have</b></td><td width=135><b>Want</b></td>';
        output += '<td width=135><b>Price</b></td><td width=135><b>Amount</b></td>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/13_order_creation.png>';
        output += '<br><font size="-2">Order Creation</font></td>';

        if (data[i].transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.creator + get_lang() + '>' + data[i].transaction.creator + '</a></td>';
        }

        output += '<td>' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td>' + ' <a href=?asset=' + data[i].transaction.order.have + get_lang() + '>' + getAssetName(data[i].transaction.order.have, data[i].transaction.haveName) + '</a>';

        output += '<td>' + ' <a href=?asset=' + data[i].transaction.order.want + get_lang() + '>' + getAssetName(data[i].transaction.order.want, data[i].transaction.wantName) + '</a>';

        output += '<td>' + addCommas(data[i].transaction.order.price);

        output += '<br>' + getAssetNameMini(data[i].transaction.order.want, data[i].transaction.wantName);
        output += '/' + getAssetNameMini(data[i].transaction.order.have, data[i].transaction.haveName);

        output += '<td>' + addCommas(data[i].transaction.order.amount);

        output += ' ' + getAssetNameMini(data[i].transaction.order.have, data[i].transaction.haveName);

        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (data[i].transaction.type == 14) // Asset cancel order
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Сreator</b></td><td width=100><b>Fee</b></td>';
        output += '<td width=135><b>Have</b></td><td width=135><b>Want</b></td>';
        output += '<td width=135><b>Price</b></td><td width=135><b>Amount<font size=-2> left from <a href=?order=' + data[i].transaction.order + get_lang() + '>order</a></b></td>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        output += '<img src=img/14_cancel_order.png>';
        output += '<br><font size="-2">Cancel Order</font></td>';

        if (data[i].transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.creator + get_lang() + '>' + data[i].transaction.creator + '</a></td>';
        }

        output += '<td>' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td>' + ' <a href=?asset=' + data[i].transaction.orderSource.have + get_lang() + '>' + getAssetName(data[i].transaction.orderSource.have, data[i].transaction.orderSource.haveName) + '</a>';

        output += '<td>' + ' <a href=?asset=' + data[i].transaction.orderSource.want + get_lang() + '>' + getAssetName(data[i].transaction.orderSource.want, data[i].transaction.orderSource.wantName) + '</a>';

        output += '<td>' + addCommas(data[i].transaction.orderSource.price);

        output += '<br>' + getAssetNameMini(data[i].transaction.orderSource.want, data[i].transaction.orderSource.wantName);
        output += '/' + getAssetNameMini(data[i].transaction.orderSource.have, data[i].transaction.orderSource.haveName);

        output += '<td>' + addCommas(data[i].transaction.orderSource.amountLeft);

        output += ' ' + getAssetNameMini(data[i].transaction.orderSource.have, data[i].transaction.orderSource.haveName);

        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (data[i].transaction.type == 15) // Multi Payment
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Sender</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Recipient</b></td>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        if (data[i].transaction.creator == data.address) {
            output += '<img src=img/15_multi_payment_out.png>';
        } else {
            output += '<img src=img/15_multi_payment_in.png>';
        }
        output += '<br><font size="-2">Multi Payment</font></td>';

        if (data[i].transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.creator + get_lang() + '>' + data[i].transaction.creator + '</a></td>';
        }

        output += '<td>';

        assetsAmounts = data[i].transaction.assetsAmounts;
        for (key in assetsAmounts) {
            output += addCommas(assetsAmounts[key].amount) + ' ' + getAssetNameMini(key, assetsAmounts[key].name) + '<br>';
        }

        output += 'fee: ' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td>';
        payments = data[i].transaction.payments;
        for (key in payments) {
            output += addCommas(payments[key].amount) + ' ' + getAssetNameMini(payments[key].asset,
                assetsAmounts[payments[key].asset].assetName) + ' -> ';
            if (payments[key].recipient == data.address) {
                output += '<font color="dimgray">' + payments[key].recipient + '</font>';
            } else {
                output += '<a href=?address=' + payments[key].recipient + get_lang() + '>' + payments[key].recipient + '</font></a>';
            }
            output += '<br>';
        }
        output += '</td>';

        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';

    } else if (data[i].transaction.type == 16) // Deploy AT
    {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Creator</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Values';

        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top" rowspan=2>';

        output += '<img src=img/16_deploy_at.png>';
        output += '<br><font size="-2">Deploy AT</font></td>';

        if (data[i].transaction.creator == data.address) {
            output += '<td><font color="dimgray">' + data[i].transaction.creator + '</font></td>';
        } else {
            output += '<td><a href=?address=' + data[i].transaction.creator + get_lang() + '>' + data[i].transaction.creator + '</a></td>';
        }
        output += '<td>' + addCommas(data[i].transaction.amount) + ' <font size="-2">ERA</font>';
        output += '<br>fee: ' + addCommas(data[i].transaction.fee) + ' <font size="-2">ERA</font></td>';

        output += '<td><b>Name: </b>' + data[i].transaction.name;
        if (data[i].transaction.atAddress == data.address) {
            output += '<br><b>AT Address: </b> <font color="dimgray">' + data[i].transaction.atAddress + '</font>';
        } else {
            output += '<br><b>AT Address: </b><a href=?address=' + data[i].transaction.atAddress + get_lang() + '>' + data[i].transaction.atAddress + '</a>';
        }
        output += '<br><b>AT Type:</b> ' + data[i].transaction.atType;
        output += '<br><b>Description:</b> ' + fformat(data[i].transaction.description);
        output += '<br><b>Tags:</b> ' + data[i].transaction.tags;
        output += '</td>';


        if (data[i].hasOwnProperty('balance')) {
            output += '<td rowspan=2>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr>';


        output += '<tr><td colspan=3><b>Creation Bytes:</b><pre>' + wordwrap(data[i].transaction.creationBytes, 138, '\n', true);

        output += '</tr></table>';

        output += '</table><br>';

    } else {
        output += '<table width="100%"><tr><td>';

        output += '<table width="100%" style="border-top: 1px solid #ddd; background-color: #f1f1f1; border-spacing: 7px; border-collapse: separate;">';

        output += '<tr><td><a href=' + urlstart + '#' + i + get_lang() + '>#' + i + '</a>&nbsp;&nbsp;';

        output += '<a href=?tx=' + data[i].transaction.signature + get_lang() + '>' + data[i].transaction.signature + '</a></td>';
        output += '<td align=right width=250>';
        if (data[i].transaction.hasOwnProperty('blockHeight')) {
            output += 'Height: <a href=?block=' + data[i].transaction.blockHeight + get_lang() + '>' + data[i].transaction.blockHeight + '</a>';
            output += ' / confirmations: ' + data[i].transaction.confirmations;
        }
        output += '</td>';
        output += '<td align=right width=200>' + data[i].transaction.dateTime + '</td></tr></table>';

        output += '<tr><td>';
        output += '<table width="100%" class="table table-striped"><tr><td width="70" align="center">';

        output += '<b>Action</b></td><td width="290"><b>Sender</b></td><td width=200><b>Amount</b></td>';
        output += '<td><b>Asset</b></td><td><b>Recipient</b></td>';
        if (data[i].hasOwnProperty('balance')) {
            output += '<td width=180><b>Balance</b></td>';
        }
        output += '</tr>';
        output += '<tr><td align="center" valign="top">';

        if (data[i].transaction.creator == data.address) {
            output += '<img src=img/12_asset_transfer_out.png>';
        } else {
            output += '<img src=img/12_asset_transfer_in.png>';
        }

        output += '<td>';
        if (data[i].transaction.creator == data.address) {
            output += '<font color="dimgray">' + data[i].transaction.creator + '</font>';
        } else {
            output += '<a href=?address=' + data[i].transaction.creator + get_lang() + '>' + data[i].transaction.creator + '</a>';
        }
        output += '<br><font>' + data[i].transaction.record_type + '</font>';
        output += '<br>fee: ' + addCommas(data[i].transaction.fee) + ' <font size="-2">COMPU</font>';
        output += '</td>';

        if (data[i].hasOwnProperty('balance')) {
            output += '<td>' + printBalance(data[i].balance) + '</td>';
        }
        output += '</tr></table>';

        output += '</table><br>';
    }

    return output;
}
