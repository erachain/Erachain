package org.erachain.core.transCalculated;

public class CalculatedFactory {

    private static CalculatedFactory instance;

    private CalculatedFactory() {

    }

    public static CalculatedFactory getInstance() {
        if (instance == null) {
            instance = new CalculatedFactory();
        }

        return instance;
    }

    public Calculated parse(byte[] data) throws Exception {
        //READ TYPE
        int type = Byte.toUnsignedInt(data[0]);

        switch (type) {

            case Calculated.COUNTER_CALCULATED:

                // PARSE COUNTER
                return CalculatedCounter.Parse(data);

            case Calculated.CHANGE_BALANCE_CALCULATED:

                // PARSE CHANGE BAlANCE
                return CSend.Parse(data);
        }

        throw new Exception("Invalid transaction type: " + type);
    }

}
