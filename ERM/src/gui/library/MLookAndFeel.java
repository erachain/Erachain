package gui.library;

import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.plaf.basic.BasicLookAndFeel;

import de.muntjak.tinylookandfeel.TinyLookAndFeel;

public class MLookAndFeel extends TinyLookAndFeel {
	
	
	protected void initClassDefaults ( UIDefaults table )
	{
	    // По прежнему оставляем дефолтную инициализацию, так как мы пока что не реализовали все
	    // различные UI-классы для J-компонентов
	    super.initClassDefaults ( table );

	    // А вот, собственно, самое важное
	    table.put (  "InternalFrameUI", MTyniInternalFrameUI.class.getCanonicalName () );
	    
	    table=table;
	  
	}

	
	
	

}
