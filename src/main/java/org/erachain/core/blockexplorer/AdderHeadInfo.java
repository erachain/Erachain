package org.erachain.core.blockexplorer;

import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;

import java.util.Map;

public class AdderHeadInfo {

    /**
     * Добавляет переведенные на соответствующий язык информацию(бирки) для блоков
     *
     * @param output словарь, в который добавляется информация
     */
    public static void addHeadInfoCapBlocks(Map output, DCSet dcSet, JSONObject langObj) {
        output.put("unconfirmedTxs", dcSet.getTransactionMap().size());
        output.put("totaltransactions", dcSet.getTransactionFinalMap().size());
        output.put("Label_Unconfirmed_transactions",
                Lang.getInstance().translateFromLangObj("Unconfirmed transactions", langObj));
        output.put("Label_total_transactions",
                Lang.getInstance().translateFromLangObj("Total Transactions", langObj));
        output.put("Label_Height", Lang.getInstance().translateFromLangObj("Height", langObj));
        output.put("Label_Time", Lang.getInstance().translateFromLangObj("Time", langObj));
        output.put("Label_Generator", Lang.getInstance().translateFromLangObj("Creator", langObj));
        output.put("Label_Gen_balance", Lang.getInstance().translateFromLangObj("Gen.Balance", langObj));
        output.put("Label_TXs", Lang.getInstance().translateFromLangObj("TXs", langObj));
        output.put("Label_Fee", Lang.getInstance().translateFromLangObj("Fee", langObj));
        output.put("Label_Target", Lang.getInstance().translateFromLangObj("Target", langObj));
        addLaterPrevious(output, langObj);
        output.put("Label_Blocks", Lang.getInstance().translateFromLangObj("Blocks", langObj));
    }

    /**
     * Добавляет переведенные на соответствующий язык информацию(бирки) для личностей(персон)
     *
     * @param output словарь, в который добавляется информация
     */
    public static void addHeadInfoCapPersons(Map output, DCSet dcSet, JSONObject langObj) {
        output.put("unconfirmedTxs", dcSet.getTransactionMap().size());
        output.put("Label_Unconfirmed_transactions",
                Lang.getInstance().translateFromLangObj("Unconfirmed transactions", langObj));
        output.put("Label_key", Lang.getInstance().translateFromLangObj("Key", langObj));
        output.put("Label_name", Lang.getInstance().translateFromLangObj("Name", langObj));
        output.put("Label_creator", Lang.getInstance().translateFromLangObj("Creator", langObj));
        output.put("Label_Persons", Lang.getInstance().translateFromLangObj("Persons", langObj));
        addLaterPrevious(output, langObj);
    }

    /**
     * Добавляет переведенные на соответствующий язык информацию(бирки) для актовов
     *
     * @param output словарь, в который добавляется информация
     */
    public static void addHeadInfoCapAssets(Map output, JSONObject langObj) {
        output.put("label_Title", Lang.getInstance().translateFromLangObj("Assets", langObj));
        output.put("label_table_key", Lang.getInstance().translateFromLangObj("Key", langObj));
        output.put("label_table_asset_name", Lang.getInstance().translateFromLangObj("Name", langObj));
        output.put("label_table_asset_creator", Lang.getInstance().translateFromLangObj("Creator", langObj));
        output.put("label_table_asset_movable", Lang.getInstance().translateFromLangObj("Movable", langObj));
        output.put("label_table_asset_description", Lang.getInstance().translateFromLangObj("Description", langObj));
        output.put("label_table_asset_divisible", Lang.getInstance().translateFromLangObj("Divisible", langObj));
        output.put("label_table_asset_amount", Lang.getInstance().translateFromLangObj("Amount", langObj));
        output.put("label_Assets", Lang.getInstance().translateFromLangObj("Assets", langObj));
        output.put("label_operations", Lang.getInstance().translateFromLangObj("Operations", langObj));
        addLaterPrevious(output, langObj);
    }

    /**
     * Добавляет переведенные на соответствующий язык информацию(бирки) для или статусов или шаблонов
     *
     * @param output словарь, в который добавляется информация
     */
    public static void addHeadInfoCapStatusesTemplates(Map output, JSONObject langObj) {
        output.put("label_table_key", Lang.getInstance().translateFromLangObj("Key", langObj));
        output.put("label_table_name", Lang.getInstance().translateFromLangObj("Name", langObj));
        output.put("label_table_creator", Lang.getInstance().translateFromLangObj("Creator", langObj));
        output.put("label_table_description", Lang.getInstance().translateFromLangObj("Description", langObj));
        output.put("Label_Statuses", Lang.getInstance().translateFromLangObj("Statuses", langObj));
        output.put("Label_Templates", Lang.getInstance().translateFromLangObj("Templates", langObj));
        addLaterPrevious(output, langObj);
    }

    /**
     * Добавляет переведенные на соответствующий язык информацию(бирки) о предыдущем и следующем элементе
     *
     * @param output словарь, в который добавляется информация
     */
    private static void addLaterPrevious(Map output, JSONObject langObj) {
        output.put("Label_Later", Lang.getInstance().translateFromLangObj("Later", langObj));
        output.put("Label_Previous", Lang.getInstance().translateFromLangObj("Previous", langObj));
    }


}
