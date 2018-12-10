
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

function statements2(data){
	var output = '';
	
	if(data.hasOwnProperty('error'))
	{
		return '<h2>' + data.error + '</h2>';
	}
	
	output += lastBlock(data.lastBlock);
	
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

function openStatement(){
	var params = document.getElementById('statement_search_q').value.split('-');
	if (params.length > 1){
		document.location.href = '?statement=' + params[0] + '&Seg_No=' + params[1] + get_lang() ;
	}
}

function statement(data){

	var output = '';
	
	if(data.hasOwnProperty('error'))
	{
		return '<h2>' + data.error + '</h2>';
	}
	
	//output += lastBlock(data.lastBlock);

	output += '<table><tr><td>';
	output +='<div style="word-wrap: break-word;  width: 800px;">';
	output +=  '<b>'+data.Label_statement +'</b>';
	output +='<br><br>' + data.Label_block + ':&nbsp&nbsp <a href=?block='+ data.block + get_lang()+'><b>' + data.block +'</b></a>';
	output +='<br>' + data.Label_date + ':&nbsp&nbsp'+ data.date;
	
	if (data.creator_key !="") {
		output += '<br>'  + data.Label_creator + ':&nbsp&nbsp <a href=?person='+ data.creator_key + get_lang()+'><b>' + data.creator +'</b></a>';
	} else {
		output +='<br>' + data.Label_creator + ':&nbsp&nbsp' + data.creator ;
	}
// vouches	
	output +='<br><br>' + data.statement;
	output +='</div>';
/*	output +=   '</td></tr></table>';
	output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="">';
	output += '<tr><td align=left>';
	output += '<br>' + data.Label_vouchs +':<br>';
	output += '<table id=blocks BORDER=0 cellpadding=15 cellspacing=0 width="1180"  class="table table-striped" style="border: 1px solid #ddd;  width: 800px;">';
	output += '<tr><td><b>'+ data.Label_No+'<td><b>'+ data.Label_block  +'</td><td><b>'+ data.Label_accounts_table_creator +' <td><b>'+data.Label_accounts_table_data; 
	minHeight = (data.maxHeight <= 20) ? 1 : data.maxHeight - 20;
	for(var i in data.vouches)
	{
		output += '<tr><td>'+ i;
		output += '<td><a href=?block='+ data.vouches[i].block + get_lang() +'>'+ data.vouches[i].block+'</a>';
		output += '<td>'+ data.vouches[i].creator;
		if (data.vouches[i].hasOwnProperty("creator_name")) 
		{
			output += '<a href = ?person='+ data.vouches[i].creator_key+ get_lang() +'>';
			output += '<br>'+data.vouches[i].creator_name+ '<a>';
		}	
		output += '<td>'+ data.vouches[i].date ;

	}
	
	output += '</table><br>';
	*/
	if(data.hasOwnProperty('vouches_table')){
 output +=data.vouches_table;
 
 }	
	return output;


}




