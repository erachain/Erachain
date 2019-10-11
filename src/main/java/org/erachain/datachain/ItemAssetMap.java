package org.erachain.datachain;

import org.erachain.core.BlockChain;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.database.serializer.ItemSerializer;
import org.erachain.utils.ObserverMessage;
import org.mapdb.DB;

//import database.serializer.AssetSerializer;

/**
 * Хранение активов.<br>
 * Ключ: номер (автоинкремент)<br>
 * Значение: Актив<br>
 */
public class ItemAssetMap extends ItemMap {

    // private Atomic.Long atomicKey;
    // private long key;
    static final String NAME = "item_assets";
    static final int TYPE = ItemCls.ASSET_TYPE;

    public ItemAssetMap(DCSet databaseSet, DB database) {
        super(databaseSet, database,
                // TYPE,
                NAME, ObserverMessage.RESET_ASSET_TYPE, ObserverMessage.ADD_ASSET_TYPE,
                ObserverMessage.REMOVE_ASSET_TYPE, ObserverMessage.LIST_ASSET_TYPE);
    }

    public ItemAssetMap(ItemAssetMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    // type+name not initialized yet! - it call as Super in New
    protected void openMap() {

        // OPEN MAP
        map = database.createTreeMap(NAME).valueSerializer(new ItemSerializer(TYPE))
                // .valueSerializer(new AssetSerializer())
                // key instead size - .counterEnable()
                .makeOrGet();
    }

    public boolean contains(Long key) {
        if (BlockChain.DEVELOP_USE && key > 100 && key < 1000) {
            return true;
        } else {
            return super.contains(key);
        }
    }

    // http://seo-mayak.com/sozdanie-bloga/wordpress-dlya-novichkov/simvoly-kotoryx-net-na-klaviature.html
    public AssetCls get(Long key) {

        AssetCls item;
        if (BlockChain.DEVELOP_USE && key > 100 && key < 1000) {
            switch (key.intValue()) {

                case (int)AssetCls.LIA_KEY:
                    item = new AssetVenture((byte) 0, GenesisBlock.CREATOR, AssetCls.LIA_NAME, null, null,
                            AssetCls.LIA_DESCR, AssetCls.AS_ACCOUNTING, 0, 0l);
                    item = null;

                    break;
                case 555:
                    item = new AssetVenture((byte) 0, GenesisBlock.CREATOR, new String("¤¤¤"), null, null,
                            "Businessman", AssetCls.AS_ACCOUNTING, 8, 0l);
                    break;
                case 666:
                    item = new AssetVenture((byte) 0, GenesisBlock.CREATOR, new String("♠♠♠"), null, null, // ♠♠♠
                            "bad, angry", AssetCls.AS_ACCOUNTING, 8, 0l);
                    break;
                case 777:
                    item = new AssetVenture((byte) 0, GenesisBlock.CREATOR, new String("♥♥♥"), null, null,
                            "Good Shine", AssetCls.AS_ACCOUNTING, 8, 0l);
                    break;
                case 643:
                    item = new AssetVenture((byte) 0, GenesisBlock.CREATOR, new String("RUB"), null, null,
                            "Accounting currency by ISO 4217 standard", AssetCls.AS_ACCOUNTING, 2, 0l);
                    break;
                case 840:
                    item = new AssetVenture((byte) 0, GenesisBlock.CREATOR, new String("USD"), null, null,
                            "Accounting currency by ISO 4217 standard", AssetCls.AS_ACCOUNTING, 2, 0l);
                    break;
                case 978:
                    item = new AssetVenture((byte) 0, GenesisBlock.CREATOR, new String("EUR"), null, null,
                            "Accounting currency by ISO 4217 standard", AssetCls.AS_ACCOUNTING, 2, 0l);
                    break;
                case 959:
                    item = new AssetVenture((byte) 0, GenesisBlock.CREATOR, new String("XAU"), null, null,
                            "Accounting currency by ISO 4217 standard", AssetCls.AS_ACCOUNTING, 2, 0l);
                    break;
                default:
                    item = new AssetVenture((byte) 0, GenesisBlock.CREATOR, "ISO." + key, null, null,
                            "Accounting currency by ISO 4217 standard", AssetCls.AS_ACCOUNTING, 2, 0l);
            }
        } else {
            if (key.equals(AssetCls.LIA_KEY)) {
                item = new AssetVenture((byte) 0, GenesisBlock.CREATOR, AssetCls.LIA_NAME, null, null,
                        AssetCls.LIA_DESCR, AssetCls.AS_ACCOUNTING, 0, 0l);
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
