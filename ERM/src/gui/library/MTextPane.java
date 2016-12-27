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
		str= "<head><style>"
				+ " h1{ font-size: 24px;  } "
				+ " h2{ font-size: 24px;  }"
				+ " h3{ font-size: 24px;  }"
				+ " h4{ font-size: 24px;  }"
				+ " h5{ font-size: 24px;  }"
				+ " body{ font-family:"+UIManager.getFont("Label.font").getFamily()
				+  "; font-size:"+ UIManager.getFont("Label.font").getSize() +"px}"
				+ "</style> </head><body>"
				+str +"</body>";
		this.setText(str);
		
		
	}
	
}
