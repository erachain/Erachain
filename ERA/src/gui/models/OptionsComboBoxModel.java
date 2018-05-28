package gui.models;

import javax.swing.*;
import java.util.List;

@SuppressWarnings("serial")
public class OptionsComboBoxModel extends DefaultComboBoxModel<String> {

    public OptionsComboBoxModel(List<String> options) {
        for (String option : options) {
            this.addElement(option);
        }
    }
}
