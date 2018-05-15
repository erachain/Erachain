package datachain;

import java.util.Map;

import org.mapdb.DB;

import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.assets.AssetVenture;
import database.serializer.ItemSerializer;
//import database.serializer.AssetSerializer;
import utils.ObserverMessage;

public class ItemAssetMap extends Item_Map 
{
	//private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	//private Atomic.Long atomicKey;
	//private long key;
	static final String NAME = "item_assets";
	static final int TYPE = ItemCls.ASSET_TYPE;
	
	public ItemAssetMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database,
				//TYPE,
				NAME,
				ObserverMessage.RESET_ASSET_TYPE,
				ObserverMessage.ADD_ASSET_TYPE,
				ObserverMessage.REMOVE_ASSET_TYPE,
				ObserverMessage.LIST_ASSET_TYPE
				);
	}

	public ItemAssetMap(ItemAssetMap parent) 
	{
		super(parent);
	}

	// type+name not initialized yet! - it call as Super in New
	protected Map<Long, ItemCls> getMap(DB database) 
	{
		
		//OPEN MAP
		return database.createTreeMap(NAME)
				.valueSerializer(new ItemSerializer(TYPE))
				//.valueSerializer(new AssetSerializer())
				// key instead size - .counterEnable()
				.makeOrGet();
	}

	public boolean contains(Long key) {
		if (key > 100 && key < 1000) {
			return true;
		} else {
			return super.contains(key);
		}
	}

	public AssetCls get(Long key) {
		
		if (key > 100 && key < 1000) {
			AssetCls item;
			switch (key.intValue()) {
				case 643:
					item = new AssetVenture((byte)0, core.block.GenesisBlock.CREATOR, new String("RUB"),
							null, null, "Accounting currency by ISO 4217 standard", 3, 2, 0l);
					break;
				default:
					item = new AssetVenture((byte)0, core.block.GenesisBlock.CREATOR, "ISO." + key,
							null, null, "Accounting currency by ISO 4217 standard", 3, 2, 0l);
			}
			item.setKey(key);
			return item;
		} else {
			return (AssetCls)super.get(key);			
		}
	}

}
