package gui.models;

//import java.util.Observable;
//import java.util.Observer;

//import org.mapdb.Fun.Tuple2;

//import qora.item.notes.NoteCls;
import utils.ObserverMessage;
//import controller.Controller;
//import database.SortableList;
import lang.Lang;

@SuppressWarnings("serial")
public class WalletItemNotesTableModel extends WalletItem_TableModel
{
	
	public WalletItemNotesTableModel()
	{
		super( ObserverMessage.ADD_NOTE_TYPE,  ObserverMessage.REMOVE_NOTE_TYPE,  ObserverMessage.LIST_NOTE_TYPE);
	}	
}
