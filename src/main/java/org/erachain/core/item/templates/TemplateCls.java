package org.erachain.core.item.templates;


import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.IssueItemMap;
import org.erachain.datachain.ItemMap;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TemplateCls extends ItemCls {

    public static final int TYPE_KEY = ItemCls.TEMPLATE_TYPE;

    public static final long MIN_START_KEY = 1000L;

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

    public TemplateCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description) {
        super(typeBytes, owner, name, icon, image, description);

    }

    public TemplateCls(int type, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description) {
        this(new byte[TYPE_LENGTH], owner, name, icon, image, description);
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
            return BlockChain.START_KEY_UO_ITEMS;

        return START_KEY;
    }

    @Override
    public long MIN_START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UO_ITEMS;

        return MIN_START_KEY;
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

    public IssueItemMap getDBIssueMap(DCSet db) {
        return db.getIssueTemplateMap();
    }

}
