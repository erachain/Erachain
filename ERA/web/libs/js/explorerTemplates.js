function templates(data){
	var output = '';
	
	if(data.hasOwnProperty('error'))
	{
		return '<h2>' + data.error + '</h2>';
	}
	
	output += lastBlock(data.lastBlock);
	
	output += '<table width="1280" border=0><tr><td align=left><br>';

	output += '<table width=80% BORDER=0 cellpadding=10 cellspacing=0 class="table table-striped" style="border: 1px solid #ddd;">';
	output += '<thead><tr><td><b>'+ data.label_table_key  +': ' + data.label_table_name + '</b></td><td><b>' + data.label_table_description + '</b></td><td><b>' + data.label_table_creator + '</b></td></tr></thead>';
	
	for(var key in data.templates)
	{
		output += '<tr><td><a href="?template=' + key + get_lang()+ '">'+ key + ': ';
		output += '<b>'+ data.templates[key].name + '</b></a></td>';
		output += '<td>' + data.templates[key].description.substr(0, 100) + '</td>';
		output += '<td><a href=?addr='+ data.templates[key].owner + get_lang() +'>'+ htmlFilter(data.templates[key].owner) +'</a></td>';
		output += '</tr>';
	}
	output += '<tr><td colspan=2>';
	if(data.hasMore)
	{
		output += '<a href=?templates&start='+(data.start_row + data.view_Row - 1)+get_lang()+'>'+data.Label_Later + '</a>';
	}
	output += '</td>';
	
	output += '<td align=right>';
	if(data.hasLess) {
		output += '<a href=?templates&start='+(data.start_row - data.view_Row)+get_lang()+'>'+data.Label_Previous + '</a>';
	}
	output += '</td>';

	output += '</tr>';
	output += '</table>';
	output += '</table>';

	return output;
}


function template(data)
{
	var output = '';
	
	if(data.hasOwnProperty('error'))
	{
		return '<h2>' + data.error + '</h2>';
	}
	
	output += lastBlock(data.lastBlock);
	
	output += '<table width="1280" border=0><tr><td align=left><br>';
	
	output += '<h3 style="display:inline;">'+ data.label_Template+ ':</h3>';
	
	//output += '<h3 style="display:inline;"> | </h3>';
	
	output += '<a href="?template='+data.template.key+get_lang()+'"><h3 style="display:inline;">';
	output += getAssetName2(data.template.key, data.template.name)+'</h3></a>';
	
	output += '<br><br>';
	
	output += '<b>'+ data.label_Key + ':</b> ' + data.template.key;
	
	output += '<br><br>';
	
	
	output += '<b>' + data.label_Creator + ':</b> <a href=?addr='+data.template.owner+get_lang()+'>' + data.template.owner + '</a>';
	
	output += '<br><br>';
	
	
	output += '<b>' + data.label_Description + ':</b> ' + wordwrap(data.template.description, 80, '\n', true);

	
	return output;
}
