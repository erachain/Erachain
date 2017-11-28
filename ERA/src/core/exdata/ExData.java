package core.exdata;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import com.github.rjeschke.txtmark.Processor;
import com.google.common.primitives.Ints;

import core.BlockChain;
import core.item.notes.NoteCls;
import utils.StrJSonFine;

public class ExData {

	private static final int DATA_TITLE_PART_LENGTH = 4; // size title message
	private static final int DATA_JSON_PART_LENGTH = 6; // size JSON part
	private static final int DATA_TYPE_PART_LENGTH = 1; // size type part
	private static final int DATA_VERSION_PART_LENGTH = 1; // size version part
	private String type ="";
	private String version = "";
	private String title ="";
	private String message ="";
	private JSONObject json ;
	private HashMap<String,String> hashes;
	private HashMap<String, Tuple3<Boolean, Integer, byte[]>> files;
	private int title_end;
	private int message_end;
	private int json_end;
	
	
	/*
	 *  JSON
	 * "TM" - template key
	 * "PR" - template params
	 * "HS" - Hashes
	 * "MS" - message
	 * 
	 *  PARAMS
	 * template:NoteCls
	 * param_keys: [id:text]
	 * hashes_Set: [name:hash]
	 * mess: message
	 * title: Title
	 * file_Set: [file Name, ZIP? , file byte[]]
	 * 
	 */
	
	public ExData(){
	
	}
	
	
	
	public byte[] toByte(String type, String vers, String title, NoteCls template, Set<Tuple2<String, String>> param_keys, Set<Tuple2<String,String>> hashes_Set, String mess,
	
			Set<Tuple3<String,Boolean,byte[]>>file_Set){
			byte[] messageBytes = null;
		HashMap<String,Object>  out_Map = new HashMap<String,Object>();
		HashMap <String,String> params_Map = new HashMap  <String,String>();
		 HashMap<String,Tuple2<Boolean,byte[]>>files_1 = null;
		try
		{
// template params
			if (template !=null){
			out_Map.put("TM", template.getKey()+"");
			Iterator<Tuple2<String, String>> it_par = param_keys.iterator();
			while (it_par.hasNext()){
			Tuple2<String, String> p = it_par.next();
			params_Map.put(p.a, p.b);
			}
			it_par = null;
			if (params_Map.size()>0) out_Map.put("PR", params_Map);
			}
			params_Map.clear();
// hashes			
			if (hashes_Set != null){
			Iterator<Tuple2<String, String>> it_Hashes = hashes_Set.iterator();
			while (it_Hashes.hasNext()){
				Tuple2<String, String> a = it_Hashes.next();
				params_Map.put(a.a,a.b);
			}
			it_Hashes = null;
			if (params_Map.size()>0) out_Map.put("HS", params_Map);
			}
			params_Map = null;
// mess
			if (mess.length()>0 && mess != null) out_Map.put("MS", mess);
		
// files	
			if (file_Set != null && file_Set.size()!=0){
			Iterator<Tuple3<String, Boolean, byte[]>> it_F = file_Set.iterator();
			//	HashMap out_Files_data = new HashMap();
			files_1 = new  HashMap<String,Tuple2<Boolean,byte[]>>();
			 
			 while (it_F.hasNext()){
				 Tuple3<String, Boolean, byte[]> ff = it_F.next();
			 	/*
				  взять имя файлов 					attached_Files_Model.getValueAt(row,0);
				  взять признак орхивирования		attached_Files_Model.getValueAt(row,2);
				  взять содержимое файлов. Если ZIP то зашифрованный, если нет то не зашифрованный 		attached_Files_Model.getValueAt(row,5);
				*/
			files_1.put( ff.a, new Tuple2<Boolean,byte[]>( ff.b,ff.c));
			}
			}
			messageBytes =	 StrJSonFine.convert(out_Map).getBytes( Charset.forName("UTF-8") );
			out_Map = null;
			 messageBytes = Json_Files_to_Byte_V2(type, vers, title, new JSONObject(out_Map), files_1);
			if ( messageBytes.length < 10 || messageBytes.length > BlockChain.MAX_REC_DATA_BYTES )
			{
				;
		}
	}catch(Exception e){
	}
		return messageBytes;
	}
	
