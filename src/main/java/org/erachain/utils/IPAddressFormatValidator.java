package org.erachain.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPAddressFormatValidator {

    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private Pattern pattern;
    private Matcher matcher;

    public IPAddressFormatValidator() {
        pattern = Pattern.compile(IPADDRESS_PATTERN);
    }

    public boolean validate(final String ipAddress) {
        matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }
}