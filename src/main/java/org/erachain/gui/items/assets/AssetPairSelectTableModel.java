package org.erachain.gui.items.assets;

// 30/03

import org.erachain.controller.Controller;
import org.erachain.controller.PairsController;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.TradePair;
import org.erachain.database.PairMapImpl;
import org.erachain.database.wallet.FavoriteItemMapAsset;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;
import org.erachain.datachain.ItemMap;
import org.erachain.dbs.IteratorCloseable;
import org.erachain.gui.models.TimerTableModelCls;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple6;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("serial")
public class AssetPairSelectTableModel extends TimerTableModelCls<Fun.Tuple2<AssetCls, TradePair>> implements Observer {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_LAST_PRICE = 2;
    public static final int COLUMN_CHANGE_PRICE = 3;
    public static final int COLUMN_BASE_VOLUME = 4;
    public static final int COLUMN_QUOTE_VOLUME = 5;
    public static final int COLUMN_TRADES_COUNT = 6;

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetPairSelectTableModel.class);

    public long key;
    public AssetCls assetPair;
    Map<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> all;

    private String filter_Name;

    public AssetPairSelectTableModel(long key) {
        super(DCSet.getInstance().getItemAssetMap(), new String[]{"Key", "Name", "Last Price", "Change % 24h", "Base Volume 24h",
                        "Quote Volume 24h", "Trades 24h"},
                new Boolean[]{false, true, false, false, false, false, false}, true);

        this.key = key;

        ItemAssetMap assetMap = DCSet.getInstance().getItemAssetMap();
        assetPair = assetMap.get(key);
        FavoriteItemMapAsset favoriteMap = Controller.getInstance().getWallet().database.getAssetFavoritesSet();
        list = new ArrayList<>();

        PairMapImpl pairsMap = Controller.getInstance().dlSet.getPairMap();

        try (IteratorCloseable<Long> iterator = favoriteMap.getIterator()) {
            while (iterator.hasNext()) {
                AssetCls asset = assetMap.get(iterator.next());
                if (asset == null) {
                    continue;
                }

                TradePair pair = PairsController.reCalcAndUpdate(asset, assetPair, pairsMap, 30);
                list.add(new Fun.Tuple2<AssetCls, TradePair>(asset, pair));
            }
        } catch (IOException e) {
        }

    }

    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || row > this.list.size() - 1) {
            return null;
        }

        AssetCls asset = this.list.get(row).a;
        TradePair tradePair = this.list.get(row).b;

        try {

            switch (column) {
                case COLUMN_KEY:
                    return asset.getKey();

                case COLUMN_NAME:
                    return asset.viewName();

                case COLUMN_LAST_PRICE:
                    return tradePair.getLastPrice();

                case COLUMN_CHANGE_PRICE:
                    return tradePair.getFirstPrice();

                case COLUMN_BASE_VOLUME:
                    return tradePair.getBase_volume();

                case COLUMN_QUOTE_VOLUME:
                    return tradePair.getQuote_volume();

                case COLUMN_TRADES_COUNT:
                    return tradePair.getCount24();

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

        list.add(new Fun.Tuple2<>(asset, PairsController.reCalcAndUpdate(asset, assetPair,
                Controller.getInstance().dlSet.getPairMap(), 30)));

        this.fireTableDataChanged();
    }

    public void set_Filter_By_Name(String str) {
        filter_Name = str;
        list = new ArrayList<>();
        PairMapImpl pairsMap = Controller.getInstance().dlSet.getPairMap();

        List<ItemCls> foundAssets = ((ItemMap) map).getByFilterAsArray(filter_Name, null, 0, 1000, descending);
        for (ItemCls asset : foundAssets) {
            list.add(new Fun.Tuple2<>((AssetCls) asset, PairsController.reCalcAndUpdate((AssetCls) asset, assetPair,
                    pairsMap, 30)));
        }

        this.fireTableDataChanged();

    }

    public void clear() {
        list = new ArrayList<>();
        this.fireTableDataChanged();

    }
}
