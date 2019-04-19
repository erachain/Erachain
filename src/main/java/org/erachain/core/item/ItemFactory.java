package org.erachain.core.item;

import org.erachain.core.item.assets.AssetFactory;
import org.erachain.core.item.imprints.Imprint;
import org.erachain.core.item.persons.PersonFactory;
import org.erachain.core.item.polls.PollFactory;
import org.erachain.core.item.statuses.StatusFactory;
import org.erachain.core.item.templates.TemplateFactory;
import org.erachain.core.item.unions.UnionFactory;

public class ItemFactory {

    private static ItemFactory instance;

    private ItemFactory() {

    }

    public static ItemFactory getInstance() {
        if (instance == null) {
            instance = new ItemFactory();
        }

        return instance;
    }

    public ItemCls parse(int type, byte[] data, boolean includeReference) throws Exception {

        switch (type) {
            case ItemCls.ASSET_TYPE:
                return AssetFactory.getInstance().parse(data, includeReference);
            case ItemCls.IMPRINT_TYPE:
                return Imprint.parse(data, includeReference);
            case ItemCls.TEMPLATE_TYPE:
                return TemplateFactory.getInstance().parse(data, includeReference);
            case ItemCls.PERSON_TYPE:
                return PersonFactory.getInstance().parse(data, includeReference);
            case ItemCls.POLL_TYPE:
                return PollFactory.getInstance().parse(data, includeReference);
            case ItemCls.STATUS_TYPE:
                return StatusFactory.getInstance().parse(data, includeReference);
            case ItemCls.UNION_TYPE:
                return UnionFactory.getInstance().parse(data, includeReference);
        }

        throw new Exception("Invalid ITEM type: " + type);
    }

}
