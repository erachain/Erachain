package org.erachain.datachain;

import com.google.common.primitives.SignedBytes;
import org.mapdb.DB;

import java.util.HashMap;

/**
 * Get the parent post for a comment (the blogpost that was commented)
 *
 * @author Skerberus
 */
public class CommentPostMap extends DCUMap<byte[], byte[]> {

    public CommentPostMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public CommentPostMap(DCUMap<byte[], byte[]> parent) {
        super(parent, null);
    }

    @Override
    protected void openMap() {

        map = database.createTreeMap("CommentPostMapTree")
                .comparator(SignedBytes.lexicographicalComparator())
                .makeOrGet();

    }

    public void add(byte[] signatureOfComment, byte[] signatureOfBlogPost) {
        put(signatureOfComment, signatureOfBlogPost);
    }

    public void delete(byte[] signatureOfComment) {
        this.delete(signatureOfComment);
    }

    @Override
    protected void getMemoryMap() {
        map = new HashMap<>();
    }

    @Override
    protected byte[] getDefaultValue() {
        return null;
    }

    @Override
    protected void createIndexes() {
    }
}

