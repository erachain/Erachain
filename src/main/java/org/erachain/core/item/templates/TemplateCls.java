package org.erachain.core.item.templates;


import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemMap;
import org.erachain.lang.Lang;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TemplateCls extends ItemCls {

    public static final int TYPE_KEY = ItemCls.TEMPLATE_TYPE;

    // PERS KEY
    public static final long EMPTY_KEY = 1L;
    public static final long LICENSE_KEY = 2L;
    public static final long MARRIAGE_KEY = 3L;
    public static final long UNMARRIAGE_KEY = 4L;
    public static final long HIRING_KEY = 5L;
    public static final long UNHIRING_KEY = 6L;
    public static final int INITIAL_FAVORITES = 10;
    protected static final int PLATE = 1;
    protected static final int SAMPLE = 2;
    protected static final int PAPER = 3;
    private static Pattern varsPattern = Pattern.compile(Pattern.quote("{{") + "(.+?)" + Pattern.quote("}}"));
    private List<String> variables;

    public TemplateCls(byte[] typeBytes, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description) {
        super(typeBytes, appData, maker, name, icon, image, description);

    }

    public TemplateCls(int type, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description) {
        this(new byte[TYPE_LENGTH], appData, maker, name, icon, image, description);
        this.typeBytes[0] = (byte) type;
    }

    //GETTERS/SETTERS

    @Override
    public int getItemType() {
        return TYPE_KEY;
    }

    @Override
    public long START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UP_ITEMS;

        return START_KEY_OLD;
    }

    @Override
    public long MIN_START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UP_ITEMS;

        return MIN_START_KEY_OLD;
    }

    public String getItemTypeName() {
        return "template";
    }

    public List<String> getVarNames() {
        if (variables != null) {
            return variables;
        }

        variables = new ArrayList<>();
        Matcher matcher = varsPattern.matcher(description);
        while (matcher.find()) {
            String varName = matcher.group(1);
            variables.add(varName);
        }
        return variables;
    }

    @Override
    public String viewName() {
        if (this.key > 2 && this.key < 100) {
            return "Example. Reserved";
        }

        return this.name;
    }

    @Override
    public String viewDescription() {
        if (this.key > 2 && this.key < 100) {
            return "<b>EXAMPLE</b><br>USE {{param.1}} etc. for set values in parameters";
        }

        return this.description;
    }

    // DB
    public ItemMap getDBMap(DCSet db) {
        return db.getItemTemplateMap();
    }

    public JSONObject jsonForExplorerInfo(DCSet dcSet, JSONObject langObj, boolean forPrint) {

        JSONObject itemJson = super.jsonForExplorerInfo(dcSet, langObj, forPrint);
        itemJson.put("Label_Template", Lang.T("Template", langObj));

        if (!forPrint) {
        }

        return itemJson;
    }

}
