package org.erachain.core.item.assets;

public class AssetFactory {

    private static AssetFactory instance;

    private AssetFactory() {

    }

    public static AssetFactory getInstance() {
        if (instance == null) {
            instance = new AssetFactory();
        }

        return instance;
    }

    public AssetCls parse(int forDeal, byte[] data, boolean includeReference) throws Exception {
        //READ TYPE
        int type = data[0];

        switch (type) {
            case AssetCls.VENTURE:
                return AssetVenture.parse(forDeal, data, includeReference);

            case AssetCls.UNIQUE:
                return AssetUnique.parse(forDeal, data, includeReference);

            //case AssetCls.UNIQUE_SERIES:
            //    return AssetUniqueSeries.parse(forDeal, data, includeReference);

            case AssetCls.UNIQUE_COPY:
                return AssetUniqueSeriesCopy.parse(forDeal, data, includeReference);

            case AssetCls.NAME:
                //return RegisterNameTransaction.Parse(data, includeReference);

        }

        throw new Exception("Invalid asset type: " + type);
    }

}
