package gui.models;

import java.util.List;

import javax.swing.DefaultComboBoxModel;

@SuppressWarnings("serial")
public class OptionsComboBoxModel extends DefaultComboBoxModel<String> {

	public OptionsComboBoxModel(List<String> options)
	{
		for(String option: options)
		{
			this.addElement(option);
		}
	}
}
