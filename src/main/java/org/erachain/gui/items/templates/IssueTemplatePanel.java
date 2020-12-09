package org.erachain.gui.items.templates;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.IssueTemplateRecord;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;

@SuppressWarnings("serial")
public class IssueTemplatePanel extends IssueItemPanel {

    public static String NAME = "IssueTemplatePanel";
    public static String TITLE = "Issue Template";

    public IssueTemplatePanel() {
        super(NAME, TITLE, null, "Template issue has been sent");

        initComponents();

        initBottom(initTopArea());

        setVisible(true);
    }

    protected boolean checkValues() {
        return true;
    }

    protected void makeTransaction() {

        transaction = (IssueTemplateRecord) Controller.getInstance().issueTemplate(creator,
                exLink, textName.getText(), textAreaDescription.getText(),
                addLogoIconLabel.getImgBytes(), addImageLabel.getImgBytes(),
                feePow);
    }

    protected String makeTransactionView() {

        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation transaction issue template") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + transaction.getCreator() + "<br>";
        text += Lang.getInstance().translate("Title") + ":&nbsp;" + transaction.getItem().viewName() + "<br>";
        text += Lang.getInstance().translate("Description") + ":<br>"
                + Library.to_HTML(transaction.getItem().getDescription()) + "<br>";

        return text;

    }

}