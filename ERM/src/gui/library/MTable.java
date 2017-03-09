package gui.library;

import java.awt.FontMetrics;
import java.math.BigDecimal;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

import org.mapdb.Fun.Tuple2;

import core.account.PublicKeyAccount;
import gui.models.Renderer_Boolean;
import gui.models.Renderer_Left;
import gui.models.Renderer_Right;

public class MTable extends JTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MTable(TableModel model) {
		super();
	// set model
		setModel(model);
	// height row in table	
		setRowHeight( (int) (getFontMetrics(getFont()).getHeight()));
	// set renders
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
/*	
		setDefaultRenderer(Integer.class, new Renderer_Right()); // set renderer
	setDefaultRenderer(String.class, new Renderer_Left()); // set renderer
		setDefaultRenderer(Long.class, new Renderer_Right()); // set renderer
		setDefaultRenderer(Boolean.class, new Renderer_Boolean()); // set renderer
		setDefaultRenderer(Tuple2.class, new Renderer_Left()); // set renderer
		setDefaultRenderer(Date.class, new Renderer_Right()); // set renderer
		setDefaultRenderer(PublicKeyAccount.class, new Renderer_Left()); // set renderer
		//RenderingHints.
		setDefaultRenderer(Double.class, new Renderer_Right()); // set renderer
		
	*/		
		
		
	
	}

}
