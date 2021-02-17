package org.erachain.gui.library;


import javax.swing.*;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MImprintEDITPane extends JTextPane {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public HashMap<String, String> pars;
    public JTextPane th;
    public String text;

    public MImprintEDITPane() {
        pars = new HashMap<String, String>();
        th = this;
        setContentType("text/html");
        setEditable(false);

    }

    public void setText(String text) {
        this.text = text;
        super.setText(init_String(true));
        setCaretPosition((caretPosition = 0));
    }

    public int caretPosition;

    public void fixCaretPosition() {
        caretPosition = getCaretPosition();
    }

    public void updateText() {
        super.setText(init_String(false));
        setCaretPosition(caretPosition);
        caretPosition = 0;

    }

    public int indexOf(String value) {
        return text.indexOf(value);
    }

    String updatedParam;

    public void updateParam(String param, String value) {
        updatedParam = param;
        pars.replace("{{" + param + "}}", value);
        updateText();
    }


    public String init_String(boolean first) {
        Pattern p = Pattern.compile("\\{\\{(.+?)\\}\\}");

        // переводим в нужный формат
        String out = Library.viewDescriptionHTML(text);
        out = out.replaceAll("\n", "<br>");

        Matcher m = p.matcher(out);
        // начальный разбор и присвоение начальных параметров строке
        while (m.find()) {
            if (first) pars.put(m.group(), m.group(1));
            out = out.replace(m.group(), "<A href='!$@!" + m.group(1) + "' style='color:green'>" + to_HTML(pars.get(m.group())) + "</a>");
        }

        updatedParam = null;
        int fontSize = UIManager.getFont("Label.font").getSize();

        return "<head><style>"
                + " h1{ font-size: " + (fontSize + 5) + "px;  } "
                + " h2{ font-size: " + (fontSize + 3) + "px;  }"
                + " h3{ font-size: " + (fontSize + 1) + "px;  }"
                + " h4{ font-size: " + fontSize + "px;  }"
                + " h5{ font-size: " + (fontSize - 1) + "px;  }"
                + " body{ font-family:"
                + UIManager.getFont("Label.font").getFamily() + "; font-size:" + fontSize + "px;"
                + "word-wrap:break-word;}"
                + "</style> </head><body>" + out
                + "</body>";

    }

    // TODO Auto-generated method stub
    public HashMap<String, String> get_Params() {
        Set<String> aa = pars.keySet();
        HashMap<String, String> pps = new HashMap<String, String>();
        for (String a : aa) {
            pps.put(a.replace("{{", "").replace("}}", ""), pars.get(a));
        }
        return pps;
    }

    public String to_HTML(String str) {
        String out = null;
        out = str.replaceAll(" ", "&ensp;").replaceAll("\t", "&ensp&ensp&ensp&ensp&ensp&ensp&ensp&ensp").replaceAll("\n", "<br>");

        return out;
    }


}

