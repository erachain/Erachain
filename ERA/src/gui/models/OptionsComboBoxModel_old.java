package gui.models;

import java.util.List;

import javax.swing.DefaultComboBoxModel;

import core.voting.PollOption;

@SuppressWarnings("serial")
public class OptionsComboBoxModel_old extends DefaultComboBoxModel<PollOption> {

	public OptionsComboBoxModel_old(List<PollOption> options)
	{
		for(PollOption option: options)
		{
			this.addElement(option);
		}
	}
}
