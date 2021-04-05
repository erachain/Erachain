package org.erachain.core.item.unions;

public class UnionFactory {

    private static UnionFactory instance;

    private UnionFactory() {

    }

    public static UnionFactory getInstance() {
        if (instance == null) {
            instance = new UnionFactory();
        }

        return instance;
    }

    public UnionCls parse(int forDeal, byte[] data, boolean includeReference) throws Exception {
        //READ TYPE
        int type = data[0];

        switch (type) {
            case UnionCls.UNION:

                //PARSE SIMPLE PLATE
                return Union.parse(forDeal, data, includeReference);

        }

        throw new Exception("Invalid Union type: " + type);
    }

}
