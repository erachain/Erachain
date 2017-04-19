package gui.library;


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
	
		
	public String init_String(String text, boolean first){
		Pattern p = Pattern.compile("\\{\\{(.+?)\\}\\}");
	
	 String out = text;  // переводим в маркдаун
	 
	Matcher m = p.matcher(text);
	// начальный разбор и присвоение начальных параметров строке  
	while (m.find()){
		if (first) pars.put(m.group(), m.group(1));
		out = out.replace(m.group(), "<A href=" + m.group(1)  +">" + pars.get(m.group())+ "</a>");
		
	}
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
			+ "</style> </head><body>" + out + "</body>";
	
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

}

