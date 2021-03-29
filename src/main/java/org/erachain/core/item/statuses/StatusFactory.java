package org.erachain.core.item.statuses;

public class StatusFactory {

    private static StatusFactory instance;

    private StatusFactory() {

    }

    public static StatusFactory getInstance() {
        if (instance == null) {
            instance = new StatusFactory();
        }

        return instance;
    }

    public StatusCls parse(int forDeal, byte[] data, boolean includeReference) throws Exception {
        //READ TYPE
        int type = data[0];

        switch (type) {
            case StatusCls.STATUS:

                //PARSE SIMPLE STATUS
                return Status.parse(forDeal, data, includeReference);

            case StatusCls.TITLE:

                //
                //return Status.parse(data, includeReference);
        }

        throw new Exception("Invalid Status type: " + type);
    }

}
