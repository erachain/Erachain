package gui.items.imprints;

import java.awt.Component;
import core.item.ItemCls;
import core.item.imprints.ImprintCls;
import gui.items.Item_Search_SplitPanel;

public class Imprints_Search_SplitPanel extends Item_Search_SplitPanel {

	private static final long serialVersionUID = 2717571093561259483L;

	private static TableModelImprintsSearch search_Table_Model = new TableModelImprintsSearch() ;

	public Imprints_Search_SplitPanel() {
		super(search_Table_Model, "Persons_Search_SplitPanel", "Persons_Search_SplitPanel");
		
	}

	// show details
	@Override
	public Component get_show(ItemCls item) {

		return new Imprints_Info_Panel((ImprintCls) item);

	}
}
