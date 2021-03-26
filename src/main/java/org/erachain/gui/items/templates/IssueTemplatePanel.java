package org.erachain.gui.items.templates;

import org.erachain.controller.Controller;
import org.erachain.core.transaction.IssueTemplateRecord;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.items.utils.GUIConstants;
import org.erachain.gui.library.Library;
import org.erachain.lang.Lang;

@SuppressWarnings("serial")
public class IssueTemplatePanel extends IssueItemPanel {

    public static String NAME = "IssueTemplatePanel";
    public static String TITLE = "Issue Template";

    public IssueTemplatePanel() {
        super(NAME, TITLE, "Template issue has been sent", true, GUIConstants.WIDTH_IMAGE, GUIConstants.WIDTH_IMAGE, true);

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
                addIconLabel.getImgBytes(), addImageLabel.getImgBytes(),
                feePow);
    }

    protected String makeTransactionView() {

        String text = "<HTML><body>";
        text += Lang.T("Confirmation transaction issue template") + "<br><br><br>";
        text += Lang.T("Creator") + ":&nbsp;" + transaction.getCreator() + "<br>"
                + (exLink == null ? "" : Lang.T("Append to") + ":&nbsp;<b>" + exLink.viewRef() + "</b><br>");
        text += Lang.T("Title") + ":&nbsp;" + transaction.getItem().viewName() + "<br>";
        text += Lang.T("Description") + ":<br>"
                + Library.to_HTML(transaction.getItem().getDescription()) + "<br>";

        return text;

    }

}