	@SuppressWarnings("unchecked")
	private  byte[]  Json_Files_to_Byte_V2(String type, String vers, String title, JSONObject json, HashMap<String,Tuple2<Boolean,byte[]>> files) throws Exception {
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		// type
		byte[] type_B = type.getBytes(Charset.forName("UTF-8"));
		byte[] size_Type = ByteBuffer.allocate(DATA_TYPE_PART_LENGTH).putInt(type_B.length).array();
		outStream.write (size_Type);
		outStream.write (type_B);
		type_B= null;
		size_Type = null;
		// version
		byte[] ver_B = type.getBytes(Charset.forName("UTF-8"));
		byte[] size_Ver = ByteBuffer.allocate(DATA_VERSION_PART_LENGTH).putInt(ver_B.length).array();
		outStream.write (size_Ver);
		outStream.write (ver_B);
		ver_B = null;
		size_Ver = null;
		
		// title
		byte[] title_Bytes = "".getBytes(Charset.forName("UTF-8"));
		if (title !=null){
			title_Bytes = title.getBytes(Charset.forName("UTF-8"));
		}
		byte[] size_Title = ByteBuffer.allocate(DATA_TITLE_PART_LENGTH).putInt(title_Bytes.length).array();
		outStream.write(size_Title);
		outStream.write(title_Bytes);
		size_Title = null;
		title_Bytes=null;
		
		// json
		if (json == null || json.equals("") ) return outStream.toByteArray();
		
		byte[] JSON_Bytes ;
		byte[] size_Json;
		
		if (files == null || files.size() == 0){
			JSON_Bytes = json.toString().getBytes(Charset.forName("UTF-8"));
			// convert int to byte
			size_Json = ByteBuffer.allocate(DATA_JSON_PART_LENGTH).putInt( JSON_Bytes.length).array();
			outStream.write(size_Json);
			outStream.write(JSON_Bytes);
			JSON_Bytes = null;
			size_Json = null;
			return outStream.toByteArray(); 
		}
		// if insert Files
		Iterator<Entry<String, Tuple2<Boolean, byte[]>>> it = files.entrySet().iterator();
		JSONObject files_Json = new JSONObject();
		int i = 0;
		 ArrayList<byte[]> out_files = new ArrayList<byte[]>();
		while(it.hasNext()){
			Entry<String, Tuple2<Boolean, byte[]>> file = it.next();
			JSONObject file_Json = new JSONObject();
			file_Json.put("FN", file.getKey()); //File_Name 
			file_Json.put("ZP", file.getValue().a.toString()); //ZIP
			file_Json.put("SZ", file.getValue().b.length+""); //Size
			files_Json.put(i+"", file_Json);
			out_files.add(i,file.getValue().b);
			i++;
		} 
		json.put("F",files_Json);
		files_Json = null;
		JSON_Bytes = json.toString().getBytes(Charset.forName("UTF-8"));
		// convert int to byte
		size_Json = ByteBuffer.allocate(DATA_JSON_PART_LENGTH).putInt( JSON_Bytes.length).array();
		outStream.write(size_Json);
		outStream.write(JSON_Bytes);
		size_Json = null;
		JSON_Bytes = null;
		for(i=0; i<out_files.size(); i++){
				outStream.write(out_files.get(i));	
		}
		return outStream.toByteArray();
	}
	
	
	
