package org.erachain.utils;
// 30/03

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.erachain.api.BlogPostResource;
import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.web.BlogBlackWhiteList;
import org.erachain.core.web.BlogProfile;
import org.erachain.core.web.NameStorageMap;
import org.erachain.core.web.Profile;
import org.erachain.core.web.blog.BlogEntry;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.PostCommentMap;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;

//import com.twitter.Extractor;

public class BlogUtils {


    private static final Logger LOGGER = LoggerFactory.getLogger(BlogUtils.class);
    public static int COMMENT_SERVICE_ID = 778;

    /**
     * @return triplet of name, title, description of all enabled blogs.
     */
    public static List<BlogProfile> getEnabledBlogs(String searchvalueOpt) {

        NameStorageMap nameMap = DCSet.getInstance().getNameStorageMap();
        Set<String> names = nameMap.keySet();

        Map<String, List<String>> followMap = new HashMap<>();
        List<Profile> resultProfiles = new ArrayList<>();

        for (String name : names) {

            Profile profile = Profile.getProfileOpt(name);

            if (profile != null && profile.isProfileEnabled()) {
                List<String> followedBlogs = profile.getFollowedBlogs();
                if (followedBlogs != null) {
                    List<String> alreadyProcessed = new ArrayList<String>();
                    for (String followedBlog : followedBlogs) {
                        if (!alreadyProcessed.contains(followedBlog)
                                && !name.equals(followedBlog)) {
                            alreadyProcessed.add(followedBlog);
                            if (followMap.containsKey(followedBlog)) {
                                List<String> followerList = followMap
                                        .get(followedBlog);
                                if (!followerList.contains(name)) {
                                    followerList.add(name);
                                }
                                followMap.put(followedBlog, followerList);
                            } else {
                                List<String> followerList = new ArrayList<>();
                                followerList.add(name);
                                followMap.put(followedBlog, followerList);
                            }
                        }
                    }
                }

                if (profile.isBlogEnabled()) {

                    String title = profile.getBlogTitleOpt();
                    String description = profile.getBlogDescriptionOpt();
                    if (searchvalueOpt != null) {
                        searchvalueOpt = searchvalueOpt.toLowerCase();
                        if (name.toLowerCase().contains(searchvalueOpt)
                                || (title != null && title.toLowerCase()
                                .contains(searchvalueOpt))
                                || (description != null)
                                && description.toLowerCase().contains(
                                searchvalueOpt)) {
                            resultProfiles.add(profile);
                        }
                        continue;
                    }
                    resultProfiles.add(profile);
                }
            }

        }

        List<BlogProfile> blogprofiles = new ArrayList<>();
        for (Profile profileWithBlog : resultProfiles) {

            String name = profileWithBlog.getName().getAddress();
            if (followMap.containsKey(name)) {
                blogprofiles.add(new BlogProfile(profileWithBlog, followMap
                        .get(name)));
            } else {
                blogprofiles.add(new BlogProfile(profileWithBlog,
                        new ArrayList<String>()));
            }

        }

        Collections.sort(blogprofiles);

        return blogprofiles;
    }

    public static List<BlogEntry> getBlogPosts(List<String> blogList) {
        List<BlogEntry> blogPosts = new ArrayList<BlogEntry>();
        for (String blogname : blogList) {
            blogPosts.addAll(getBlogPosts(blogname));
        }
        Collections.sort(blogPosts, new BlogEntryTimestampComparator());

        Collections.reverse(blogPosts);

        return blogPosts;
    }

    public static List<BlogEntry> getHashTagPosts(String hashtag) {
        List<BlogEntry> results = new ArrayList<BlogEntry>();
        List<byte[]> list = DCSet.getInstance().getHashtagPostMap()
                .get(hashtag);

        if (list != null) {
            for (byte[] bs : list) {
                BlogEntry blogEntryOpt = getBlogEntryOpt(bs);
                if (blogEntryOpt != null) {
                    results.add(blogEntryOpt);
                }
            }
        }

        Collections.sort(results, new BlogEntryTimestampComparator());

        Collections.reverse(results);

        return results;
    }

    public static List<String> getHashTags(String text) {
        //List<String> extractHashtags = new Extractor().extractHashtags(text);
        List<String> extractHashtags = null;
        List<String> result = new ArrayList<String>();
        for (String hashTag : extractHashtags) {
            result.add("#" + hashTag);
        }
        return result;
    }

    public static List<String> getBlogTags(String text) {
//        List<String> extractScreenNames = new Extractor()
//                .extractMentionedScreennames(text);
        List<String> extractScreenNames = null;
        List<String> result = new ArrayList<String>();
        for (String screenNames : extractScreenNames) {
            result.add("@" + screenNames);
        }
        return result;
    }

    public static List<BlogEntry> getBlogPosts(String blogOpt) {
        return getBlogPosts(blogOpt, -1);
    }

