package org.erachain.datachain;

import com.google.common.primitives.SignedBytes;
import org.erachain.utils.ByteArrayUtils;
import org.mapdb.DB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Get all comments for a blogpost!
 *
 * @author Skerberus
 */
public class PostCommentMap extends DCUMap<byte[], List<byte[]>> {

    public PostCommentMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public PostCommentMap(DCUMap<byte[], List<byte[]>> parent) {
        super(parent, null);
    }

    @Override
    public void openMap() {

        map = database.createTreeMap("CommentPostMap")
                .comparator(SignedBytes.lexicographicalComparator())
                .makeOrGet();

    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<>();
    }


    public void add(byte[] signatureOfPostToComment, byte[] signatureOfComment) {
        List<byte[]> list;
        list = get(signatureOfPostToComment);

        if (list == null) {
            list = new ArrayList<>();
        }

        if (!ByteArrayUtils.contains(list, signatureOfComment)) {
            list.add(signatureOfComment);
        }

        put(signatureOfPostToComment, list);

    }

    public void remove(byte[] signatureOfPost, byte[] signatureOfComment) {
        List<byte[]> list;
        list = get(signatureOfPost);

        if (list == null) {
            return;
        }

        if (ByteArrayUtils.contains(list, signatureOfComment)) {
            ByteArrayUtils.remove(list, signatureOfComment);
        }

        if (list.isEmpty()) {
            delete(signatureOfPost);
        } else {
            put(signatureOfPost, list);
        }

    }

}
