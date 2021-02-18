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
        try {
            setCaretPosition(caretPosition);
        } catch (Exception e) {
            // if out size
        }
        caretPosition = 0;

    }

    String updatedParam;

    public void updateParam(String param, String value) {
        updatedParam = "{{" + param + "}}";
        pars.replace(updatedParam, value);
        updateText();
    }


    public String init_String(boolean first) {
        Pattern p = Pattern.compile("\\{\\{(.+?)\\}\\}");

        // переводим в нужный формат
        String out = text;

        Matcher m = p.matcher(out);
        // начальный разбор и присвоение начальных параметров строке
        String updatedValue = "";
        while (m.find()) {
            if (first)
                pars.put(m.group(), m.group(1));

            String newValue = "<A href='!$@!" + m.group(1) + "' style='color:green'>" + to_HTML(pars.get(m.group())) + "</a>";

            if (caretPosition == 0 && updatedParam != null && updatedParam.equals(m.group())) {
                updatedValue = newValue;
            }
            out = out.replace(m.group(), newValue);
        }

        out = Library.viewDescriptionHTML(out);
        out = out.replaceAll("\n", "<br>");

        int fontSize = UIManager.getFont("Label.font").getSize();

        if (caretPosition == 0) {
            // UTF-8 2 byte
            caretPosition = out.indexOf(updatedValue) >> 1;
        }

        out = "<head><style>"
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

        updatedParam = null;
        return out;

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

