package org.erachain.core.exdata;

public abstract class ExEstimate {

    public final static int LIKE = 1; // нравится люблю
    public final static int SCIENTIFIC = 2; // научность
    public final static int FUNNY = 3;
    public final static int USEFUL = 4; // полезность
    //
    // достоверность: Истина, Ложь
    //


    public ExEstimate() {
    }

    public static String getTypeName(int type) {
        switch (type) {
            case LIKE:
                return "Like";
            case SCIENTIFIC:
                return "Scientific";
            case FUNNY:
                return "Funny";
            case USEFUL:
                return "Useful";
        }

        return null;
    }

    public static String getValueName(int type, int value) {
        switch (type) {
            case LIKE:
            case SCIENTIFIC:
            case FUNNY:
            case USEFUL:
        }

        switch (value) {
            case 4:
                return "+4";
            case 3:
                return "+3";
            case 2:
                return "+2";
            case 1:
                return "+1";
            case -4:
                return "-4";
            case -3:
                return "-3";
            case -2:
                return "-2";
            case -1:
                return "-1";
        }

        return null;
    }

    public static String[] getValues(int type) {
        switch (type) {
            case LIKE:
                return new String[]{"Like", "", "", "", "", "", "", ""};
            case SCIENTIFIC:
                return new String[]{"Like", "", "", "", "", "", "", ""};
            case FUNNY:
                return new String[]{"Like", "", "", "", "", "", "", ""};
            case USEFUL:
                return new String[]{"Like", "", "", "", "", "", "", ""};
        }

        return null;
    }
}
