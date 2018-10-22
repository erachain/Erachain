package org.erachain.gui.models;

import org.erachain.core.voting.PollOption;

import javax.swing.*;
import java.util.List;

@SuppressWarnings("serial")
public class OptionsComboBoxModel_old extends DefaultComboBoxModel<PollOption> {

    public OptionsComboBoxModel_old(List<PollOption> options) {
        for (PollOption option : options) {
            this.addElement(option);
        }
    }
}
