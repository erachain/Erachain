package gui.library;


import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;



public class MImprintEDIT_Pane extends JTextPane {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HashMap<String, String> pars;
	private JTextPane th;
	private String text;
	
	public MImprintEDIT_Pane(){
		
	}
	public void setText(String text1){
	
		th = this;
		this.text = text1;
		pars = new HashMap<String, String>();
	//	this.text = "werwe{{parametr_1}}  kgjdflkgdlfk  {{parametr_2}} cxvxvxcvxc {{parametr_1}}";
	//
		
	
		addHyperlinkListener(new HyperlinkListener(){

			@Override
			public void hyperlinkUpdate(HyperlinkEvent arg0) {
				// TODO Auto-generated method stub
				 if (arg0.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
				 String str = JOptionPane.showInputDialog(th, "Set Param", pars.get("{{"+ arg0.getDescription()+"}}"));
				 if (str==null || str.equals("")) return;
				 pars.replace("{{"+ arg0.getDescription()+"}}", str);
				 setText(init_String(text, false));
			
			}
			
			
			
			
		});
		setContentType("text/html");
		setEditable(false);
		setText(init_String(text, true));
	
		
	}
	
		
	private String init_String(String text, boolean first){
		Pattern p = Pattern.compile("\\{\\{(.+?)\\}\\}");
	
	 String out = text;  // переводим в маркдаун
	 
	Matcher m = p.matcher(text);
	// начальный разбор и присвоение начальных параметров строке  
	while (m.find()){
		if (first) pars.put(m.group(), m.group(1));
		out = out.replace(m.group(), "<A href=" + m.group(1)  +">" + pars.get(m.group())+ "</a>");
		
	}

	return out;
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

