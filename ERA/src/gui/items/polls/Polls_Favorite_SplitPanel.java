package gui.items.polls;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.polls.PollCls;
import core.item.templates.TemplateCls;
import core.transaction.Transaction;
import datachain.DCSet;
import gui.items.Item_SplitPanel;
import gui.records.VouchRecordDialog;
import lang.Lang;

public class Polls_Favorite_SplitPanel extends Item_SplitPanel {
	private static final long serialVersionUID = 2717571093561259483L;
	private static Polls_Favorite_TableModel table_Model = new Polls_Favorite_TableModel();
	private Polls_Favorite_SplitPanel th;

	public Polls_Favorite_SplitPanel() {
		super(table_Model, "Polls_Favorite_SplitPanel");
		this.setName(Lang.getInstance().translate("Favorite Polls"));
		th = this;
		JMenuItem vouch_menu= new JMenuItem(Lang.getInstance().translate("Vouch"));
		vouch_menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DCSet db = DCSet.getInstance();
				Transaction trans = db.getTransactionFinalMap().getTransaction(((TemplateCls) th.item_Menu).getReference());
				new VouchRecordDialog(trans.getBlockHeight(db), trans.getSeqNo(db));
				
			}
		});
		th.menu_Table.add(vouch_menu);
		JMenuItem setVote_Menu = new JMenuItem(Lang.getInstance().translate("Voting"));
		setVote_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				new UnionSetStatusDialog(th, (UnionCls) item_Menu);

				PollCls poll = (PollCls)(item_Table_Selected);
				AssetCls AssetCls = DCSet.getInstance().getItemAssetMap().get((long) (1));
				new Polls_Dialog(poll,0, AssetCls);
			}
		});
		th.menu_Table.add(setVote_Menu);
	}

	// show details
	@Override
	public Component get_show(ItemCls item) {
		//return  null;//new Info_Templates((TemplateCls) item);
		AssetCls AssetCls = DCSet.getInstance().getItemAssetMap().get((long) (1));
		PollsDetailPanel pollInfo = new PollsDetailPanel((PollCls) item, AssetCls);
		
		return pollInfo;
	}
	
	@Override
	protected void splitClose(){ 
		table_Model.removeObservers();
		
	}
}
