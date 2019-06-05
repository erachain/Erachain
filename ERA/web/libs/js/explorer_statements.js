
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
	output += '<tr><td><b>'+ data.Label_Timestamp+'<td><b>'+ data.Label_Creator +' <td><b>'+data.Label_Template+'<td><b>'+ data.Label_Statement; 

	minHeight = (data.maxHeight <= 20) ? 1 : data.maxHeight - 20;
	for(var i in data.Peers)
	{
		output += '<tr><td>'+ i;
		output += '<td>'+ data.Peers[i].Timestamp+'<td>'+data.Peers[i].Creator+'<td>'+data.Peers[i].Template;
		output += '<td>'+ escapeHtml(data.Peers[i].Statement);
		
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