    public static List<BlogEntry> getCommentBlogPosts(String signatureOfBlogPost) {
        return getCommentBlogPosts(signatureOfBlogPost, -1);
    }

    public static List<BlogEntry> getCommentBlogPosts(
            String signatureOfBlogPost, int limit) {
        List<BlogEntry> results = new ArrayList<>();

        PostCommentMap commentPostMap = DCSet.getInstance().getPostCommentMap();

        List<byte[]> list = commentPostMap.get(Base58
                .decode(signatureOfBlogPost));

        Collections.reverse(list);

        List<ArbitraryTransaction> blogPostTX = new ArrayList<>();
        if (list != null) {
            for (byte[] blogArbTx : list) {
                Transaction transaction = Controller.getInstance()
                        .getTransaction(blogArbTx);
                if (transaction != null) {
                    blogPostTX.add((ArbitraryTransaction) transaction);
                }
            }
        }

        int i = 0;

        for (ArbitraryTransaction transaction : blogPostTX) {

            // String creator = transaction.getCreator().getAddress();

            // TODO ARE COMMENTS ALLOWED CHECK!
            // BlogBlackWhiteList blogBlackWhiteList = BlogBlackWhiteList
            // .getBlogBlackWhiteList(blogOpt);

            BlogEntry blogEntry = getCommentBlogEntryOpt(transaction);

            // String nameOpt = blogEntry.getNameOpt();
            if (blogEntry != null) {
                results.add(blogEntry);
                i++;
            }
            // if (blogBlackWhiteList.isAllowedPost(
            // nameOpt != null ? nameOpt : creator, creator)) {
            // results.add(blogEntry);
            // i ++;
            // }

            if (i == limit)
                break;
        }

        return results;

    }

    public static List<BlogEntry> getBlogPosts(String blogOpt, int limit) {
        List<BlogEntry> results = new ArrayList<>();

        List<byte[]> blogPostList = DCSet.getInstance().getBlogPostMap()
                .get(blogOpt == null ? "Erachain.org" : blogOpt);

        List<byte[]> list = blogPostList != null ? Lists
                .newArrayList(blogPostList) : new ArrayList<byte[]>();

        Collections.reverse(list);

        List<ArbitraryTransaction> blogPostTX = new ArrayList<>();
        if (list != null) {
            for (byte[] blogArbTx : list) {
                Transaction transaction = Controller.getInstance()
                        .getTransaction(blogArbTx);
                if (transaction != null) {
                    blogPostTX.add((ArbitraryTransaction) transaction);
                }
            }
        }

        int i = 0;

        for (ArbitraryTransaction transaction : blogPostTX) {

            String creator = transaction.getCreator().getAddress();

            BlogBlackWhiteList blogBlackWhiteList = BlogBlackWhiteList
                    .getBlogBlackWhiteList(blogOpt);

            BlogEntry blogEntry = getBlogEntryOpt(transaction);

            String nameOpt;
            if (blogEntry != null) {
                if (blogEntry.getShareAuthorOpt() != null)
                    nameOpt = blogEntry.getShareAuthorOpt();
                else
                    nameOpt = blogEntry.getNameOpt();

                if (blogBlackWhiteList.isAllowedPost(nameOpt != null ? nameOpt
                        : creator, creator)) {
                    results.add(blogEntry);
                    i++;
                }
            }

            if (i == limit)
                break;
        }

        return results;

    }

    public static void addCommentsToBlogEntry(ArbitraryTransaction transaction,
                                              BlogEntry blogEntry) {

        if (blogEntry.getBlognameOpt() == null || Profile.getProfileOpt(blogEntry.getBlognameOpt()) != null && Profile.getProfileOpt(blogEntry.getBlognameOpt()).isCommentingAllowed()) {
            PostCommentMap commentPostMap = DCSet.getInstance().getPostCommentMap();
            List<byte[]> comments = commentPostMap.get(transaction.getSignature());
            if (comments != null) {
                for (byte[] commentByteArray : comments) {
                    Transaction commentTa = Controller.getInstance()
                            .getTransaction(commentByteArray);
                    if (commentTa != null) {
                        BlogEntry commentBlogEntryOpt = getCommentBlogEntryOpt((ArbitraryTransaction) commentTa);
                        if (commentBlogEntryOpt != null) {
                            blogEntry.addComment(commentBlogEntryOpt);
                        }
                    }
                }
            }
        }
    }


    public static BlogEntry getCommentBlogEntryOpt(String signatureOfComment) {
        BlogEntry result = null;
        Transaction commentTa = Controller.getInstance()
                .getTransaction(Base58
                        .decode(signatureOfComment));

        if (commentTa != null) {
            result = getCommentBlogEntryOpt((ArbitraryTransaction) commentTa);
        }

        return result;


    }


    public static BlogEntry getBlogEntryOpt(String signature) {
        return getBlogEntryOpt(Base58.decode(signature));
    }

