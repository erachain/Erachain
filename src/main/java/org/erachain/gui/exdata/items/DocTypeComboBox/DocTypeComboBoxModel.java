package org.erachain.gui.exdata.items.DocTypeComboBox;

import org.erachain.core.exdata.ExData;

import javax.swing.*;

// model class
class DocTypeComboBoxModel extends DefaultComboBoxModel<Integer> {

    public DocTypeComboBoxModel() {
        this.addElement((int) ExData.LINK_SIMPLE_TYPE);
        this.addElement((int) ExData.LINK_APPENDIX_TYPE);
        //this.addElement((int) ExData.LINK_REPLY_TYPE);
        //this.addElement((int) ExData.LINK_COMMENT_TYPE);
        //this.addElement((int) ExData.LINK_SURELY_TYPE);

    }

}