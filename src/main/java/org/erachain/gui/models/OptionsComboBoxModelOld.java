package org.erachain.gui.models;

import org.erachain.core.voting.PollOption;

import javax.swing.*;
import java.util.List;

@SuppressWarnings("serial")
public class OptionsComboBoxModelOld extends DefaultComboBoxModel<PollOption> {

    public OptionsComboBoxModelOld(List<PollOption> options) {
        for (PollOption option : options) {
            this.addElement(option);
        }
    }
}
