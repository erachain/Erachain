
function statements(data){

	var output = '';
	
	if(data.hasOwnProperty('error'))
	{
		return '<h2>' + data.error + '</h2>';
	}
	
	output += lastBlock(data.lastBlock);

	output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="">';
	output += '<tr><td align=left>';
//	output += '<a href=?unconfirmed'+get_lang()+'>' + data.Label_Unconfirmed_transactions+': '+data.unconfirmedTxs+'</a>.';

	output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1180"  class="table table-striped" style="border: 1px solid #ddd;">';
	output += '<tr><td>'+ data.Label_No  +'</td><td><b>'+ data.Label_Timestamp +'<td><b>'+ data.Label_block  +' <td><b>'+data.Label_Template+'<td><b>'+ data.Label_Statement +'<td><b>'+ data.Label_Creator; 

	minHeight = (data.maxHeight <= 20) ? 1 : data.maxHeight - 20;
	
	for(var i  in data.Statements)
	{
		output += '<tr><td>'+ i;
		output += '<td>'+ data.Statements[i].Timestamp;
		output += '<td><a href =?block='+data.Statements[i].Block+get_lang() + '>' +data.Statements[i].Block +'</a>';
		output += '<td>'+data.Statements[i].Template; 
		output += '<td><a href = ?statement='+data.Statements[i].Block+'&Seg_No='+data.Statements[i].Seg_No+ get_lang() + '>';
		output +='<div style="word-wrap: break-word;  width: 300px;">'+ data.Statements[i].Statement +'</div></a>';
		if (data.Statements[i].person_key !=""){
		output +='<td><a href =?person='+ data.Statements[i].person_key + get_lang() +'>'+ data.Statements[i].Creator +'</a>';
		}
		else {
		output += '<td>'+data.Statements[i].Creator
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

function statement(data){

var output = '';
	
	if(data.hasOwnProperty('error'))
	{
		return '<h2>' + data.error + '</h2>';
	}
	
	output += lastBlock(data.lastBlock);

	output += '<table><tr><td>';
	output +='<div style="word-wrap: break-word;  width: 800px;">';
	output +=  '<b>'+data.Label_statement +'</b>';
	output +='<br><br>' + data.Label_block + ':&nbsp&nbsp <a href=?block='+data.Label_block+ get_lang()+'><b>' + data.block +'</b></a>';
	output +='<br>' + data.Label_date + ':&nbsp&nbsp'+ data.date;
	
	if (data.creator_key !=""){
	output += '<br>'  + data.Label_creator + ':&nbsp&nbsp <a href=?person='+ data.creator_key + get_lang()+'><b>' + data.creator +'</b></a>';
	}
	else{
	output +='<br>' + data.Label_creator + ':&nbsp&nbsp' + data.creator ;
	
	}
// vouches	
//	output +=
	output +='<br><br>' + data.statement;
	output +='</div>';
	output +=   '</td></tr></table>';
	output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="">';
	output += '<tr><td align=left>';
//	output += '<a href=?unconfirmed'+get_lang()+'>' + data.Label_Unconfirmed_transactions+': '+data.unconfirmedTxs+'</a>.';
		output += '<br>' + data.Label_vouchs +':<br>';
	output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1180"  class="table table-striped" style="border: 1px solid #ddd;  width: 800px;">';
	output += '<tr><td><b>'+ data.Label_No+'<td><b>'+ data.Label_block  +'</td><td><b>'+ data.Label_accounts_table_creator +' <td><b>'+data.Label_accounts_table_data; 
	ii1=0;
	minHeight = (data.maxHeight <= 20) ? 1 : data.maxHeight - 20;
	for(var i  in data.vouches)
	{
		ii1=i+1;
		output += '<tr><td>'+ ii1;
		output += '<td><a href=?block='+ data.vouches[i].block+ get_lang() +'>'+ data.vouches[i].block+'</a>';
		output += '<td>'+ data.vouches[i].creator;
		if (data.vouches[i].creator_name !="") 
		{
		output += '<a href = ?person='+ data.vouches[i].creator_key+ get_lang() +'>';
		output += '<br>'+data.vouches[i].creator_name+ '<a>';
		}
		
		
		
		output += '<td>'+ data.vouches[i].date ;
		
	}
	
	output += '</table><br>';
	
	//statuses
output += '<br>'+ data.Label_statuses +':';
output += '<table id=statuses BORDER=0 cellpadding=15 cellspacing=0 width="800"  class="table table-striped" style="border: 1px solid #ddd; word-wrap: break-word;" ><tr bgcolor="f1f1f1"><td><b>'+data.Label_Status_table_status+'<td><b>'+data.Label_Status_table_data+'<td><b>'+ data.Label_accounts_table_creator + '<tr>'; 

key =0;
for(key in data.statuses) {
output += '<tr ><td >'+data.statuses[key].status_name+'<td>'+ data.statuses[key].status_data +'<td><a href ="?person='+data.statuses[key].status_creator_key+get_lang()+'">'+ data.statuses[key].status_creator  +'</a><tr>';

key ++;
}
output += '</table><br>';

	
	
	
	return output;


}




