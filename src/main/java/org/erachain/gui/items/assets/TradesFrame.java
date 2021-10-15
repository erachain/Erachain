package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.Order;
import org.erachain.lang.Lang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class TradesFrame extends JDialog {

    private OrderTradesTableModel tradesTableModel;

    public TradesFrame(Order order, boolean isSell) {

        setTitle(Controller.getInstance().getApplicationName(false) + " - " + Lang.T("Trades"));
        setModal(true);
        //ICON
        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);

        //CLOSE
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //LAYOUT
        this.setLayout(new GridBagLayout());

        //PADDING
        ((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));

        //SEACH LABEL GBC
        GridBagConstraints searchLabelGBC = new GridBagConstraints();
        searchLabelGBC.insets = new Insets(0, 5, 5, 0);
        searchLabelGBC.fill = GridBagConstraints.HORIZONTAL;
        searchLabelGBC.anchor = GridBagConstraints.NORTHWEST;
        searchLabelGBC.weightx = 0;
        searchLabelGBC.gridwidth = 1;
        searchLabelGBC.gridx = 0;
        searchLabelGBC.gridy = 0;

        //SEACH GBC
        GridBagConstraints searchGBC = new GridBagConstraints();
        searchGBC.insets = new Insets(0, 5, 5, 0);
        searchGBC.fill = GridBagConstraints.HORIZONTAL;
        searchGBC.anchor = GridBagConstraints.NORTHWEST;
        searchGBC.weightx = 1;
        searchGBC.gridwidth = 1;
        searchGBC.gridx = 1;
        searchGBC.gridy = 0;

        //TABLE GBC
        GridBagConstraints tableGBC = new GridBagConstraints();
        tableGBC.insets = new Insets(0, 5, 5, 0);
        tableGBC.fill = GridBagConstraints.BOTH;
        tableGBC.anchor = GridBagConstraints.NORTHWEST;
        tableGBC.weightx = 1;
        tableGBC.weighty = 1;
        tableGBC.gridwidth = 2;
        tableGBC.gridx = 0;
        tableGBC.gridy = 1;

        //CREATE TABLE
        this.tradesTableModel = new OrderTradesTableModel(order, isSell);
        final JTable tradesTable = new JTable(this.tradesTableModel);

        //CHECKBOX FOR CONFIRMED
        //ASSETS SORTER
        //Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
        //CoreRowSorter sorter = new CoreRowSorter(this.tradesTableModel, indexes);
        //tradesTableModel.setRowSorter(sorter);


        // TODO всатвить менгю по сделке - кто что трнзакция и т.д.
        // MENU
		/*JPopupMenu ordersMenu = new JPopupMenu();
		JMenuItem trades = new JMenuItem("Trades");
		trades.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = ordersTable.getSelectedRow();
				row = ordersTable.convertRowIndexToModel(row);

				Order order = ordersTableModel.getOrder(row);
				new TradesFrame(order);
			}
		});
		ordersMenu.add(trades);*/
		/*assetsTable.setComponentPopupMenu(nameSalesMenu);
		assetsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				int row = assetsTable.rowAtPoint(p);
				assetsTable.setRowSelectionInterval(row, row);

				if(e.getClickCount() == 2)
				{
					row = assetsTable.convertRowIndexToModel(row);
					Check asset = assetsTableModel.getAsset(row);
					new AssetFrame(asset);
				}
			}
		});*/

        this.add(new JScrollPane(tradesTable), tableGBC);

        //PACK
        this.pack();
        this.setSize(800, this.getHeight());
        this.setResizable(true);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

}