    public static BlogEntry getBlogEntryOpt(byte[] signature) {
        ArbitraryTransaction transaction = null;
        try {
            transaction = (ArbitraryTransaction) Controller.getInstance()
                    .getTransaction(signature);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        return transaction == null ? null : BlogUtils
                .getBlogEntryOpt(transaction);
    }

    public static BlogEntry getCommentBlogEntryOpt(
            ArbitraryTransaction transaction) {
        if (transaction.getService() != COMMENT_SERVICE_ID) {
            return null;
        }

        byte[] data = ((ArbitraryTransaction) transaction).getData();
        String string = new String(data, StandardCharsets.UTF_8);

        JSONObject jsonObject = (JSONObject) JSONValue.parse(string);
        if (jsonObject != null) {
            // MAINBLOG OR CUSTOM BLOG?

            String title = (String) jsonObject.get(BlogPostResource.TITLE_KEY);
            String post = (String) jsonObject.get(BlogPostResource.POST_KEY);
            String nameOpt = (String) jsonObject.get(BlogPostResource.AUTHOR);
            String blognameOpt = (String) jsonObject
                    .get(BlogPostResource.BLOGNAME_KEY);
            String postID = (String) jsonObject
                    .get(BlogPostResource.COMMENT_POSTID_KEY);

            String creator = transaction.getCreator().getAddress();

            if (StringUtil.isNotBlank(post) && StringUtil.isNotBlank(postID)) {
                BlogEntry be = new BlogEntry(title, post, nameOpt,
                        transaction.getTimestamp(), creator,
                        Base58.encode(transaction.getSignature()), blognameOpt);
                be.setCommentPostidOpt(postID);
                return be;
            }
        }

        return null;

    }

    /**
     * returns blogentry without any restrictions
     *
     * @param transaction
     * @return
     */
    // TODO MAYBE JOIN WITH SHARE SO THAT THIS ALSO CONTAINS SHAREDPOSTS!
    public static BlogEntry getBlogEntryOpt(ArbitraryTransaction transaction) {
        if (transaction.getService() != 777) {
            return null;
        }
        byte[] data = ((ArbitraryTransaction) transaction).getData();
        String string = new String(data, StandardCharsets.UTF_8);

        JSONObject jsonObject = (JSONObject) JSONValue.parse(string);
        if (jsonObject != null) {
            // MAINBLOG OR CUSTOM BLOG?

            String title = (String) jsonObject.get(BlogPostResource.TITLE_KEY);
            String post = (String) jsonObject.get(BlogPostResource.POST_KEY);
            String nameOpt = (String) jsonObject.get(BlogPostResource.AUTHOR);
            String blognameOpt = (String) jsonObject
                    .get(BlogPostResource.BLOGNAME_KEY);
            String share = (String) jsonObject.get(BlogPostResource.SHARE_KEY);

            String creator = transaction.getCreator().getAddress();

            if (StringUtils.isNotEmpty(share)) {
                BlogEntry blogEntryToShareOpt = BlogUtils
                        .getBlogEntryOpt((ArbitraryTransaction) Controller
                                .getInstance().getTransaction(
                                        Base58.decode(share)));
                if (blogEntryToShareOpt != null
                        && StringUtils.isNotBlank(blogEntryToShareOpt
                        .getDescription())) {
                    // share gets time of sharing!
                    blogEntryToShareOpt.setTime(transaction.getTimestamp());
                    blogEntryToShareOpt
                            .setShareAuthor(nameOpt != null ? nameOpt : creator);
                    blogEntryToShareOpt.setShareSignatureOpt(Base58
                            .encode(transaction.getSignature()));
                    addCommentsToBlogEntry(transaction, blogEntryToShareOpt);
                    return blogEntryToShareOpt;
                }
            }

            // POST NEEDS TO BE FILLED
            if (StringUtil.isNotBlank(post)) {
                BlogEntry resultBlogEntry = new BlogEntry(title, post, nameOpt,
                        transaction.getTimestamp(), creator,
                        Base58.encode(transaction.getSignature()), blognameOpt);
                addCommentsToBlogEntry(transaction, resultBlogEntry);
                return resultBlogEntry;
            }
        }

        return null;
    }

    public static String getCreatorOrBlogOwnerOpt(BlogEntry blogEntryOpt) {
        String creator = blogEntryOpt.getCreator();

        //WE don't have creator account
        if (Controller.getInstance().getWalletAccountByAddress(creator) == null) {
            creator = null;
            String blognameOpt = blogEntryOpt.getBlognameOpt();
            Profile profileOpt = Profile.getProfileOpt(blognameOpt);

            if (profileOpt != null) {
                String blogowner = profileOpt.getName().getAddress();
                //are we the owner of the blog?
                if (Controller.getInstance().getWalletAccountByAddress(blogowner) != null) {
                    creator = blogowner;
                }

            }

        }
        return creator;
    }


}
