package org.erachain.core.blockexplorer;

import org.apache.commons.net.util.Base64;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.item.assets.Trade;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.datachain.*;
import org.erachain.lang.Lang;
import org.erachain.utils.NumberAsString;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConverterListInMap<T> {
    private static final Logger logger = LoggerFactory.getLogger(ConverterListInMap.class);

    public static Map mapJSON(Class type, List keys, JSONObject langObj) {
        //Результирующий сортированный в порядке добавления словарь(map)
        Map result = new LinkedHashMap();
        DCSet dcSet = DCSet.getInstance();

        if (type == PersonCls.class) {
            ItemPersonMap sourceMap = dcSet.getItemPersonMap();
            for (int i = 0; i < keys.size(); i++) {
                Map personJSON = new LinkedHashMap();
                PersonCls person = (PersonCls) sourceMap.get((Long)keys.get(i));
                personJSON.put("key", person.getKey());
                personJSON.put("name", person.getName());
                personJSON.put("creator", person.getOwner().getAddress());
                personJSON.put("img", Base64.encodeBase64String(person.getImage()));
                personJSON.put("ico", Base64.encodeBase64String(person.getIcon()));
                result.put(i, personJSON);
            }
        } else if (type == AssetCls.class) {
            ItemAssetMap sourceMap = dcSet.getItemAssetMap();
            for (int i = 0; i < keys.size(); i++) {
                Map assetJSON = new LinkedHashMap();
                AssetCls asset = (AssetCls) sourceMap.get((Long)keys.get(i));
                assetJSON.put("key", asset.getKey());
                assetJSON.put("name", asset.getName());
                assetJSON.put("description", Lang.getInstance().translateFromLangObj(asset.viewDescription(), langObj));
                assetJSON.put("owner", asset.getOwner().getAddress());
                assetJSON.put("quantity", NumberAsString.formatAsString(asset.getTotalQuantity(dcSet)));
                assetJSON.put("scale", asset.getScale());
                assetJSON.put("assetType", Lang.getInstance().translateFromLangObj(asset.viewAssetType(), langObj));
                assetJSON.put("img", Base64.encodeBase64String(asset.getImage()));
                assetJSON.put("icon", Base64.encodeBase64String(asset.getIcon()));
                List<Order> orders = dcSet
                        .getOrderMap().getOrders(asset.getKey());
                List<Trade> trades = dcSet.getTradeMap()
                        .getTrades(asset.getKey());
                assetJSON.put("operations", orders.size() + trades.size());
                result.put(i, assetJSON);
            }
        } else if (type == Block.class) {
            BlockMap sourceMap = dcSet.getBlockMap();
            for (int i = 0; i < keys.size(); i++) {
                Map mapJSON = new LinkedHashMap();
                Block item = sourceMap.get((Integer)keys.get(i));
                mapJSON.put("height", item.getHeight());
                mapJSON.put("signature", Base58.encode(item.getSignature()));
                mapJSON.put("generator", item.getCreator().getAddress());
                mapJSON.put("generatingBalance", item.getForgingValue());
                mapJSON.put("target", item.getTarget());
                mapJSON.put("winValue", item.getWinValue());
                mapJSON.put("winValueTargeted", item.calcWinValueTargeted() - 100000);
                mapJSON.put("transactionsCount", item.getTransactionCount());
                mapJSON.put("timestamp", item.getTimestamp());
                mapJSON.put("dateTime", BlockExplorer.timestampToStr(item.getTimestamp()));
                item.loadHeadMind(dcSet);
                mapJSON.put("totalFee", item.viewFeeAsBigDecimal());
                Block.BlockHead blockHead = item.blockHead;
                Fun.Tuple2<Integer, Integer> forgingPoint = blockHead.creator.getForgingData(DCSet.getInstance(), item.heightBlock);
                mapJSON.put("deltaHeight", blockHead.heightBlock - forgingPoint.a);
                result.put(i, mapJSON);
            }
        } else if (type == StatusCls.class) {
            ItemStatusMap sourceMap = dcSet.getItemStatusMap();
            for (int i = 0; i < keys.size(); i++) {
                Map mapJSON = new LinkedHashMap();
                StatusCls item = (StatusCls) sourceMap.get((Long)keys.get(i));
                mapJSON.put("key", item.getKey());
                mapJSON.put("name", item.getName());
                mapJSON.put("description", item.getDescription());
                mapJSON.put("owner", item.getOwner().getAddress());
                result.put(i, mapJSON);
            }
        } else if (type == TemplateCls.class) {
            ItemTemplateMap sourceMap = dcSet.getItemTemplateMap();
            for (int i = 0; i < keys.size(); i++) {
                Map mapJSON = new LinkedHashMap();
                TemplateCls item = (TemplateCls) sourceMap.get((Long)keys.get(i));
                mapJSON.put("key", item.getKey());
                mapJSON.put("name", item.getName());
                mapJSON.put("description", item.getDescription());
                mapJSON.put("owner", item.getOwner().getAddress());
                result.put(i, mapJSON);
            }
        }

        return result;
    }

    /**
     * Перегоняет информацию из списка блоков в словарь
     *
     * @param blocks список блоков
     * @return словарь с добавленной информацией из списка блоков
     * с ключами соответствующими номерам элементов в исходном списке
     */
    public static Map blocksJSON(List<Block> blocks, DCSet dcSet) {
        //Результирующий сортированный в порядке добавления словарь(map)
        Map result = new LinkedHashMap();
        for (int i = 0; i < blocks.size(); i++) {
            Map blockJSON = new LinkedHashMap();
            Block block = blocks.get(i);
            blockJSON.put("height", block.getHeight());
            blockJSON.put("signature", Base58.encode(block.getSignature()));
            blockJSON.put("generator", block.getCreator().getAddress());
            blockJSON.put("generatingBalance", block.getForgingValue());
            blockJSON.put("target", block.getTarget());
            blockJSON.put("winValue", block.getWinValue());
            blockJSON.put("winValueTargeted", block.calcWinValueTargeted() - 100000);
            blockJSON.put("transactionsCount", block.getTransactionCount());
            blockJSON.put("timestamp", block.getTimestamp());
            blockJSON.put("dateTime", BlockExplorer.timestampToStr(block.getTimestamp()));
            block.loadHeadMind(dcSet);
            blockJSON.put("totalFee", block.viewFeeAsBigDecimal());
            Block.BlockHead blockHead = block.blockHead;
            Fun.Tuple2<Integer, Integer> forgingPoint = blockHead.creator.getForgingData(DCSet.getInstance(), block.heightBlock);
            blockJSON.put("deltaHeight", blockHead.heightBlock - forgingPoint.a);
            result.put(i, blockJSON);
        }
        return result;
    }

    /**
     * Перегоняет информацию из списка песрон в словарь
     *
     * @param persons список персон
     * @return словарь с добавленной информацией из списка персон
     * с ключами соответствующими номерам элементов в исходном списке
     */
    public static Map personsJSON(List<ItemCls> persons) {
        //Результирующий сортированный в порядке добавления словарь(map)
        Map result = new LinkedHashMap();
        for (int i = 0; i < persons.size(); i++) {
            Map personJSON = new LinkedHashMap();
            PersonCls person = (PersonCls)persons.get(i);
            personJSON.put("key", person.getKey());
            personJSON.put("name", person.getName());
            personJSON.put("creator", person.getOwner().getAddress());
            personJSON.put("img", Base64.encodeBase64String(person.getImage()));
            personJSON.put("ico", Base64.encodeBase64String(person.getIcon()));
            result.put(i, personJSON);
        }
        return result;
    }

    public static Map personsJSON(DCMap<Long, ItemCls> map, List<Long> keys) {
        //Результирующий сортированный в порядке добавления словарь(map)
        Map result = new LinkedHashMap();
        for (int i = 0; i < keys.size(); i++) {
            Map personJSON = new LinkedHashMap();
            PersonCls person = (PersonCls)map.get(keys.get(i));
            personJSON.put("key", person.getKey());
            personJSON.put("name", person.getName());
            personJSON.put("creator", person.getOwner().getAddress());
            personJSON.put("img", Base64.encodeBase64String(person.getImage()));
            personJSON.put("ico", Base64.encodeBase64String(person.getIcon()));
            result.put(i, personJSON);
        }
        return result;
    }

    /**
     * Перегоняет информацию из списка активов в словарь
     *
     * @param assets список активов
     * @return словарь с добавленной информацией из списка активов
     * с ключами соответствующими номерам элементов в исходном списке
     */
    public static Map assetsJSON(List<ItemCls> assets, DCSet dcSet, JSONObject langObj) {
        //Результирующий сортированный в порядке добавления словарь(map)
        Map result = new LinkedHashMap();
        for (int i = 0; i < assets.size(); i++) {
            Map assetJSON = new LinkedHashMap();
            AssetCls asset = (AssetCls) assets.get(i);
            assetJSON.put("key", asset.getKey());
            assetJSON.put("name", asset.getName());
            assetJSON.put("description", Lang.getInstance().translateFromLangObj(asset.viewDescription(), langObj));
            assetJSON.put("owner", asset.getOwner().getAddress());
            assetJSON.put("quantity", NumberAsString.formatAsString(asset.getTotalQuantity(dcSet)));
            assetJSON.put("scale", asset.getScale());
            assetJSON.put("assetType", Lang.getInstance().translateFromLangObj(asset.viewAssetType(), langObj));
            assetJSON.put("img", Base64.encodeBase64String(asset.getImage()));
            assetJSON.put("icon", Base64.encodeBase64String(asset.getIcon()));
            List<Order> orders = dcSet
                    .getOrderMap().getOrders(asset.getKey());
            List<Trade> trades = dcSet.getTradeMap()
                    .getTrades(asset.getKey());
            assetJSON.put("operations", orders.size() + trades.size());
            result.put(i, assetJSON);
        }
        return result;
    }

    /**
     * Перегоняет информацию из списка шаблонов или статусов в словарь
     *
     * @param type TemplateCls.class - шаблон, StatusCls.class - статус соответственно
     * @param list список шаблонов или статусов
     * @param <T>  тип элементов - {@link TemplateCls} или {@link StatusCls}
     * @return словарь с добавленной информацией из списка активов
     * с ключами соответствующими номерам элементов в исходном списке
     * @throws Exception передан неподдерживаемый type
     */
    public static <T> Map statusTemplateJSON(Class<T> type, List<T> list) throws Exception {
        //Результирующий сортированный в порядке добавления словарь(map)
        Map result = new LinkedHashMap();
        for (int i = 0; i < list.size(); i++) {
            Map map = new LinkedHashMap();
            ItemCls element;
            if (type == TemplateCls.class) {
                element = (TemplateCls) list.get(i);
            } else if (type == StatusCls.class) {
                element = (StatusCls) list.get(i);
            } else {
                logger.error("incorrect type of element list while converting in JSON Map");
                throw new Exception();
            }
            map.put("key", element.getKey());
            map.put("name", element.getName());
            map.put("description", element.getDescription());
            map.put("owner", element.getOwner().getAddress());
            result.put(i, map);
        }
        return result;
    }


}
