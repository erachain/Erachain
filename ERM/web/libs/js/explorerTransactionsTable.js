function transactions_Table(data){

	output = data.Transactions.label_transactions_table + ':<br>';
	output += '<table id="transactions" id=accounts BORDER=0 cellpadding=15 cellspacing=0 width="800"  class="table table-striped" style="border: 1px solid #ddd; word-wrap: break-word;" >';
			
	output += '<tr bgcolor="f1f1f1"><td><b>'+data.Transactions.label_block+'<td><b>'+ data.Transactions.label_signature+'<td><b>'+data.Transactions.label_type_transaction+'<td><b>'+data.Transactions.label_reference+'<td><b>'+ data.Transactions.label_date+'<td><b>'+data.Transactions.label_creator+'<td><b>'+data.Transactions.label_size+'<td><b>'+data.Transactions.label_fee+'<td><b>'+ data.Transactions.label_confirmations+'</tr>';
		for (key in data.Transactions.transactions){
			output += '<tr><td><a href ="?tx='+data.Transactions.transactions[key].signature+get_lang()+'">'+ data.Transactions.transactions[key].block +'-'+ data.Transactions.transactions[key].seq+'</a><td><a href="?tx='+data.Transactions.transactions[key].signature+get_lang()+'" title = "'+ data.Transactions.transactions[key].signature  +get_lang()+'">'+data.Transactions.transactions[key].signature.slice(0, 11)+'...</a><td><a href="?tx='+data.Transactions.transactions[key].signature+get_lang()+'">'+data.Transactions.transactions[key].type+'</a><td>'+data.Transactions.transactions[key].reference+'<td>'+data.Transactions.transactions[key].date;
			if (data.Transactions.transactions[key].creator_key == '-'){
				output += '<td>'+data.Transactions.transactions[key].creator+'</a>'; 
			}else if (data.Transactions.transactions[key].creator_key == '+'){
					output += '<td><a href ="?addr='+data.Transactions.transactions[key].creator+get_lang()+'">'+data.Transactions.transactions[key].creator+'</a>';
				}else{
					output += '<td><a href ="?person='+data.Transactions.transactions[key].creator_key+get_lang()+'">'+data.Transactions.transactions[key].creator+'</a>';
				}
				output += '<td>'+ data.Transactions.transactions[key].size +'<td>'+ data.Transactions.transactions[key].fee +'<td>'+ data.Transactions.transactions[key].confirmations + '</td></tr>';
			
			}
	output +='</table></td></tr></table>';

 return output;

}