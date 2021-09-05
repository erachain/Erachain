package org.erachain.core.web;

import javax.servlet.http.HttpServletRequest;

@Deprecated
public class ServletUtils {


    public static boolean isRemoteRequest(HttpServletRequest servletRequestOpt, String ipAddress) {
        if (servletRequestOpt != null) {

            if (!ipAddress.equals("127.0.0.1")
                //&& !ipAddress.equals("localhost") - localhost = erro in accessHandler.setWhite(Settings.getInstance().getRpcAllowed());
            ) {

                return true;
            }
        }

        return false;
    }

    public static String getRemoteAddress(HttpServletRequest servletRequest) {

        String ipAddress = servletRequest.getHeader("X-FORWARDED-FOR");

        if (ipAddress == null) {
            ipAddress = servletRequest.getRemoteAddr();
        }
        return ipAddress;
    }

}
