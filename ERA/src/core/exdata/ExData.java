package core.exdata;

import com.github.rjeschke.txtmark.Processor;

public class ExData {

	
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
