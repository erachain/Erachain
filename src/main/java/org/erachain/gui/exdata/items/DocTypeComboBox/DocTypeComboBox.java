package org.erachain.gui.exdata.items.DocTypeComboBox;



import javax.swing.*;


public class DocTypeComboBox extends JComboBox<Integer> {
    private final DocTypeComboBoxModel model;

    public DocTypeComboBox(){
        model = new DocTypeComboBoxModel();
        this.setModel(model);
        this.setRenderer(new DocTypeComboBoxRender());
    }
}
