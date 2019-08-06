package org.erachain.gui.items.assets;

// 30/03

import org.erachain.controller.Controller;
import org.erachain.core.blockexplorer.BlockExplorer;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.database.SortableList;
import org.erachain.database.wallet.FavoriteItemMapAsset;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.datachain.ItemMap;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.utils.Pair;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mapdb.Fun.Tuple6;
import org.erachain.utils.NumberAsString;
import org.erachain.utils.ObserverMessage;

import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("serial")
public class AssetPairSelectTableModel extends TimerTableModelCls<ItemCls> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ORDERS_COUNT = 2;
    public static final int COLUMN_ORDERS_VOLUME = 3;
    public static final int COLUMN_TRADES_COUNT = 4;
    public static final int COLUMN_TRADES_VOLUME = 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetPairSelectTableModel.class);

    public long key;
    Map<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> all;

    private String filter_Name;

    public AssetPairSelectTableModel(long key)
    {
        super(DCSet.getInstance().getItemAssetMap(), new String[]{"Key", "Name", "<html>Orders<br>count</html>", "Orders Volume",
                "<html>Trades<br>count</html>", "Trades Volume"},
                new Boolean[]{false, true, false, false, false, false}, false);

        this.key = key;
        this.all = BlockExplorer.getInstance().calcForAsset(DCSet.getInstance().getOrderMap().getOrders(this.key),
                DCSet.getInstance().getTradeMap().getTrades(this.key));


        ItemAssetMap assetMap = DCSet.getInstance().getItemAssetMap();
        FavoriteItemMapAsset favoriteMap = Controller.getInstance().wallet.database.getAssetFavoritesSet();

        Collection<Long> favorites = favoriteMap.getFromToKeys(0, Long.MAX_VALUE);
        //favorites.sort();

        list = new ArrayList<>();
        for (Long itemKey: favorites) {
            AssetCls asset = assetMap.get(itemKey);
            if (asset == null){
                continue;
            }
            list.add(asset);
        }

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        Long keyAsset = this.list.get(row).getKey();
        Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal> item = this.all.get(keyAsset);

        try {


            switch (column) {
                case COLUMN_KEY:

                    return keyAsset;

                case COLUMN_NAME:

                    return this.list.get(row).viewName();

                case COLUMN_ORDERS_COUNT:


                    return item == null ? "" : item.a;

                case COLUMN_ORDERS_VOLUME:


                    return item == null ? "" : ("<html>" + (item.c == null ? "0" : NumberAsString.formatAsString(item.c)))
                            //+ " " + this.list.get(row).getShort() + "&hArr;  "//"<br>"
                            + " " + this.list.get(row).getShort()
                            + " +" + NumberAsString.formatAsString(item.d)
                            + " " + Controller.getInstance().getAsset(keyAsset).getShort()
                            + "</html>";

                case COLUMN_TRADES_COUNT:

                    if (item == null) return "";
                    if (item.b > 0)
                        return item.b;
                    else
                        return "";

                case COLUMN_TRADES_VOLUME:

                    if (item == null) return "";
                    if (item.b > 0)
                        return "<html>" + NumberAsString.formatAsString(item.e)
                                //+ " " + this.list.get(row).getShort() + "&hArr; " //"<br>"
                                + " " + this.list.get(row).getShort()
                                + " +" + NumberAsString.formatAsString(item.f)
                                + " " + Controller.getInstance().getAsset(keyAsset).getShort()
                                + "</html>";
                    else
                        return "";


            }

        } catch (NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

    public synchronized void syncUpdate(Observable o, Object arg) {
    }

    public void findByKey(String text) {
        if (text.equals("") || text == null)
            return;
        if (!text.matches("[0-9]*"))
            return;
        Long key_filter = new Long(text);
        list = new ArrayList<>();
        AssetCls asset = Controller.getInstance().getAsset(key_filter);
        if (asset == null || asset.getKey() == this.key)
            return;
        list.add(asset);
        this.fireTableDataChanged();
    }

    public void set_Filter_By_Name(String str) {
        filter_Name = str;
        list = ((ItemMap)map).getByFilterAsArray(filter_Name, 0, 1000);
        this.fireTableDataChanged();

    }

    public void clear() {
        list = new ArrayList<ItemCls>();
        this.fireTableDataChanged();

    }
}
