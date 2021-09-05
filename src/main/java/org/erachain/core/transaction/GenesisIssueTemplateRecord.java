package org.erachain.core.transaction;

import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.item.templates.TemplateFactory;

import java.util.Arrays;

public class GenesisIssueTemplateRecord extends GenesisIssueItemRecord {

    private static final byte TYPE_ID = (byte) GENESIS_ISSUE_TEMPLATE_TRANSACTION;
    private static final String NAME_ID = "GENESIS Issue Template";

    public GenesisIssueTemplateRecord(TemplateCls template) {
        super(TYPE_ID, NAME_ID, template);
    }

    //PARSE CONVERT
    public static Transaction Parse(byte[] data) throws Exception {
        //CHECK IF WE MATCH BLOCK LENGTH
        if (data.length < BASE_LENGTH) {
            throw new Exception("Data does not match block length");
        }

        // READ TYPE
        int position = SIMPLE_TYPE_LENGTH;

        //READ PLATE
        // read without reference
        TemplateCls template = TemplateFactory.getInstance().parse(Transaction.FOR_NETWORK, Arrays.copyOfRange(data, position, data.length), false);

        return new GenesisIssueTemplateRecord(template);
    }

}
