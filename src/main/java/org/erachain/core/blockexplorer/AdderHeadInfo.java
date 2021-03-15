package org.erachain.core.blockexplorer;

import org.erachain.core.block.Block;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.imprints.ImprintCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.item.polls.PollCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Map;

public class AdderHeadInfo {

    public static void addHeadInfoCap(Class type, Map output, DCSet dcSet, JSONObject langObj) {
        if(type == Transaction.class) {
            addHeadInfoCapBlocks(output, dcSet, langObj);

        } else if(type == Block.class) {
            addHeadInfoCapBlocks(output, dcSet, langObj);

        } else if(type == Block.BlockHead.class) {
            addHeadInfoCapBlocks(output, dcSet, langObj);

        } else if(type == AssetCls.class) {
            addHeadInfoCapAssets(output, langObj);

        } else if (type == PersonCls.class) {
            addHeadInfoCapPersons(output, dcSet, langObj);

        } else if (type == StatusCls.class || type == TemplateCls.class
                || type == ImprintCls.class || type == UnionCls.class) {
            addHeadInfoCapStatusesTemplates(output, langObj);
        } else if (type == PollCls.class
                ) {
            addHeadInfoCapStatusesTemplates(output, langObj);
            output.put("Label_table_total_votes", Lang.T("Total Vote", langObj));
            output.put("Label_table_options_count", Lang.T("Options Count", langObj));

        }

    }

        /**
         * Добавляет переведенные на соответствующий язык информацию(бирки) для блоков
         *
         * @param output словарь, в который добавляется информация
         */
    public static void addHeadInfoCapBlocks(Map output, DCSet dcSet, JSONObject langObj) {
        output.put("search_placeholder", Lang.T("Insert block number or signature", langObj));

        output.put("unconfirmedTxs", dcSet.getTransactionTab().size());
        output.put("totaltransactions", dcSet.getTransactionFinalMap().size());
        output.put("Label_Unconfirmed_transactions",
                Lang.T("Unconfirmed transactions", langObj));
        output.put("Label_total_transactions",
                Lang.T("Total Transactions", langObj));
        output.put("Label_Height", Lang.T("Height", langObj));
        output.put("Label_Time", Lang.T("Timestamp creation block", langObj));
        output.put("Label_Generator", Lang.T("Creator account", langObj));
        output.put("Label_TXs", Lang.T("TXs", langObj));
        output.put("Label_Fee", Lang.T("Fee", langObj));
        output.put("Label_Target", Lang.T("Target", langObj));
        addLaterPrevious(output, langObj);
        output.put("Label_Blocks", Lang.T("Blocks", langObj));
        output.put("Label_Gen_balance", Lang.T("Gen.Balance", langObj));
        output.put("Label_Delta_Height", Lang.T("Gen.Period", langObj));
        output.put("Label_WV", Lang.T("Win", langObj));
        output.put("Label_dtWV", Lang.T("Delta", langObj));
    }

    /**
     * Добавляет переведенные на соответствующий язык информацию(бирки) для личностей(персон)
     *
     * @param output словарь, в который добавляется информация
     */
    public static void addHeadInfoCapPersons(Map output, DCSet dcSet, JSONObject langObj) {
        output.put("search_placeholder", Lang.T("Type searching words or person key", langObj));

        output.put("unconfirmedTxs", dcSet.getTransactionTab().size());
        output.put("Label_Unconfirmed_transactions",
                Lang.T("Unconfirmed transactions", langObj));
        output.put("Label_key", Lang.T("Key", langObj));
        output.put("Label_name", Lang.T("Name", langObj));
        output.put("Label_creator", Lang.T("Creator", langObj));
        output.put("Label_image", Lang.T("Image", langObj));
        //output.put("Label_description", Lang.T("Description", langObj));
        output.put("Label_age", Lang.T("Age", langObj));
        output.put("Label_Persons", Lang.T("Persons", langObj));
        addLaterPrevious(output, langObj);
    }

    /**
     * Добавляет переведенные на соответствующий язык информацию(бирки) для актовов
     *
     * @param output словарь, в который добавляется информация
     */
    public static void addHeadInfoCapAssets(Map output, JSONObject langObj) {
        output.put("search_placeholder", Lang.T("Type searching words or asset key", langObj));

        output.put("Label_Title", Lang.T("Assets", langObj));
        output.put("Label_table_asset_key", Lang.T("Key", langObj));
        output.put("Label_table_asset_name", Lang.T("Name", langObj));
        output.put("Label_table_asset_maker", Lang.T("Maker", langObj));
        output.put("Label_table_asset_type", Lang.T("Type", langObj));
        output.put("Label_table_asset_description", Lang.T("Description", langObj));
        output.put("Label_table_asset_scale", Lang.T("Scale", langObj));
        output.put("Label_table_asset_amount", Lang.T("Amount", langObj));
        output.put("Label_Assets", Lang.T("Assets", langObj));
        output.put("Label_table_asset_orders", Lang.T("Orders", langObj));
        output.put("Label_table_asset_quantity", Lang.T("Quantity", langObj));
        output.put("Label_table_asset_released", Lang.T("Released", langObj));
        output.put("Label_table_asset_marketCap", Lang.T("Market Cap", langObj));
        output.put("Label_table_asset_lastPrice", Lang.T("Price", langObj));
        output.put("Label_table_asset_changePrice", Lang.T("24h", langObj));
        output.put("Label_table_asset_changePrice", Lang.T("24h", langObj));

        JSONArray assetTypesAbbrevs = new JSONArray();
        for (int type : AssetCls.assetTypes()) {
            assetTypesAbbrevs.add(AssetCls.viewAssetTypeAbbrev(type));
        }
        output.put("types_abbrevs", assetTypesAbbrevs);


        addLaterPrevious(output, langObj);
    }

    /**
     * Добавляет переведенные на соответствующий язык информацию(бирки) для или статусов или шаблонов
     *
     * @param output словарь, в который добавляется информация
     */
    public static void addHeadInfoCapStatusesTemplates(Map output, JSONObject langObj) {
        output.put("search_placeholder", Lang.T("Type searching words or item key", langObj));

        output.put("Label_table_key", Lang.T("Key", langObj));
        output.put("Label_table_name", Lang.T("Name", langObj));
        output.put("Label_table_creator", Lang.T("Creator", langObj));
        output.put("Label_table_description", Lang.T("Description", langObj));
        output.put("Label_Statuses", Lang.T("Statuses", langObj));
        output.put("Label_Templates", Lang.T("Templates", langObj));
        addLaterPrevious(output, langObj);
    }

    /**
     * Добавляет переведенные на соответствующий язык информацию(бирки) о предыдущем и следующем элементе
     *
     * @param output словарь, в который добавляется информация
     */
    private static void addLaterPrevious(Map output, JSONObject langObj) {
        output.put("Label_Later", Lang.T("Later", langObj));
        output.put("Label_Previous", Lang.T("Previous", langObj));
    }


}
