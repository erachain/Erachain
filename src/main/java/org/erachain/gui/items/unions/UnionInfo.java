package org.erachain.gui.items.unions;

import org.erachain.core.item.unions.UnionCls;
import org.erachain.gui.library.MTextPane;
import org.erachain.lang.Lang;

import java.text.SimpleDateFormat;

// Info for union
public class UnionInfo extends MTextPane {

    private static final long serialVersionUID = 1L;
    private String message = "<HTML>" + Lang.T("Select union");

    public UnionInfo() {
    }

    public String Get_HTML_Union_Info_001(UnionCls union) {

        String dateAlive;
        String date_birthday;

        // устанавливаем формат даты
        SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");


        if (union != null) {
            //date_birthday =  formatDate.format(new Date(Long.valueOf(union.getBirthday())));
            date_birthday = union.getBirthdayStr();
            message = "<html><div>#" + "<b>" + union.getKey() + " : " + date_birthday + "</b>"
                    + "<br>" + union.viewName().toString() +
                    "<br>" +
                    "</div>";

        } else {
            message = "<html><p>" + Lang.T("Not found!") + "</p>";
        }
        message = message + "</html>";


        return message;
    }

    public String Get_HTML_Union_Info_002(UnionCls union) {


        String Date_Acti;

        SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");
        if (union != null) { //if (table.getSelectedRow() >= 0 ){

            message = message + "</html>";
        }

        return message;
    }

    public void show_Union_001(UnionCls union) {

        setText(Get_HTML_Union_Info_001(union));
        return;
    }

    public void show_Union_002(UnionCls union) {

        setText(Get_HTML_Union_Info_002(union));
        return;
    }


}
