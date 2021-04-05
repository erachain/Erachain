package org.erachain.gui.records;

import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;

import javax.swing.*;

//import org.erachain.gui.*;

// Info for record
public class RecordInfo extends JTextPane {

    //private static final long serialVersionUID = 4763074704570450206L;
    private static final long serialVersionUID = 2717571093561259483L;


    public RecordInfo() {

        this.setContentType("text/html");
        //	this.setBackground(MainFrame.getFrames()[0].getBackground());

    }


    static String Get_HTML_Record_Info_001(Transaction record) {

        DCSet db = DCSet.getInstance();

        String message = "";
        //SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");

        if (record == null) return message += "Empty Record";

        if (!record.isConfirmed(db)) {
            message = Lang.T("Not confirmed");
        } else {
            message = "Block Height - SeqNo.: " + record.viewHeightSeq() + ", Confs.: " + record.getConfirmations(DCSet.getInstance());
        }
        message = "<div><b>" + message + "</b>"
                + ", time: " + record.viewTimestamp() + "</div>";
        message += "<div> type: <b>" + Lang.T(record.viewFullTypeName()) + "</b>, size: " + record.viewSize(Transaction.FOR_NETWORK)
                + ", fee: " + record.viewFeeAndFiat(UIManager.getFont("Label.font").getSize()) + "</div>";

        //message += "<div>REF: <font size='2'>" + record.viewReference() + "</font></div>";
        message += "<div>SIGN: <font size='2'>" + record.viewSignature() + "</font></div>";

        message += "<div>Creator: <font size='4'>" + record.viewCreator() + "</font></div>";
        message += "<div>Item: <font size='4'>" + record.viewItemName() + "</font></div>";
        message += "<div>Amount: <font size='4'>" + record.viewAmount() + "</font></div>";
        message += "<div>Recipient: <font size='4'>" + record.viewRecipient() + "</font></div>";
        message += "<div>JSON: <font size='4'>" + record.toJson().toString() + "</font></div>";

        return message;
    }

    public void show_001(Transaction record) {

        setText("<html>" + Get_HTML_Record_Info_001(record) + "</html>");
        return;
    }

    public void show_mess(String mess) {

        setText("<html>" + mess + "</html>");
        return;
    }

}
