package org.erachain.core.web;

import org.erachain.core.account.Account;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Deprecated
public class ProfileHelper {

    private static ProfileHelper instance;
    private Profile currentProfile = null;

    public ProfileHelper() {
        List<Profile> enabledProfiles = Profile.getEnabledProfiles();
        if (!enabledProfiles.isEmpty()) {
            currentProfile = enabledProfiles.get(0);
        }
    }

    public static synchronized ProfileHelper getInstance() {
        if (instance == null) {
            instance = new ProfileHelper();
        }

        return instance;

    }

    public Profile getActiveProfileOpt(HttpServletRequest servletRequestOpt) {
        // ACTIVE PROFILE NOT FOR REMOTE
        String ipAddress = ServletUtils.getRemoteAddress(servletRequestOpt);
        if (ServletUtils.isRemoteRequest(servletRequestOpt, ipAddress)) {
            return null;
        }

        if (currentProfile != null) {
            Account name = currentProfile.getName();
            // RELOADING CURRENT VALUES
            Profile profile = Profile.getProfileOpt(name.getAddress());
            // PROFILE STILL ENABLED AND DO I OWN IT?
            if (profile != null && profile.isProfileEnabled()
                //&& Controller.getInstance().getName(name.getName()) != null
            ) {
                currentProfile = profile;
            } else {
                currentProfile = null;
            }

        }
        return currentProfile;
    }

    public void switchProfileOpt(String profileString) {

        if (profileString != null) {
            Account name = new Account(profileString); //Controller.getInstance().getName(profileString);
            if (name != null // && Controller.getInstance().getWalletNamesAsList().contains(name)
            ) {
                Profile profile = Profile.getProfileOpt(profileString);
                if (profile != null && profile.isProfileEnabled()) {
                    currentProfile = profile;
                }

            }
        }

    }


    public void disconnect() {
        currentProfile = null;
    }

}