		public static  Tuple5<String, String,String,JSONObject,HashMap <String,Tuple2<Boolean, byte[]>>> parse_ALL(byte[] data, boolean addfile) throws Exception{
		//Version, Title, JSON, Files	
			
			//CHECK IF WE MATCH BLOCK LENGTH
			if (data.length < DATA_JSON_PART_LENGTH)
			{
				throw new Exception("Data does not match block length " + data.length);
			}
			int position = 0;
			
			// read type
			byte[] type_Size_Byte = Arrays.copyOfRange(data, position , DATA_TYPE_PART_LENGTH);
			int type_Size = Ints.fromByteArray(type_Size_Byte);
			position += DATA_TYPE_PART_LENGTH;
			byte[] typeByte = Arrays.copyOfRange(data, position , position + type_Size);
			position +=type_Size;
			
			// read version
			byte[] version_Size_Byte = Arrays.copyOfRange(data, position , DATA_VERSION_PART_LENGTH);
			int version_Size = Ints.fromByteArray(version_Size_Byte);
			position += DATA_VERSION_PART_LENGTH;
			byte[] versionByte = Arrays.copyOfRange(data, position , position + version_Size);
			position +=version_Size;
			
			// read title
			byte[] titleSizeBytes = Arrays.copyOfRange(data, position, position + DATA_TITLE_PART_LENGTH);
			int titleSize = Ints.fromByteArray(titleSizeBytes);
			position += DATA_TITLE_PART_LENGTH;
			
			byte[] titleByte = Arrays.copyOfRange(data, position , position + titleSize);
			
			position +=titleSize;
			//READ Length JSON PART
			byte[] dataSizeBytes = Arrays.copyOfRange(data, position , position + DATA_JSON_PART_LENGTH);
			int JSONSize = Ints.fromByteArray(dataSizeBytes);	
			
			position += DATA_JSON_PART_LENGTH;
			//READ JSON
			byte[] arbitraryData = Arrays.copyOfRange(data, position, position + JSONSize);
			JSONObject json = (JSONObject) JSONValue.parseWithException(new String(arbitraryData, Charset.forName("UTF-8")));
			
			String title = new String(titleByte, Charset.forName("UTF-8"));
			String version = new String(versionByte, Charset.forName("UTF-8"));
			position += JSONSize;
			HashMap<String,Tuple2<Boolean, byte[]>> out_Map = new HashMap<String,Tuple2<Boolean, byte[]>>();
			JSONObject files;
			if (json.containsKey("F") && addfile) { // return new Tuple4(version,title,json, null);
			files =(JSONObject) json.get("F");
			@SuppressWarnings("rawtypes")
			Set files_key_Set = files.keySet();
			for (int i = 0; i < files_key_Set.size(); i++) {
				JSONObject file = (JSONObject) files.get(i+"");
					String name = (String) file.get("FN"); // File_Name
					Boolean zip = new Boolean((String) file.get("ZP")); // ZIP
					byte[] bb = Arrays.copyOfRange(data, position, position + new Integer((String) file.get("SZ"))); //Size
					position = position + new Integer((String) file.get("SZ")); //Size
					out_Map.put(name, new Tuple2<Boolean, byte[]>(zip,bb));	
			}
			 return new Tuple5<String, String, String, JSONObject, HashMap<String, Tuple2<Boolean, byte[]>>>(new String(typeByte, Charset.forName("UTF-8")),version, title, json, out_Map);
			}
			return new Tuple5<String, String, String, JSONObject, HashMap<String, Tuple2<Boolean, byte[]>>>(new String(typeByte, Charset.forName("UTF-8")),version, title, json, null);
		}
	
	
	public static String viewDescriptionHTML(String descr) {
		
		if (descr.startsWith("#"))
			// MARK DOWN
			return Processor.process(descr);

		if (descr.startsWith("]"))
			// FORUM CKeditor
			// TODO CK_editor INSERT
			return Processor.process(descr);

		if (descr.startsWith("}"))
			// it is DOCX
			// TODO DOCX insert
			return descr;

		if (descr.startsWith(">"))
			// it is HTML
			return descr;

		// PLAIN TEXT
		return descr.replaceAll(" ", "&ensp;").replaceAll("\t", "&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;").replaceAll("\n","<br>");

	}
	
}
