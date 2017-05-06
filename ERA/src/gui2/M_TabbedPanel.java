package gui2;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

public class M_TabbedPanel extends JTabbedPane {
	public M_TabbedPanel(){
		super();
	}
	public M_TabbedPanel(int a, int b){
	//TODO	public JTabbedPane(int tabPlacement, int tabLayoutPolicy)
		super(a,b);
	}
	public void addTabWithCloseButton (String str, Component comp){
		this.addTab(str, comp);
		this.setTabComponentAt(this.indexOfComponent(comp), new ButtonTabComponent(this));	
	}
	public void addTabWithCloseButton (String str, Icon icon, Component comp){
		this.addTab(str, icon, comp);
		this.setTabComponentAt(this.indexOfComponent(comp), new ButtonTabComponent(this));	
	}
	public void addTabWithCloseButton (String str, Icon icon, Component comp, String tip){
		this.addTab(str, icon, comp, tip);
		this.setTabComponentAt(this.indexOfComponent(comp), new ButtonTabComponent(this));	
	}
	
	
	
}
