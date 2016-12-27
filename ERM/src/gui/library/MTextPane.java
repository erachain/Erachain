package gui.library;

import javax.swing.JTextPane;
import javax.swing.UIManager;

import com.github.rjeschke.txtmark.Processor;

public class MTextPane extends JTextPane{
	public MTextPane(){
		super();
	}

	public MTextPane(String str){
		
		super();
		setContentType("text/html");
		str =  Processor.process(str);
		int font_saze = UIManager.getFont("Label.font").getSize();
		str= "<head><style>"
				+ " h1{ font-size: "+ font_saze +"px;  } "
				+ " h2{ font-size: "+ font_saze +"px;  }"
				+ " h3{ font-size: "+ font_saze +"px;  }"
				+ " h4{ font-size: "+ font_saze +"px;  }"
				+ " h5{ font-size: "+ font_saze +"px;  }"
				+ " body{ font-family:"+UIManager.getFont("Label.font").getFamily()
				+  "; font-size:"+ font_saze +"px}"
				+ "</style> </head><body>"
				+str +"</body>";
		this.setText(str);
		
		
	}
	
}
