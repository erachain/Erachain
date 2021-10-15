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

    public ItemCls parse(int forDeal, int type, byte[] data, boolean includeReference) throws Exception {

        switch (type) {
            case ItemCls.ASSET_TYPE:
                return AssetFactory.getInstance().parse(forDeal, data, includeReference);
            case ItemCls.IMPRINT_TYPE:
                return Imprint.parse(forDeal, data, includeReference);
            case ItemCls.TEMPLATE_TYPE:
                return TemplateFactory.getInstance().parse(forDeal, data, includeReference);
            case ItemCls.PERSON_TYPE:
            case ItemCls.AUTHOR_TYPE:
                return PersonFactory.getInstance().parse(forDeal, data, includeReference);
            case ItemCls.POLL_TYPE:
                return PollFactory.getInstance().parse(forDeal, data, includeReference);
            case ItemCls.STATUS_TYPE:
                return StatusFactory.getInstance().parse(forDeal, data, includeReference);
            case ItemCls.UNION_TYPE:
                return UnionFactory.getInstance().parse(forDeal, data, includeReference);
        }

        throw new Exception("Invalid ITEM type: " + type);
    }

}
