package org.erachain.gui.items.templates;

import org.erachain.controller.Controller;
import org.erachain.gui.items.IssueItemPanel;
import org.erachain.gui.items.utils.GUIConstants;

@SuppressWarnings("serial")
public class IssueTemplatePanel extends IssueItemPanel {

    public static String NAME = "IssueTemplatePanel";
    public static String TITLE = "Issue Template";

    public IssueTemplatePanel() {
        super(NAME, TITLE, null, null, true, GUIConstants.WIDTH_IMAGE, GUIConstants.WIDTH_IMAGE, true, true);

        initComponents();

        initBottom(initTopArea(true));

        setVisible(true);
    }

    protected boolean checkValues() {
        return true;
    }

    @Override
    protected void makeTransaction() {

        transaction = Controller.getInstance().issueTemplate(itemAppData, creator,
                exLink, nameField.getText(), textAreaDescription.getText(),
                addIconLabel.getMediaBytes(), addImageLabel.getMediaBytes(),
                feePow);
    }

}