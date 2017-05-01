package gui.library;


import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import com.github.rjeschke.txtmark.Processor;

import core.BlockChain;



public class MImprintEDIT_Pane extends JTextPane {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public HashMap<String, String> pars;
	public JTextPane th;
	public String text;
	
	public MImprintEDIT_Pane(){
		pars = new HashMap<String, String>();
		th = this;
		setContentType("text/html");
		setEditable(false);
		
	}
	public void set_Text(String text1){
	this.text = text1;
		setText(init_String(text, true));
	
		
	}
	public void set_View(String text1){
		this.text = text1;
			setText(init_String(text, true));
		
			
		}
	
		
	public String init_String(String text, boolean first){
		Pattern p = Pattern.compile("\\{\\{(.+?)\\}\\}");
	//	if(BlockChain.DEVELOP_USE)	text = text + "\n {{!Bottom}}";
	 String out = text;  // переводим в маркдаун
	 
	Matcher m = p.matcher(text);
	// начальный разбор и присвоение начальных параметров строке  
	while (m.find()){
		if (first) pars.put(m.group(), m.group(1));
		out = Processor.process(out);
		out = out.replace(m.group(), "<A href=" + m.group(1)  +">" + to_HTML(pars.get(m.group()))+ "</a>");
		
	}
	
	out  = out.replaceAll("\n","<br>");
	
	int font_saze = UIManager.getFont("Label.font").getSize();
	
	
	return "<head><style>" 
			+ " h1{ font-size: " + font_saze + "px;  } " 
			+ " h2{ font-size: " + font_saze + "px;  }"
			+ " h3{ font-size: " + font_saze + "px;  }" 
			+ " h4{ font-size: " + font_saze + "px;  }"
			+ " h5{ font-size: " + font_saze + "px;  }" 
			+ " body{ font-family:"
			+ UIManager.getFont("Label.font").getFamily() + "; font-size:" + font_saze + "px;"
			+ "word-wrap:break-word;}"
			+ "</style> </head><body>" + out
		//	+ "<br><a href= 111jcnfkBOTTOM>Bottom</a>"
			+ "</body>";
	
	}
	// TODO Auto-generated method stub
	public HashMap<String, String> get_Params(){
	Set<String> aa = pars.keySet();
	HashMap<String,String> pps=new HashMap<String, String>();
	for(String a:aa){
		pps.put(a.replace("{{","").replace("}}", ""), pars.get(a));
	}
	return pps;
	}
	
	public String init_view(String text1, HashMap<String,String> pars){
	 Set<String> aa = pars.keySet();
	for(String par:aa){
		text1 = text1.replace("{{"+par+"}}", pars.get(par));
	}
	return text1;
	}
	public String to_HTML(String str){
	String out= null;
	out = str.replaceAll(" ", "&ensp").replaceAll("\t", "&ensp&ensp&ensp&ensp&ensp&ensp&ensp&ensp").replaceAll("\n","<br>");
	
	return out;
	}
	
	
	
}

