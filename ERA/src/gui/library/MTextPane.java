package gui.library;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import com.github.rjeschke.txtmark.Processor;

public class MTextPane extends JScrollPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JTextPane text_pane;

	public MTextPane() {
		super();
		install();
	}

	public MTextPane(String str) {
		super();
		
		install();
		//setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
		//setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);

		//text_pane = new JTextPane();
		//text_pane.setEditable(false);

		//text_pane.setContentType("text/html");
		set_text(str);
		//setViewportView(text_pane);

	}
	public void set_text(String str){
		str = library.viewDescriptionHTML(str);
		int font_saze = UIManager.getFont("Label.font").getSize();
		str = "<head><style>" 
				+ " body{ font-family:"
				+ UIManager.getFont("Label.font").getFamily() + "; font-size:" + font_saze + "px;"
				+ "word-wrap:break-word;}"
				+ "</style> </head><body><div style='style='word-wrap: break-word; '>" + str + "</body>";
		text_pane.setText(str);
		
		
		
	}
	private void install(){
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
		setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);

		text_pane = new JTextPane();
		text_pane.setEditable(false);

		text_pane.setContentType("text/html");
		setViewportView(text_pane);
		
		
		
	}

}
