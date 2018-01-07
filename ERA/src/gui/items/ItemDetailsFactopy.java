package gui.items;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import controller.Controller;
import core.item.ItemCls;
import core.item.unions.UnionCls;
import gui.items.unions.Union_Info;
import lang.Lang;

public class ItemDetailsFactopy extends JPanel {

	private static final long serialVersionUID = 4763074704570450206L;

	private ItemCls item;

	private JButton favoritesButton;
	private static ItemDetailsFactopy instance;

	public static ItemDetailsFactopy getInstance() {
		if (instance == null) {
			instance = new ItemDetailsFactopy();
		}
		return instance;
	}

	private ItemDetailsFactopy() {

	}

	public Object show(ItemCls item) {
		int in = item.getItemTypeInt();
		
		switch (in) {
		case ItemCls.ASSET_TYPE:
			return null;
		case ItemCls.IMPRINT_TYPE:
			return null;
		case ItemCls.TEMPLATE_TYPE:
			return null;
		case ItemCls.PERSON_TYPE:
			return null;
		case ItemCls.UNION_TYPE:
			Union_Info cc;
			cc = new Union_Info();
			cc.show_Union_001((UnionCls) item);
			return cc;
		}
		return null;

	}

}
