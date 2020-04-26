package org.erachain.core.item.templates;


import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.IssueItemMap;
import org.erachain.datachain.ItemMap;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TemplateCls extends ItemCls {

    public static final long START_KEY = BlockChain.SIDE_MODE ? 1L << 14 : 1000L;

    // PERS KEY
    public static final long EMPTY_KEY = 1l;
    public static final long LICENSE_KEY = 2l;
    public static final long MARRIAGE_KEY = 3l;
    public static final long UNMARRIAGE_KEY = 4l;
    public static final long HIRING_KEY = 5l;
    public static final long UNHIRING_KEY = 6l;
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

    public int getItemType() {
        return ItemCls.TEMPLATE_TYPE;
    }

    @Override
    public long getStartKey() {
        if (BlockChain.MAIN_MODE || BlockChain.startKeys[ItemCls.TEMPLATE_TYPE] < START_KEY)
            return START_KEY;
        return BlockChain.startKeys[ItemCls.TEMPLATE_TYPE];
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
