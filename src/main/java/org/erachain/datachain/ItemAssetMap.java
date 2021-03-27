package org.erachain.datachain;

import org.erachain.core.BlockChain;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

//import database.serializer.AssetSerializer;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Актив<br>
 */
public class ItemAssetMap extends ItemMap {

    public ItemAssetMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                ItemCls.ASSET_TYPE, ObserverMessage.RESET_ASSET_TYPE, ObserverMessage.ADD_ASSET_TYPE,
                ObserverMessage.REMOVE_ASSET_TYPE, ObserverMessage.LIST_ASSET_TYPE);
    }

    public ItemAssetMap(ItemAssetMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    public boolean contains(Long key) {
        if (BlockChain.TEST_MODE && key > 100 && key < 1000) {
            return true;
        } else {
            return super.contains(key);
        }
    }

    // http://seo-mayak.com/sozdanie-bloga/wordpress-dlya-novichkov/simvoly-kotoryx-net-na-klaviature.html
    public AssetCls get(Long key) {

        AssetCls item;
        if (BlockChain.TEST_MODE && key > 100 && key < 1000) {
            byte[] appData = null;
            switch (key.intValue()) {

                case (int) AssetCls.LIA_KEY:
                    item = new AssetVenture((byte) 0, appData, GenesisBlock.CREATOR, AssetCls.LIA_NAME, null, null,
                            AssetCls.LIA_DESCR, AssetCls.AS_ACCOUNTING, 0, 0L);
                    item = null;

                    break;
                case 555:
                    item = new AssetVenture((byte) 0, appData, GenesisBlock.CREATOR, new String("¤¤¤"), null, null,
                            "Businessman", AssetCls.AS_ACCOUNTING, 8, 0L);
                    break;
                case 666:
                    item = new AssetVenture((byte) 0, appData, GenesisBlock.CREATOR, new String("♠♠♠"), null, null, // ♠♠♠
                            "bad, angry", AssetCls.AS_ACCOUNTING, 8, 0L);
                    break;
                case 777:
                    item = new AssetVenture((byte) 0, appData, GenesisBlock.CREATOR, new String("♥♥♥"), null, null,
                            "Good Shine", AssetCls.AS_ACCOUNTING, 8, 0L);
                    break;
                case 643:
                    item = new AssetVenture((byte) 0, appData, GenesisBlock.CREATOR, new String("RUB"), null, null,
                            "Accounting currency by ISO 4217 standard", AssetCls.AS_ACCOUNTING, 2, 0L);
                    break;
                case 840:
                    item = new AssetVenture((byte) 0, appData, GenesisBlock.CREATOR, new String("USD"), null, null,
                            "Accounting currency by ISO 4217 standard", AssetCls.AS_ACCOUNTING, 2, 0L);
                    break;
                case 978:
                    item = new AssetVenture((byte) 0, appData, GenesisBlock.CREATOR, new String("EUR"), null, null,
                            "Accounting currency by ISO 4217 standard", AssetCls.AS_ACCOUNTING, 2, 0L);
                    break;
                case 959:
                    item = new AssetVenture((byte) 0, appData, GenesisBlock.CREATOR, new String("XAU"), null, null,
                            "Accounting currency by ISO 4217 standard", AssetCls.AS_ACCOUNTING, 2, 0L);
                    break;
                default:
                    item = new AssetVenture((byte) 0, appData, GenesisBlock.CREATOR, "ISO." + key, null, null,
                            "Accounting currency by ISO 4217 standard", AssetCls.AS_ACCOUNTING, 2, 0L);
            }
        } else {
            if (key.equals(AssetCls.LIA_KEY)) {
                byte[] appData = null;
                item = new AssetVenture((byte) 0, appData, GenesisBlock.CREATOR, AssetCls.LIA_NAME, null, null,
                        AssetCls.LIA_DESCR, AssetCls.AS_ACCOUNTING, 0, 0L);
                item = null;
            } else {
                item = (AssetCls) super.get(key);
            }
        }

        if (item != null)
            item.setKey(key);

        return item;
    }

}
