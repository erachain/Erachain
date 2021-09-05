package org.erachain.core.web;
// 30/03 

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.transaction.Transaction;
import org.erachain.utils.Corekeys;
import org.erachain.utils.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Used to determine which names or accounts are allowed to post in a blog
 *
 * @author Skerberus
 */

@Deprecated
public class BlogBlackWhiteList {

    /**
     * @param blogname
     * the blog to check for white/blacklist
     * @return An object containing all names or accounts that are
     * allowed/forbidden the blogowner is always part of the whitelist
     */

    private static final long FEE_KEY = Transaction.FEE_KEY;
    private final List<String> blackwhiteList;
    private final String blogname;
    private boolean whitelist;
    private BlogBlackWhiteList(boolean isWhiteList,
                               List<String> blackwhiteList, String blogname) {
        setWhitelist(isWhiteList);
        this.blackwhiteList = blackwhiteList;
        this.blogname = blogname;
    }

    public static BlogBlackWhiteList getBlogBlackWhiteList(String blogname) {

        if (blogname == null) {
            return new BlogBlackWhiteList(false, new ArrayList<String>(), null);
        }

        return new BlogBlackWhiteList(true, new ArrayList<String>(), blogname);

    }

    public List<String> getBlackwhiteList() {
        return Collections.unmodifiableList(blackwhiteList);
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
    }

    public boolean isBlacklist() {
        return !isWhitelist();
    }

    public String getBlogname() {
        return blogname;
    }

    /**
     * Checks if post is allowed in blog.
     *
     * @param accountOrName name if post by name, creator else
     * @param creator       the creator of that post
     * @return true if post is allowed, false else
     */
    public boolean isAllowedPost(String accountOrName, String creator) {

        /*
        Pair<Account, NameResult> nameToAdress = NameUtils
                .nameToAdress(accountOrName);
        if (nameToAdress.getB() == NameResult.OK) {
            String address = nameToAdress.getA().getAddress();
            // Name is not matching creator, maybe name sold or someone tried to
            // fake a post.
            if (!address.equals(creator)) {
                return false;
            }

            // blogowner can always post
            if (accountOrName.equals(blogname)) {
                return true;
            }

        }

         */

        if (isWhitelist()) {
            return (blackwhiteList.contains(accountOrName));
        } else {
            return !blackwhiteList.contains(accountOrName);
        }
    }

    /**
     * @return a pair containing every account and every name that current user
     * owns and that is allowed to post in the blog. In case of a
     * whitelist the blogowner is always part of that
     */
    public Pair<List<Account>, List<Account>> getOwnAllowedElements(
            boolean removeZeroBalance) {

        List<Account> resultingNames = new CopyOnWriteArrayList<Account>();
        List<Account> resultingAccounts = new CopyOnWriteArrayList<Account>();

        List<Account> myaccounts;
        if (Controller.getInstance().doesWalletDatabaseExists()) {
            myaccounts = new ArrayList<Account>(Controller.getInstance()
                    .getWalletAccounts());
        } else {
            myaccounts = new ArrayList<Account>();
        }
        List<Account> myNames = new ArrayList<Account>(Controller.getInstance().getWalletAccounts());

        if (isWhitelist()) {
            for (String accountOrName : blackwhiteList) {
                /*
                Pair<Account, NameResult> nameToAdress = NameUtils
                        .nameToAdress(accountOrName);

                if (nameToAdress.getB() == NameResult.OK) {
                    // DO I OWN THAT NAME?
                    Name name = Controller.getInstance().getName(accountOrName);
                    if (myNames.contains(name)) {
                        if (!resultingNames.contains(name)) {
                            // YOU CAN ONLY POST BY NAME IF PROFILE IS ENABLED
                            Profile profileOpt = Profile.getProfileOpt(name);
                            if (profileOpt != null
                                    && profileOpt.isProfileEnabled()) {
                                resultingNames.add(name);
                            }
                        }
                    }

                } else if (Crypto.getInstance().isValidAddress(accountOrName)) {
                    Account accountByAddress = Controller.getInstance()
                            .getWalletAccountByAddress(accountOrName);

                    // DO I OWN THAT ADDRESS?
                    if (accountByAddress != null) {
                        if (!resultingAccounts.contains(accountByAddress)) {
                            resultingAccounts.add(accountByAddress);
                        }
                    }

                }

                 */

            }
            // IF IT IS MY OWN BLOG, MY NAME WILL BE OF COURSE PART OF THE
            // WHITELIST
            Account blogName = new Account(blogname);
            if (myNames.contains(blogName)) {
                if (!resultingNames.contains(blogName)) {
                    resultingNames.add(blogName);
                }
            }
        } else {
            List<Profile> activeProfiles = Profile.getEnabledProfiles();
            for (Profile profile : activeProfiles) {
                if (profile.isProfileEnabled()) {
                    resultingNames.add(profile.getName());
                }
            }
            resultingAccounts.addAll(myaccounts);

            for (String accountOrName : blackwhiteList) {
                /*
                Pair<Account, NameResult> nameToAdress = NameUtils
                        .nameToAdress(accountOrName);

                if (nameToAdress.getB() == NameResult.OK) {
                    // DO I OWN THAT NAME?
                    Name name = Controller.getInstance().getName(accountOrName);
                    if (myNames.contains(name)) {
                        resultingNames.remove(name);
                    }

                } else if (!Crypto.getInstance().isValidAddress(accountOrName)) {
                    Account accountByAddress = Controller.getInstance()
                            .getWalletAccountByAddress(accountOrName);

                    // DO I OWN THAT ADDRESS?
                    if (accountByAddress != null) {
                        resultingAccounts.remove(accountByAddress);
                    }

                }

                 */

            }
        }

        if (removeZeroBalance) {
            for (Account name : resultingNames) {
                // No balance account not shown
                if (name.getConfBalance3(0, FEE_KEY).a.compareTo(BigDecimal.ZERO) <= 0) {
                    resultingNames.remove(name);
                }
            }

            for (Account account : resultingAccounts) {
                if (account.getConfBalance3(0, FEE_KEY).a.compareTo(BigDecimal.ZERO) <= 0) {
                    resultingAccounts.remove(account);
                }
            }
        }

        return new Pair<List<Account>, List<Account>>(resultingAccounts,
                resultingNames);

    }

    public void addAddressOrName(String nameOrAddress) {
        if (!blackwhiteList.contains(nameOrAddress)) {
            blackwhiteList.add(nameOrAddress);
        }
    }

    public void clearList() {
        blackwhiteList.clear();
    }

    public void removeAddressOrName(String nameOrAddress) {
        blackwhiteList.remove(nameOrAddress);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Pair<String, String> getJsonKeyPairRepresentation() {
        String results = StringUtils.join(blackwhiteList, ";");
        return new Pair<>(isWhitelist() ? Corekeys.BLOGWHITELIST.toString()
                : Corekeys.BLOGBLACKLIST.toString(), results);
    }

}
