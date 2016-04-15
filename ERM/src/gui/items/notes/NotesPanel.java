package gui.items.notes;


//import qora.item.notes.NoteCls;
import utils.ObserverMessage;
import gui.items.ItemsPanel;

@SuppressWarnings("serial")
public class NotesPanel extends ItemsPanel
{
	public NotesPanel()
	{		
		super(ObserverMessage.ADD_NOTE_TYPE, ObserverMessage.REMOVE_NOTE_TYPE, ObserverMessage.LIST_NOTE_TYPE,
				"All Notes", "Issue Note");
	}
	
}
