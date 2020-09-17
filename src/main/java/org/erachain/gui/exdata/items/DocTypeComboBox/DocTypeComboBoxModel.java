package org.erachain.gui.exdata.items.DocTypeComboBox;

import org.erachain.core.exdata.ExData;

import javax.swing.*;

// model class
class DocTypeComboBoxModel extends DefaultComboBoxModel<Integer> {

    public DocTypeComboBoxModel() {
        this.addElement(ExData.LINK_APPENDIX_TYPE);
        this.addElement(ExData.LINK_COMMENT_TYPE);
        this.addElement(ExData.LINK_RATING_TYPE);
        this.addElement(ExData.LINK_SURELY_TYPE);

    }

}